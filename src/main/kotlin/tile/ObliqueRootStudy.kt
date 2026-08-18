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
import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.coupling.dropoutDishingSample
import com.xemantic.nano.plentyofroom.coupling.dropoutEnsemble
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.coupling.latticeInfluenceSurrogate
import com.xemantic.nano.plentyofroom.coupling.measuredDepthIncorporation
import com.xemantic.nano.plentyofroom.coupling.summariseDropoutDishing
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.floor

// ---------------------------------------------------------------------------------------------
// T-206 -- what does an OBLIQUE attachment root cost against a perpendicular one?
//
// C-0122 censused the honeycomb's attachment lattice and could NOT price it: half a top face's
// helices carry a free azimuth pointing straight out of the slab and half carry two oblique ones,
// and every station in that census is treated as equivalent. C-0118's flat coupled cells stand on
// those stations.
//
// The cheap bound runs first and it is the whole method: a root's stiffness is a symmetric tensor
// diagonal in its own radial and tangential axes, so
//
//     kappa(psi) = k_z(0)/k_z(psi) = cos^2 psi + sin^2 psi * A,   A = k_radial/k_tangential,
//
// which is MONOTONE in one anisotropy, is >= 1 whenever A >= 1, and equals 1 exactly when the root
// is isotropic. At the honeycomb's own psi = 60 degrees it is 0.25 + 0.75 A: three quarters of the
// load path is the TANGENTIAL axis whatever the radial one is.
// ---------------------------------------------------------------------------------------------

private const val T206_SAMPLES: Int = 81
private const val T206_TOLERANCE: Double = 0.10
private const val T206_RIM_STANDOFF: Double = 1.0
private const val T206_ROW_BP: Int = 112
private const val T206_REALISATIONS: Int = 4000
/** `C-0118`'s own seed, deliberately: it makes the equal-spring rows a **bit-exact** reproduction
 *  of `T-197` rather than a second draw, and `CLAUDE.md` records that a difference of two nearby
 *  cells must be read on a PAIRED sample — the alternating rows share this one stream with them. */
private const val T206_SEED: Long = 197_197L
private const val T206_ROWS: Int = 10
private const val T206_LAYERS: Int = 6

/** `null` where a quantity is not finite — `kotlinx.serialization` refuses `NaN` and `Infinity`,
 *  and a constraint's stiffness is the absence of a number rather than a large one. */
private fun Double.orNull(): Double? = if (isFinite()) this else null

private fun Double.emitted(digits: Int = 9): String =
    if (!isFinite() || this == 0.0) toString()
    else java.math.BigDecimal(this).round(java.math.MathContext(digits)).toDouble().toString()

@Serializable
private class T206RootReading(
    val model: String,
    /** `null` where the quantity is not a number in this model — a covalent tie read as a
     *  CONSTRAINT has no stiffness, and `CLAUDE.md` records that a margin of `Infinity` is the
     *  absence of a requirement rather than a number. `kotlinx.serialization` refuses both. */
    val radialStiffness: Double?,
    val tangentialStiffness: Double,
    val anisotropy: Double?,
    val perpendicularNormalStiffness: Double?,
    val obliqueNormalStiffness: Double,
    val costFactor: Double?,
    val pairedObliqueNormalStiffness: Double?,
    val pairedCostFactor: Double?,
    val naiveScalarPairSum: Double,
    val pairTensorOverScalar: Double?,
    val representableAsARatio: Boolean,
    val provenance: String
)

@Serializable
private class T206CheapBound(
    val anisotropy: Double,
    val costFactorAtOblique: Double,
    val monotone: Boolean
)

@Serializable
private class T206LinkSensitivity(
    val inPlaneMultiplier: Double?,
    val radialStiffness: Double?,
    val perpendicularNormalStiffness: Double?,
    val obliqueNormalStiffness: Double,
    val costFactor: Double?
)

@Serializable
private class T206PathReading(
    val crossSection: String,
    val pathCount: Int,
    val demandPerPath: Double,
    val seriesPartner: Double,
    val obliquePathFractionSingle: Double,
    val obliquePathFractionPaired: Double,
    val partnerResizeFactor: Double,
    val couplingTotalIfNotResized: Double,
    val mandateShortfall: Double
)

@Serializable
private class T206Cell(
    val crossSection: String,
    val columns: Int,
    val rows: Int,
    val pathCount: Int,
    val distribution: String,
    val obliqueFraction: Double,
    val perpendicularPathStiffness: Double,
    val obliquePathStiffness: Double,
    val totalStiffness: Double,
    val nominalOverStroke: Double,
    val medianOverStroke: Double,
    val p90OverStroke: Double,
    val worstOverStroke: Double,
    val exceedance: Double,
    val flatAtNominal: Boolean,
    val flatAtP90: Boolean,
    /** Read on the SAME dropout stream as the equal-spring row of the same cell, so the ratio is
     *  a design difference and not two draws — `CLAUDE.md`'s common-random-numbers discipline.
     *  `null` on the equal-spring row, which is its own reference. */
    val pairedMedianRatioToEqual: Double?,
    val realisationsWorseThanEqual: Int?
)

@Serializable
private class T206Census(
    val crossSection: String,
    val topFaceHelices: Int,
    val perpendicularRootHelices: Int,
    val obliqueRootHelices: Int,
    val freeAzimuthsOnTheTopFace: Int,
    val stationsPerLadder: Int,
    val censusedStations: Int,
    val correctedStations: Int,
    val correctionFactor: Double
)

@Serializable
private class T206Convergence(
    val axis: String, val values: List<Double>, val results: List<Double>,
    val departure: Double, val note: String
)

@Serializable
private class T206Reproduction(
    val source: String, val quantity: String, val published: Double,
    val reproduced: Double, val departure: Double, val strict: Boolean
)

@Serializable
private class T206Result(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: Map<String, String>,
    val parameters: Map<String, String>,
    val citedInputs: List<String>,
    val cheapBound: Map<String, String>,
    val cheapBoundLadder: List<T206CheapBound>,
    val rootReadings: List<T206RootReading>,
    val linkSensitivity: List<T206LinkSensitivity>,
    val pathReadings: List<T206PathReading>,
    val censusCorrection: List<T206Census>,
    val cells: List<T206Cell>,
    val modelBoundaries: Map<String, String>,
    val verdict: Map<String, String>,
    val convergence: List<T206Convergence>,
    val reproductions: List<T206Reproduction>,
    val falsifiers: List<String>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

private class T206Profile(val smoothDepth: Double, val smoothWidth: Double, val rimDepth: Double) {
    fun field(interiorPressure: Double, edgeX: Double, lengthY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, edgeX, lengthY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T206_RIM_STANDOFF))
        )
}

private fun t206Profile(file: File): T206Profile {
    require(file.exists()) { "C-0022's result file is missing: ${file.path}" }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .firstOrNull {
            fun value(name: String) = it.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == 2.0 && value("gapHeight") == 10.0 &&
                    value("appliedBias") == 0.192
        } ?: error("no C-0022 profile at the design state")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T206Profile(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

private class T206Tile(val rasterRows: Int, val layers: Int, private val profile: T206Profile) {
    val edgeX: Double = T206_ROW_BP * Gen1Tile.RISE_PER_BASE_PAIR
    val edgeY: Double = rasterRows * Gen1Tile.INTERHELICAL_HONEYCOMB
    val name: String = "$rasterRows x $layers"
    val rigidities: MultiLayerRigidities = multiLayerRigidities(
        layers = layers,
        interhelicalDistance = Gen1Tile.INTERHELICAL_HONEYCOMB,
        crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
        coupling = LayerCoupling.CALIBRATED
    )
    private val sheet = equivalentSheet(rigidities)
    val interiorPressure: Double = Gen1Tile.TARGET_FORCE / (edgeX * edgeY)
    val pressureField: PressureField = profile.field(interiorPressure, edgeX, edgeY)
    val freeStroke: Double = PlateOnFoundation(
        sheet.plate(edgeX, edgeY), Gen1Tile.FOUNDATION_SECANT, emptyList(), 12
    ).solve(uniformPressure(interiorPressure)).meanDeflection

    fun lattice(): OrigamiGrillage {
        val pitch = sheet.crossoverSpacing / 2.0
        val usable = edgeX - 2.0 * CrossoverLayout.EDGE_MARGIN
        return OrigamiGrillage(
            sheet = sheet,
            lengthX = edgeX,
            beamCount = rasterRows,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            columns = CrossoverLayout.centred(floor(usable / pitch).toInt() + 1, pitch),
            subdivisions = 2
        )
    }
}

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    val psi = obliqueAzimuthRadians()
    val psiDegrees = obliqueAzimuthDegrees()

    println("T-206 — the cheap bound, stated BEFORE any solve")
    println(
        ("  kappa(psi) = cos^2 psi + sin^2 psi * A,  A = k_radial/k_tangential;  " +
                "psi_oblique = %.1f deg exactly").format(psiDegrees)
    )
    println("  so kappa(oblique) = 0.25 + 0.75 A: three quarters of the path is the TANGENTIAL axis.")

    // --- the cheap bound's own ladder, which is falsifier F1 ---------------------------------
    val ladderAnisotropies = listOf(1.0, 2.0, 4.0, 8.0, 16.0, 32.0)
    val ladder = ladderAnisotropies.map { a ->
        T206CheapBound(
            anisotropy = a,
            costFactorAtOblique = ObliqueRootModel("ladder", radial = a, tangential = 1.0)
                .costFactor(psi),
            monotone = true
        )
    }
    val monotone = ladder.zipWithNext().all { (a, b) -> b.costFactorAtOblique > a.costFactorAtOblique }
    val f1Fired = !monotone

    // --- the three root models ----------------------------------------------------------------
    val roots = listOf(flexibleTieRoot(), crossoverHingedRoot(), constrainedLinkRoot())
    val rootReadings = roots.map { root ->
        val perpendicular = root.normalStiffness(0.0)
        val oblique = root.normalStiffness(psi)
        val paired = root.pairedNormalStiffness(psi)
        val naive = 2.0 * oblique
        T206RootReading(
            model = root.name,
            radialStiffness = root.radial.orNull(),
            tangentialStiffness = root.tangential,
            anisotropy = root.anisotropy.orNull(),
            perpendicularNormalStiffness = perpendicular.orNull(),
            obliqueNormalStiffness = oblique,
            costFactor = root.costFactor(psi).orNull(),
            pairedObliqueNormalStiffness = paired.orNull(),
            pairedCostFactor = (perpendicular / paired).orNull(),
            naiveScalarPairSum = naive,
            pairTensorOverScalar = (paired / naive).orNull(),
            representableAsARatio = root.costFactor(psi).isFinite(),
            provenance = root.provenance
        )
    }
    rootReadings.forEach {
        println(
            "  %-46s A = %-10s k_z(0) = %-10s k_z(60) = %.4f pN/nm".format(
                it.model, (it.anisotropy?.emitted(4) ?: "constraint"),
                (it.perpendicularNormalStiffness?.emitted(4) ?: "constraint"),
                it.obliqueNormalStiffness
            )
        )
    }

    // --- falsifier F2: how much of the answer is the ONE construction nobody has measured ------
    val hinged = crossoverHingedRoot()
    val multipliers = Gen1Tile.CROSSOVER_IN_PLANE_SWEEP + listOf(Double.POSITIVE_INFINITY)
    val linkSensitivity = multipliers.map { multiplier ->
        val root = if (multiplier.isInfinite()) constrainedLinkRoot()
        else crossoverHingedRoot(inPlaneMultiplier = multiplier)
        T206LinkSensitivity(
            inPlaneMultiplier = multiplier.orNull(),
            radialStiffness = root.radial.orNull(),
            perpendicularNormalStiffness = root.normalStiffness(0.0).orNull(),
            obliqueNormalStiffness = root.normalStiffness(psi),
            costFactor = root.costFactor(psi).orNull()
        )
    }
    val obliqueOverFullSweep = linkSensitivity.map { it.obliqueNormalStiffness }
    val fullSweepSpread = obliqueOverFullSweep.max() / obliqueOverFullSweep.min()
    val credible = linkSensitivity.filter { (it.inPlaneMultiplier ?: Double.MAX_VALUE) >= 1.0 }
        .map { it.obliqueNormalStiffness }
    val credibleSpread = credible.max() / credible.min()
    val perpendicularSpread = linkSensitivity.mapNotNull { it.perpendicularNormalStiffness }
        .let { it.max() / it.min() }
    val f2Fired = credibleSpread > 1.25

    // --- the path consequence -------------------------------------------------------------------
    val pathCells = listOf(
        "10 x 6" to listOf(10, 20, 30, 50),
        "15 x 4" to listOf(15, 30, 45, 75)
    )
    val obliqueHelixFraction = 0.5
    val pathReadings = pathCells.flatMap { (crossSection, counts) ->
        counts.map { n ->
            val demand = MANDATED_TOTAL_STIFFNESS / n
            val partner = seriesPartnerForDemand(hinged, demand)
            val single = obliquePathFraction(hinged, psi, demand)
            val pairedRoot = ObliqueRootModel(
                "paired", radial = hinged.pairedNormalStiffness(psi), tangential = 1.0
            )
            val pairedFraction =
                (1.0 / (1.0 / pairedRoot.radial + 1.0 / partner)) / demand
            val total = MANDATED_TOTAL_STIFFNESS *
                    ((1.0 - obliqueHelixFraction) + obliqueHelixFraction * single)
            T206PathReading(
                crossSection = crossSection,
                pathCount = n,
                demandPerPath = demand,
                seriesPartner = partner,
                obliquePathFractionSingle = single,
                obliquePathFractionPaired = pairedFraction,
                partnerResizeFactor = seriesPartnerForDemand(
                    ObliqueRootModel(
                        "oblique-as-root",
                        radial = hinged.normalStiffness(psi), tangential = 1.0
                    ),
                    demand
                ) / partner,
                couplingTotalIfNotResized = total,
                mandateShortfall = 1.0 - total / MANDATED_TOTAL_STIFFNESS
            )
        }
    }
    pathReadings.forEach {
        println(
            "  %-8s %3d paths  demand %.6f pN/nm  oblique delivers %.6f of it (paired %.6f)".format(
                it.crossSection, it.pathCount, it.demandPerPath,
                it.obliquePathFractionSingle, it.obliquePathFractionPaired
            )
        )
    }

    // --- the census the two free azimuths correct ------------------------------------------------
    val censusCorrection = listOf(15 to 4, 10 to 6).map { (rows, layers) ->
        val censused = honeycombStationCensus(rows, layers, T206_ROW_BP)
        val corrected = topFaceStationsCountingBothAzimuths(rows, T206_ROW_BP)
        T206Census(
            crossSection = "$rows x $layers",
            topFaceHelices = censused.topFaceHelices,
            perpendicularRootHelices = censused.perpendicularRootHelices,
            obliqueRootHelices = censused.obliqueRootHelices,
            freeAzimuthsOnTheTopFace = (0 until rows).sumOf { freeAzimuthsOnTopFace(it) },
            stationsPerLadder = censused.stationsPerHelix,
            censusedStations = censused.stations,
            correctedStations = corrected,
            correctionFactor = corrected.toDouble() / censused.stations
        )
    }
    censusCorrection.forEach {
        println(
            "  %-8s C-0122 counts %3d stations; counting BOTH free azimuths gives %3d (%.4f x)".format(
                it.crossSection, it.censusedStations, it.correctedStations, it.correctionFactor
            )
        )
    }

    // --- the re-grading -------------------------------------------------------------------------
    val profile = t206Profile(File("gpd/results/T-3b-tile-edge-load-profile.json"))
    val tile = T206Tile(T206_ROWS, T206_LAYERS, profile)
    val lattice = tile.lattice()
    val cells = ArrayList<T206Cell>()
    val reproductions = ArrayList<T206Reproduction>()
    val publishedP90 = mapOf(
        1 to 0.0278431488, 2 to 0.0541089284, 3 to 0.0461988976, 5 to 0.0408747025
    )
    listOf(1, 2, 3, 5).forEach { columns ->
        val grid = attachmentGrid(columns, tile.rasterRows, tile.edgeX, tile.edgeY)
        val surrogate = latticeInfluenceSurrogate(lattice, grid, tile.pressureField, T206_SAMPLES)
        val incorporation = measuredDepthIncorporation(tile.edgeX, tile.edgeY)
        val ensemble = dropoutEnsemble(
            grid.map { (x, y) -> incorporation.at(x, y) }, T206_REALISATIONS, T206_SEED
        )
        val demand = MANDATED_TOTAL_STIFFNESS / grid.size
        val fraction = obliquePathFraction(hinged, psi, demand)
        val distributions = listOf(
            Triple("equal springs (perpendicular everywhere)", 1.0,
                alternatingShareOfMandate(tile.rasterRows, columns, 1.0)),
            Triple("alternating, oblique on a crossover-hinged root", fraction,
                alternatingShareOfMandate(tile.rasterRows, columns, fraction))
        )
        var equalSample: DoubleArray? = null
        distributions.forEach { (label, obliqueFraction, stiffnesses) ->
            val nominal = surrogate.solve(stiffnesses).peakDishing / tile.freeStroke
            val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
            sample.indices.forEach { sample[it] = sample[it] / tile.freeStroke }
            val summary = summariseDropoutDishing(
                sample, nominal, ensemble.meanSurvivors, T206_TOLERANCE
            )
            val reference = equalSample
            val ratios = reference?.let { base ->
                sample.indices.map { sample[it] / base[it] }.sorted()
            }
            cells += T206Cell(
                crossSection = tile.name,
                columns = columns,
                rows = tile.rasterRows,
                pathCount = grid.size,
                distribution = label,
                obliqueFraction = obliqueFraction,
                perpendicularPathStiffness = stiffnesses.max(),
                obliquePathStiffness = stiffnesses.min(),
                totalStiffness = stiffnesses.sum(),
                nominalOverStroke = nominal,
                medianOverStroke = summary.median,
                p90OverStroke = summary.p90,
                worstOverStroke = summary.worst,
                exceedance = summary.exceedance,
                flatAtNominal = nominal < T206_TOLERANCE,
                flatAtP90 = summary.flatAtP90,
                pairedMedianRatioToEqual = ratios?.get(ratios.size / 2),
                realisationsWorseThanEqual = ratios?.count { it > 1.0 }
            )
            println(
                "  %-8s %d col x %2d = %3d paths, %-44s p90 %.9f  %s".format(
                    tile.name, columns, tile.rasterRows, grid.size, label, summary.p90,
                    if (summary.flatAtP90) "FLAT at p90" else "not flat at p90"
                )
            )
            if (obliqueFraction == 1.0) {
                equalSample = sample.copyOf()
                val published = publishedP90.getValue(columns)
                reproductions += T206Reproduction(
                    source = "C-0118 / T-197",
                    quantity = "10 x 6, $columns column(s), equal springs, p90 over stroke",
                    published = published,
                    reproduced = summary.p90,
                    departure = abs(summary.p90 - published) / published,
                    strict = summary.p90 == published
                )
            }
        }
    }

    val equalCells = cells.filter { it.obliqueFraction == 1.0 }
    val alternatingCells = cells.filter { it.obliqueFraction != 1.0 }
    val flatAlternating = alternatingCells.count { it.flatAtP90 }
    val bestAlternating = alternatingCells.minByOrNull { it.p90OverStroke }!!
    val worstRatio = alternatingCells.zip(equalCells)
        .maxOf { (a, e) -> a.p90OverStroke / e.p90OverStroke }

    // --- convergence ----------------------------------------------------------------------------
    val convergenceColumns = bestAlternating.columns
    val convergenceGrid =
        attachmentGrid(convergenceColumns, tile.rasterRows, tile.edgeX, tile.edgeY)
    val convergenceSurrogate =
        latticeInfluenceSurrogate(lattice, convergenceGrid, tile.pressureField, T206_SAMPLES)
    val convergenceIncorporation = measuredDepthIncorporation(tile.edgeX, tile.edgeY)
    val convergenceFraction =
        obliquePathFraction(hinged, psi, MANDATED_TOTAL_STIFFNESS / convergenceGrid.size)
    val convergenceStiffnesses =
        alternatingShareOfMandate(tile.rasterRows, convergenceColumns, convergenceFraction)
    val realisationResults = listOf(1000, 2000, 4000).map { n ->
        val ensemble = dropoutEnsemble(
            convergenceGrid.map { (x, y) -> convergenceIncorporation.at(x, y) }, n, T206_SEED
        )
        val sample = dropoutDishingSample(convergenceSurrogate, convergenceStiffnesses, ensemble)
        sample.indices.forEach { sample[it] = sample[it] / tile.freeStroke }
        summariseDropoutDishing(
            sample,
            convergenceSurrogate.solve(convergenceStiffnesses).peakDishing / tile.freeStroke,
            ensemble.meanSurvivors, T206_TOLERANCE
        ).p90
    }
    val samplingResults = listOf(41, 81, 161).map { samples ->
        latticeInfluenceSurrogate(lattice, convergenceGrid, tile.pressureField, samples)
            .solve(convergenceStiffnesses).peakDishing / tile.freeStroke
    }
    val convergence = listOf(
        T206Convergence(
            axis = "dropout realisations, the alternating cell's 90th percentile",
            values = listOf(1000.0, 2000.0, 4000.0),
            results = realisationResults,
            departure = abs(realisationResults[2] - realisationResults[1]) / realisationResults[2],
            note = "the only sampled quantity in this study; the decomposition upstream of it is " +
                    "closed form and has no discretisation at all"
        ),
        T206Convergence(
            axis = "dishing samples per side, the alternating cell's nominal dishing",
            values = listOf(41.0, 81.0, 161.0),
            results = samplingResults,
            departure = abs(samplingResults[2] - samplingResults[1]) / samplingResults[2],
            note = "exactly zero: at this cell the peak lands on a node the 41-, 81- and " +
                    "161-point grids share, so the sampling is not the limiting axis and the " +
                    "dropout ensemble is"
        )
    )

    val findings = LinkedHashMap<String, String>()
    findings["theCheapBoundSettledTheShape"] =
        ("The cost of an oblique root is `kappa = cos^2 psi + sin^2 psi * A` with " +
                "`A = k_radial/k_tangential`, so the whole question is ONE anisotropy, the answer " +
                "is monotone in it, and it is >= 1 with equality iff the root is ISOTROPIC. At the " +
                "honeycomb's own psi = %s deg that is 0.25 + 0.75 A — three quarters of the load " +
                "path is the tangential axis whatever the radial one is. No solve was needed to " +
                "establish any of it.").format(psiDegrees.emitted(3))
    findings["theAnswerDependsONWhatIsROOTED"] =
        ("There is no single number, and the split is not a bracket: a FLEXIBLE tie is isotropic " +
                "by this corpus's own invariant, so it costs EXACTLY nothing at every azimuth " +
                "(kappa = 1.0, a symmetry and not a small number); a CROSSOVER-HINGED rigid body " +
                "costs %s x, because its radial axis is the covalent link and its tangential one " +
                "is C-0009's dihedral spring on the d/2 lever.").format(
            rootReadings[1].costFactor!!.emitted(6)
        )
    findings["theRATIOIsNotRepresentableAndTheABSOLUTEIs"] =
        ("Read as this corpus reads a covalent tie everywhere else — a CONSTRAINT, a binary — the " +
                "perpendicular root's normal stiffness is not a number and the ratio is NOT " +
                "REPRESENTABLE. The oblique root's ABSOLUTE stiffness survives that: %s pN/nm on " +
                "the constraint reading against %s pN/nm on the softened-bond one, %s x apart, " +
                "because the tangential term carries three quarters of the path. Quote the " +
                "absolute; the ratio is a property of a model boundary.").format(
            rootReadings[2].obliqueNormalStiffness.emitted(6),
            rootReadings[1].obliqueNormalStiffness.emitted(6),
            (rootReadings[2].obliqueNormalStiffness /
                    rootReadings[1].obliqueNormalStiffness).emitted(6)
        )
    findings["theOneUnmeasuredConstructionBarelyReaches"] =
        ("Over Gen1Tile's own four-decade sweep of the DERIVED in-plane link constant the " +
                "PERPENDICULAR root moves %s x and the OBLIQUE one %s x; from the derived value " +
                "upward — the direction the corpus's own doubt runs, since the link is believed to " +
                "be a constraint — the oblique reading moves %s x. The oblique root is the one " +
                "that can be quoted without settling T-9.").format(
            perpendicularSpread.emitted(6), fullSweepSpread.emitted(6), credibleSpread.emitted(6)
        )
    findings["twoRootsOnOneHeadAddAsTENSORS"] =
        ("A helix with oblique azimuths has TWO of them, at +-%s deg, and rooting both on one " +
                "rigid head gives `2(cos^2 k_r + sin^2 k_t)` = %s pN/nm — a sum of STIFFNESSES. " +
                "Adding the two roots' own normal stiffnesses instead gives %s pN/nm, %s x less, " +
                "because that reading lets each head move laterally on its own and the shared head " +
                "forbids exactly that motion. The pair recovers the perpendicular root to within " +
                "%s x for two stations instead of one.").format(
            psiDegrees.emitted(3),
            rootReadings[1].pairedObliqueNormalStiffness!!.emitted(6),
            rootReadings[1].naiveScalarPairSum.emitted(6),
            rootReadings[1].pairTensorOverScalar!!.emitted(6),
            rootReadings[1].pairedCostFactor!!.emitted(6)
        )
    findings["thePathCostFallsWithThePathCount"] =
        ("A coupling path is the root in series with whatever supplies the compliance C-0017's " +
                "mandate demands, and the mandate is soft: at 10 paths the demand is %s pN/nm " +
                "against a crossover-hinged oblique root of %s pN/nm. So an oblique path with an " +
                "UNCHANGED series partner delivers %s of its share at 10 paths and %s at 75 — the " +
                "cost FALLS as the coupling gets denser, and C-0118's best cell is the sparsest " +
                "one, which is the adverse direction.").format(
            pathReadings[0].demandPerPath.emitted(6),
            rootReadings[1].obliqueNormalStiffness.emitted(6),
            pathReadings[0].obliquePathFractionSingle.emitted(6),
            pathReadings.last().obliquePathFractionSingle.emitted(6)
        )
    findings["c0118sFlatCellsSurvive"] =
        ("Re-graded with the alternation the lattice imposes — half the top-face helices oblique, " +
                "at the crossover-hinged fraction, renormalised to the mandate — %d of %d `10 x 6` " +
                "cells stay flat at the 90th percentile under C-0087's measured dropout, best %s " +
                "against the equal-spring %s. The worst cell moves %s x. C-0118's flatness is not " +
                "spent by the azimuth.").format(
            flatAlternating, alternatingCells.size,
            bestAlternating.p90OverStroke.emitted(9),
            equalCells.minOf { it.p90OverStroke }.emitted(9),
            worstRatio.emitted(6)
        )
    findings["c0122sCensusIsUNDERSTATEDByAHalf"] =
        ("`C-0122` assigns ONE free azimuth to every top-face helix — *the count is unaffected, " +
                "every top-face helix has exactly one free direction either way* — and that is " +
                "true of the perpendicular sublattice only. An oblique helix has TWO free " +
                "azimuths, and their 21 bp ladders are offset by 7 bp so they interleave rather " +
                "than collide. Counting both raises the top face from %d to %d stations on " +
                "`10 x 6` (%s x) and from %d to %d on `15 x 4` (%s x). `CH-0151` records it; the " +
                "correction is FAVOURABLE and `C-0122`'s own finding that a deeper block offers " +
                "fewer stations survives it (%d against %d).").format(
            censusCorrection[1].censusedStations, censusCorrection[1].correctedStations,
            censusCorrection[1].correctionFactor.emitted(6),
            censusCorrection[0].censusedStations, censusCorrection[0].correctedStations,
            censusCorrection[0].correctionFactor.emitted(6),
            censusCorrection[1].correctedStations, censusCorrection[0].correctedStations
        )
    findings["theCostIsASPACERLENGTHNotALATTICE"] =
        ("Where the root does cost something, the remedy is not structural: the oblique path's " +
                "series partner has to be %s x stiffer, which is a spacer CONTOUR LENGTH and is " +
                "quantised at a nucleotide, not at a lattice site. A design that uses one staple " +
                "length everywhere pays a %s per cent shortfall on C-0017's mandated SUM instead, " +
                "and that is a specification failure rather than a flatness one.").format(
            pathReadings[0].partnerResizeFactor.emitted(6),
            (100.0 * pathReadings[0].mandateShortfall).emitted(4)
        )

    val result = T206Result(
        task = "T-206",
        leaf = "A8.2",
        title = "What does an OBLIQUE attachment root cost against a perpendicular one?",
        verificationType = "logical (a closed-form decomposition on the corpus's own joint " +
                "constants) + in-silico (the re-grading of C-0118's cells under the alternation " +
                "the lattice imposes)",
        maturity = "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated.",
        units = mapOf(
            "stiffness" to "pN/nm",
            "rotationalStiffness" to "pN nm/rad",
            "length" to "nm",
            "angle" to "degrees in prose, radians in the code",
            "dishing" to "dimensionless, peak dishing over the free stroke"
        ),
        conventions = mapOf(
            "azimuth" to "the angle between a root's lattice direction and the slab's outward " +
                    "normal, in the cross-sectional plane",
            "radialAxis" to "along the root's own azimuth — the direction a crossover's covalent " +
                    "link acts in",
            "tangentialAxis" to "perpendicular to it in the cross-section — the direction the " +
                    "crossover's dihedral hinge rotates the attached body in",
            "obliqueAzimuth" to ("DERIVED as half the lattice's azimuth separation, " +
                    "%s deg, and not asserted").format(psiDegrees.emitted(3)),
            "load" to "along the slab normal; the coupling path's own direction",
            "temperature" to "300 K, aqueous 2 mM MgCl2, k_BT = 4.142 pN nm"
        ),
        parameters = mapOf(
            "obliqueAzimuthDegrees" to psiDegrees.emitted(),
            "interhelicalDistance" to Gen1Tile.INTERHELICAL_HONEYCOMB.emitted(),
            "crossoverLever" to (Gen1Tile.INTERHELICAL_HONEYCOMB / 2.0).emitted(),
            "crossoverHingeStiffness" to Gen1Tile.crossoverHingeStiffness().emitted(),
            "crossoverInPlaneStiffness" to Gen1Tile.crossoverInPlaneStiffness().emitted(),
            "mandatedTotalStiffness" to MANDATED_TOTAL_STIFFNESS.emitted(),
            "realisations" to T206_REALISATIONS.toString(),
            "seed" to T206_SEED.toString(),
            "dishingSamplesPerSide" to T206_SAMPLES.toString(),
            "tolerance" to T206_TOLERANCE.toString(),
            "rowBasePairs" to T206_ROW_BP.toString(),
            "crossSection" to "$T206_ROWS x $T206_LAYERS"
        ),
        citedInputs = listOf(
            "C-0122 — the honeycomb station census that raised this, and its perpendicular/oblique split",
            "C-0119 — the primary honeycomb rules (three azimuths, 7 bp, 21 bp)",
            "C-0118 — the flat coupled cells this re-grades",
            "C-0017 — the mandate, an equality on the SUM",
            "C-0009 / C-0020 — the crossover's dihedral spring and its DERIVED in-plane link",
            "C-0087 — the measured per-site staple incorporation",
            "C-0022 — the solved collar"
        ),
        cheapBound = mapOf(
            "statement" to "kappa(psi) = k_z(0)/k_z(psi) = cos^2 psi + sin^2 psi * A, " +
                    "A = k_radial/k_tangential",
            "whatItSettles" to "the whole question is ONE anisotropy; the answer is monotone in " +
                    "it; it is >= 1 whenever A >= 1 with equality iff A = 1; and at the " +
                    "honeycomb's own azimuth it is 0.25 + 0.75 A",
            "costBeforeAnySolve" to "nothing — three lines of trigonometry, run before the " +
                    "constants were read",
            "monotoneInTheAnisotropy" to monotone.toString()
        ),
        cheapBoundLadder = ladder,
        rootReadings = rootReadings,
        linkSensitivity = linkSensitivity,
        pathReadings = pathReadings,
        censusCorrection = censusCorrection,
        cells = cells,
        modelBoundaries = mapOf(
            "theFortyFiveDegreeGuardDoesNOTApply" to ("C-0037's TwoLinkBase refuses a misalignment " +
                    "past 45 deg, and this root sits at %s deg — but the two angles are different " +
                    "quantities. That guard is on the misalignment between a two-link CHORD's " +
                    "perpendicular bisector and the axis a COUPLE is demanded about, where past a " +
                    "half right angle the restrained and free axes exchange and the invariant " +
                    "`restrainedAxis >= freeAxis` is violated. This azimuth is a TRANSLATION " +
                    "direction and its decomposition has no such exchange: k_z(psi) is smooth and " +
                    "monotone over the whole quadrant and reproduces both endpoints exactly. The " +
                    "guard is checked and does not fire; carrying it here would have refused an " +
                    "answer the models do give.").format(psiDegrees.emitted(3)),
            "whatIsNotRepresentable" to "the RATIO, under the corpus's own constraint reading of " +
                    "the covalent link. What it would take is T-9 — the crossover elastic " +
                    "constants measured rather than derived — and specifically the LINK, which " +
                    "C-0020 constructs and nobody has fitted.",
            "whatTheGRILLAGECannotSee" to ("An oblique root's strand leaves the backbone at %s deg " +
                    "around the helix, so its station is laterally displaced by about R sin psi " +
                    "from the helix axis — under a nanometre, and below half the row pitch of %s " +
                    "nm. The grillage places every attachment on a beam that sits on a helix axis, " +
                    "so a sub-pitch lateral offset is not representable in it at all. It is " +
                    "reported and NOT modelled, and the two oblique azimuths of one helix have " +
                    "opposite offsets, so alternating their sense cancels it by " +
                    "construction.").format(
                psiDegrees.emitted(3), Gen1Tile.INTERHELICAL_HONEYCOMB.emitted(4)
            )
        ),
        verdict = mapOf(
            "obliqueAzimuthDegrees" to psiDegrees.emitted(),
            "costFactorFlexibleTie" to rootReadings[0].costFactor!!.emitted(),
            "costFactorCrossoverHinged" to rootReadings[1].costFactor!!.emitted(),
            "costFactorConstrainedLink" to (rootReadings[2].costFactor?.toString() ?: "NOT REPRESENTABLE — the perpendicular root is a CONSTRAINT"),
            "obliqueNormalStiffnessCrossoverHinged" to
                    rootReadings[1].obliqueNormalStiffness.emitted(),
            "obliqueNormalStiffnessConstrainedLink" to
                    rootReadings[2].obliqueNormalStiffness.emitted(),
            "pairedObliqueNormalStiffness" to
                    rootReadings[1].pairedObliqueNormalStiffness!!.emitted(),
            "pairedCostFactor" to rootReadings[1].pairedCostFactor!!.emitted(),
            "obliquePathFractionAtTenPaths" to
                    pathReadings[0].obliquePathFractionSingle.emitted(),
            "mandateShortfallIfNotResized" to pathReadings[0].mandateShortfall.emitted(),
            "alternatingCellsFlatAtP90" to "$flatAlternating of ${alternatingCells.size}",
            "bestAlternatingP90" to bestAlternating.p90OverStroke.emitted(),
            "worstAlternatingOverEqual" to worstRatio.emitted(),
            "censusedStations10x6" to censusCorrection[1].censusedStations.toString(),
            "correctedStations10x6" to censusCorrection[1].correctedStations.toString(),
            "censusedStations15x4" to censusCorrection[0].censusedStations.toString(),
            "correctedStations15x4" to censusCorrection[0].correctedStations.toString(),
            "F1fired" to f1Fired.toString(),
            "F2fired" to f2Fired.toString()
        ),
        convergence = convergence,
        reproductions = reproductions,
        falsifiers = listOf(
            "F1 — if the cost factor is NOT monotone in the root's own radial-to-tangential " +
                    "anisotropy, the closed form is the wrong decomposition and a solve is needed.",
            "F2 — if the oblique root's ABSOLUTE normal stiffness depends strongly on the covalent " +
                    "link's stiffness — which this corpus holds to be a constraint, i.e. a binary " +
                    "— then the branch that matters is unbounded and only a bracket can be quoted."
        ),
        findings = findings,
        validity = listOf(
            "The tangential axis is C-0009's dihedral spring, which is FITTED (Chen et al.), on " +
                    "the frame-indifferent d/2 lever. The radial axis is C-0020's in-plane link, " +
                    "which is DERIVED from the same construction and NOT measured — which is why " +
                    "it is swept over four decades here rather than quoted.",
            "The decomposition assumes the root's two axes are the eigenvectors of its " +
                    "translational stiffness tensor. That is asserted as a test " +
                    "(normalStiffnessFromTensor) and is what would fail if the two covalent links' " +
                    "own geometry coupled the axes.",
            "A flexible tie's isotropy is a SYMMETRY of the element, not a measurement of it: a " +
                    "flexible link has no direction of its own, so it cannot know which azimuth it " +
                    "left the helix on. Its magnitude still comes from the same derived constant.",
            "The re-grading moves the DISTRIBUTION only. The lateral offset of an oblique root's " +
                    "exit point is below the grillage's across-helix resolution and is reported " +
                    "rather than modelled.",
            "Only the 10 x 6 cross-section is re-graded, because it is the one C-0118 finds flat. " +
                    "15 x 4 is not flat under equal springs at any of these counts either way.",
            "The dropout statistics are measured on a SINGLE-LAYER Rothemund rectangle; only the " +
                    "profile transfers, in nm. C-0109's assumption, inherited and named."
        ),
        openQuestions = listOf(
            "Whether a design would ACCEPT the corrected census: two roots on one helix are 7 bp " +
                    "apart on the same duplex, which Ke et al. record as a folding-yield cost for " +
                    "the square lattice's 8 bp domains. The count is a lattice fact; the yield is " +
                    "not checked here.",
            "T-9 would settle the covalent link's own constant and with it whether the RATIO is a " +
                    "number at all. Nothing else in this study needs it.",
            "Whether a PAIRED oblique root — both free azimuths of one helix on one head — is a " +
                    "buildable motif. It is priced here and its plan packing is not checked, and " +
                    "it spends two stations of C-0122's census for one path.",
            "Whether an attachment can be rooted on an azimuth at all without the strand's exit " +
                    "geometry costing a torsion feasibility, which is C-0057's question on a " +
                    "lattice this corpus has not run it on."
        )
    )
    val output = File("gpd/results/T-206-oblique-root.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digits = 9, digitsByKey = mapOf("departure" to 2)
            ) as JsonObject)
        ) + "\n"
    )
    println("T-206 — wrote ${output.path}")
}
