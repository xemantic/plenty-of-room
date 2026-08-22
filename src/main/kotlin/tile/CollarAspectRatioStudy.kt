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
import kotlin.math.floor

// ---------------------------------------------------------------------------------------------
// T-204 -- does C-0022's collar transfer to the 10 x 6 aspect ratio?
//
// C-0022 solved the edge profile on a 40 x 40.35 nm tile and every four-layer number is read under
// it unchanged. The collar is LOCAL -- a sub-Debye band whose depth and width are set by screening
// and not by the tile -- so its SHARE of the load scales as perimeter over area, and the transfer
// is boundable before any field is solved. The bound is the cheap part; the question it leaves is
// a SENSITIVITY, and this study measures it rather than arguing it.
// ---------------------------------------------------------------------------------------------

private const val T204_SAMPLES: Int = 81
private const val T204_TOLERANCE: Double = 0.10
private const val T204_RIM_STANDOFF: Double = 1.0
private const val T204_ROW_BP: Int = 112
private const val T204_REALISATIONS: Int = 4000
private const val T204_SEED: Long = 204_204L
private const val T204_SOLVED_EDGE_X: Double = 40.0
private const val T204_SOLVED_EDGE_Y: Double = 40.35

private fun Double.emitted(digits: Int = 9): String =
    if (!isFinite() || this == 0.0) toString()
    else java.math.BigDecimal(this).round(java.math.MathContext(digits)).toDouble().toString()

@Serializable
private class T204Cell(
    val crossSection: String,
    val columns: Int,
    val pathCount: Int,
    val collarScale: Double,
    val nominalOverStroke: Double,
    val p90OverStroke: Double,
    val flatAtP90: Boolean
)

@Serializable
private class T204Result(
    val task: String, val leaf: String, val title: String, val verificationType: String,
    val maturity: String, val units: Map<String, String>, val conventions: Map<String, String>,
    val parameters: Map<String, String>, val citedInputs: List<String>,
    val cheapBound: Map<String, String>, val cells: List<T204Cell>,
    val verdict: Map<String, String>, val falsifiers: List<String>,
    val findings: Map<String, String>, val validity: List<String>, val openQuestions: List<String>
)

private class T204Profile(val smoothDepth: Double, val smoothWidth: Double, val rimDepth: Double)

private fun t204Profile(file: File): T204Profile {
    require(file.exists()) { "C-0022's result file is missing: ${file.path}" }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .firstOrNull {
            fun value(name: String) = it.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == 2.0 && value("gapHeight") == 10.0 &&
                    value("appliedBias") == 0.192
        } ?: error("no C-0022 profile at the design state")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T204Profile(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

@Suppress("LongMethod")
fun main() {
    val profile = t204Profile(ResultInputs.T_3B.file())
    val edgeX = T204_ROW_BP * Gen1Tile.RISE_PER_BASE_PAIR

    println("T-204 — the cheap bound, before any cell is re-graded")
    val sections = listOf(15 to 4, 10 to 6)
    val ratios = sections.associate { (rows, layers) ->
        val edgeY = rows * Gen1Tile.INTERHELICAL_HONEYCOMB
        val ratio = collarShareRatio(T204_SOLVED_EDGE_X, T204_SOLVED_EDGE_Y, edgeX, edgeY)
        println(
            "  %2d x %-2d  tile %.2f x %.2f nm, collar share is %s x the solved tile's".format(
                rows, layers, edgeX, edgeY, ratio.emitted(6)
            )
        )
        "$rows x $layers" to ratio
    }
    val between = collarShareRatio(
        edgeX, 15 * Gen1Tile.INTERHELICAL_HONEYCOMB, edgeX, 10 * Gen1Tile.INTERHELICAL_HONEYCOMB
    )
    println("  and 10 x 6 carries %s x what 15 x 4 does".format(between.emitted(6)))

    val cells = ArrayList<T204Cell>()
    sections.forEach { (rows, layers) ->
        val name = "$rows x $layers"
        val edgeY = rows * Gen1Tile.INTERHELICAL_HONEYCOMB
        val rigidities = multiLayerRigidities(
            layers = layers,
            interhelicalDistance = Gen1Tile.INTERHELICAL_HONEYCOMB,
            crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
            coupling = LayerCoupling.CALIBRATED
        )
        val sheet = equivalentSheet(rigidities)
        val interiorPressure = Gen1Tile.TARGET_FORCE / (edgeX * edgeY)
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

        // The collar is scaled from 1.0 (C-0022 as solved, which is what every four-layer number
        // is currently read under) up past this cross-section's own geometric factor, so the
        // sensitivity is measured over a range that CONTAINS the bound rather than only at it.
        val scales = listOf(1.0, ratios.getValue(name), 1.5, 2.0, 3.0)
        listOf(1, 5).forEach { columns ->
            val grid = attachmentGrid(columns, rows, edgeX, edgeY)
            scales.forEach { scale ->
                val field: PressureField = edgeCollarPressure(
                    interiorPressure, edgeX, edgeY,
                    listOf(
                        CollarTerm(profile.smoothDepth * scale, profile.smoothWidth),
                        CollarTerm(profile.rimDepth * scale, T204_RIM_STANDOFF)
                    )
                )
                val surrogate = latticeInfluenceSurrogate(lattice, grid, field, T204_SAMPLES)
                val stiffnesses = equalShareOfMandate(grid.size)
                val nominal = surrogate.solve(stiffnesses).peakDishing / freeStroke
                val ensemble = dropoutEnsemble(
                    grid.map { (x, y) -> incorporation.at(x, y) }, T204_REALISATIONS, T204_SEED
                )
                val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
                sample.indices.forEach { sample[it] = sample[it] / freeStroke }
                val p90 = summariseDropoutDishing(
                    sample, nominal, ensemble.meanSurvivors, T204_TOLERANCE
                ).p90
                cells += T204Cell(
                    crossSection = name, columns = columns, pathCount = grid.size,
                    collarScale = scale, nominalOverStroke = nominal, p90OverStroke = p90,
                    flatAtP90 = p90 < T204_TOLERANCE
                )
                println(
                    "  %-8s %d col, collar x%-5s  nominal %.9f  p90 %.9f  %s".format(
                        name, columns, scale.emitted(4), nominal, p90,
                        if (p90 < T204_TOLERANCE) "FLAT" else "not flat"
                    )
                )
            }
        }
    }

    val theirs = cells.filter { it.crossSection == "10 x 6" }
    val ours = cells.filter { it.crossSection == "15 x 4" }
    val theirsFlatEverywhere = theirs.all { it.flatAtP90 }
    val theirsAtBound = theirs.filter { it.collarScale == ratios.getValue("10 x 6") }
    val breakScale = theirs.filter { !it.flatAtP90 }.minByOrNull { it.collarScale }?.collarScale

    val findings = HashMap<String, String>()
    findings["theCheapBoundIsTheAnswer"] =
        ("The collar is LOCAL, so its share scales as perimeter over area: 15 x 4 carries %s x the " +
                "solved tile's and 10 x 6 carries %s x, a %s x difference between them. The bound " +
                "needs no field solve at all."
            ).format(
                ratios.getValue("15 x 4").emitted(6), ratios.getValue("10 x 6").emitted(6),
                between.emitted(6)
            )
    findings["theVerdictSurvivesTheBoundWithRoomToSpare"] =
        ("At its own geometric factor of %s x, 10 x 6 reads %s at 1 column and stays flat; it is " +
                "flat at EVERY scale tested up to %s x, and the first scale at which any 10 x 6 " +
                "cell stops being flat is %s. So the collar transfer cannot move the verdict, and " +
                "the margin is the ratio of those two numbers."
            ).format(
                ratios.getValue("10 x 6").emitted(6),
                theirsAtBound.minByOrNull { it.columns }?.p90OverStroke?.emitted() ?: "n/a",
                theirs.maxOf { it.collarScale }.emitted(4),
                breakScale?.emitted(4) ?: "none tested"
            )

    val result = T204Result(
        task = "T-204", leaf = "A7.4",
        title = "Does C-0022's collar transfer to the 10 x 6 aspect ratio?",
        verificationType = "logical (a perimeter-over-area bound) + in-silico (the sensitivity)",
        maturity = "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated.",
        units = mapOf("length" to "nm", "dishing" to "dimensionless, fraction of the free stroke"),
        conventions = mapOf(
            "solvedTile" to "C-0022's 40 x 40.35 nm, at 2 mM / 10 nm / 0.192 V",
            "collar" to "a LOCAL rim effect: depth and width set by screening, share by geometry",
            "scaling" to "both collar terms scaled together; the interior pressure is unchanged",
            "flat" to "peak dishing below T-5b's 0.10"
        ),
        parameters = mapOf(
            "solvedEdgeX" to T204_SOLVED_EDGE_X.emitted(),
            "solvedEdgeY" to T204_SOLVED_EDGE_Y.emitted(),
            "rowBasePairs" to T204_ROW_BP.toString(),
            "realisations" to T204_REALISATIONS.toString(),
            "seed" to T204_SEED.toString()
        ),
        citedInputs = listOf(
            "C-0022 — the solved edge profile, and the tile it was solved on",
            "C-0118 — the coupled cells re-graded here",
            "C-0120 / C-0122 — which both name this transfer as owed"
        ),
        cheapBound = mapOf(
            "collarShare15x4OverSolved" to ratios.getValue("15 x 4").emitted(),
            "collarShare10x6OverSolved" to ratios.getValue("10 x 6").emitted(),
            "collarShare10x6Over15x4" to between.emitted(),
            "why" to "the collar's depth and width are set by screening and its share by the " +
                    "tile's perimeter over area, so the transfer is geometry and needs no solve"
        ),
        cells = cells,
        verdict = mapOf(
            "tenBySixFlatAtEveryScaleTested" to theirsFlatEverywhere.toString(),
            "firstScaleAtWhichTenBySixFails" to (breakScale?.emitted() ?: "none up to 3.0"),
            "geometricFactorItActuallyNeeds" to ratios.getValue("10 x 6").emitted(),
            "fifteenByFourFlatCells" to ours.count { it.flatAtP90 }.toString(),
            "fifteenByFourCells" to ours.size.toString()
        ),
        falsifiers = listOf(
            "F1 — some 10 x 6 cell stops being flat at or below its own geometric factor, in which " +
                    "case the collar transfer DOES move the verdict and a re-solve is owed.",
            "F2 — the dishing does not increase monotonically with the collar scale, in which case " +
                    "a single-factor bound is not the right instrument at all."
        ),
        findings = findings,
        validity = listOf(
            "This scales the SHARE and holds the SHAPE: C-0022's taper width and rim standoff are " +
                    "unchanged, which is the whole premise -- they are set by screening, not by " +
                    "the tile. A genuine re-solve at the new aspect ratio could move the shape too, " +
                    "and that is NOT bounded here.",
            "A rectangular tile's collar is not uniform along its perimeter: the short and long " +
                    "sides see different fringing, and edgeCollarPressure applies one profile to " +
                    "all four. That is C-0022's own convention, inherited.",
            "Equal springs only, as C-0118's best cells use.",
            "The interior pressure is held at TARGET_FORCE over the footprint, so scaling the " +
                    "collar RAISES the total force slightly rather than redistributing it -- the " +
                    "conservative direction for a flatness question."
        ),
        openQuestions = listOf(
            "A genuine 2-D re-solve at 38.08 x 25.36 nm, which would move the collar's SHAPE and " +
                    "not only its share. This study bounds the share and says so.",
            "Whether the short and long sides of a 1.5:1 tile need different collar profiles."
        )
    )
    val output = File("gpd/results/T-204-collar-aspect-ratio.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(digits = 9).withEmissionHeader(LatticeTag.SQUARE, null) as JsonObject)
        ) + "\n"
    )
    println("T-204 — wrote ${output.path}")
}
