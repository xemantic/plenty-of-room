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

package com.xemantic.nano.plentyofroom.brush

import com.xemantic.nano.plentyofroom.ELECTRON_VOLT
import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.io.File
import kotlin.math.ln
import kotlin.math.exp
import kotlinx.serialization.json.encodeToJsonElement

/**
 * Task `T-1` / leaf `A2.1` — the stiffness of the polymer layer under the tile,
 * derived from the §3 parameters, swept across the grafting density at each of the
 * three specified layer heights.
 *
 * Run with:
 *
 * ```shell
 * ./gradlew study -Pstudy=brush.BrushStiffnessStudyKt
 * ```
 *
 * Emits `gpd/results/T-1-layer-stiffness.json`, deterministically — the file carries no
 * timestamp, so a re-run that changes nothing produces no diff, and a re-run that changes
 * something produces a reviewable one.
 */

/** Every parameter the run consumed, logged so the result is reproducible from the file alone. */
@Serializable
data class StudyParameters(
    val temperature: Double,
    val medium: String,
    val thermalEnergy: Double,
    val thermalEnergyElectronVolts: Double,
    val tileFootprint: String,
    val tileArea: Double,
    val targetForce: Double,
    val targetStrokeAcceptable: Double,
    val targetStrokeDesired: Double,
    val monomerSize: Double,
    val monomerSizeProvenance: String,
    val layerHeights: List<Double>,
    val graftingDensityRange: List<Double>,
    val graftingDensitySamples: Int
)

@Serializable
data class StudyResult(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val validity: List<String>,
    val parameters: StudyParameters,
    val designPoints: List<LayerDesignPoint>
)

/** §3: 40 × 40 nm tile footprint. */
private const val TILE_EDGE = 40.0

/** PEG monomer size in nm — a `cited` number, pending task `P-3`. */
private const val PEG_MONOMER_SIZE = 0.35

private const val TARGET_FORCE = 100.0

private val LAYER_HEIGHTS = listOf(5.0, 7.0, 10.0)

private const val GRAFTING_DENSITY_MIN = 0.002
private const val GRAFTING_DENSITY_MAX = 1.0
private const val GRAFTING_DENSITY_SAMPLES = 61

fun main() {
    val tileArea = TILE_EDGE * TILE_EDGE
    val densities = logarithmicSweep(
        GRAFTING_DENSITY_MIN, GRAFTING_DENSITY_MAX, GRAFTING_DENSITY_SAMPLES
    )
    val designPoints = LAYER_HEIGHTS.flatMap { height ->
        densities.map { density ->
            layerDesignPoint(
                layerHeight = height,
                graftingDensity = density,
                monomerSize = PEG_MONOMER_SIZE,
                tileArea = tileArea,
                targetForce = TARGET_FORCE
            )
        }
    }
    val result = StudyResult(
        task = "T-1",
        leaf = "A2.1",
        title = "Stiffness of the grafted polymer layer under the Gen-1 tile",
        verificationType = "in-silico",
        acceptance = "Number with stated model, parameters, and validity range; " +
                "sensitivity to grafting density reported",
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "energy" to "pN*nm",
            "pressure" to "pN/nm^2 (= MPa)",
            "stiffness" to "pN/nm (= mN/m)",
            "graftingDensity" to "chains/nm^2",
            "temperature" to "K"
        ),
        conventions = listOf(
            "z normal to the electrode, positive away from it, origin at the electrode surface",
            "the tile is a rigid non-adsorbing wall at height h; compression means h < L0",
            "disjoining pressure positive when the layer pushes the tile away",
            "stiffness k = -dF/dh, positive for a restoring layer",
            "the layer height is the independent variable; chain length N follows from it"
        ),
        validity = listOf(
            "0 < h <= L0 — above L0 a non-adsorbing brush loses contact and the pressure is zero",
            "the scaling form assumes the brush regime; points reported as MUSHROOM or " +
                    "CROSSOVER are outside its premise and are emitted for the boundary, not as answers",
            "the osmotic exponent is unresolved for this layer (see task P-4); all three " +
                    "candidates are reported and the spread between them is the honest uncertainty",
            "the SCF excluded volume is height-matched to the scaling form, NOT measured for " +
                    "PEG in Mg2+ buffer (see task P-3)",
            "purely mechanical: no electrostatics, no ion partitioning, no poroelasticity"
        ),
        parameters = StudyParameters(
            temperature = ROOM_TEMPERATURE,
            medium = "aqueous buffer, 2/5/10 mM MgCl2 (not yet entering this task)",
            thermalEnergy = thermalEnergy(),
            thermalEnergyElectronVolts = thermalEnergy() / ELECTRON_VOLT,
            tileFootprint = "${TILE_EDGE.toInt()} x ${TILE_EDGE.toInt()} nm",
            tileArea = tileArea,
            targetForce = TARGET_FORCE,
            targetStrokeAcceptable = 3.0,
            targetStrokeDesired = 10.0,
            monomerSize = PEG_MONOMER_SIZE,
            monomerSizeProvenance = "cited, not derived — PEG ethylene-oxide monomer, " +
                    "pending task P-3",
            layerHeights = LAYER_HEIGHTS,
            graftingDensityRange = listOf(GRAFTING_DENSITY_MIN, GRAFTING_DENSITY_MAX),
            graftingDensitySamples = GRAFTING_DENSITY_SAMPLES
        ),
        designPoints = designPoints
    )
    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-1-layer-stiffness.json")
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(
            json.encodeToJsonElement(result)
                // NINE DIGITS (`T-278`, closing `CH-0223`). `P-18`'s provenance rule is "the
                // loosest solver tolerance on any path from a model input to it". Every one of
                // this study's three compression models is analytic — `DeGennesScaling`'s
                // `alexanderDeGennesHeight` and `MilnerWittenCates`' closed-form equilibrium —
                // and the only iteration on the path is `heightUnderLoad`, a hundred bisection
                // halvings of a bracket that is `[L0e-12, L0]` wide, which is machine precision
                // and then some. `CH-0223` names this study as "downstream of a solved SCF
                // height"; neither it nor anything it calls names `SelfConsistentField` or
                // `heightAtPressure` at all, so `SOLVED_HEIGHT_SIGNIFICANT_DIGITS` is a rule
                // about a solver this study does not use.
                .roundedForResult()
                .withEmissionHeader(LatticeTag.NONE, null)
        ) + "\n"
    )
    report(result, output)
}

private fun report(result: StudyResult, output: File) {
    println("T-1 / A2.1 — ${result.title}")
    println("300 K, aqueous buffer, k_BT = ${"%.3f".format(thermalEnergy())} pN*nm")
    println("tile ${result.parameters.tileFootprint}, target force ${result.parameters.targetForce} pN")
    println()
    LAYER_HEIGHTS.forEach { height ->
        println("--- layer height L0 = $height nm ".padEnd(96, '-'))
        println(
            "%9s %8s %9s %8s %11s %11s %9s %9s".format(
                "sigma", "s[nm]", "N", "regime", "k_eq[pN/nm]", "stroke[nm]", "k_sec", "rms[nm]"
            )
        )
        result.designPoints
            .filter { it.layerHeight == height }
            .filter { it.reducedGraftingDensity > 0.5 }
            .forEach { point ->
                val response = point.responses.first()
                println(
                    "%9.4f %8.2f %9.1f %8s %11.2f %11.2f %9.2f %9.2f".format(
                        point.graftingDensity,
                        point.graftingSpacing,
                        point.monomersPerChain,
                        point.regime.take(8),
                        response.equilibriumStiffness,
                        response.strokeUnderTargetForce,
                        response.secantStiffness,
                        response.positionalRms
                    )
                )
            }
        println()
    }
    println("columns are the good-solvent semidilute scaling form (m = 9/4);")
    println("the other three models are in the JSON, and the spread between them is the uncertainty.")
    println()
    println("written: ${output.path} (${result.designPoints.size} design points)")
}

/** Returns [samples] points from [from] to [to] inclusive, evenly spaced in the logarithm. */
internal fun logarithmicSweep(from: Double, to: Double, samples: Int): List<Double> {
    require(from > 0.0) { "from must be positive, was: $from" }
    require(to > from) { "to must exceed from, was: $to" }
    require(samples >= 2) { "samples must be at least 2, was: $samples" }
    val step = (ln(to) - ln(from)) / (samples - 1)
    return List(samples) { i -> exp(ln(from) + i * step) }
}
