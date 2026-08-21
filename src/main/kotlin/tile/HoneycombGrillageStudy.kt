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
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.PressureField
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

/**
 * `T-253` — a honeycomb grillage, so a prestrain on a four-layer face can be solved at all.
 *
 * Emits `gpd/results/T-253-honeycomb-grillage.json`. Reads `gpd/results/T-3b-tile-edge-load-profile.json`
 * (`C-0022`'s solved collar) and `gpd/results/T-246-forced-scaffold-crossover-price.json`
 * (`C-0152`'s forced-crossover departure and its 10-of-59 census).
 */

private const val T253_ROW_BP: Int = 112
private const val T253_SAMPLES: Int = 81
private const val T253_TOLERANCE: Double = 0.10
private const val T253_RIM_STANDOFF: Double = 1.0
private const val T253_FORCED_CROSSOVERS: Int = 10
private const val T253_RASTER_CROSSOVERS: Int = 59

private fun Double.emitted(digits: Int = 9): String =
    if (!isFinite() || this == 0.0) toString()
    else java.math.BigDecimal(this).round(java.math.MathContext(digits)).toDouble().toString()

@Serializable
private class T253CheapBoundRow(
    val requirement: String,
    val whatOrigamiGrillageAssumes: String,
    val whatAHoneycombBlockSupplies: String,
    val adaptable: Boolean,
    val consequence: String
)

@Serializable
private class T253CensusRow(
    val crossSection: String,
    val helices: Int,
    val faceHelices: Int,
    val inPlaneInterfaces: Int,
    val interlayerInterfaces: Int,
    val bondedInterfaces: Int,
    val maximumDegree: Int,
    val representableAsAPathGraph: Boolean,
    val oneLayerComponents: Int,
    val bonds: Int,
    val degreesOfFreedom: Int,
    val bandwidth: Int,
    val denseMegabytes: Double,
    val bandedMegabytes: Double,
    val storageRatio: Double,
    val plateEdgeY: Double,
    val lengthS: Double
)

@Serializable
private class T253RigidityRow(
    val crossSection: String,
    val quantity: String,
    val measuredOnTheLattice: Double,
    val closedForm: Double,
    val corpusFormulaAtTheBondLength: Double,
    val corpusFormulaAtTheRowPitch: Double,
    val latticeOverCorpusAtTheBondLength: Double,
    val latticeOverCorpusAtTheRowPitch: Double,
    val note: String
)

@Serializable
private class T253RealisedRow(
    val crossSection: String,
    val independentLimit: Double,
    val parallelAxisLimit: Double,
    val parallelAxisFactor: Double,
    val realisedOnThisLattice: Double,
    val compositeFraction: Double,
    val measuredBandLow: Double,
    val measuredBandHigh: Double,
    val insideTheMeasuredBand: Boolean,
    val realisedEnhancementAtThisFraction: Double,
    val realisedEnhancementAtTheMeasuredBand: Double
)

@Serializable
private class T253LengthRow(
    val crossSection: String,
    val rowBasePairs: Int,
    val lengthS: Double,
    val degreesOfFreedom: Int,
    val independentLimit: Double,
    val parallelAxisLimit: Double,
    val realisedOnThisLattice: Double,
    val compositeFraction: Double
)

@Serializable
private class T253CouplingRow(
    val slipStiffnessMultiplier: Double,
    val slipStiffness: Double,
    val realisedAlongHelixRigidity: Double,
    val compositeFractionOnRigidity: Double,
    val freeDishingOverStroke: Double,
    val compositeFractionOnDishing: Double
)

@Serializable
private class T253FlatnessRow(
    val crossSection: String,
    val hingeStiffnessEnhancement: Double,
    val subdivisions: Int,
    val freeDishingOverStroke: Double,
    val flat: Boolean
)

@Serializable
private class T253PrestrainRow(
    val crossSection: String,
    val hingeStiffnessEnhancement: Double,
    val bonds: Int,
    val rimBonds: Int,
    val departureDegrees: Double,
    val unitPeakDishingPerRadian: Double,
    val largestUnitOverStroke: Double,
    val tenLargestOverStroke: Double,
    val tenLargestRimOverStroke: Double,
    val freeDishingOverStroke: Double,
    val ceilingOverStroke: Double,
    val rimCeilingOverStroke: Double,
    val insideTolerance: Boolean,
    val departureThatWouldReachTheTolerance: Double?
)

@Serializable
private class T253Convergence(
    val quantity: String,
    val axis: String,
    val values: List<Double>,
    val results: List<Double>,
    val relativeDeparture: Double,
    val note: String
)

@Serializable
private class T253Reproduction(
    val source: String,
    val quantity: String,
    val published: Double,
    val here: Double,
    val relativeDeparture: Double,
    val reproduced: Boolean
)

@Serializable
private class T253Falsifier(
    val name: String,
    val statement: String,
    val fired: Boolean,
    val note: String
)

@Serializable
private class T253Result(
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
    val cheapBound: List<T253CheapBoundRow>,
    val census: List<T253CensusRow>,
    val longWavelength: List<T253RigidityRow>,
    val realisedRigidity: List<T253RealisedRow>,
    val lengthDependence: List<T253LengthRow>,
    val interlayerCoupling: List<T253CouplingRow>,
    val flatness: List<T253FlatnessRow>,
    val prestrain: List<T253PrestrainRow>,
    val convergence: List<T253Convergence>,
    val reproductions: List<T253Reproduction>,
    val falsifiers: List<T253Falsifier>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val proseFailure: String
)

private class T253Collar(val smoothDepth: Double, val smoothWidth: Double, val rimDepth: Double) {
    fun field(interiorPressure: Double, lengthS: Double, lengthY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, lengthS, lengthY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T253_RIM_STANDOFF))
        )
}

private fun readCollar(file: File): T253Collar {
    require(file.exists()) { "C-0022's result file is missing: ${file.path}" }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .firstOrNull {
            fun value(name: String) = it.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == 2.0 && value("gapHeight") == 10.0 &&
                    value("appliedBias") == 0.192
        } ?: error("no C-0022 profile at the design state")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T253Collar(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

/** `C-0152`'s minimal forced-crossover azimuthal departure in degrees, read at run time. */
private fun readForcedDeparture(file: File): Double {
    require(file.exists()) { "C-0152's result file is missing: ${file.path}" }
    return Json.parseToJsonElement(file.readText()).jsonObject
        .getValue("cheapBound").jsonObject
        .getValue("minimalAzimuthalDepartureDegrees").jsonPrimitive.content.toDouble()
}

private class T253Block(val rasterRows: Int, val helicesPerRow: Int) {

    val name: String = "$rasterRows x $helicesPerRow"
    val block: HoneycombBlock = HoneycombBlock(rasterRows, helicesPerRow)

    fun lattice(
        subdivisions: Int = 1,
        enhancement: Double = 1.0,
        slip: Double = Gen1Tile.crossoverInPlaneStiffness(),
        prestrains: Map<HoneycombBondSite, Double> = emptyMap()
    ): HoneycombGrillage = HoneycombGrillage(
        block = block,
        rowBasePairs = T253_ROW_BP,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        hingeStiffnessEnhancement = enhancement,
        slipStiffness = slip,
        subdivisions = subdivisions,
        bondPrestrains = prestrains
    )
}

/** The realised enhancement `1 + f (factor − 1)` this block's four layers carry. */
private fun realisedEnhancement(helicesPerRow: Int): Double = multiLayerRigidities(
    layers = helicesPerRow,
    interhelicalDistance = Gen1Tile.INTERHELICAL_HONEYCOMB,
    crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
    coupling = LayerCoupling.CALIBRATED,
    layerSpacing = HoneycombCrossSectionGeometry.columnPitch(Gen1Tile.INTERHELICAL_HONEYCOMB)
).realisedEnhancement

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    val collar = readCollar(File("gpd/results/T-3b-tile-edge-load-profile.json"))
    val forcedDeparture =
        readForcedDeparture(File("gpd/results/T-246-forced-scaffold-crossover-price.json"))
    val forcedRadians = Math.toRadians(forcedDeparture)

    println("T-253 - the cheap bound, before any solver")
    val candidates = listOf(T253Block(10, 6), T253Block(15, 4))

    // ---------------------------------------------------------------- the cheap bound
    val cheapBound = listOf(
        T253CheapBoundRow(
            requirement = "the interface graph on the duplexes",
            whatOrigamiGrillageAssumes = "beam i is bonded to beam i+1 and to nothing else, so " +
                    "the interfaces form a PATH: maximum degree 2 (C-0056, CH-0066)",
            whatAHoneycombBlockSupplies = "three lattice neighbours per site, so maximum degree " +
                    "3 at every m x n with n >= 3",
            adaptable = false,
            consequence = "no relabelling of the helices puts every bond between consecutive " +
                    "indices; this is a REPLACEMENT and not a parameter. At n = 2 the degree is " +
                    "2 and a block IS path-representable, which is where the boundary lies."
        ),
        T253CheapBoundRow(
            requirement = "the across-helix beam spacing",
            whatOrigamiGrillageAssumes = "a uniform pitch d, beamY[i] = (i - (n-1)/2) d",
            whatAHoneycombBlockSupplies = "an alternating d, 2d within one layer, mean 3d/2 - " +
                    "the corrugation the caDNAno paper states in its own sentence",
            adaptable = false,
            consequence = "C-0141's 1.5x in-plane pitch is not a rescaling of a uniform lattice; " +
                    "the face is a zigzag and its beams are off their own tributary centres."
        ),
        T253CheapBoundRow(
            requirement = "which adjacent pairs are bonded at all",
            whatOrigamiGrillageAssumes = "every adjacent pair, at alternating column parity",
            whatAHoneycombBlockSupplies = "exactly half the in-plane adjacent pairs, decided by " +
                    "(r + c) mod 2",
            adaptable = false,
            consequence = "ONE LAYER OF A HONEYCOMB BLOCK IS NOT A SHEET - it is a set of " +
                    "dimers. The across-helix load path necessarily traverses the thickness, " +
                    "which is why layers x (single-layer D_perp) is not the honeycomb's D_perp."
        ),
        T253CheapBoundRow(
            requirement = "the crossover column combinatorics",
            whatOrigamiGrillageAssumes = "a TWO-parity alternation of columns between a helix's " +
                    "two neighbours - the square lattice's 16 / 32 bp",
            whatAHoneycombBlockSupplies = "THREE bond classes at 7 bp, the same pair recurring " +
                    "every 21 bp: class c occupies plane q with q = c (mod 3)",
            adaptable = false,
            consequence = "CrossoverLayout's parities cannot express a three-class lattice; the " +
                    "in-plane bonds are all ONE class and the two interlayer ones share the rest."
        ),
        T253CheapBoundRow(
            requirement = "the degrees of freedom per node",
            whatOrigamiGrillageAssumes = "three - W, dW/ds and the roll; no axial coordinate",
            whatAHoneycombBlockSupplies = "the parallel-axis enhancement that `layers` buys is " +
                    "S x sum(z^2), which is an AXIAL strain in offset fibres",
            adaptable = false,
            consequence = "OrigamiGrillage cannot read `layers` because it has no coordinate for " +
                    "what `layers` does. Adding the axial DOF is what makes the interlayer " +
                    "coupling an OUTPUT (a slip spring) rather than a NONE/RIGID binary."
        ),
        T253CheapBoundRow(
            requirement = "the foundation",
            whatOrigamiGrillageAssumes = "a Winkler strip of width d under EVERY beam",
            whatAHoneycombBlockSupplies = "one gap-facing face of m helices, tributary 3d/2, and " +
                    "3m buried helices that touch no polymer at all",
            adaptable = false,
            consequence = "a smeared equivalent sheet puts the whole block's rigidity on one " +
                    "beam row and therefore cannot carry a load on a NAMED subset of crossovers."
        )
    )
    cheapBound.forEach { println("  " + it.requirement + " -> adaptable = " + it.adaptable) }

    // ---------------------------------------------------------------- the census
    val census = candidates.map { candidate ->
        val lattice = candidate.lattice()
        val pairs = honeycombBondPairs(candidate.block)
        val inPlane = pairs.count {
            candidate.block.sites[it.first].column == candidate.block.sites[it.second].column
        }
        val dof = lattice.degreesOfFreedom
        val dense = dof.toDouble() * dof * 8.0 / 1e6
        val banded = dof.toDouble() * (lattice.bandwidth + 1) * 8.0 / 1e6
        T253CensusRow(
            crossSection = candidate.name,
            helices = candidate.block.helices,
            faceHelices = lattice.faceBeams.size,
            inPlaneInterfaces = inPlane,
            interlayerInterfaces = pairs.size - inPlane,
            bondedInterfaces = pairs.size,
            maximumDegree = honeycombMaximumDegree(candidate.block),
            representableAsAPathGraph = honeycombMaximumDegree(candidate.block) <= 2,
            oneLayerComponents = honeycombBondGraphComponents(
                HoneycombBlock(candidate.rasterRows, 1)
            ),
            bonds = lattice.bonds.size,
            degreesOfFreedom = dof,
            bandwidth = lattice.bandwidth,
            denseMegabytes = dense,
            bandedMegabytes = banded,
            storageRatio = dense / banded,
            plateEdgeY = lattice.lengthY,
            lengthS = lattice.lengthS
        )
    }
    census.forEach {
        println(
            "  " + it.crossSection + "  helices " + it.helices + "  bonded interfaces " +
                    it.bondedInterfaces + "  max degree " + it.maximumDegree +
                    "  one-layer components " + it.oneLayerComponents + "  DOF " +
                    it.degreesOfFreedom + "  dense/banded " + it.storageRatio.emitted(4)
        )
    }

    // ---------------------------------------------------------------- long wavelength
    val curvature = 1e-4
    val longWavelength = candidates.flatMap { candidate ->
        val lattice = candidate.lattice()
        val along = lattice.alongHelixCurvatureField(curvature)
        val alongEnergy = lattice.beamEnergy(along) + lattice.axialEnergy(along)
        val alongMeasured = 2.0 * alongEnergy / (curvature * curvature * lattice.area)
        val alongClosed = (candidate.helicesPerRow * Gen1Tile.DUPLEX_BENDING_RIGIDITY +
                Gen1Tile.DUPLEX_STRETCH_MODULUS *
                lattice.beamZ.sumOf { it * it } / candidate.rasterRows) / lattice.rowPitch
        val across = lattice.acrossHelixCurvatureField(curvature)
        val acrossMeasured =
            2.0 * lattice.hingeEnergy(across) / (curvature * curvature * lattice.area)
        val acrossClosed = lattice.hingeStiffness * Gen1Tile.INTERHELICAL_HONEYCOMB *
                Gen1Tile.INTERHELICAL_HONEYCOMB *
                lattice.bonds.sumOf { it.unitY * it.unitY } / lattice.area
        fun corpus(coupling: LayerCoupling, pitch: Double) = multiLayerRigidities(
            layers = candidate.helicesPerRow,
            interhelicalDistance = pitch,
            crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
            coupling = coupling,
            layerSpacing = HoneycombCrossSectionGeometry.columnPitch(Gen1Tile.INTERHELICAL_HONEYCOMB)
        )
        // C-0109 and C-0120 read the in-plane pitch as `d`; C-0141 corrected it to `3d/2`.
        // Both are emitted, because only one of the two comparisons is a challenge.
        val corpus = corpus(LayerCoupling.COMPOSITE, Gen1Tile.INTERHELICAL_HONEYCOMB)
        val corpusAtRowPitch = corpus(LayerCoupling.COMPOSITE, lattice.rowPitch)
        val corpusIndependent = corpus(LayerCoupling.INDEPENDENT, Gen1Tile.INTERHELICAL_HONEYCOMB)
        val corpusIndependentAtRowPitch = corpus(LayerCoupling.INDEPENDENT, lattice.rowPitch)
        listOf(
            T253RigidityRow(
                crossSection = candidate.name,
                quantity = "D_parallel at rigid composite action, pN nm",
                measuredOnTheLattice = alongMeasured,
                closedForm = alongClosed,
                corpusFormulaAtTheBondLength = corpus.alongHelixRigidity,
                corpusFormulaAtTheRowPitch = corpusAtRowPitch.alongHelixRigidity,
                latticeOverCorpusAtTheBondLength = alongMeasured / corpus.alongHelixRigidity,
                latticeOverCorpusAtTheRowPitch =
                    alongMeasured / corpusAtRowPitch.alongHelixRigidity,
                note = "the imposed field carries zero slip and zero link extension, so the " +
                        "lattice's own energy IS the parallel-axis closed form; it differs from " +
                        "the corpus's only by the in-plane pitch, d against 3d/2"
            ),
            T253RigidityRow(
                crossSection = candidate.name,
                quantity = "D_perpendicular, carried entirely by the dihedral springs, pN nm",
                measuredOnTheLattice = acrossMeasured,
                closedForm = acrossClosed,
                corpusFormulaAtTheBondLength = corpusIndependent.acrossHelixRigidity,
                corpusFormulaAtTheRowPitch = corpusIndependentAtRowPitch.acrossHelixRigidity,
                latticeOverCorpusAtTheBondLength =
                    acrossMeasured / corpusIndependent.acrossHelixRigidity,
                latticeOverCorpusAtTheRowPitch =
                    acrossMeasured / corpusIndependentAtRowPitch.acrossHelixRigidity,
                note = "an in-plane bond carries a full d of lever arm and an interlayer bond " +
                        "only d/2, and only half the in-plane adjacent pairs are bonded at all - " +
                        "which is what the corpus's layers x (k_theta d / p) cannot see"
            )
        )
    }
    longWavelength.forEach {
        println(
            "  " + it.crossSection + "  " + it.quantity + " = " +
                    it.measuredOnTheLattice.emitted(6) + "  lattice/corpus at d " +
                    it.latticeOverCorpusAtTheBondLength.emitted(6) + "  at 3d/2 " +
                    it.latticeOverCorpusAtTheRowPitch.emitted(6)
        )
    }

    // ------------------------------------------------- the composite fraction, as a MEASUREMENT
    println("T-253 - the composite fraction, measured on the lattice rather than inherited")
    val realisedRigidity = candidates.map { candidate ->
        val lattice = candidate.lattice()
        val independent = candidate.helicesPerRow * Gen1Tile.DUPLEX_BENDING_RIGIDITY / lattice.rowPitch
        val along = lattice.alongHelixCurvatureField(curvature)
        val composite = 2.0 * (lattice.beamEnergy(along) + lattice.axialEnergy(along)) /
                (curvature * curvature * lattice.area)
        val realised = lattice.realisedAlongHelixRigidity(curvature)
        val fraction = (realised - independent) / (composite - independent)
        val factor = composite / independent
        T253RealisedRow(
            crossSection = candidate.name,
            independentLimit = independent,
            parallelAxisLimit = composite,
            parallelAxisFactor = factor,
            realisedOnThisLattice = realised,
            compositeFraction = fraction,
            measuredBandLow = 0.26,
            measuredBandHigh = 0.33,
            insideTheMeasuredBand = fraction > 0.26 && fraction < 0.33,
            realisedEnhancementAtThisFraction = 1.0 + fraction * (factor - 1.0),
            realisedEnhancementAtTheMeasuredBand = realisedEnhancement(candidate.helicesPerRow)
        )
    }
    realisedRigidity.forEach {
        println(
            "  " + it.crossSection + "  independent " + it.independentLimit.emitted(6) +
                    "  parallel axis " + it.parallelAxisLimit.emitted(6) + "  realised " +
                    it.realisedOnThisLattice.emitted(6) + "  f = " +
                    it.compositeFraction.emitted(6) +
                    (if (it.insideTheMeasuredBand) "  inside 0.26-0.33" else "  OUTSIDE 0.26-0.33")
        )
    }

    // -------------------------------------------- is the composite fraction a MATERIAL constant?
    println("T-253 - the composite fraction against the row LENGTH")
    val lengthDependence = listOf(56, 112, 224, 448).map { basePairs ->
        val lattice = HoneycombGrillage(
            block = candidates.first().block,
            rowBasePairs = basePairs,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT
        )
        val independent = candidates.first().helicesPerRow *
                Gen1Tile.DUPLEX_BENDING_RIGIDITY / lattice.rowPitch
        val along = lattice.alongHelixCurvatureField(curvature)
        val composite = 2.0 * (lattice.beamEnergy(along) + lattice.axialEnergy(along)) /
                (curvature * curvature * lattice.area)
        val realised = lattice.realisedAlongHelixRigidity(curvature)
        T253LengthRow(
            crossSection = candidates.first().name,
            rowBasePairs = basePairs,
            lengthS = lattice.lengthS,
            degreesOfFreedom = lattice.degreesOfFreedom,
            independentLimit = independent,
            parallelAxisLimit = composite,
            realisedOnThisLattice = realised,
            compositeFraction = (realised - independent) / (composite - independent)
        )
    }
    lengthDependence.forEach {
        println(
            "  " + it.rowBasePairs + " bp = " + it.lengthS.emitted(6) + " nm  f = " +
                    it.compositeFraction.emitted(6)
        )
    }

    // ---------------------------------------------------------------- flatness and coupling
    val design = candidates.first()
    val designLattice = design.lattice()
    val interiorPressure = Gen1Tile.TARGET_FORCE / designLattice.area
    val freeStroke = interiorPressure / Gen1Tile.FOUNDATION_SECANT
    val pressure = collar.field(interiorPressure, designLattice.lengthS, designLattice.lengthY)

    println("T-253 - the interlayer coupling as an OUTPUT of the lattice")
    val nominalSlip = Gen1Tile.crossoverInPlaneStiffness()
    val slipMultipliers = listOf(1e-4, 0.03125, 0.125, 0.5, 1.0, 2.0, 8.0, 32.0, 128.0, 1e4)
    val designIndependent =
        design.helicesPerRow * Gen1Tile.DUPLEX_BENDING_RIGIDITY / designLattice.rowPitch
    val designComposite = 2.0 * (
            designLattice.beamEnergy(designLattice.alongHelixCurvatureField(curvature)) +
                    designLattice.axialEnergy(designLattice.alongHelixCurvatureField(curvature))
            ) / (curvature * curvature * designLattice.area)
    val slipReadings = slipMultipliers.map { multiplier ->
        val lattice = design.lattice(slip = nominalSlip * multiplier)
        Triple(
            multiplier,
            lattice.solve(pressure).peakDishing(T253_SAMPLES) / freeStroke,
            lattice.realisedAlongHelixRigidity(curvature)
        )
    }
    val uncoupled = slipReadings.first().second
    val rigid = slipReadings.last().second
    val interlayerCoupling = slipReadings.map { (multiplier, dishing, realised) ->
        T253CouplingRow(
            slipStiffnessMultiplier = multiplier,
            slipStiffness = nominalSlip * multiplier,
            realisedAlongHelixRigidity = realised,
            compositeFractionOnRigidity =
                (realised - designIndependent) / (designComposite - designIndependent),
            freeDishingOverStroke = dishing,
            compositeFractionOnDishing = if (abs(uncoupled - rigid) > 0.0) {
                (uncoupled - dishing) / (uncoupled - rigid)
            } else 0.0
        )
    }
    interlayerCoupling.forEach {
        println(
            "  k_s x " + it.slipStiffnessMultiplier.emitted(4) + "  D_realised " +
                    it.realisedAlongHelixRigidity.emitted(6) + "  f(rigidity) " +
                    it.compositeFractionOnRigidity.emitted(6) + "  dishing " +
                    it.freeDishingOverStroke.emitted(6) + "  f(dishing) " +
                    it.compositeFractionOnDishing.emitted(6)
        )
    }

    println("T-253 - the free tile under C-0022's solved collar")
    val enhancements = candidates.associate { candidate ->
        val row = realisedRigidity.first { it.crossSection == candidate.name }
        candidate.name to listOf(
            1.0,
            row.realisedEnhancementAtTheMeasuredBand,
            row.realisedEnhancementAtThisFraction
        )
    }
    val flatness = candidates.flatMap { candidate ->
        val factors = enhancements.getValue(candidate.name)
        factors.flatMap { factor -> listOf(factor to 1, factor to 2) }.map { (factor, subdivisions) ->
            val lattice = candidate.lattice(subdivisions = subdivisions, enhancement = factor)
            val ownPressure = collar.field(
                Gen1Tile.TARGET_FORCE / lattice.area, lattice.lengthS, lattice.lengthY
            )
            val ownStroke = Gen1Tile.TARGET_FORCE / lattice.area / Gen1Tile.FOUNDATION_SECANT
            val dishing = lattice.solve(ownPressure).peakDishing(T253_SAMPLES) / ownStroke
            T253FlatnessRow(
                crossSection = candidate.name,
                hingeStiffnessEnhancement = factor,
                subdivisions = subdivisions,
                freeDishingOverStroke = dishing,
                flat = dishing < T253_TOLERANCE
            )
        }
    }
    flatness.forEach {
        println(
            "  " + it.crossSection + "  enhancement " + it.hingeStiffnessEnhancement.emitted(6) +
                    "  subdivisions " + it.subdivisions + "  dishing " +
                    it.freeDishingOverStroke.emitted(6) + (if (it.flat) "  flat" else "  NOT flat")
        )
    }

    // ---------------------------------------------------------------- the prestrain
    println("T-253 - the forced-crossover prestrain, as a rigorous ceiling over every choice")
    val prestrain = candidates.flatMap { candidate ->
        enhancements.getValue(candidate.name).map { factor ->
            val lattice = candidate.lattice(enhancement = factor)
            val ownPressure = collar.field(
                Gen1Tile.TARGET_FORCE / lattice.area, lattice.lengthS, lattice.lengthY
            )
            val ownStroke = Gen1Tile.TARGET_FORCE / lattice.area / Gen1Tile.FOUNDATION_SECANT
            val free = lattice.solve(ownPressure).peakDishing(T253_SAMPLES) / ownStroke
            val lastPlane = lattice.planeBasePairs.size - 1
            val unit = lattice.bonds.map { bond ->
                bond to lattice.unitPrestrainResponse(bond).peakDishing(T253_SAMPLES) / ownStroke
            }
            val sorted = unit.map { it.second }.sortedDescending()
            val rim = unit.filter { it.first.plane == 0 || it.first.plane == lastPlane }
                .map { it.second }.sortedDescending()
            val largest = sorted.first()
            val tenLargest = sorted.take(T253_FORCED_CROSSOVERS).sum()
            val tenLargestRim = rim.take(T253_FORCED_CROSSOVERS).sum()
            val ceiling = free + tenLargest * forcedRadians
            val rimCeiling = free + tenLargestRim * forcedRadians
            T253PrestrainRow(
                crossSection = candidate.name,
                hingeStiffnessEnhancement = factor,
                bonds = lattice.bonds.size,
                rimBonds = rim.size,
                departureDegrees = forcedDeparture,
                unitPeakDishingPerRadian = largest,
                largestUnitOverStroke = largest * forcedRadians,
                tenLargestOverStroke = tenLargest * forcedRadians,
                tenLargestRimOverStroke = tenLargestRim * forcedRadians,
                freeDishingOverStroke = free,
                ceilingOverStroke = ceiling,
                rimCeilingOverStroke = rimCeiling,
                insideTolerance = ceiling < T253_TOLERANCE,
                // a root-finder handed a target the function never reaches returns null, and
                // the null is a VERDICT: where the FREE tile already exceeds the tolerance there
                // is no departure at which the forced crossovers are what decides it.
                departureThatWouldReachTheTolerance = if (free >= T253_TOLERANCE) null
                else Math.toDegrees((T253_TOLERANCE - free) / tenLargest)
            )
        }
    }
    prestrain.forEach {
        println(
            "  " + it.crossSection + "  enhancement " + it.hingeStiffnessEnhancement.emitted(6) +
                    "  free " + it.freeDishingOverStroke.emitted(6) + "  ceiling " +
                    it.ceilingOverStroke.emitted(6) +
                    (if (it.insideTolerance) "  INSIDE T-5b" else "  OUTSIDE T-5b") +
                    "  threshold " +
                    (it.departureThatWouldReachTheTolerance?.emitted(6) ?: "none - the free tile " +
                            "already exceeds the tolerance") + " deg"
        )
    }

    // ---------------------------------------------------------------- convergence
    val subdivisionValues = listOf(1, 2, 4)
    val subdivisionResults = subdivisionValues.map { subdivisions ->
        val lattice = design.lattice(subdivisions = subdivisions)
        val ownPressure = collar.field(
            Gen1Tile.TARGET_FORCE / lattice.area, lattice.lengthS, lattice.lengthY
        )
        val ownStroke = Gen1Tile.TARGET_FORCE / lattice.area / Gen1Tile.FOUNDATION_SECANT
        lattice.solve(ownPressure).peakDishing(T253_SAMPLES) / ownStroke
    }
    val sampleValues = listOf(41, 81, 161)
    val designSolution = design.lattice().solve(pressure)
    val sampleResults = sampleValues.map { designSolution.peakDishing(it) / freeStroke }
    val convergence = listOf(
        T253Convergence(
            quantity = "the free-tile peak dishing over the stroke at 10 x 6",
            axis = "nested beam subdivisions between crossover planes",
            values = subdivisionValues.map { it.toDouble() },
            results = subdivisionResults,
            relativeDeparture = abs(subdivisionResults[2] - subdivisionResults[1]) /
                    abs(subdivisionResults[2]),
            note = "NOT read under a uniform pressure: a free lattice on a uniform foundation " +
                    "answers that one with an exact rigid translation at every mesh."
        ),
        T253Convergence(
            quantity = "the same, over the dishing sample grid",
            axis = "samples per side of the face grid",
            values = sampleValues.map { it.toDouble() },
            results = sampleResults,
            relativeDeparture = abs(sampleResults[2] - sampleResults[1]) / abs(sampleResults[2]),
            note = "the peak is a max over a grid, so it is monotone non-decreasing in the " +
                    "sample count and its departure is one-sided."
        )
    )
    convergence.forEach {
        println("  " + it.axis + " departure " + it.relativeDeparture.emitted(2))
    }

    // ---------------------------------------------------------------- reproductions
    val designCensus = census.first()
    val uniformSolution = design.lattice().solve(uniformPressure(interiorPressure))
    val reproductions = listOf(
        T253Reproduction(
            source = "C-0141 - the honeycomb plateEdgeY of 10 x 6",
            quantity = "the in-plane width in nm",
            published = 38.04,
            here = designCensus.plateEdgeY,
            relativeDeparture = abs(designCensus.plateEdgeY - 38.04) / 38.04,
            reproduced = abs(designCensus.plateEdgeY - 38.04) < 1e-9
        ),
        T253Reproduction(
            source = "C-0141 - the honeycomb plateEdgeY of 15 x 4",
            quantity = "the in-plane width in nm",
            published = 57.06,
            here = census[1].plateEdgeY,
            relativeDeparture = abs(census[1].plateEdgeY - 57.06) / 57.06,
            reproduced = abs(census[1].plateEdgeY - 57.06) < 1e-9
        ),
        T253Reproduction(
            source = "C-0152 - the minimal forced-crossover azimuthal departure",
            quantity = "degrees, read out of T-246's result file at run time",
            published = 17.1428571,
            here = forcedDeparture,
            relativeDeparture = abs(forcedDeparture - 17.1428571) / 17.1428571,
            reproduced = abs(forcedDeparture - 17.1428571) < 1e-6
        ),
        T253Reproduction(
            source = "multiLayerRigidities at COMPOSITE and C-0141's corrected 3d/2 in-plane " +
                    "pitch - the corpus's own smeared four-layer D_parallel",
            quantity = "the along-helix flexural rigidity in pN nm at rigid composite action, " +
                    "10 x 6",
            published = longWavelength[0].corpusFormulaAtTheRowPitch,
            here = longWavelength[0].measuredOnTheLattice,
            relativeDeparture = abs(
                longWavelength[0].measuredOnTheLattice - longWavelength[0].corpusFormulaAtTheRowPitch
            ) / longWavelength[0].corpusFormulaAtTheRowPitch,
            reproduced = abs(
                longWavelength[0].measuredOnTheLattice - longWavelength[0].corpusFormulaAtTheRowPitch
            ) < 1e-9 * longWavelength[0].corpusFormulaAtTheRowPitch
        ),
        T253Reproduction(
            source = "the uniform-load identity - a free lattice translates by q / k_f exactly",
            quantity = "the mean face deflection in nm",
            published = freeStroke,
            here = uniformSolution.meanDeflection,
            relativeDeparture = abs(uniformSolution.meanDeflection - freeStroke) / freeStroke,
            reproduced = abs(uniformSolution.meanDeflection - freeStroke) < 1e-9 * freeStroke
        )
    )
    reproductions.forEach {
        println(
            "  " + it.source + "  departure " + it.relativeDeparture.emitted(2) +
                    (if (it.reproduced) "  reproduced" else "  NOT reproduced")
        )
    }

    // ---------------------------------------------------------------- falsifiers
    val uniformDishing = uniformSolution.peakDishing(T253_SAMPLES)
    val balance = abs(uniformSolution.foundationForce - uniformSolution.appliedForce) /
            abs(uniformSolution.appliedForce)
    val designPrestrain = prestrain.first()
    val designRealised = realisedRigidity.first()
    val designPrestrainCalibrated = prestrain.first {
        it.crossSection == design.name &&
                it.hingeStiffnessEnhancement == designRealised.realisedEnhancementAtTheMeasuredBand
    }
    val falsifiers = listOf(
        T253Falsifier(
            name = "F1",
            statement = "a uniform pressure on the honeycomb lattice on a uniform Winkler " +
                    "foundation produces zero dishing",
            fired = uniformDishing > 1e-6 * freeStroke,
            note = "peak dishing " + uniformDishing.emitted(3) + " nm against a stroke of " +
                    freeStroke.emitted(6) + " nm; the standing falsifier DOES transfer to a load " +
                    "and it is the one that catches a wrong tributary."
        ),
        T253Falsifier(
            name = "F2",
            statement = "the lattice's measured long-wavelength D_parallel equals the " +
                    "parallel-axis closed form",
            fired = longWavelength.filter { it.quantity.startsWith("D_parallel") }.any {
                abs(it.measuredOnTheLattice - it.closedForm) > 1e-9 * abs(it.closedForm)
            },
            note = "the imposed composite-compatible field has zero slip and zero link " +
                    "extension identically, so the agreement tests the axial arms and the " +
                    "geometry at once."
        ),
        T253Falsifier(
            name = "F3",
            statement = "no single layer of a honeycomb block is a connected sheet",
            fired = census.any { it.oneLayerComponents <= 1 },
            note = "written the favourable way round: its NOT firing is the finding. A one-helix" +
                    " row bonds (r, 0) to (r+1, 0) only where r + c is even, so a layer is a set" +
                    " of dimers - " + designCensus.oneLayerComponents +
                    " components on the 10-row face."
        ),
        T253Falsifier(
            name = "F4",
            statement = "no OrigamiGrillage, at any beam count and any CrossoverLayout, " +
                    "reproduces a four-layer honeycomb block's bond graph",
            fired = census.any { it.representableAsAPathGraph },
            note = "an OrigamiGrillage's interfaces are a PATH, maximum degree 2; both " +
                    "candidate blocks have maximum degree 3. The boundary is exact and it is at " +
                    "n = 2, where a block IS path-representable."
        ),
        T253Falsifier(
            name = "F5",
            statement = "the ten forced crossovers never DECIDE the flatness verdict - there is " +
                    "no state in which the free tile is inside T-5b's 0.10 and the ceiling on " +
                    "the forcing is outside it",
            fired = prestrain.any { it.freeDishingOverStroke < T253_TOLERANCE && !it.insideTolerance },
            note = "this is the question C-0152 refused, and it is the right way round: where " +
                    "the FREE tile already exceeds the tolerance the forcing is not what breaks " +
                    "the design. The ceiling is free dishing plus the ten largest UNIT responses " +
                    "times the departure, which bounds EVERY choice of ten sites by the triangle " +
                    "inequality and needs no raster path."
        ),
        T253Falsifier(
            name = "F6",
            statement = "the global force balance holds - the foundation carries the whole " +
                    "applied load",
            fired = balance > 1e-7,
            note = "relative residual " + balance.emitted(2) + "; exact on a field the two " +
                    "quadratures integrate identically, which a uniform pressure is."
        ),
        T253Falsifier(
            name = "F7",
            statement = "the composite fraction is a property of the CROSSOVERS, so a value " +
                    "measured on a micron-long bundle transfers to a 38 nm tile unchanged",
            fired = lengthDependence.maxOf { it.compositeFraction } /
                    lengthDependence.minOf { it.compositeFraction } > 1.1,
            note = "its FIRING is the finding: the partial-composite effect is a boundary layer " +
                    "at the free ends, so f is a property of the LENGTH and the LOAD CASE. Over " +
                    lengthDependence.first().rowBasePairs.toString() + " to " +
                    lengthDependence.last().rowBasePairs.toString() + " bp it runs " +
                    lengthDependence.first().compositeFraction.emitted(4) + " to " +
                    lengthDependence.last().compositeFraction.emitted(4) + "."
        )
    )
    falsifiers.forEach {
        println("  " + it.name + (if (it.fired) "  FIRED" else "  did not fire"))
    }

    // ---------------------------------------------------------------- emit
    val result = T253Result(
        task = "T-253",
        leaf = "A8.2",
        title = "A honeycomb grillage, so a prestrain on a four-layer face can be solved at all",
        verificationType = "logical (a graph census of what the honeycomb supplies against what " +
                "OrigamiGrillage requires) + in-silico (a three-dimensional beam-and-bond " +
                "lattice, its long-wavelength limits and a linear prestrain solve)",
        maturity = "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated. " +
                "Nothing here is measured on a folded object; the lattice rule is read from the " +
                "caDNAno paper and the elastic constants are this repository's own.",
        units = mapOf(
            "length" to "nm", "force" to "pN", "energy" to "pN nm",
            "rigidity" to "pN nm", "stiffness" to "pN/nm",
            "rotationalStiffness" to "pN nm/rad", "angle" to "degrees at the API, rad internally",
            "foundation" to "pN/nm^3", "dishing" to "dimensionless, over the free stroke"
        ),
        conventions = mapOf(
            "axes" to "s along the helices, y across them in the plane of the face (the m " +
                    "direction, pitch 3d/2), z through the block's thickness (the n direction, " +
                    "pitch d sqrt(3)/2)",
            "deflection" to "W positive DOWNWARD, toward the electrode - C-0006's convention",
            "face" to "the gap-facing face is column 0; the flatness field is read on it alone",
            "crossoverPlanes" to "class c carries its crossovers at residues b0 + 7c mod 21, so " +
                    "with b0 = 0 the planes are every 7 bp and class c occupies plane q = c mod 3",
            "prestrain" to "a LOAD, not a stiffness: the couple is taken at the physical " +
                    "k_theta and never at the enhanced one",
            "tributary" to "each face beam owns a strip of one row pitch CENTRED on its own " +
                    "axis; the face is corrugated, so a tiling strip would be off-centre and a " +
                    "uniform pressure would then apply a rolling moment at the row pitch"
        ),
        parameters = mapOf(
            "interhelicalDistanceHoneycomb" to Gen1Tile.INTERHELICAL_HONEYCOMB.emitted(),
            "inPlaneRowPitch" to designLattice.rowPitch.emitted(),
            "layerPitch" to HoneycombCrossSectionGeometry
                .columnPitch(Gen1Tile.INTERHELICAL_HONEYCOMB).emitted(),
            "rowBasePairs" to T253_ROW_BP.toString(),
            "risePerBasePair" to Gen1Tile.RISE_PER_BASE_PAIR.emitted(),
            "crossoverPlaneStepBasePairs" to
                    HoneycombCrossoverRule.ANY_AZIMUTH_STEP_BP.toString(),
            "samePairPeriodBasePairs" to HoneycombCrossoverRule.SAME_PAIR_PERIOD_BP.toString(),
            "hingeStiffness" to Gen1Tile.crossoverHingeStiffness().emitted(),
            "slipStiffness" to nominalSlip.emitted(),
            "linkPenalty" to HoneycombGrillage.RIGID_LINK_STIFFNESS.emitted(),
            "duplexBendingRigidity" to Gen1Tile.DUPLEX_BENDING_RIGIDITY.emitted(),
            "duplexTorsionalRigidity" to Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY.emitted(),
            "duplexStretchModulus" to Gen1Tile.DUPLEX_STRETCH_MODULUS.emitted(),
            "foundationStiffness" to Gen1Tile.FOUNDATION_SECANT.emitted(),
            "targetForce" to Gen1Tile.TARGET_FORCE.emitted(),
            "freeStroke" to freeStroke.emitted(),
            "flatnessTolerance" to T253_TOLERANCE.emitted(),
            "forcedCrossovers" to T253_FORCED_CROSSOVERS.toString(),
            "rasterCrossovers" to T253_RASTER_CROSSOVERS.toString(),
            "forcedDepartureDegrees" to forcedDeparture.emitted(),
            "realisedEnhancement10x6" to realisedEnhancement(6).emitted(),
            "realisedEnhancement15x4" to realisedEnhancement(4).emitted(),
            "dishingSamples" to T253_SAMPLES.toString(),
            "temperature" to "300 K, aqueous 2 mM MgCl2"
        ),
        sources = listOf(
            "gpd/results/T-3b-tile-edge-load-profile.json",
            "gpd/results/T-246-forced-scaffold-crossover-price.json"
        ),
        citedInputs = listOf(
            "C-0141 - the honeycomb cross-section, the 3d/2 in-plane pitch, the d sqrt(3)/2 " +
                    "layer pitch and the m x n block, consumed unmodified from " +
                    "tile/HoneycombFaceLattice.kt",
            "C-0148 - the published crossover-class rule: 21 bp per pair, 7 bp per class step",
            "C-0152 / CH-0188 - the 10-of-59 forced-crossover census and the 17.1428571 degree " +
                    "minimal departure, read from T-246's result file at run time",
            "C-0104 / T-172 - a prestrain is a LOAD and peak dishing is a convex seminorm, which " +
                    "is what makes the triangle-inequality ceiling rigorous",
            "C-0022 / T-3b - the solved edge collar at 2 mM, 10 nm, 0.192 V",
            "C-0001 - the secant foundation stiffness",
            "C-0116 - the measured composite fraction and its 0.26-0.33 band",
            "T-5b - the 0.10 flatness convention"
        ),
        cheapBound = cheapBound,
        census = census,
        longWavelength = longWavelength,
        realisedRigidity = realisedRigidity,
        lengthDependence = lengthDependence,
        interlayerCoupling = interlayerCoupling,
        flatness = flatness,
        prestrain = prestrain,
        convergence = convergence,
        reproductions = reproductions,
        falsifiers = falsifiers,
        findings = emptyMap(),
        validity = emptyList(),
        openQuestions = emptyList(),
        proseFailure = "none"
    )

    val findings = HashMap<String, String>()
    var proseFailure = "none"
    try {
        findings["theCheapBoundDecidedIt"] =
            "OrigamiGrillage bonds beam i to beam i+1 and to nothing else, so its interfaces " +
                    "form a PATH on the duplexes and its maximum degree is 2. A honeycomb site " +
                    "has THREE lattice neighbours, so both four-layer candidates have maximum " +
                    "degree 3 and no relabelling of the helices puts every bond between " +
                    "consecutive indices. That is one integer, it runs before any solver, and " +
                    "it says REPLACE rather than ADAPT. The boundary is exact: at two helices " +
                    "per row the degree is 2 and a block IS path-representable."
        findings["oneLayerOfAHoneycombBlockIsNotASheet"] =
            "Only half the in-plane adjacent pairs are bonded - (r + c) even - so a single " +
                    "layer of a " + design.name + " block falls into " +
                    designCensus.oneLayerComponents + " components, which are dimers. The " +
                    "across-helix load path therefore NECESSARILY traverses the thickness, and " +
                    "OrigamiSheet's layers x (k_theta d / p) is a statement about a stack of " +
                    "independent single-layer sheets, of which a honeycomb block has none. " +
                    "Measured on the lattice, D_perpendicular is " +
                    longWavelength[1].latticeOverCorpusAtTheRowPitch.emitted(6) +
                    " of that formula at C-0141's corrected in-plane pitch, and " +
                    longWavelength[1].latticeOverCorpusAtTheBondLength.emitted(6) +
                    " of it at C-0120's, at " + design.name + "."
        findings["theCompositeFractionIsAnOutputAndNotAnINPUT"] =
            "Adding the axial degree of freedom and Chen et al.'s own slip spring " +
                    "k_s = 2 alpha S / (100 a) = " + nominalSlip.emitted(6) + " pN/nm makes the " +
                    "composite fraction something the lattice REPORTS. Imposing the bending " +
                    "kinematics and relaxing the axial coordinates, the " + design.name + " " +
                    "block realises " + designRealised.realisedOnThisLattice.emitted(6) + " " +
                    "pN nm along the helices between an independent " +
                    designRealised.independentLimit.emitted(6) + " and a parallel-axis " +
                    designRealised.parallelAxisLimit.emitted(6) + " - a fraction of " +
                    designRealised.compositeFraction.emitted(6) + " against the 0.26-0.33 " +
                    "measured on four origami bundles. The two are NOT the same quantity: in " +
                    "uniform bending an infinitely long composite is rigid whatever its " +
                    "connectors, so the partial-composite effect is a boundary layer at the free " +
                    "ends and f is a property of the LENGTH and the LOAD CASE. Read on the " +
                    "dishing under C-0022's collar instead of on the rigidity the same lattice " +
                    "reports " + interlayerCoupling.first { it.slipStiffnessMultiplier == 1.0 }
                        .compositeFractionOnDishing.emitted(6) + ", between " +
                    uncoupled.emitted(6) + " and " + rigid.emitted(6) + " of the stroke - which " +
                    "is CLAUDE.md's own quote it with the state it is read at, on a calibration."
        findings["theForcedCrossoverPrestrain"] =
            "At the calibrated across-helix enhancement the ceiling on ALL TEN forced " +
                    "crossovers, over EVERY choice of sites, is " +
                    designPrestrainCalibrated.ceilingOverStroke.emitted(6) + " of the stroke at " +
                    design.name + " against T-5b's 0.10, of which the free tile alone is " +
                    designPrestrainCalibrated.freeDishingOverStroke.emitted(6) + " and the " +
                    "forcing " +
                    designPrestrainCalibrated.tenLargestOverStroke.emitted(6) + ". The largest " +
                    "single bond is worth " +
                    designPrestrainCalibrated.largestUnitOverStroke.emitted(6) + " at the " +
                    designPrestrainCalibrated.departureDegrees.emitted(9) + " degree departure, " +
                    "and the departure that would reach the tolerance is " +
                    (designPrestrainCalibrated.departureThatWouldReachTheTolerance?.emitted(6)
                        ?: "unreachable") + " degrees. WITHOUT the across-helix enhancement the " +
                    "free tile alone is " + designPrestrain.freeDishingOverStroke.emitted(6) +
                    ", already past the tolerance, so at that end of the bracket the forced " +
                    "crossovers are not what decides the verdict - the term this lattice does " +
                    "not carry is."
        findings["theCompositeFractionIsNotAMaterialCONSTANT"] =
            "Swept over the row length at " + design.name + " the same lattice, the same " +
                    "crossovers and the same k_s report f = " +
                    lengthDependence.joinToString(", ") {
                        it.rowBasePairs.toString() + " bp: " + it.compositeFraction.emitted(4)
                    } + ". In UNIFORM bending an infinitely long composite is rigid whatever its " +
                    "connectors, so what a partial composite measures is a boundary layer at the " +
                    "free ends - which is why Kauert et al.'s 0.26-0.33, measured on 740 nm to " +
                    "2 micron bundles, is not a constant this project may substitute into a " +
                    "38 nm tile. That it lands so close here is a coincidence of two effects " +
                    "and not a validation of either."
        findings["whatTheCeilingCosts"] =
            "A prestrain is a load, so the field is linear in it and peak dishing is a convex " +
                    "seminorm: the sum of the ten largest UNIT responses bounds every choice of " +
                    "ten sites. One factorisation and " + designCensus.bonds + " " +
                    "back-substitutions settle the whole placement question without " +
                    "reconstructing the raster path - which is the half C-0152 could not run."
        findings["theBandedOrderingIsWhatMakesItAffordable"] =
            "Node-major ordering makes the half-bandwidth " + designCensus.bandwidth +
                    " on " + designCensus.degreesOfFreedom + " unknowns, so the factor is " +
                    designCensus.bandedMegabytes.emitted(3) + " MB against a dense " +
                    designCensus.denseMegabytes.emitted(3) + " MB - " +
                    designCensus.storageRatio.emitted(4) + "x - and the work falls from O(n^3) " +
                    "to n b^2 / 2."
    } catch (e: Exception) {
        proseFailure = e.toString()
    }

    val complete = T253Result(
        task = result.task, leaf = result.leaf, title = result.title,
        verificationType = result.verificationType, maturity = result.maturity,
        units = result.units, conventions = result.conventions, parameters = result.parameters,
        sources = result.sources, citedInputs = result.citedInputs,
        cheapBound = result.cheapBound, census = result.census,
        longWavelength = result.longWavelength, realisedRigidity = result.realisedRigidity,
        lengthDependence = result.lengthDependence,
        interlayerCoupling = result.interlayerCoupling,
        flatness = result.flatness, prestrain = result.prestrain,
        convergence = result.convergence, reproductions = result.reproductions,
        falsifiers = result.falsifiers,
        findings = findings,
        validity = listOf(
            "TRL 1-3, model-consistent and traceable, not empirically demonstrated.",
            "The lattice carries NO across-helix parallel-axis term: the layers' membrane action " +
                    "across the helices needs an in-plane transverse coordinate this model does " +
                    "not have, so its D_perpendicular is the INDEPENDENT one and therefore a " +
                    "LOWER bound. The bracket is run explicitly at THREE ends - no enhancement " +
                    "at all, the corpus's calibrated one, and the one this lattice's own " +
                    "measured composite fraction implies through k_s / k_theta = S / B - and " +
                    "the two ends that carry ANY interlayer coupling agree on every verdict. " +
                    "At the end that carries none the FREE tile already exceeds the tolerance, " +
                    "so the forcing is not what decides it there either.",
            "The composite fraction this lattice measures is a property of the ROW LENGTH: it " +
                    "runs 0.0717 at 56 bp to 0.7371 at 448 bp on one cross-section, one k_s and " +
                    "one load case. Kauert et al.'s 0.26-0.33 is measured on 740 nm to 2 micron " +
                    "bundles by bending fluctuations, which is neither this length nor this load " +
                    "case; the closeness of the 38 nm reading to it is not a validation.",
            "The 112 / 108 raster is a TWO-LENGTH raster and this lattice carries ONE row " +
                    "length. The 4 bp difference is 1.36 nm of axial extent and is not modelled.",
            "The forced crossovers are SCAFFOLD crossovers, which sit 5 bp from a staple " +
                    "position; the lattice's node stations are the staple planes, so the ceiling " +
                    "is taken over the lattice's own bonds rather than at the scaffold offsets. " +
                    "That is why it is quoted as a ceiling over EVERY bond and not as a value at " +
                    "ten named ones.",
            "k_theta is Gen1Tile's square-lattice-fitted constant (Chen et al.); no honeycomb " +
                    "measurement of it exists in this repository. k_s is a CONSTRUCTION, not a " +
                    "measurement, and is swept over four decades.",
            "Kirchhoff is not safe at these thicknesses (C-0109, C-0120): transverse shear is " +
                    "not carried, so every D_parallel here is an upper bound.",
            "The face's peak dishing is read on a grid over the full in-plane width while each " +
                    "face beam's tributary is centred on its own axis, so the outermost quarter " +
                    "of a bond length at each edge is evaluated on the nearest beam.",
            "The foundation acts on the gap-facing face only, and the opposite face is free - " +
                    "which is the physical arrangement and not a simplification.",
            "The block is FREE: no attachment coupling is applied here, so every dishing number " +
                    "is C-0109's uncoupled reference and not a design."
        ),
        openQuestions = listOf(
            "What the across-helix parallel-axis term is worth once an in-plane transverse " +
                    "coordinate is carried. CLAUDE.md records that k_s / k_theta = S / B makes " +
                    "the enhancement the same factor both ways under Chen et al.'s " +
                    "construction; this lattice can test that rather than assume it.",
            "Whether a coupled honeycomb block - C-0142's cells re-graded on THIS lattice rather " +
                    "than on a smeared equivalent sheet - moves any flatness verdict. Every " +
                    "coupled cell in the corpus is a smeared single-layer square-lattice solve.",
            "Whether the raster TURN's position on the block's axial rim gives it the zero " +
                    "coefficient C-0147 proved for the raggedness (T-254). The ceiling here is " +
                    "placement-free and therefore does not answer it.",
            "What a per-LAYER defect does. This lattice can remove one crossover of one " +
                    "interface of one layer, which a smeared equivalent sheet cannot express at " +
                    "all - C-0087's dropout statistics have never been read on a multi-layer " +
                    "body."
        ),
        proseFailure = proseFailure
    )

    val output = File("gpd/results/T-253-honeycomb-grillage.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(complete).roundedForResult(
                digits = 9, floor = 1e-12
            ) as JsonObject)
        ) + "\n"
    )
    println("T-253 - wrote " + output.path)
    if (proseFailure != "none") error("prose failure: " + proseFailure)
}
