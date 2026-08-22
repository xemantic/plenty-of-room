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

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Task `T-5b` / leaf `A8.2` — the deflected shape of the tile under the actuation load,
 * against the stroke, and the verdict on the rigid-plate assumption `C-0001` makes.
 *
 * ```shell
 * ./gradlew study -Pstudy=structure.TileFlatnessStudyKt
 * ```
 *
 * Emits `gpd/results/T-5b-tile-flatness.json`, deterministically.
 */

@Serializable
data class DishingSource(
    val source: String,
    val mechanism: String,
    val peakDishing: Double,
    val rmsDishing: Double,
    val stroke: Double,
    val dishingOverStroke: Double,
    val rigidPlateUpheld: Boolean
)

@Serializable
data class RippleTransmission(
    val wavelength: Double,
    val alongHelix: Double,
    val acrossHelix: Double
)

@Serializable
data class ThermalCase(
    val label: Double,
    val foundationStiffness: Double,
    val pistonRms: Double,
    val tiltRms: Double,
    val dishingRms: Double,
    val centreRms: Double,
    val dishingOverPiston: Double,
    val dishingOverStroke: Double,
    val rigidPlateUpheld: Boolean,
    val dishingRmsByBasisDegree: Map<Int, Double>
)

@Serializable
data class SensingConsequence(
    val dishingRms: Double,
    val debyeLength: Double,
    val chargeSensorApparentOffset: Double,
    val chargeSensorOffsetOverStroke: Double,
    val leverPointRms: Double,
    val leverMinusMeanRms: Double,
    val osmoticMeanForceCorrection: Double,
    val osmoticExponent: Double
)

@Serializable
data class FlatnessCase(
    val label: String,
    val multiplier: Double,
    val foundationStiffness: Double,
    val stroke: Double,
    val winklerLengthAlongHelix: Double,
    val winklerLengthAcrossHelix: Double,
    val ratioAlongHelixToHalfWidth: Double,
    val ratioAcrossHelixToHalfWidth: Double,
    val winklerLengthOverCrossoverSpacing: Double,
    val continuumPlateReductionValid: Boolean,
    val sources: List<DishingSource>,
    val rippleTransmission: List<RippleTransmission>,
    val thermal: ThermalCase,
    val sensing: SensingConsequence
)

@Serializable
data class FlatnessParameters(
    val temperature: Double,
    val medium: String,
    val thermalEnergy: Double,
    val tileFootprint: String,
    val tileArea: Double,
    val targetForce: Double,
    val targetPressure: Double,
    val acceptableStroke: Double,
    val desiredStroke: Double,
    val debyeLength: Double,
    val layerHeight: Double,
    val basisDegree: Int,
    val edgeTaperWidth: Double,
    val edgeTaperDepth: Double,
    val anchorCount: Int,
    val anchorStiffnessFraction: Double,
    val leverAttachmentCount: Int,
    val osmoticExponent: Double,
    val foundationReference: String,
    val foundationSweep: List<Double>,
    val rigidPlateCriterion: String
)

@Serializable
data class FlatnessResult(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val validity: List<String>,
    val parameters: FlatnessParameters,
    val sheets: List<SheetVariant>,
    val cases: List<FlatnessCase>,
    val verdict: Map<String, String>
)

private const val BASIS_DEGREE = 12

/** The edge-taper depth: the electrostatic load is taken to fall by half at the rim. */
private const val EDGE_TAPER_DEPTH = 0.5

/** A second, shallower taper, carried only to demonstrate that the response is linear in it. */
private const val SHALLOW_TAPER_DEPTH = 0.1

/** Basis degrees the thermal dishing amplitude is reported at, as an in-file gate-4 record. */
private val CONVERGENCE_DEGREES = listOf(8, 12, 16)

/** The anchor layout the primary dishing source is evaluated at: four corners, stiff. */
private const val ANCHOR_SIDE = 2
private const val ANCHOR_STIFFNESS_FRACTION = 1.0

/** The lever attachment count the concentrated case is evaluated at: one tether. */
private const val LEVER_ATTACHMENTS = 1

/**
 * The local osmotic exponent of the layer at the working point — **CITED**, `C-0002`:
 * `m_eff = 1.672` at the `T-1` window lower edge, not the 9/4 `C-0001` used.
 */
private const val OSMOTIC_EXPONENT = 1.672

/** A dishing amplitude below this fraction of the stroke leaves the rigid-plate picture standing. */
private const val RIGID_PLATE_TOLERANCE = 0.10

fun main() {
    val variants = gen1SheetVariants()
    val (_, sheet) = variants.first()
    val plate = sheet.plate(Gen1Tile.EDGE_X, Gen1Tile.EDGE_Y)
    val pressure = Gen1Tile.TARGET_FORCE / plate.area

    val cases = Gen1Tile.FOUNDATION_SWEEP.map { multiplier ->
        flatnessCase(multiplier, sheet, plate, pressure)
    }
    val nominal = cases.first { it.multiplier == 1.0 }

    val result = FlatnessResult(
        task = "T-5b",
        leaf = "A8.2",
        title = "Deflected shape of the Gen-1 tile under the actuation load",
        verificationType = "in-silico",
        acceptance = "Deformation amplitude reported against the stroke; rigid-plate assumption " +
                "upheld or rejected, with consequences for force transfer to the lever and for " +
                "what an adjacent charge sensor would see",
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "pressure" to "pN/nm^2 (= MPa)",
            "foundationStiffness" to "pN/nm^3",
            "flexuralRigidity" to "pN*nm",
            "temperature" to "K"
        ),
        conventions = listOf(
            "x along the helices, y across them, origin at the centre of the footprint",
            "w positive DOWNWARD, compressing the polymer layer",
            "w = w_rigid + w_dish, with w_rigid the area-averaged least-squares best-fit plane, " +
                    "which is exactly the first three Legendre coefficients",
            "'dishing amplitude' is reported as both an area RMS and a peak over the footprint",
            "the stroke it is compared against is the area-averaged deflection"
        ),
        validity = listOf(
            "Kirchhoff plate on a linear Winkler foundation; the real layer is strongly nonlinear",
            "the continuum plate reduction across the helices requires the Winkler length to " +
                    "exceed the crossover spacing; where continuumPlateReductionValid is false " +
                    "the tile is better described as quasi-independent duplex beams and the " +
                    "reported dishing is an underestimate of the true shape freedom",
            "the electrostatic load is NOT solved: it enters as a 100 pN total and a bounded " +
                    "edge taper (T-3 owns the load model)",
            "static only; whether the tile can dish fast enough to matter at 1 kHz is T-7",
            "k_f from C-0001 are LOWER BOUNDS per CH-0001 and are being re-derived under T-1c",
            "in-plane membrane stiffening of the sheet is neglected, which is conservative"
        ),
        parameters = FlatnessParameters(
            temperature = ROOM_TEMPERATURE,
            medium = "aqueous buffer, 2/5/10 mM MgCl2",
            thermalEnergy = thermalEnergy(),
            tileFootprint = "${Gen1Tile.EDGE_X.toInt()} x ${Gen1Tile.EDGE_Y.toInt()} nm",
            tileArea = plate.area,
            targetForce = Gen1Tile.TARGET_FORCE,
            targetPressure = pressure,
            acceptableStroke = Gen1Tile.ACCEPTABLE_STROKE,
            desiredStroke = Gen1Tile.DESIRED_STROKE,
            debyeLength = Gen1Tile.DEBYE_LENGTH,
            layerHeight = Gen1Tile.LAYER_HEIGHT,
            basisDegree = BASIS_DEGREE,
            edgeTaperWidth = Gen1Tile.DEBYE_LENGTH,
            edgeTaperDepth = EDGE_TAPER_DEPTH,
            anchorCount = ANCHOR_SIDE * ANCHOR_SIDE,
            anchorStiffnessFraction = ANCHOR_STIFFNESS_FRACTION,
            leverAttachmentCount = LEVER_ATTACHMENTS,
            osmoticExponent = OSMOTIC_EXPONENT,
            foundationReference = "C-0001, 10 nm layer at sigma = 0.024 nm^-2. Loaded cases use " +
                    "the secant 20.201 pN/nm; the thermal case uses the tangent at first contact, " +
                    "7.402 pN/nm, because that is the stiffness an unbiased tile fluctuates against.",
            foundationSweep = Gen1Tile.FOUNDATION_SWEEP,
            rigidPlateCriterion = "dishing amplitude below $RIGID_PLATE_TOLERANCE of the stroke"
        ),
        sheets = variants.map { it.first },
        cases = cases,
        verdict = mapOf(
            "uniform-load" to "UPHELD exactly — a uniform load on a uniform foundation makes a " +
                    "free plate translate, whatever its rigidity. Dishing is zero to machine " +
                    "precision at every k_f in the sweep.",
            "edge-tapered-load" to verdictOf(nominal, "electrostatic-edge-taper"),
            "discrete-anchors" to verdictOf(nominal, "discrete-anchors"),
            "concentrated-lever" to verdictOf(nominal, "concentrated-lever-attachment"),
            "thermal" to if (nominal.thermal.rigidPlateUpheld) "UPHELD" else
                ("REJECTED — the tile's own bending modes carry %.2f nm RMS at 300 K, " +
                        "%.0f%% of the stroke, and %.2f times the rigid piston mode. " +
                        "A rigid tile cannot represent this.").format(
                    nominal.thermal.dishingRms,
                    100.0 * nominal.thermal.dishingOverStroke,
                    nominal.thermal.dishingOverPiston
                ),
            "continuum-plate-reduction" to
                    "ell_perp/p = ${"%.2f".format(nominal.winklerLengthOverCrossoverSpacing)} " +
                    "at the nominal foundation, and below 1 across the whole sweep: the " +
                    "across-helix bending length is SHORTER than the crossover spacing, so the " +
                    "continuum plate is itself marginal and the tile is closer to 15 " +
                    "quasi-independent duplex beams than to a plate."
        )
    )

    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-5b-tile-flatness.json")
    output.parentFile.mkdirs()
    output.writeText(json.encodeToString(json.encodeToJsonElement(result).roundedForResult().withEmissionHeader(LatticeTag.SQUARE, null)) + "\n")
    report(result, output)
}

private fun verdictOf(case: FlatnessCase, source: String): String {
    val record = case.sources.first { it.source == source }
    return if (record.rigidPlateUpheld) {
        "UPHELD — peak dishing %.3f nm, %.1f%% of the %.2f nm stroke".format(
            record.peakDishing, 100.0 * record.dishingOverStroke, record.stroke
        )
    } else {
        "REJECTED — peak dishing %.3f nm, %.0f%% of the %.2f nm stroke".format(
            record.peakDishing, 100.0 * record.dishingOverStroke, record.stroke
        )
    }
}

private fun flatnessCase(
    multiplier: Double,
    sheet: OrigamiSheet,
    plate: OrthotropicPlate,
    pressure: Double
): FlatnessCase {
    val foundation = Gen1Tile.FOUNDATION_SECANT * multiplier
    val stroke = pressure / foundation
    val ellAlong = winklerLength(plate.rigidityX, foundation)
    val ellAcross = winklerLength(plate.rigidityY, foundation)

    val free = PlateOnFoundation(plate, foundation, basisDegree = BASIS_DEGREE)
    val uniform = free.solve(uniformPressure(pressure))
    val tapered = free.solve(
        edgeTaperedPressure(pressure, plate, Gen1Tile.DEBYE_LENGTH, EDGE_TAPER_DEPTH)
    )
    val shallowTaper = free.solve(
        edgeTaperedPressure(pressure, plate, Gen1Tile.DEBYE_LENGTH, SHALLOW_TAPER_DEPTH)
    )
    val anchors = insetGrid(ANCHOR_SIDE, plate.lengthX, plate.lengthY).map { (x, y) ->
        PointSupport(
            x, y,
            ANCHOR_STIFFNESS_FRACTION * foundation * plate.area / (ANCHOR_SIDE * ANCHOR_SIDE)
        )
    }
    val anchored = PlateOnFoundation(plate, foundation, anchors, BASIS_DEGREE)
        .solve(uniformPressure(pressure))
    val lever = free.solve(
        pointLoads = insetGrid(LEVER_ATTACHMENTS, plate.lengthX, plate.lengthY).map { (x, y) ->
            PointLoad(x, y, Gen1Tile.TARGET_FORCE / LEVER_ATTACHMENTS)
        }
    )

    val sources = listOf(
        dishingSource(
            "uniform-load",
            "the leading-order load case: uniform pressure, uniform foundation, free edges",
            uniform, stroke
        ),
        dishingSource(
            "electrostatic-edge-taper",
            "pressure falling by ${(100 * EDGE_TAPER_DEPTH).toInt()}% over one Debye length at " +
                    "the rim — the finite-tile field effect, as a bounded perturbation",
            tapered, stroke
        ),
        dishingSource(
            "electrostatic-edge-taper-shallow",
            "the same taper at ${(100 * SHALLOW_TAPER_DEPTH).toInt()}% depth. The problem is " +
                    "linear, so this exists to DEMONSTRATE that the dishing is proportional to " +
                    "the taper depth rather than to assert it: the ratio to the deep case must " +
                    "be exactly ${EDGE_TAPER_DEPTH / SHALLOW_TAPER_DEPTH}.",
            shallowTaper, stroke
        ),
        dishingSource(
            "discrete-anchors",
            "${ANCHOR_SIDE * ANCHOR_SIDE} tethers of total stiffness equal to the layer's own, " +
                    "reacting a uniform load at points — the §4(g) geometry",
            anchored, stroke
        ),
        dishingSource(
            "concentrated-lever-attachment",
            "the whole 100 pN leaving the tile through $LEVER_ATTACHMENTS attachment(s) while " +
                    "entering distributed",
            lever, stroke
        )
    )

    val thermalFoundation = Gen1Tile.FOUNDATION_AT_REST * multiplier
    val fluctuation = PlateOnFoundation(plate, thermalFoundation, basisDegree = BASIS_DEGREE)
        .thermalFluctuation()
    val thermal = ThermalCase(
        label = multiplier,
        foundationStiffness = thermalFoundation,
        pistonRms = fluctuation.pistonRms,
        tiltRms = fluctuation.tiltRms,
        dishingRms = fluctuation.dishingRms,
        centreRms = fluctuation.centreRms,
        dishingOverPiston = fluctuation.dishingRms / fluctuation.pistonRms,
        dishingOverStroke = fluctuation.dishingRms / stroke,
        rigidPlateUpheld = fluctuation.dishingRms / stroke < RIGID_PLATE_TOLERANCE,
        dishingRmsByBasisDegree = CONVERGENCE_DEGREES.associateWith { degree ->
            PlateOnFoundation(plate, thermalFoundation, basisDegree = degree)
                .thermalFluctuation().dishingRms
        }
    )

    val delta = fluctuation.dishingRms
    val sensorOffset = delta * delta / (2.0 * Gen1Tile.DEBYE_LENGTH)
    val leverPoint = sqrt(
        fluctuation.pistonRms * fluctuation.pistonRms + delta * delta
    )
    val sensing = SensingConsequence(
        dishingRms = delta,
        debyeLength = Gen1Tile.DEBYE_LENGTH,
        chargeSensorApparentOffset = sensorOffset,
        chargeSensorOffsetOverStroke = sensorOffset / stroke,
        leverPointRms = leverPoint,
        leverMinusMeanRms = delta,
        osmoticMeanForceCorrection =
            OSMOTIC_EXPONENT * (OSMOTIC_EXPONENT + 1.0) / 2.0 *
                    (delta / Gen1Tile.LAYER_HEIGHT) * (delta / Gen1Tile.LAYER_HEIGHT),
        osmoticExponent = OSMOTIC_EXPONENT
    )

    return FlatnessCase(
        label = if (multiplier == 1.0) "C-0001 secant, 10 nm layer at sigma = 0.024 nm^-2"
        else "C-0001 secant x $multiplier",
        multiplier = multiplier,
        foundationStiffness = foundation,
        stroke = stroke,
        winklerLengthAlongHelix = ellAlong,
        winklerLengthAcrossHelix = ellAcross,
        ratioAlongHelixToHalfWidth = ellAlong / (plate.lengthX / 2.0),
        ratioAcrossHelixToHalfWidth = ellAcross / (plate.lengthY / 2.0),
        winklerLengthOverCrossoverSpacing = ellAcross / sheet.crossoverSpacing,
        continuumPlateReductionValid = ellAcross > sheet.crossoverSpacing,
        sources = sources,
        rippleTransmission = listOf(80.0, 40.0, 20.0, 8.0, 4.0).map { wavelength ->
            RippleTransmission(
                wavelength = wavelength,
                alongHelix = loadRippleTransmission(ellAlong, wavelength),
                acrossHelix = loadRippleTransmission(ellAcross, wavelength)
            )
        },
        thermal = thermal,
        sensing = sensing
    )
}

private fun dishingSource(
    source: String,
    mechanism: String,
    deflection: PlateDeflection,
    stroke: Double
): DishingSource {
    val peak = deflection.peakDishing(81)
    return DishingSource(
        source = source,
        mechanism = mechanism,
        peakDishing = peak,
        rmsDishing = deflection.dishingRms,
        stroke = stroke,
        dishingOverStroke = peak / stroke,
        rigidPlateUpheld = peak / stroke < RIGID_PLATE_TOLERANCE
    )
}

private fun report(result: FlatnessResult, output: File) {
    println("T-5b / A8.2 — ${result.title}")
    println("300 K, aqueous buffer, k_BT = ${"%.3f".format(thermalEnergy())} pN*nm")
    println("tile ${result.parameters.tileFootprint}, target force ${result.parameters.targetForce} pN")
    println()
    println("--- dishing by source, against the stroke ".padEnd(112, '-'))
    println("%7s %8s %32s %11s %11s %11s %8s".format(
        "k_f x", "stroke", "source", "peak[nm]", "rms[nm]", "peak/stroke", "rigid?"
    ))
    result.cases.forEach { case ->
        case.sources.forEach {
            println("%7.2f %8.2f %32s %11.4f %11.4f %11.4f %8s".format(
                case.multiplier, case.stroke, it.source, it.peakDishing, it.rmsDishing,
                it.dishingOverStroke, if (it.rigidPlateUpheld) "yes" else "NO"
            ))
        }
    }
    println()
    println("--- thermal fluctuation at 300 K, unloaded ".padEnd(112, '-'))
    println("%7s %10s %10s %10s %10s %10s %12s %8s".format(
        "k_f x", "k_f", "piston", "tilt", "dishing", "centre", "dish/stroke", "rigid?"
    ))
    result.cases.forEach {
        val t = it.thermal
        println("%7.2f %10.5f %10.3f %10.3f %10.3f %10.3f %12.3f %8s".format(
            it.multiplier, t.foundationStiffness, t.pistonRms, t.tiltRms, t.dishingRms,
            t.centreRms, t.dishingOverStroke, if (t.rigidPlateUpheld) "yes" else "NO"
        ))
    }
    println()
    println("--- load-ripple transmission, nominal k_f ".padEnd(112, '-'))
    val nominal = result.cases.first { it.multiplier == 1.0 }
    println("ell_par = %.2f nm, ell_perp = %.2f nm, ell_perp/p = %.2f (continuum valid: %s)".format(
        nominal.winklerLengthAlongHelix, nominal.winklerLengthAcrossHelix,
        nominal.winklerLengthOverCrossoverSpacing, nominal.continuumPlateReductionValid
    ))
    println("%12s %14s %14s".format("lambda[nm]", "along helix", "across helix"))
    nominal.rippleTransmission.forEach {
        println("%12.1f %14.5f %14.5f".format(it.wavelength, it.alongHelix, it.acrossHelix))
    }
    println()
    println("--- §4(g) consequences, nominal k_f ".padEnd(112, '-'))
    val s = nominal.sensing
    println("thermal dishing RMS                        %.3f nm".format(s.dishingRms))
    println("charge sensor apparent offset              %.4f nm (%.2f%% of stroke)".format(
        s.chargeSensorApparentOffset, 100.0 * s.chargeSensorOffsetOverStroke
    ))
    println("lever attachment point RMS                 %.3f nm".format(s.leverPointRms))
    println("lever minus tile-mean RMS                  %.3f nm".format(s.leverMinusMeanRms))
    println("osmotic mean-force correction              %+.2f%%".format(
        100.0 * s.osmoticMeanForceCorrection
    ))
    println()
    result.verdict.forEach { (key, value) -> println("$key: $value") }
    println()
    println("written: ${output.path}")
    check(abs(nominal.sources.first().peakDishing) < 1e-9) {
        "the uniform-load case must dish by nothing at all; the solver is wrong if it does not"
    }
}
