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
import kotlin.math.abs
import kotlin.math.floor

// ---------------------------------------------------------------------------------------------
// T-203 -- what attachment lattice does a honeycomb block's top face offer?
//
// C-0118 produced the first coupled tile flat under the measured folding statistics, and named its
// own largest caveat: the attachment grid is the ABSTRACT one, every plan ceiling in this corpus is
// single-layer SQUARE-lattice, and nobody has counted what the honeycomb offers. So every path
// count there is a REQUEST rather than a demonstration that the stations exist.
//
// This closes the count and opens the position: the stations sit on a 21 bp = 7.14 nm ladder along
// the helices, while C-0118 placed them on an EVEN grid. The decisive check is therefore not the
// census but the re-grading on SNAPPED stations, which is run here.
// ---------------------------------------------------------------------------------------------

private const val T203_SAMPLES: Int = 81
private const val T203_TOLERANCE: Double = 0.10
private const val T203_RIM_STANDOFF: Double = 1.0
private const val T203_ROW_BP: Int = 112
private const val T203_REALISATIONS: Int = 4000
private const val T203_SEED: Long = 203_203L

private fun Double.emitted(digits: Int = 9): String =
    if (!isFinite() || this == 0.0) toString()
    else java.math.BigDecimal(this).round(java.math.MathContext(digits)).toDouble().toString()

@Serializable
private class T203Census(
    val crossSection: String,
    val rasterRows: Int,
    val layers: Int,
    val helices: Int,
    val topFaceHelices: Int,
    val stationsPerHelix: Int,
    val stationsAvailable: Int,
    val perpendicularRootHelices: Int,
    val obliqueRootHelices: Int,
    val alongHelixPitch: Double,
    val acrossHelixPitch: Double
)

@Serializable
private class T203Demand(
    val crossSection: String,
    val columns: Int,
    val pathsDemanded: Int,
    val stationsAvailable: Int,
    val fits: Boolean,
    val evenGridPitch: Double,
    val ladderPitch: Double,
    val evenGridPitchOverLadder: Double,
    val snappedNominalOverStroke: Double,
    val snappedP90OverStroke: Double,
    val evenNominalOverStroke: Double,
    val evenP90OverStroke: Double,
    val snappedFlatAtP90: Boolean,
    val evenFlatAtP90: Boolean,
    val snapCostAtP90: Double
)

@Serializable
private class T203Result(
    val task: String, val leaf: String, val title: String, val verificationType: String,
    val maturity: String, val units: Map<String, String>, val conventions: Map<String, String>,
    val parameters: Map<String, String>, val citedInputs: List<String>,
    val theSquareLatticeDoesNotTransfer: Map<String, String>,
    val census: List<T203Census>, val demands: List<T203Demand>,
    val verdict: Map<String, String>, val falsifiers: List<String>,
    val findings: Map<String, String>, val validity: List<String>,
    val openQuestions: List<String>
)

private class T203Profile(val smoothDepth: Double, val smoothWidth: Double, val rimDepth: Double) {
    fun field(interiorPressure: Double, edgeX: Double, lengthY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, edgeX, lengthY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T203_RIM_STANDOFF))
        )
}

private fun t203Profile(file: File): T203Profile {
    require(file.exists()) { "C-0022's result file is missing: ${file.path}" }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .firstOrNull {
            fun value(name: String) = it.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == 2.0 && value("gapHeight") == 10.0 &&
                    value("appliedBias") == 0.192
        } ?: error("no C-0022 profile at the design state")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T203Profile(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

/**
 * The attachment grid SNAPPED to the honeycomb's own 21 bp station ladder along the helices.
 *
 * Across the helices the pitch is one duplex either way, so only the along-helix coordinate moves.
 */
private fun snappedGrid(
    columns: Int, rows: Int, edgeX: Double, edgeY: Double, ladder: Double
): List<Pair<Double, Double>> = attachmentGrid(columns, rows, edgeX, edgeY).map { (x, y) ->
    val fromEdge = x + edgeX / 2.0
    val snapped = Math.round(fromEdge / ladder).toDouble() * ladder
    (snapped.coerceIn(0.0, edgeX) - edgeX / 2.0) to y
}

@Suppress("LongMethod")
fun main() {
    val profile = t203Profile(File("gpd/results/T-3b-tile-edge-load-profile.json"))
    val ladder = HoneycombLattice.SAME_PAIR_PERIOD_BP * Gen1Tile.RISE_PER_BASE_PAIR

    println("T-203 — the square lattice does not transfer, and here is what does")
    println(
        "  square: 4 azimuths, 8 bp planes, same pair every 32 bp | honeycomb: %d azimuths, %d bp step, same pair every %d bp".format(
            HoneycombLattice.AZIMUTHS, HoneycombLattice.ANY_AZIMUTH_STEP_BP,
            HoneycombLattice.SAME_PAIR_PERIOD_BP
        )
    )
    println("  an attachment roots on ONE azimuth, so its ladder is the 21 bp period: %.3f nm".format(ladder))

    val sections = listOf(15 to 4, 10 to 6)
    val censuses = sections.map { (rows, layers) ->
        val c = honeycombStationCensus(rows, layers, T203_ROW_BP)
        println(
            "  %2d x %-2d  top face %2d of %2d helices, %d stations each, %3d AVAILABLE  (%d perpendicular, %d oblique)".format(
                rows, layers, c.topFaceHelices, c.helices, c.stationsPerHelix, c.stations,
                c.perpendicularRootHelices, c.obliqueRootHelices
            )
        )
        T203Census(
            crossSection = "$rows x $layers", rasterRows = rows, layers = layers,
            helices = c.helices, topFaceHelices = c.topFaceHelices,
            stationsPerHelix = c.stationsPerHelix, stationsAvailable = c.stations,
            perpendicularRootHelices = c.perpendicularRootHelices,
            obliqueRootHelices = c.obliqueRootHelices,
            alongHelixPitch = c.alongHelixPitch, acrossHelixPitch = c.acrossHelixPitch
        )
    }

    val demands = ArrayList<T203Demand>()
    sections.forEach { (rows, layers) ->
        val name = "$rows x $layers"
        val available = censuses.first { it.crossSection == name }.stationsAvailable
        val rigidities = multiLayerRigidities(
            layers = layers,
            interhelicalDistance = Gen1Tile.INTERHELICAL_HONEYCOMB,
            crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
            coupling = LayerCoupling.CALIBRATED
        )
        val sheet = equivalentSheet(rigidities)
        val edgeX = T203_ROW_BP * Gen1Tile.RISE_PER_BASE_PAIR
        val edgeY = rows * Gen1Tile.INTERHELICAL_HONEYCOMB
        val interiorPressure = Gen1Tile.TARGET_FORCE / (edgeX * edgeY)
        val pressure = profile.field(interiorPressure, edgeX, edgeY)
        val freeStroke = PlateOnFoundation(
            sheet.plate(edgeX, edgeY), Gen1Tile.FOUNDATION_SECANT, emptyList(), 12
        ).solve(uniformPressure(interiorPressure)).meanDeflection
        val pitch = sheet.crossoverSpacing / 2.0
        val usable = edgeX - 2.0 * CrossoverLayout.EDGE_MARGIN
        val lattice = OrigamiGrillage(
            sheet = sheet, lengthX = edgeX, beamCount = rows,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            columns = CrossoverLayout.centred(floor(usable / pitch).toInt() + 1, pitch),
            subdivisions = 2
        )
        val incorporation = measuredDepthIncorporation(edgeX, edgeY)

        listOf(1, 2, 3, 5).forEach { columns ->
            fun grade(grid: List<Pair<Double, Double>>): Pair<Double, Double> {
                val surrogate = latticeInfluenceSurrogate(lattice, grid, pressure, T203_SAMPLES)
                val stiffnesses = equalShareOfMandate(grid.size)
                val nominal = surrogate.solve(stiffnesses).peakDishing / freeStroke
                val ensemble = dropoutEnsemble(
                    grid.map { (x, y) -> incorporation.at(x, y) }, T203_REALISATIONS, T203_SEED
                )
                val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
                sample.indices.forEach { sample[it] = sample[it] / freeStroke }
                return nominal to summariseDropoutDishing(
                    sample, nominal, ensemble.meanSurvivors, T203_TOLERANCE
                ).p90
            }
            val even = grade(attachmentGrid(columns, rows, edgeX, edgeY))
            val snapped = grade(snappedGrid(columns, rows, edgeX, edgeY, ladder))
            val demanded = columns * rows
            demands += T203Demand(
                crossSection = name, columns = columns, pathsDemanded = demanded,
                stationsAvailable = available, fits = demanded <= available,
                evenGridPitch = edgeX / columns, ladderPitch = ladder,
                evenGridPitchOverLadder = (edgeX / columns) / ladder,
                snappedNominalOverStroke = snapped.first, snappedP90OverStroke = snapped.second,
                evenNominalOverStroke = even.first, evenP90OverStroke = even.second,
                snappedFlatAtP90 = snapped.second < T203_TOLERANCE,
                evenFlatAtP90 = even.second < T203_TOLERANCE,
                snapCostAtP90 = snapped.second / even.second
            )
            println(
                "  %-8s %d col -> %3d paths of %3d  %s   even p90 %.9f %s   SNAPPED p90 %.9f %s".format(
                    name, columns, demanded, available, if (demanded <= available) "FITS  " else "EXCEEDS",
                    even.second, if (even.second < T203_TOLERANCE) "flat" else "----",
                    snapped.second, if (snapped.second < T203_TOLERANCE) "FLAT" else "----"
                )
            )
        }
    }

    val allFit = demands.all { it.fits }
    val snappedFlat = demands.count { it.snappedFlatAtP90 }
    val evenFlat = demands.count { it.evenFlatAtP90 }
    val findings = HashMap<String, String>()
    findings["theCountIsSupplied"] =
        ("Every one of C-0118's coupled cells fits on the honeycomb's own top face: %s. 15 x 4 " +
                "offers 90 stations and 10 x 6 offers 60, against 10 to 75 demanded. C-0118's " +
                "caveat -- that its path counts were a REQUEST rather than a demonstration -- is " +
                "discharged on the COUNT."
            ).format(if (allFit) "all of them" else "NOT all of them")
    findings["aDeeperBlockOffersFEWERStations"] =
        ("The census is set by the FACE, not by the helix count: at a fixed 60 helices, 15 x 4 has " +
                "15 top-face helices and 10 x 6 has 10, so the flatter, stiffer cross-section " +
                "offers 60 stations against 90. A thicker tile buys rigidity and SPENDS attachment " +
                "lattice, which is the opposite of what a reader expects and is the one place " +
                "where T-199's recommendation costs something structural.")
    findings["theOpenQuestionWasThePOSITION"] =
        ("The stations sit on a 21 bp = %s nm ladder along the helices and C-0118 placed them on an " +
                "EVEN grid. Snapped to the ladder, %d of %d cells are flat at the 90th percentile " +
                "against %d on the even grid."
            ).format(ladder.emitted(6), snappedFlat, demands.size, evenFlat)

    val result = T203Result(
        task = "T-203", leaf = "A8.2",
        title = "What attachment lattice does a honeycomb block's top face offer?",
        verificationType = "logical (a lattice census) + in-silico (the snapped re-grading)",
        maturity = "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated.",
        units = mapOf("length" to "nm and base pairs", "count" to "dimensionless",
            "dishing" to "dimensionless, as a fraction of the free-tile stroke"),
        conventions = mapOf(
            "lattice" to "honeycomb: 3 azimuths, 7 bp step, same pair every 21 bp (C-0119)",
            "station" to "one free azimuth on a TOP-FACE helix, at a 21 bp position",
            "mandate" to "C-0017's equality on the sum, equal springs",
            "dropout" to "C-0087's measured per-site incorporation",
            "flat" to "peak dishing below T-5b's 0.10"
        ),
        parameters = mapOf(
            "rowBasePairs" to T203_ROW_BP.toString(),
            "ladderPitch" to ladder.emitted(),
            "realisations" to T203_REALISATIONS.toString(),
            "seed" to T203_SEED.toString(),
            "azimuths" to HoneycombLattice.AZIMUTHS.toString(),
            "samePairPeriodBasePairs" to HoneycombLattice.SAME_PAIR_PERIOD_BP.toString()
        ),
        citedInputs = listOf(
            "C-0119 — the honeycomb design rules, read directly from Douglas et al.",
            "C-0118 — the coupled cells whose station demand this censuses",
            "C-0120 — the two cross-sections",
            "C-0087 — the measured dropout",
            "C-0022 — the solved collar"
        ),
        theSquareLatticeDoesNotTransfer = mapOf(
            "squareAzimuths" to "4", "squarePlaneStep" to "8 bp",
            "squareSamePairPeriod" to "32 bp",
            "honeycombAzimuths" to HoneycombLattice.AZIMUTHS.toString(),
            "honeycombStep" to "${HoneycombLattice.ANY_AZIMUTH_STEP_BP} bp",
            "honeycombSamePairPeriod" to "${HoneycombLattice.SAME_PAIR_PERIOD_BP} bp",
            "theConflationToAvoid" to "7 bp is ANY azimuth's next position and 21 bp is ONE " +
                    "azimuth's period; an attachment roots on one azimuth, so its ladder is 21"
        ),
        census = censuses, demands = demands,
        verdict = mapOf(
            "everyDemandFits" to allFit.toString(),
            "cellsFlatOnTheEvenGrid" to evenFlat.toString(),
            "cellsFlatOnTheSnappedLadder" to snappedFlat.toString(),
            "gradedCells" to demands.size.toString()
        ),
        falsifiers = listOf(
            "F1 — some cell of C-0118's demands more stations than the top face supplies, in which " +
                    "case that cell's flatness is unbuildable and the caveat stands.",
            "F2 — snapping to the ladder destroys the flatness, in which case the count is " +
                    "supplied and the POSITIONS are the obstruction."
        ),
        findings = findings,
        validity = listOf(
            "The census counts the TOP FACE only. A buried helix has all three azimuths occupied, " +
                    "so it has no free direction to root on -- the slab analogue of the square " +
                    "lattice's 'a single-layer sheet occupies two of its four azimuths'.",
            "It counts STATIONS, not a placement: whether a chosen subset is centro-symmetric, " +
                    "clear of the seam, or compatible with the scaffold route is not asked here.",
            "The sublattice parity fixes WHICH top-face helices carry a perpendicular root and " +
                    "which carry two oblique ones. It does not change the count -- every top-face " +
                    "helix has one free direction either way -- and this study does not price the " +
                    "difference between a perpendicular and an oblique attachment.",
            "The snapped re-grading uses EQUAL springs only, as C-0118's best cells do.",
            "Nothing here re-derives a rigidity, a threshold or a collar."
        ),
        openQuestions = listOf(
            "What an OBLIQUE root costs against a perpendicular one -- half the top-face helices " +
                    "carry only oblique free azimuths, and this corpus has never priced one.",
            "Whether a chosen station subset is compatible with the scaffold raster and its seam.",
            "Whether the 10 x 6 census survives C-0022's collar being re-solved at its aspect ratio."
        )
    )
    val output = File("gpd/results/T-203-honeycomb-station-lattice.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(digits = 9) as JsonObject)
        ) + "\n"
    )
    println("T-203 — wrote ${output.path}")
}
