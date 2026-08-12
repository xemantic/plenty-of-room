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
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.ceil
import kotlin.math.sqrt

/**
 * Task `T-5` / leaf `A1.2` — load distribution across the DNA-origami tile,
 * distributed and concentrated attachment treated separately, reported against the
 * §4(f) structural-survival bands.
 *
 * ```shell
 * ./gradlew study -Pstudy=structure.TileLoadDistributionStudyKt
 * ```
 *
 * Emits `gpd/results/T-5-load-distribution.json`, deterministically — no timestamp, so a
 * re-run that changes nothing produces no diff.
 */

@Serializable
data class LoadPathRecord(
    val name: String,
    val description: String,
    val paths: Double,
    val totalForce: Double,
    val forcePerPath: Double,
    val band: String
)

@Serializable
data class AnchoredCase(
    val anchorCount: Int,
    val anchorStiffnessFraction: Double,
    val anchorStiffnessEach: Double,
    val peakAnchorForce: Double,
    val peakAnchorBand: String,
    val isolatedAnchorSaturation: Double,
    val anchorShareOfLoad: Double,
    val meanDeflection: Double,
    val strokeLossFraction: Double,
    val peakDishing: Double,
    val dishingOverStroke: Double,
    val forcePerContourPath: Double,
    val contourPaths: Double
)

@Serializable
data class ConcentratedCase(
    val attachmentCount: Int,
    val forcePerAttachment: Double,
    val band: String,
    val peakDeflection: Double,
    val peakDishing: Double,
    val dishingOverStroke: Double,
    val exceedsLayerHeight: Boolean
)

@Serializable
data class FoundationCase(
    val label: String,
    val multiplier: Double,
    val foundationStiffness: Double,
    val layerStiffness: Double,
    val stroke: Double,
    val winklerLengthAlongHelix: Double,
    val winklerLengthAcrossHelix: Double,
    val winklerLengthEffective: Double,
    val ratioAlongHelixToHalfWidth: Double,
    val ratioAcrossHelixToHalfWidth: Double,
    val winklerLengthOverCrossoverSpacing: Double,
    val distributedLoadPaths: List<LoadPathRecord>,
    val peakCrossoverLineShear: Double,
    val peakDuplexLineShear: Double,
    val anchored: List<AnchoredCase>,
    val concentrated: List<ConcentratedCase>,
    val attachmentsForFlatness: Int
)

@Serializable
data class LoadDistributionParameters(
    val temperature: Double,
    val medium: String,
    val thermalEnergy: Double,
    val tileFootprint: String,
    val tileArea: Double,
    val targetForce: Double,
    val targetPressure: Double,
    val layerHeight: Double,
    val graftingDensity: Double,
    val brushChainsUnderTile: Double,
    val basisDegree: Int,
    val foundationReference: String,
    val foundationSweep: List<Double>,
    val isomerisationThreshold: Double,
    val disassemblyThreshold: Double,
    val disassemblyCeiling: Double,
    val duplexShearAllowable: Double,
    val duplexUnzipAllowable: Double,
    val overstretchingCeiling: Double,
    val provenance: Map<String, String>
)

@Serializable
data class LoadDistributionResult(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val validity: List<String>,
    val parameters: LoadDistributionParameters,
    val sheets: List<SheetVariant>,
    val minimumLoadPaths: Map<String, Int>,
    val foundationCases: List<FoundationCase>
)

private const val BASIS_DEGREE = 12

/** Anchor stiffness budgets, as a fraction of the whole layer's stiffness `k_f A`. */
private val ANCHOR_STIFFNESS_FRACTIONS = listOf(0.1, 1.0, 10.0)

private val ANCHOR_GRIDS = listOf(1, 2, 3, 4, 5)

private val ATTACHMENT_GRIDS = listOf(1, 2, 3, 4, 5, 7)

/** Returns the positions of an [side] × [side] grid inset within a [lengthX] × [lengthY] footprint. */
internal fun insetGrid(side: Int, lengthX: Double, lengthY: Double): List<Pair<Double, Double>> =
    (0 until side).flatMap { i ->
        (0 until side).map { j ->
            Pair(
                -lengthX / 2.0 + lengthX * (i + 0.5) / side,
                -lengthY / 2.0 + lengthY * (j + 0.5) / side
            )
        }
    }

fun main() {
    val variants = gen1SheetVariants()
    val (_, nominalSheet) = variants.first()
    val plate = nominalSheet.plate(Gen1Tile.EDGE_X, Gen1Tile.EDGE_Y)
    val area = plate.area
    val pressure = Gen1Tile.TARGET_FORCE / area
    val brushChains = Gen1Tile.GRAFTING_DENSITY * area

    val cases = Gen1Tile.FOUNDATION_SWEEP.map { multiplier ->
        foundationCase(multiplier, nominalSheet, plate, pressure, brushChains)
    }

    val result = LoadDistributionResult(
        task = "T-5",
        leaf = "A1.2",
        title = "Load distribution across the Gen-1 DNA-origami tile",
        verificationType = "in-silico",
        acceptance = "Peak per-load-path force reported against the 35-60 pN disassembly band, " +
                "distributed and concentrated attachment treated separately",
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "pressure" to "pN/nm^2 (= MPa)",
            "stiffness" to "pN/nm (= mN/m)",
            "foundationStiffness" to "pN/nm^3",
            "flexuralRigidity" to "pN*nm",
            "hingeStiffness" to "pN*nm/rad",
            "temperature" to "K"
        ),
        conventions = listOf(
            "x runs along the DNA helices, y across them, origin at the centre of the footprint",
            "the plate deflection w is measured DOWNWARD, positive when the polymer layer is " +
                    "compressed; this is the opposite sense to the z of T-1, deliberately, and " +
                    "the two are never added",
            "applied pressure q > 0 acts downward; the foundation reaction is k_f w upward",
            "a load path carries tension as positive",
            "crossover spacing is the PER-INTERFACE spacing (32 bp for a Rothemund sheet), " +
                    "not the per-helix one (16 bp)"
        ),
        validity = listOf(
            "Kirchhoff plate theory: safe for a 2 nm thick single-layer sheet spanning 40 nm, " +
                    "NOT safe for the 10 nm four-layer reading, where thickness/span reaches 1/4 " +
                    "and transverse shear is neglected — that variant is a bound, not an answer",
            "the continuum plate reduction across the helices requires many crossovers per " +
                    "bending wavelength; where the reported winklerLengthOverCrossoverSpacing is " +
                    "below 1 that requirement FAILS and the tile is better described as a set of " +
                    "quasi-independent duplex beams",
            "the foundation is linear Winkler; the real layer is strongly nonlinear (C-0001), " +
                    "which is why three different k_f are carried and swept rather than one",
            "k_f is taken from C-0001, whose numbers are LOWER BOUNDS per CH-0001 and are being " +
                    "re-derived under T-1c; every conclusion is stated across the sweep",
            "the 35-60 pN band is a WHOLE-STRUCTURE disassembly force at 5.5 pN/s, not a " +
                    "per-load-path allowable; the per-path allowables are the single-duplex " +
                    "shear and unzip numbers, and both are reported",
            "no electrostatics is solved here: the load enters only as a 100 pN total and a " +
                    "bounded non-uniformity (T-3 owns the load model)"
        ),
        parameters = LoadDistributionParameters(
            temperature = ROOM_TEMPERATURE,
            medium = "aqueous buffer, 2/5/10 mM MgCl2",
            thermalEnergy = thermalEnergy(),
            tileFootprint = "${Gen1Tile.EDGE_X.toInt()} x ${Gen1Tile.EDGE_Y.toInt()} nm",
            tileArea = area,
            targetForce = Gen1Tile.TARGET_FORCE,
            targetPressure = pressure,
            layerHeight = Gen1Tile.LAYER_HEIGHT,
            graftingDensity = Gen1Tile.GRAFTING_DENSITY,
            brushChainsUnderTile = brushChains,
            basisDegree = BASIS_DEGREE,
            foundationReference = "C-0001, 10 nm layer at sigma = 0.024 nm^-2: k(L0) = 7.402, " +
                    "k_secant = 20.201, k(h) = 53.337 pN/nm over 1600 nm^2. Secant is nominal.",
            foundationSweep = Gen1Tile.FOUNDATION_SWEEP,
            isomerisationThreshold = ISOMERISATION_THRESHOLD,
            disassemblyThreshold = DISASSEMBLY_THRESHOLD,
            disassemblyCeiling = DISASSEMBLY_CEILING,
            duplexShearAllowable = Gen1Tile.DUPLEX_SHEAR_ALLOWABLE,
            duplexUnzipAllowable = Gen1Tile.DUPLEX_UNZIP_ALLOWABLE,
            overstretchingCeiling = Gen1Tile.OVERSTRETCHING_CEILING,
            provenance = mapOf(
                "duplexBendingRigidity" to "CITED — CanDo, Kim et al. NAR 40:2862 (2012), 230 pN*nm^2",
                "duplexTorsionalRigidity" to "CITED — CanDo, 460 pN*nm^2; measured C = 97-103 nm",
                "duplexStretchModulus" to "CITED, MEASURED — Wang et al. Biophys J 72:1335 (1997), 1100 pN",
                "interhelicalDistance" to "CITED, MEASURED — Fischer et al. Nano Lett 16:4282 (2016), " +
                        "SAXS: 26.9 +/- 0.2 A for a one-layer sheet, 25.36 +/- 0.03 A honeycomb",
                "crossoverSpacing" to "CITED — Rothemund Nature 440:297 (2006), 1.5 turns alternating " +
                        "between two neighbours = 32 bp per interface; Douglas et al. Nature 459:414 " +
                        "(2009), one per 21 bp per interface for honeycomb",
                "crossoverHingeStiffness" to "CITED, fitted to measurement — Chen et al. JACS 136:6995 " +
                        "(2014) SI: k2 = alpha*B/(100a) per phosphate bond, 2 bonds per crossover, " +
                        "alpha in [0.6, 1.2]",
                "isomerisationAndDisassemblyBands" to "CITED, MEASURED — Shrestha et al. NAR 44:6574 " +
                        "(2016), optical tweezers at 5.5 pN/s in 20 mM Tris / 10 mM MgCl2; " +
                        "WHOLE-STRUCTURE, not per-path",
                "duplexShearAllowable" to "CITED, MEASURED — Strunz et al. PNAS 96:11277 (1999), " +
                        "48 +/- 2 pN for 30 bp at ~50 nm/s; Morfill et al. Biophys J 93:2400 (2007), " +
                        "65 pN at 2697 pN/s. LOADING-RATE DEPENDENT.",
                "duplexUnzipAllowable" to "CITED, MEASURED — Essevaz-Roulet et al. PNAS 94:11935 " +
                        "(1997), 10-15 pN near equilibrium",
                "overstretchingCeiling" to "CITED, MEASURED — van Mameren et al. PNAS 106:18231 (2009), " +
                        "65 pN with nicks or free ends present",
                "foundationStiffness" to "DERIVED from C-0001 (challenged by CH-0001 — lower bounds)"
            )
        ),
        sheets = variants.map { it.first },
        minimumLoadPaths = mapOf(
            "below35pN_disassemblyBand" to
                    minimumLoadPaths(Gen1Tile.TARGET_FORCE, DISASSEMBLY_THRESHOLD),
            "below10pN_isomerisationBand" to
                    minimumLoadPaths(Gen1Tile.TARGET_FORCE, ISOMERISATION_THRESHOLD),
            "below48pN_duplexShear" to
                    minimumLoadPaths(Gen1Tile.TARGET_FORCE, Gen1Tile.DUPLEX_SHEAR_ALLOWABLE),
            "below10pN_duplexUnzip" to
                    minimumLoadPaths(Gen1Tile.TARGET_FORCE, Gen1Tile.DUPLEX_UNZIP_ALLOWABLE),
            "below65pN_overstretchingCeiling" to
                    minimumLoadPaths(Gen1Tile.TARGET_FORCE, Gen1Tile.OVERSTRETCHING_CEILING)
        ),
        foundationCases = cases
    )

    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-5-load-distribution.json")
    output.parentFile.mkdirs()
    output.writeText(json.encodeToString(json.encodeToJsonElement(result).roundedForResult()) + "\n")
    report(result, output)
}

private fun foundationCase(
    multiplier: Double,
    sheet: OrigamiSheet,
    plate: OrthotropicPlate,
    pressure: Double,
    brushChains: Double
): FoundationCase {
    val foundation = Gen1Tile.FOUNDATION_SECANT * multiplier
    val stroke = pressure / foundation
    val ellAlong = winklerLength(plate.rigidityX, foundation)
    val ellAcross = winklerLength(plate.rigidityY, foundation)
    val ellEffective = winklerLength(plate.effectiveRigidity, foundation)
    val solver = PlateOnFoundation(plate, foundation, basisDegree = BASIS_DEGREE)
    val distributed = solver.solve(uniformPressure(pressure))

    val crossoversOnCut = sheet.crossoversOnCut(plate.lengthX)
    val duplexesOnCut = sheet.duplexesOnCut(plate.lengthY)
    val crossoverShear = distributed.peakCrossoverLineShear(81)
    val duplexShear = distributed.peakDuplexLineShear(81)

    val distributedPaths = listOf(
        loadPathRecord(
            "LP-brush", "one grafted PEG chain under the tile — the distributed reaction",
            brushChains, Gen1Tile.TARGET_FORCE
        ),
        loadPathRecord(
            "LP-crossover", "one crossover on the worst cut parallel to the helices",
            crossoversOnCut, crossoverShear
        ),
        loadPathRecord(
            "LP-duplex", "one duplex on the worst cut perpendicular to the helices",
            duplexesOnCut, duplexShear
        )
    )

    val anchored = ANCHOR_GRIDS.flatMap { side ->
        ANCHOR_STIFFNESS_FRACTIONS.map { fraction ->
            anchoredCase(side, fraction, sheet, plate, foundation, pressure, stroke, ellAlong, ellAcross)
        }
    }
    val concentrated = ATTACHMENT_GRIDS.map { side ->
        concentratedCase(side, plate, foundation, stroke)
    }

    return FoundationCase(
        label = when (multiplier) {
            1.0 -> "C-0001 secant, 10 nm layer at sigma = 0.024 nm^-2"
            else -> "C-0001 secant x $multiplier"
        },
        multiplier = multiplier,
        foundationStiffness = foundation,
        layerStiffness = foundation * plate.area,
        stroke = stroke,
        winklerLengthAlongHelix = ellAlong,
        winklerLengthAcrossHelix = ellAcross,
        winklerLengthEffective = ellEffective,
        ratioAlongHelixToHalfWidth = ellAlong / (plate.lengthX / 2.0),
        ratioAcrossHelixToHalfWidth = ellAcross / (plate.lengthY / 2.0),
        winklerLengthOverCrossoverSpacing = ellAcross / sheet.crossoverSpacing,
        distributedLoadPaths = distributedPaths,
        peakCrossoverLineShear = crossoverShear,
        peakDuplexLineShear = duplexShear,
        anchored = anchored,
        concentrated = concentrated,
        attachmentsForFlatness = ceil(1.25 * plate.area / (ellEffective * ellEffective)).toInt()
    )
}

private fun loadPathRecord(
    name: String,
    description: String,
    paths: Double,
    totalForce: Double
): LoadPathRecord {
    val path = LoadPath(name, description, paths, totalForce)
    return LoadPathRecord(
        name = name,
        description = description,
        paths = paths,
        totalForce = totalForce,
        forcePerPath = path.forcePerPath,
        band = path.band.name
    )
}

private fun anchoredCase(
    side: Int,
    fraction: Double,
    sheet: OrigamiSheet,
    plate: OrthotropicPlate,
    foundation: Double,
    pressure: Double,
    freeStroke: Double,
    ellAlong: Double,
    ellAcross: Double
): AnchoredCase {
    val positions = insetGrid(side, plate.lengthX, plate.lengthY)
    val each = fraction * foundation * plate.area / positions.size
    val supports = positions.map { (x, y) -> PointSupport(x, y, each) }
    val solver = PlateOnFoundation(plate, foundation, supports, BASIS_DEGREE)
    val deflection = solver.solve(uniformPressure(pressure))
    val forces = deflection.supportForces
    val peak = forces.maxOf { kotlin.math.abs(it) }
    val saturation = 8.0 * pressure * sqrt(plate.effectiveRigidity / foundation)
    val contourPaths = 4.0 * ellAlong / sheet.crossoverSpacing +
            4.0 * ellAcross / sheet.interhelicalDistance
    return AnchoredCase(
        anchorCount = positions.size,
        anchorStiffnessFraction = fraction,
        anchorStiffnessEach = each,
        peakAnchorForce = peak,
        peakAnchorBand = structuralBand(peak).name,
        isolatedAnchorSaturation = saturation,
        anchorShareOfLoad = forces.sum() / deflection.appliedForce,
        meanDeflection = deflection.meanDeflection,
        strokeLossFraction = 1.0 - deflection.meanDeflection / freeStroke,
        peakDishing = deflection.peakDishing(81),
        dishingOverStroke = deflection.peakDishing(81) / freeStroke,
        forcePerContourPath = peak / contourPaths,
        contourPaths = contourPaths
    )
}

private fun concentratedCase(
    side: Int,
    plate: OrthotropicPlate,
    foundation: Double,
    freeStroke: Double
): ConcentratedCase {
    val positions = insetGrid(side, plate.lengthX, plate.lengthY)
    val perAttachment = Gen1Tile.TARGET_FORCE / positions.size
    val loads = positions.map { (x, y) -> PointLoad(x, y, perAttachment) }
    val solver = PlateOnFoundation(plate, foundation, basisDegree = BASIS_DEGREE)
    val deflection = solver.solve(pointLoads = loads)
    val peakDishing = deflection.peakDishing(81)
    return ConcentratedCase(
        attachmentCount = positions.size,
        forcePerAttachment = perAttachment,
        band = structuralBand(perAttachment).name,
        peakDeflection = deflection.peakDeflection(81),
        peakDishing = peakDishing,
        dishingOverStroke = peakDishing / freeStroke,
        exceedsLayerHeight = deflection.peakDeflection(81) > Gen1Tile.LAYER_HEIGHT
    )
}

private fun report(result: LoadDistributionResult, output: File) {
    println("T-5 / A1.2 — ${result.title}")
    println("300 K, aqueous buffer, k_BT = ${"%.3f".format(thermalEnergy())} pN*nm")
    println("tile ${result.parameters.tileFootprint}, target force ${result.parameters.targetForce} pN")
    println()
    println("--- sheet variants ".padEnd(110, '-'))
    println("%42s %8s %10s %10s %8s %7s %7s".format(
        "variant", "t[nm]", "D_par", "D_perp", "D_k", "aniso", "n_dup"
    ))
    result.sheets.forEach {
        println("%42s %8.2f %10.2f %10.3f %8.2f %7.1f %7.1f".format(
            it.name.take(42), it.thickness, it.alongHelixRigidity, it.acrossHelixRigidity,
            it.twistingRigidity, it.anisotropy, it.duplexesAcrossTile
        ))
    }
    println()
    println("--- foundation sweep, nominal sheet ".padEnd(110, '-'))
    println("%7s %10s %8s %8s %8s %8s %8s %9s".format(
        "k_f x", "k_f", "stroke", "l_par", "l_perp", "l_par/L", "l_pp/L", "l_pp/p"
    ))
    result.foundationCases.forEach {
        println("%7.2f %10.5f %8.2f %8.2f %8.2f %8.3f %8.3f %9.2f".format(
            it.multiplier, it.foundationStiffness, it.stroke,
            it.winklerLengthAlongHelix, it.winklerLengthAcrossHelix,
            it.ratioAlongHelixToHalfWidth, it.ratioAcrossHelixToHalfWidth,
            it.winklerLengthOverCrossoverSpacing
        ))
    }
    println()
    println("--- distributed attachment ".padEnd(110, '-'))
    result.foundationCases.forEach { case ->
        println("k_f x ${case.multiplier}: peak shear across a crossover line = " +
                "%.3e pN, across a duplex line = %.3e pN".format(
                    case.peakCrossoverLineShear, case.peakDuplexLineShear
                ))
        case.distributedLoadPaths.forEach {
            println("    %-14s %8.1f paths  %10.3e pN each  %s".format(
                it.name, it.paths, it.forcePerPath, it.band
            ))
        }
    }
    println()
    println("--- concentrated attachment, k_f nominal ".padEnd(110, '-'))
    val nominal = result.foundationCases.first { it.multiplier == 1.0 }
    println("%6s %10s %26s %12s %10s".format("n", "F/n [pN]", "band", "dish [nm]", "dish/stroke"))
    nominal.concentrated.forEach {
        println("%6d %10.2f %26s %12.3f %10.3f".format(
            it.attachmentCount, it.forcePerAttachment, it.band, it.peakDishing, it.dishingOverStroke
        ))
    }
    println()
    println("minimum load paths: ${result.minimumLoadPaths}")
    println("attachments needed for dishing < 10% of stroke, per k_f: " +
            result.foundationCases.joinToString { "x${it.multiplier}: ${it.attachmentsForFlatness}" })
    println()
    println("--- discrete anchors, k_f nominal ".padEnd(110, '-'))
    println("%6s %8s %12s %26s %12s %12s".format(
        "n", "frac", "peak F [pN]", "band", "saturation", "stroke loss"
    ))
    nominal.anchored.forEach {
        println("%6d %8.1f %12.2f %26s %12.2f %12.3f".format(
            it.anchorCount, it.anchorStiffnessFraction, it.peakAnchorForce,
            it.peakAnchorBand, it.isolatedAnchorSaturation, it.strokeLossFraction
        ))
    }
    println()
    println("written: ${output.path}")
}
