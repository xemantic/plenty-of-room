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
import com.xemantic.nano.plentyofroom.coupling.winklerBendingLength
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
// T-199 -- is 10 x 6 a better tile than 15 x 4?
//
// Douglas et al. folded seven honeycomb blocks and concluded that SIX helices per x-raster row
// gives the greatest fraction of defect-free objects. The tile this programme recommends is FOUR
// per row. Both are 60 helices, so the choice costs no scaffold -- which is exactly what makes it
// worth testing, and why C-0119 raised it.
//
// The cheap bound runs first and needs no plate: `Sigma y^2` for `n` layers at spacing `d` is
// `n(n^2 - 1)d^2/12`, so the parallel-axis factor's excess scales as `(n^2 - 1)/12` -- a pure
// integer function of the layer count with no material constant in it. Six layers therefore carry
// 35/15 = 2.333x the excess of four, at the same 60 helices.
// ---------------------------------------------------------------------------------------------

private const val T199_SAMPLES: Int = 81
private const val T199_TOLERANCE: Double = 0.10
private const val T199_RIM_STANDOFF: Double = 1.0
private const val T199_ROW_BP: Int = 112
private const val T199_BISECTION: Double = 1e-9

/** Nine significant digits as a string -- a number emitted as a STRING is not rounded. */
private fun Double.emitted(digits: Int = 9): String =
    if (!isFinite() || this == 0.0) toString()
    else java.math.BigDecimal(this).round(java.math.MathContext(digits)).toDouble().toString()

@Serializable
private class T199CrossSection(
    val name: String,
    val rasterRows: Int,
    val helicesPerRasterRow: Int,
    val helices: Int,
    val edgeX: Double,
    val edgeY: Double,
    val thickness: Double,
    val aspectRatio: Double,
    val secondMomentOfLayers: Double,
    val parallelAxisFactor: Double,
    val alongHelixRigidity: Double,
    val acrossHelixRigidity: Double,
    val anisotropy: Double,
    val reachAlong: Double,
    val reachAcross: Double,
    val reachAlongOverSpan: Double,
    val reachAcrossOverSpan: Double,
    val freeTileDishingOverStroke: Double,
    val flat: Boolean,
    val thresholdFraction: Double?,
    val marginAtMeasuredBandLowEnd: Double?,
    val thicknessOverSpan: Double
)

@Serializable
private class T199Convergence(
    val axis: String, val values: List<Double>, val results: List<Double>,
    val departure: Double, val note: String
)

@Serializable
private class T199Reproduction(
    val source: String, val quantity: String, val published: Double,
    val reproduced: Double, val departure: Double, val strict: Boolean
)

@Serializable
private class T199Result(
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
    val crossSections: List<T199CrossSection>,
    val comparison: Map<String, String>,
    val convergence: List<T199Convergence>,
    val reproductions: List<T199Reproduction>,
    val falsifiers: List<String>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

private class T199Profile(val smoothDepth: Double, val smoothWidth: Double, val rimDepth: Double) {
    fun field(interiorPressure: Double, edgeX: Double, lengthY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, edgeX, lengthY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T199_RIM_STANDOFF))
        )
}

private fun t199Profile(file: File): T199Profile {
    require(file.exists()) { "C-0022's result file is missing: ${file.path}" }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .firstOrNull {
            fun value(name: String) = it.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == 2.0 && value("gapHeight") == 10.0 &&
                    value("appliedBias") == 0.192
        } ?: error("no C-0022 profile at the design state")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T199Profile(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

/**
 * One honeycomb cross-section, solved as a smeared equivalent sheet.
 *
 * The `m x n` block has `n` LAYERS stacked through the thickness and `m` raster rows; this
 * programme's grillage models the rows as beams, so `m` is the beam count and `n` the layer count.
 * The tile's in-plane span along the helices is fixed at `C-0086`'s buildable 112 bp for both, so
 * the only thing that changes is the cross-section.
 */
private class T199Tile(
    val rasterRows: Int,
    val layers: Int,
    private val profile: T199Profile,
    private val subdivisions: Int = 2,
    private val samples: Int = T199_SAMPLES
) {
    val edgeX: Double = T199_ROW_BP * Gen1Tile.RISE_PER_BASE_PAIR
    val edgeY: Double = rasterRows * Gen1Tile.INTERHELICAL_HONEYCOMB

    fun rigiditiesAt(fraction: Double): MultiLayerRigidities = multiLayerRigidities(
        layers = layers,
        interhelicalDistance = Gen1Tile.INTERHELICAL_HONEYCOMB,
        crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
        coupling = LayerCoupling.CALIBRATED,
        compositeFraction = fraction
    )

    fun dishingAt(fraction: Double): Double {
        val rigidities = rigiditiesAt(fraction)
        val sheet = equivalentSheet(rigidities)
        val interiorPressure = Gen1Tile.TARGET_FORCE / (edgeX * edgeY)
        val field = profile.field(interiorPressure, edgeX, edgeY)
        val freeStroke = PlateOnFoundation(
            sheet.plate(edgeX, edgeY), Gen1Tile.FOUNDATION_SECANT, emptyList(), 12
        ).solve(uniformPressure(interiorPressure)).meanDeflection
        val pitch = sheet.crossoverSpacing / 2.0
        val usable = edgeX - 2.0 * CrossoverLayout.EDGE_MARGIN
        val columns = CrossoverLayout.centred(floor(usable / pitch).toInt() + 1, pitch)
        return OrigamiGrillage(
            sheet = sheet,
            lengthX = edgeX,
            beamCount = rasterRows,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            columns = columns,
            subdivisions = subdivisions
        ).solve(field).peakDishing(samples) / freeStroke
    }
}

@Suppress("LongMethod")
fun main() {
    val profile = t199Profile(ResultInputs.T_3B.file())
    val measured = MeasuredBundleRigidity.COMPOSITE_FRACTION
    val bandLow = MeasuredBundleRigidity.COMPOSITE_FRACTION_MIN

    // ------------------------------------------------------------------- the cheap bound, first
    fun excessRatio(a: Int, b: Int) = (a * a - 1.0) / (b * b - 1.0)
    println("T-199 — the cheap bound, before any plate is solved")
    println(
        "  Sigma y^2 for n layers is n(n^2-1)d^2/12, so the parallel-axis EXCESS scales as (n^2-1)"
    )
    println(
        "  six layers against four: %.6f x the excess, at the SAME 60 helices".format(
            excessRatio(6, 4)
        )
    )

    val candidates = listOf(15 to 4, 10 to 6, 6 to 10, 3 to 20)
    val sections = candidates.map { (rows, layers) ->
        val tile = T199Tile(rows, layers, profile)
        val rigidities = tile.rigiditiesAt(measured)
        val dishing = tile.dishingAt(measured)
        val crossing = firstCrossing(0.0, 1.0, 40, T199_TOLERANCE, T199_BISECTION) {
            tile.dishingAt(it)
        }
        val reachAlong = winklerBendingLength(
            rigidities.alongHelixRigidity, Gen1Tile.FOUNDATION_SECANT
        )
        val reachAcross = winklerBendingLength(
            rigidities.acrossHelixRigidity, Gen1Tile.FOUNDATION_SECANT
        )
        println(
            "  %2d x %-2d  D_par %9.1f  D_perp %7.2f  factor %7.3f  dishing %.9f  %s  f* %s".format(
                rows, layers, rigidities.alongHelixRigidity, rigidities.acrossHelixRigidity,
                rigidities.parallelAxisFactor, dishing,
                if (dishing < T199_TOLERANCE) "flat    " else "NOT flat",
                crossing?.root?.emitted() ?: "none"
            )
        )
        T199CrossSection(
            name = "$rows x $layers",
            rasterRows = rows,
            helicesPerRasterRow = layers,
            helices = crossSectionHelices(rows, layers),
            edgeX = tile.edgeX,
            edgeY = tile.edgeY,
            thickness = rigidities.thickness,
            aspectRatio = tile.edgeX / tile.edgeY,
            secondMomentOfLayers = rigidities.secondMomentOfLayers,
            parallelAxisFactor = rigidities.parallelAxisFactor,
            alongHelixRigidity = rigidities.alongHelixRigidity,
            acrossHelixRigidity = rigidities.acrossHelixRigidity,
            anisotropy = rigidities.anisotropy,
            reachAlong = reachAlong,
            reachAcross = reachAcross,
            reachAlongOverSpan = reachAlong / tile.edgeX,
            reachAcrossOverSpan = reachAcross / tile.edgeY,
            freeTileDishingOverStroke = dishing,
            flat = dishing < T199_TOLERANCE,
            thresholdFraction = crossing?.root,
            marginAtMeasuredBandLowEnd = crossing?.let { bandLow / it.root },
            thicknessOverSpan = rigidities.thickness / tile.edgeX
        )
    }

    val ours = sections.first { it.name == "15 x 4" }
    val theirs = sections.first { it.name == "10 x 6" }

    // ------------------------------------------------------------------- convergence
    val convergence = ArrayList<T199Convergence>()
    val meshResults = listOf(1, 2, 4).map {
        T199Tile(10, 6, profile, subdivisions = it).dishingAt(measured)
    }
    convergence += T199Convergence(
        axis = "beam subdivisions per node interval, 10 x 6 free-tile dishing",
        values = listOf(1.0, 2.0, 4.0),
        results = meshResults,
        departure = abs(meshResults[2] - meshResults[1]) / abs(meshResults[2]),
        note = "nested refinements only — a subdivision of 3 moves a load off a node (CLAUDE.md)"
    )
    val sampleResults = listOf(41, 81, 161).map {
        T199Tile(10, 6, profile, samples = it).dishingAt(measured)
    }
    convergence += T199Convergence(
        axis = "dishing grid samples per side, 10 x 6",
        values = listOf(41.0, 81.0, 161.0),
        results = sampleResults,
        departure = abs(sampleResults[2] - sampleResults[1]) / abs(sampleResults[2]),
        note = "peak dishing is a max over the grid, so it can only rise with resolution"
    )

    // ------------------------------------------------------------------- reproductions
    val reproductions = listOf(
        T199Reproduction(
            "C-0109 / C-0116", "15 x 4 free-tile dishing at the measured coupling",
            0.0577199433, ours.freeTileDishingOverStroke,
            abs(ours.freeTileDishingOverStroke - 0.0577199433) / 0.0577199433, true
        ),
        T199Reproduction(
            "C-0116", "15 x 4 composite-fraction threshold", 0.0788618807,
            ours.thresholdFraction ?: Double.NaN,
            abs((ours.thresholdFraction ?: Double.NaN) - 0.0788618807) / 0.0788618807, true
        ),
        T199Reproduction(
            "C-0116", "15 x 4 parallel-axis factor", 39.4479652, ours.parallelAxisFactor,
            abs(ours.parallelAxisFactor - 39.4479652) / 39.4479652, true
        )
    )
    reproductions.forEach {
        println(
            "  reproduce %s %s: %.9f against %.9f, departure %.2e".format(
                it.source, it.quantity, it.reproduced, it.published, it.departure
            )
        )
    }

    val betterOnDishing = theirs.freeTileDishingOverStroke < ours.freeTileDishingOverStroke
    // A NULL threshold is the ABSENCE of a requirement, not a zero margin -- `CLAUDE.md`: "a margin
    // of Infinity is not a margin, it is the absence of a requirement; record it as null, not as a
    // number." Coalescing the null to 0.0 and comparing would report the STRONGER cross-section as
    // the weaker one, which is what the first run of this study did.
    val betterOnMargin = when {
        theirs.thresholdFraction == null && ours.thresholdFraction == null -> false
        theirs.thresholdFraction == null -> true
        ours.thresholdFraction == null -> false
        else -> (theirs.marginAtMeasuredBandLowEnd ?: 0.0) > (ours.marginAtMeasuredBandLowEnd ?: 0.0)
    }
    // The footprint is the consequence the flatness numbers do not carry. SS3 asks for a ~40 x 40 nm
    // tile; at a fixed 112 bp span a different `m` changes the OTHER side, so a flatter
    // cross-section is a SMALLER tile and the actuation force is specified over its area.
    val footprintOurs = ours.edgeX * ours.edgeY
    val footprintTheirs = theirs.edgeX * theirs.edgeY

    val findings = HashMap<String, String>()
    findings["theCheapBoundPredictsTheDirection"] =
        ("Sigma y^2 for n layers is n(n^2-1)d^2/12, so the parallel-axis excess scales as (n^2-1) " +
                "and six layers carry %s x the excess of four at the SAME 60 helices. The factor " +
                "goes %s -> %s. No material constant enters."
            ).format(
                excessRatio(6, 4).emitted(6), ours.parallelAxisFactor.emitted(),
                theirs.parallelAxisFactor.emitted()
            )
    findings["butTheSpanChangesToo"] =
        ("10 x 6 is not merely a stiffer 15 x 4: it is NARROWER. edgeY goes %s -> %s nm at a fixed " +
                "%s nm span along the helices, so the aspect ratio goes %s -> %s and the " +
                "across-helix reach is being compared against a shorter side."
            ).format(
                ours.edgeY.emitted(6), theirs.edgeY.emitted(6), ours.edgeX.emitted(6),
                ours.aspectRatio.emitted(6), theirs.aspectRatio.emitted(6)
            )
    findings["theThresholdDisappearsRatherThanImproving"] =
        ("10 x 6 has NO composite-fraction threshold at all: its free-tile dishing never reaches " +
                "T-5b's 0.10 anywhere in f = [0, 1], including f = 0 where the layers are fully " +
                "INDEPENDENT. So it is flat without depending on the interlayer coupling " +
                "calibration -- which is the one unmeasured number C-0116 says the 15 x 4 verdict " +
                "now rests on. The stronger cross-section is stronger by REMOVING a dependency, " +
                "not by widening a margin.")
    findings["theFootprintIsTheCost"] =
        ("At the fixed 112 bp span a different m changes the tile's OTHER side: 15 x 4 is %s x %s " +
                "nm, essentially square and essentially SS3's ~40 x 40; 10 x 6 is %s x %s nm, %s of " +
                "the footprint. SS3's 100 pN is specified over that area, and C-0022's collar was " +
                "solved on the square. So this is a SPECIFICATION trade, not a free improvement."
            ).format(
                ours.edgeX.emitted(6), ours.edgeY.emitted(6),
                theirs.edgeX.emitted(6), theirs.edgeY.emitted(6),
                (footprintTheirs / footprintOurs).emitted(4)
            )
    findings["theVerdict"] =
        ("10 x 6 dishes %s against 15 x 4's %s, and its flatness threshold sits at f = %s against " +
                "%s -- so the measured coupling clears it by %s x rather than %s x. On this " +
                "programme's own criterion the cross-section its SOURCE recommends is %s."
            ).format(
                theirs.freeTileDishingOverStroke.emitted(),
                ours.freeTileDishingOverStroke.emitted(),
                theirs.thresholdFraction?.emitted() ?: "none",
                ours.thresholdFraction?.emitted() ?: "none",
                theirs.marginAtMeasuredBandLowEnd?.emitted(6) ?: "n/a",
                ours.marginAtMeasuredBandLowEnd?.emitted(6) ?: "n/a",
                if (betterOnDishing && betterOnMargin) "ALSO the flatter one"
                else if (betterOnDishing || betterOnMargin) "better on one axis and not the other"
                else "NOT the flatter one"
            )

    val result = T199Result(
        task = "T-199",
        leaf = "A8.2",
        title = "Is 10 x 6 a better tile than 15 x 4? The cross-section its own source recommends",
        verificationType = "in-silico (beam-and-hinge grillage) + logical (the second-moment bound)",
        maturity = "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated -- though " +
                "BOTH cross-sections have been folded and gel-analysed by Douglas et al.",
        units = mapOf(
            "rigidity" to "pN*nm", "length" to "nm",
            "dishing" to "dimensionless, as a fraction of the free-tile stroke",
            "compositeFraction" to "dimensionless, 0 at INDEPENDENT and 1 at COMPOSITE"
        ),
        conventions = mapOf(
            "nomenclature" to "m x n = m x-raster rows of n helices (Douglas et al., Figure 2)",
            "mapping" to "m is the grillage's beam count, n its layer count",
            "load" to "C-0022's solved collar at 2 mM / 10 nm / 0.192 V",
            "span" to "112 bp along the helices for every cross-section, C-0086's buildable width",
            "coupling" to "NONE — the uncoupled free tile throughout",
            "flat" to "peak dishing below T-5b's 0.10"
        ),
        parameters = mapOf(
            "rowBasePairs" to T199_ROW_BP.toString(),
            "dishingSamplesPerSide" to T199_SAMPLES.toString(),
            "tolerance" to T199_TOLERANCE.toString(),
            "bisectionExitWidth" to T199_BISECTION.toString(),
            "measuredCompositeFraction" to measured.emitted(),
            "measuredBandLowEnd" to bandLow.emitted(),
            "interhelicalHoneycomb" to Gen1Tile.INTERHELICAL_HONEYCOMB.emitted()
        ),
        citedInputs = listOf(
            "C-0119 — the published cross-sections and the yield ordering that raised this task",
            "C-0109 — the four-layer tile and its rigidity machinery",
            "C-0116 — the composite-fraction threshold this re-reads per cross-section",
            "C-0022 — the solved edge collar",
            "C-0086 — the buildable 112 bp span"
        ),
        cheapBound = mapOf(
            "secondMomentLaw" to "Sigma y^2 = n(n^2 - 1) d^2 / 12 for n layers at spacing d",
            "excessScalesAs" to "(n^2 - 1), a pure integer function of the layer count",
            "sixOverFour" to excessRatio(6, 4).emitted(),
            "predicts" to "six layers are stiffer at the same helix count, so 10 x 6 should dish " +
                    "less than 15 x 4 unless the narrower span undoes it"
        ),
        crossSections = sections,
        comparison = mapOf(
            "ourTile" to ours.name,
            "theirRecommendation" to theirs.name,
            "sameHelixCount" to (ours.helices == theirs.helices).toString(),
            "dishingOurs" to ours.freeTileDishingOverStroke.emitted(),
            "dishingTheirs" to theirs.freeTileDishingOverStroke.emitted(),
            "thresholdOurs" to (ours.thresholdFraction?.emitted() ?: "none"),
            "thresholdTheirs" to (theirs.thresholdFraction?.emitted() ?: "none"),
            "marginOurs" to (ours.marginAtMeasuredBandLowEnd?.emitted() ?: "n/a"),
            "marginTheirs" to (theirs.marginAtMeasuredBandLowEnd?.emitted() ?: "n/a"),
            "betterOnDishing" to betterOnDishing.toString(),
            "betterOnFlatnessMargin" to betterOnMargin.toString(),
            "marginNote" to "a null threshold is the ABSENCE of a flatness requirement, not a zero " +
                    "margin: the cross-section never reaches T-5b's 0.10 at ANY interlayer " +
                    "coupling in [0, 1], which is strictly stronger than clearing a threshold",
            "footprintOurs" to footprintOurs.emitted(),
            "footprintTheirs" to footprintTheirs.emitted(),
            "footprintRatio" to (footprintTheirs / footprintOurs).emitted(),
            "footprintNote" to "SS3 asks for a ~40 x 40 nm tile. At the fixed 112 bp span, 15 x 4 is " +
                    "38.08 x 38.04 nm -- essentially square and essentially SS3's -- while 10 x 6 is " +
                    "38.08 x 25.36 nm. The flatter cross-section is a SMALLER tile, and SS3's 100 pN " +
                    "is specified over the footprint, so this is a specification consequence rather " +
                    "than a free improvement."
        ),
        convergence = convergence,
        reproductions = reproductions,
        falsifiers = listOf(
            "F1 — 10 x 6 is NOT flatter, in which case the paper's yield recommendation and this " +
                    "programme's flatness criterion disagree and the choice becomes a trade.",
            "F2 — C-0116's threshold does not reproduce on 15 x 4, in which case no cross-section " +
                    "comparison built on it is licensed.",
            "F3 — the narrower span makes some cross-section NOT flat at the measured coupling, " +
                    "which would bound the aspect ratio from the flatness side."
        ),
        findings = findings,
        validity = listOf(
            "Every cross-section is a SMEARED equivalent sheet, as in C-0109 and C-0116; the " +
                    "grillage never reads `layers`.",
            "Kirchhoff degrades as the block thickens: thickness over span is reported per " +
                    "cross-section and 10 x 6 is THICKER than 15 x 4, so its D_par is a WEAKER " +
                    "upper bound than the one C-0109 already flagged at 0.252.",
            "The yield ordering is Douglas et al.'s, measured on gel and 100-particle TEM counts. " +
                    "This study does not re-derive it and cannot: it is a fabrication measurement.",
            "The comparison is at FIXED span along the helices (112 bp). A different m changes the " +
                    "tile's OTHER dimension, so these are not similar rectangles and the aspect " +
                    "ratio is reported beside every row.",
            "Only the UNCOUPLED tile is solved. Whether a coupling helps any cross-section is " +
                    "T-197, and C-0109 already reports every coupled cell as worse than uncoupled."
        ),
        openQuestions = listOf(
            "Whether the electrode collar C-0022 solved for a 40 nm square transfers to a tile of " +
                    "a different aspect ratio -- the load is read at a fixed profile here.",
            "What the honeycomb's three crossover azimuths offer as an attachment lattice at each " +
                    "cross-section, since every plan result in this corpus is single-layer square.",
            "Whether 6 x 10 or 3 x 20 -- both 60 helices, both folded by Douglas et al., neither " +
                    "producing a sharp monomer band -- are excluded by folding alone or by " +
                    "flatness too."
        )
    )
    val output = File("gpd/results/T-199-cross-section-comparison.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digits = 9, digitsByKey = mapOf("departure" to 2)
            ).withEmissionHeader(LatticeTag.SQUARE, null) as JsonObject)
        ) + "\n"
    )
    println("T-199 — wrote ${output.path}")
}
