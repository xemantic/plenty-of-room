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
import com.xemantic.nano.plentyofroom.coupling.winklerBendingLength
import com.xemantic.nano.plentyofroom.coupling.worstSinglePathRemoval
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.ResultInputs
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
import kotlin.math.floor

// ---------------------------------------------------------------------------------------------
// T-197 -- is a COUPLED four-layer tile flat under the measured staple dropout?
//
// C-0109 grades ONE distribution (equal), ONE placement family (a uniform grid) and ONE topology
// (an array), and reports every coupled cell as worse than the UNCOUPLED tile. That comparison is
// only decisive if the uncoupled tile is a design the device could have, and it is not: SS3
// requires 100 pN to reach a load, so C-0017's mandate is an EQUALITY on the sum and the coupling
// total is fixed and non-zero by specification.
//
// So the question is: AT THE MANDATED TOTAL, is the four-layer tile flat under C-0087's measured
// dropout -- and does either unspent axis (the DISTRIBUTION, worth 1.30-1.61x on C-0089's array;
// the CROSS-SECTION, which T-199 has just shown is worth 6.6x uncoupled) change the answer?
// ---------------------------------------------------------------------------------------------

private const val T197_SAMPLES: Int = 81
private const val T197_TOLERANCE: Double = 0.10
private const val T197_RIM_STANDOFF: Double = 1.0
private const val T197_ROW_BP: Int = 112
private const val T197_REALISATIONS: Int = 4000
private const val T197_SEED: Long = 197_197L

private fun Double.emitted(digits: Int = 9): String =
    if (!isFinite() || this == 0.0) toString()
    else java.math.BigDecimal(this).round(java.math.MathContext(digits)).toDouble().toString()

@Serializable
private class T197Cell(
    val crossSection: String,
    val columns: Int,
    val rows: Int,
    val pathCount: Int,
    val distribution: String,
    val perPathStiffness: Double,
    val totalStiffness: Double,
    val attachmentPitchAlong: Double,
    val attachmentPitchAcross: Double,
    val pitchAlongOverReach: Double,
    val pitchAcrossOverReach: Double,
    val nominalOverStroke: Double,
    val worstSingleRemovalOverStroke: Double,
    val medianOverStroke: Double,
    val p90OverStroke: Double,
    val worstOverStroke: Double,
    val exceedance: Double,
    val flatAtNominal: Boolean,
    val flatAtP90: Boolean,
    val beatsUncoupledAtNominal: Boolean,
    val beatsUncoupledAtP90: Boolean
)

@Serializable
private class T197Reference(
    val crossSection: String,
    val uncoupledDishingOverStroke: Double,
    val flat: Boolean,
    val reachAlong: Double,
    val reachAcross: Double,
    val edgeX: Double,
    val edgeY: Double
)

@Serializable
private class T197Convergence(
    val axis: String, val values: List<Double>, val results: List<Double>,
    val departure: Double, val note: String
)

@Serializable
private class T197Reproduction(
    val source: String, val quantity: String, val published: Double,
    val reproduced: Double, val departure: Double, val strict: Boolean
)

@Serializable
private class T197Result(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: Map<String, String>,
    val parameters: Map<String, String>,
    val citedInputs: List<String>,
    val theFraming: Map<String, String>,
    val references: List<T197Reference>,
    val cells: List<T197Cell>,
    val verdict: Map<String, String>,
    val convergence: List<T197Convergence>,
    val reproductions: List<T197Reproduction>,
    val falsifiers: List<String>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

private class T197Profile(val smoothDepth: Double, val smoothWidth: Double, val rimDepth: Double) {
    fun field(interiorPressure: Double, edgeX: Double, lengthY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, edgeX, lengthY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T197_RIM_STANDOFF))
        )
}

private fun t197Profile(file: File): T197Profile {
    require(file.exists()) { "C-0022's result file is missing: ${file.path}" }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .firstOrNull {
            fun value(name: String) = it.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == 2.0 && value("gapHeight") == 10.0 &&
                    value("appliedBias") == 0.192
        } ?: error("no C-0022 profile at the design state")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T197Profile(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

private class T197Tile(val rasterRows: Int, val layers: Int, private val profile: T197Profile) {
    val edgeX: Double = T197_ROW_BP * Gen1Tile.RISE_PER_BASE_PAIR
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
    /** Named `pressureField` and not `field`: inside a property accessor, `field` is the
     *  backing-field keyword, so a property called `field` is shadowed there and the compiler
     *  reports it as an uninitialised property on a line that looks correct. */
    val pressureField: PressureField = profile.field(interiorPressure, edgeX, edgeY)
    val freeStroke: Double = PlateOnFoundation(
        sheet.plate(edgeX, edgeY), Gen1Tile.FOUNDATION_SECANT, emptyList(), 12
    ).solve(uniformPressure(interiorPressure)).meanDeflection
    val reachAlong: Double =
        winklerBendingLength(rigidities.alongHelixRigidity, Gen1Tile.FOUNDATION_SECANT)
    val reachAcross: Double =
        winklerBendingLength(rigidities.acrossHelixRigidity, Gen1Tile.FOUNDATION_SECANT)

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

    val uncoupledDishing: Double get() = lattice().solve(pressureField).peakDishing(T197_SAMPLES) / freeStroke
}

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    val profile = t197Profile(ResultInputs.T_3B.file())
    val tiles = listOf(T197Tile(15, 4, profile), T197Tile(10, 6, profile))

    println("T-197 — the framing, before any coupled cell is graded")
    println("  SS3 requires 100 pN to reach a load, so C-0017's mandate is an EQUALITY on the SUM:")
    println("  the coupling total is %s pN/nm and cannot be zero. The UNCOUPLED tile is a".format(
        MANDATED_TOTAL_STIFFNESS.emitted(6)
    ))
    println("  REFERENCE, never a design — which is what C-0109's comparison needed and lacked.")

    val references = tiles.map { tile ->
        val uncoupled = tile.uncoupledDishing
        println(
            "  %-8s uncoupled %.9f  %s   reach %.2f / %.2f nm   tile %.2f x %.2f".format(
                tile.name, uncoupled, if (uncoupled < T197_TOLERANCE) "flat    " else "NOT flat",
                tile.reachAlong, tile.reachAcross, tile.edgeX, tile.edgeY
            )
        )
        T197Reference(
            crossSection = tile.name,
            uncoupledDishingOverStroke = uncoupled,
            flat = uncoupled < T197_TOLERANCE,
            reachAlong = tile.reachAlong,
            reachAcross = tile.reachAcross,
            edgeX = tile.edgeX,
            edgeY = tile.edgeY
        )
    }

    val cells = ArrayList<T197Cell>()
    tiles.forEach { tile ->
        val uncoupled = tile.uncoupledDishing
        val lattice = tile.lattice()
        listOf(1, 2, 3, 5).forEach { columns ->
            val grid = attachmentGrid(columns, tile.rasterRows, tile.edgeX, tile.edgeY)
            val surrogate = latticeInfluenceSurrogate(lattice, grid, tile.pressureField, T197_SAMPLES)
            val incorporation = measuredDepthIncorporation(tile.edgeX, tile.edgeY)
            val probabilities = grid.map { (x, y) -> incorporation.at(x, y) }
            val ensemble = dropoutEnsemble(probabilities, T197_REALISATIONS, T197_SEED)
            val pitchAlong = tile.edgeX / columns
            val pitchAcross = tile.edgeY / tile.rasterRows

            // Two distributions on the SAME mandate: equal, and `C-0058`'s rim grading. The
            // distribution axis is a redistribution of a fixed budget, never a change of total.
            val distributions = listOf(
                "equal springs" to equalShareOfMandate(grid.size),
                "rim-graded 5:1" to rimGradedShareOfMandate(
                    grid.map { (x, y) ->
                        val onRim = abs(x) > tile.edgeX / 2.0 - 6.7 || abs(y) > tile.edgeY / 2.0 - 6.7
                        if (onRim) 5.0 else 1.0
                    }
                )
            )
            distributions.forEach { (label, stiffnesses) ->
                val nominal = surrogate.solve(stiffnesses).peakDishing / tile.freeStroke
                val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
                sample.indices.forEach { sample[it] = sample[it] / tile.freeStroke }
                val summary = summariseDropoutDishing(
                    sample, nominal, ensemble.meanSurvivors, T197_TOLERANCE
                )
                val worstSingle = worstSinglePathRemoval(surrogate, stiffnesses) / tile.freeStroke
                cells += T197Cell(
                    crossSection = tile.name,
                    columns = columns,
                    rows = tile.rasterRows,
                    pathCount = grid.size,
                    distribution = label,
                    perPathStiffness = stiffnesses.max(),
                    totalStiffness = stiffnesses.sum(),
                    attachmentPitchAlong = pitchAlong,
                    attachmentPitchAcross = pitchAcross,
                    pitchAlongOverReach = pitchAlong / tile.reachAlong,
                    pitchAcrossOverReach = pitchAcross / tile.reachAcross,
                    nominalOverStroke = nominal,
                    worstSingleRemovalOverStroke = worstSingle,
                    medianOverStroke = summary.median,
                    p90OverStroke = summary.p90,
                    worstOverStroke = summary.worst,
                    exceedance = summary.exceedance,
                    flatAtNominal = nominal < T197_TOLERANCE,
                    flatAtP90 = summary.flatAtP90,
                    beatsUncoupledAtNominal = nominal < uncoupled,
                    beatsUncoupledAtP90 = summary.p90 < uncoupled
                )
                println(
                    "  %-8s %d col x %2d = %3d paths, %-14s  nominal %.9f  p90 %.9f  %s".format(
                        tile.name, columns, tile.rasterRows, grid.size, label, nominal, summary.p90,
                        if (summary.flatAtP90) "FLAT at p90" else "not flat at p90"
                    )
                )
            }
        }
    }

    val flatCells = cells.filter { it.flatAtP90 }
    val bestByP90 = cells.minByOrNull { it.p90OverStroke }!!
    val bestOurs = cells.filter { it.crossSection == "15 x 4" }.minByOrNull { it.p90OverStroke }!!
    val bestTheirs = cells.filter { it.crossSection == "10 x 6" }.minByOrNull { it.p90OverStroke }!!

    val realisationResults = listOf(1000, 2000, 4000).map { n ->
                val tile = tiles.first { it.name == bestByP90.crossSection }
                val grid = attachmentGrid(
                    bestByP90.columns, tile.rasterRows, tile.edgeX, tile.edgeY
                )
                val surrogate = latticeInfluenceSurrogate(
                    tile.lattice(), grid, tile.pressureField, T197_SAMPLES
                )
                val incorporation = measuredDepthIncorporation(tile.edgeX, tile.edgeY)
                val ensemble = dropoutEnsemble(
                    grid.map { (x, y) -> incorporation.at(x, y) }, n, T197_SEED
                )
                val stiffnesses = equalShareOfMandate(grid.size)
                val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
                sample.indices.forEach { sample[it] = sample[it] / tile.freeStroke }
                summariseDropoutDishing(
                    sample, surrogate.solve(stiffnesses).peakDishing / tile.freeStroke,
                    ensemble.meanSurvivors, T197_TOLERANCE
                ).p90
    }
    val convergence = listOf(
        T197Convergence(
            axis = "dropout realisations, best cell's 90th percentile",
            values = listOf(1000.0, 2000.0, 4000.0),
            results = realisationResults,
            departure = abs(realisationResults[2] - realisationResults[1]) /
                    abs(realisationResults[2]),
            note = "a Monte Carlo percentile on a COMMON stream — the same seed restricted, not " +
                    "three independent draws, so the departure is a convergence and not a variance"
        )
    )

    val ours = references.first { it.crossSection == "15 x 4" }
    val reproductions = listOf(
        T197Reproduction(
            "C-0109 / C-0116 / C-0120", "15 x 4 UNCOUPLED free-tile dishing", 0.0577199433,
            ours.uncoupledDishingOverStroke,
            abs(ours.uncoupledDishingOverStroke - 0.0577199433) / 0.0577199433, true
        ),
        T197Reproduction(
            "C-0120", "10 x 6 UNCOUPLED free-tile dishing", 0.00874363524,
            references.first { it.crossSection == "10 x 6" }.uncoupledDishingOverStroke,
            abs(
                references.first { it.crossSection == "10 x 6" }.uncoupledDishingOverStroke -
                        0.00874363524
            ) / 0.00874363524, true
        )
    )
    reproductions.forEach {
        println(
            "  reproduce %s %s: %.9f against %.9f, departure %.2e".format(
                it.source, it.quantity, it.reproduced, it.published, it.departure
            )
        )
    }

    val findings = HashMap<String, String>()
    findings["theUncoupledTileIsNotADesign"] =
        ("SS3 requires 100 pN to reach a load, so C-0017's mandate is an EQUALITY on the SUM and the " +
                "coupling total is fixed at %s pN/nm by specification. C-0109's finding that every " +
                "coupled cell is worse than the uncoupled tile is therefore true and NOT a design " +
                "verdict: the uncoupled tile is a reference the device cannot be."
            ).format(MANDATED_TOTAL_STIFFNESS.emitted(6))
    findings["theVerdict"] =
        ("Of %d graded cells, %d are flat at the 90th percentile under C-0087's measured dropout. " +
                "The best is %s at %d columns on the %s cross-section, p90 = %s."
            ).format(
                cells.size, flatCells.size, bestByP90.distribution, bestByP90.columns,
                bestByP90.crossSection, bestByP90.p90OverStroke.emitted()
            )
    findings["theCrossSectionDominatesTheDistribution"] =
        ("Best on 15 x 4: %s (%s, %d columns). Best on 10 x 6: %s (%s, %d columns). The " +
                "cross-section is worth %s x on the 90th percentile, against what the DISTRIBUTION " +
                "axis buys within either one."
            ).format(
                bestOurs.p90OverStroke.emitted(), bestOurs.distribution, bestOurs.columns,
                bestTheirs.p90OverStroke.emitted(), bestTheirs.distribution, bestTheirs.columns,
                (bestOurs.p90OverStroke / bestTheirs.p90OverStroke).emitted(6)
            )

    val result = T197Result(
        task = "T-197",
        leaf = "A8.2",
        title = "Is a coupled four-layer tile flat under the measured staple dropout?",
        verificationType = "in-silico (influence surrogate over the grillage, Monte Carlo dropout)",
        maturity = "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated.",
        units = mapOf(
            "stiffness" to "pN/nm", "length" to "nm",
            "dishing" to "dimensionless, as a fraction of the free-tile stroke"
        ),
        conventions = mapOf(
            "mandate" to "C-0017's equality on the SUM, SS3's acceptable clause: 100 pN / 3 nm",
            "load" to "C-0022's solved collar at 2 mM / 10 nm / 0.192 V",
            "dropout" to "C-0087's measured per-site staple incorporation, depth convention",
            "flat" to "peak dishing below T-5b's 0.10",
            "span" to "112 bp along the helices for both cross-sections"
        ),
        parameters = mapOf(
            "realisations" to T197_REALISATIONS.toString(),
            "seed" to T197_SEED.toString(),
            "dishingSamplesPerSide" to T197_SAMPLES.toString(),
            "tolerance" to T197_TOLERANCE.toString(),
            "mandatedTotalStiffness" to MANDATED_TOTAL_STIFFNESS.emitted(),
            "rowBasePairs" to T197_ROW_BP.toString()
        ),
        citedInputs = listOf(
            "C-0109 — the four-layer tile, and the coupled cells this extends",
            "C-0120 — the cross-section comparison, and the 10 x 6 tile graded here",
            "C-0017 — the mandate, an equality on the SUM",
            "C-0087 — the measured per-site staple incorporation",
            "C-0058 / C-0089 — the distribution axis"
        ),
        theFraming = mapOf(
            "whyTheUncoupledTileIsNotADesign" to findings["theUncoupledTileIsNotADesign"]!!,
            "whatIsActuallyBeingAsked" to "at the mandated total, is the four-layer tile flat under " +
                    "the measured dropout, and does the DISTRIBUTION or the CROSS-SECTION change it"
        ),
        references = references,
        cells = cells,
        verdict = mapOf(
            "gradedCells" to cells.size.toString(),
            "flatAtP90" to flatCells.size.toString(),
            "bestCrossSection" to bestByP90.crossSection,
            "bestColumns" to bestByP90.columns.toString(),
            "bestDistribution" to bestByP90.distribution,
            "bestP90" to bestByP90.p90OverStroke.emitted(),
            "bestOn15x4" to bestOurs.p90OverStroke.emitted(),
            "bestOn10x6" to bestTheirs.p90OverStroke.emitted(),
            "crossSectionWorth" to (bestOurs.p90OverStroke / bestTheirs.p90OverStroke).emitted()
        ),
        convergence = convergence,
        reproductions = reproductions,
        falsifiers = listOf(
            "F1 — no coupled cell is flat at the 90th percentile on EITHER cross-section, in which " +
                    "case C-0109's residual is irreducible on the axes this study can reach.",
            "F2 — the uncoupled references do not reproduce C-0120's numbers, in which case no " +
                    "comparison is licensed.",
            "F3 — the DISTRIBUTION axis outperforms the CROSS-SECTION axis, which would make the " +
                    "coupling the thing to design and the tile the thing to accept."
        ),
        findings = findings,
        validity = listOf(
            "The mandate is read at SS3's ACCEPTABLE clause (100 pN / 3 nm). CLAUDE.md records that " +
                    "the DESIRED clause gives 10 pN/nm and a different device, and NDI's answer to " +
                    "decision 4 is that both exist.",
            "The dropout statistics are measured on a SINGLE-LAYER Rothemund rectangle; only the " +
                    "PROFILE transfers, in nm, and a four-layer tile has a different staple " +
                    "population. This is C-0109's assumption, inherited and named.",
            "The attachment grid is the abstract one, not a lattice census: every plan ceiling in " +
                    "this corpus is single-layer SQUARE-lattice and the honeycomb's three azimuths " +
                    "are a different inventory (C-0119). So a path count here is a REQUEST, not a " +
                    "demonstration that the stations exist.",
            "Only two distributions are graded — equal and C-0058's rim rule. C-0089's percentile " +
                    "DESCENT is not run, and it is worth 1.30-1.61x on an array.",
            "The shared-body TOPOLOGY (C-0093) is not run here either."
        ),
        openQuestions = listOf(
            "Whether C-0089's percentile descent and C-0093's shared body move the answer on the " +
                    "cross-section that survives, which is the remainder of this task's original scope.",
            "Whether the honeycomb lattice supplies the attachment stations any of these cells asks " +
                    "for -- a census question this corpus has only ever answered for the square lattice.",
            "Whether C-0022's collar transfers to the 10 x 6 aspect ratio, which T-199 also flags."
        )
    )
    val output = File("gpd/results/T-197-coupled-four-layer.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digits = 9, digitsByKey = mapOf("departure" to 2)
            ).withEmissionHeader(LatticeTag.SQUARE, null) as JsonObject)
        ) + "\n"
    )
    println("T-197 — wrote ${output.path}")
}
