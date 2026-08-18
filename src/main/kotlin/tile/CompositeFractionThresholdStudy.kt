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
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.OrigamiSheet
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sqrt

// ---------------------------------------------------------------------------------------------
// T-196 -- where the four-layer tile stops being flat.
//
// `C-0109` found the four-layer honeycomb tile flat with NO coupling at all, and said in its own
// section 11 that the verdict turns on the interlayer coupling fraction `f`: the crossing lies
// somewhere in (0.00, 0.26) and locating it is one sweep of the same study.
//
// The CHEAP BOUND runs first and it removes most of the sweep: `f` enters `multiLayerRigidities`
// only through `realised = 1 + f(factor - 1)`, and that ONE number multiplies both `D_par` and
// `D_perp`. So `f` is a pure SCALE on the plate, the dishing is a function of that single scale,
// and the threshold in `f` follows from the threshold in the scale by one division.
// ---------------------------------------------------------------------------------------------

private const val T196_DUPLEXES: Int = 15
private const val T196_BUILDABLE_EDGE_X: Double = 38.08
private const val T196_BUILDABLE_ROW_BP: Int = 112
private const val T196_SAMPLES: Int = 81
private const val T196_TOLERANCE: Double = 0.10
private const val T196_RIM_STANDOFF: Double = 1.0

/** The bisection's exit width on the fraction axis. Reported, and swept as a convergence axis. */
private const val T196_BISECTION: Double = 1e-9

@Serializable
private class T196Sample(
    val compositeFraction: Double,
    val realisedEnhancement: Double,
    val alongHelixRigidity: Double,
    val acrossHelixRigidity: Double,
    val reachAlong: Double,
    val reachAcross: Double,
    val freeTileDishingOverStroke: Double,
    val flat: Boolean
)

@Serializable
private class T196Convergence(
    val axis: String,
    val values: List<Double>,
    val results: List<Double>,
    val departure: Double,
    val note: String
)

@Serializable
private class T196Reproduction(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val departure: Double,
    val strict: Boolean
)

@Serializable
private class T196Result(
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
    val samples: List<T196Sample>,
    val threshold: Map<String, String>,
    val convergence: List<T196Convergence>,
    val reproductions: List<T196Reproduction>,
    val falsifiers: List<String>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

/**
 * Nine significant digits, as a string.
 *
 * `CLAUDE.md`: **a number emitted as a STRING is not rounded** — `roundedForResult` dispatches on
 * the JSON type and passes strings through, correctly, so a `Double.toString()` inside a
 * `Map<String, String>` carries full round-trip precision into a file that declares nine digits.
 * Every number this study reports through a string map goes through here instead.
 */
private fun Double.emitted(digits: Int = 9): String =
    if (!isFinite() || this == 0.0) toString()
    else java.math.BigDecimal(this).round(java.math.MathContext(digits)).toDouble().toString()

/** `C-0022`'s solved edge profile, keyed on every dimension its sweep varied. */
private class T196Profile(val smoothDepth: Double, val smoothWidth: Double, val rimDepth: Double) {
    fun field(interiorPressure: Double, edgeX: Double, lengthY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, edgeX, lengthY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T196_RIM_STANDOFF))
        )
}

private fun t196Profile(file: File): T196Profile {
    require(file.exists()) { "C-0022's result file is missing: ${file.path}" }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .firstOrNull {
            fun value(name: String) = it.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == 2.0 && value("gapHeight") == 10.0 &&
                    value("appliedBias") == 0.192
        } ?: error("no C-0022 profile at the design state")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T196Profile(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

/** The free-tile dishing of a smeared multi-layer sheet, at one subdivision count. */
private class T196FreeTile(
    private val profile: T196Profile,
    private val subdivisions: Int = 2,
    private val samples: Int = T196_SAMPLES
) {
    fun rigiditiesAt(fraction: Double, layerSpacing: Double? = null): MultiLayerRigidities =
        multiLayerRigidities(
            layers = 4,
            interhelicalDistance = Gen1Tile.INTERHELICAL_HONEYCOMB,
            crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
            coupling = LayerCoupling.CALIBRATED,
            compositeFraction = fraction,
            layerSpacing = layerSpacing ?: Gen1Tile.INTERHELICAL_HONEYCOMB
        )

    fun singleLayerRigidities(): MultiLayerRigidities = multiLayerRigidities(
        layers = 1,
        interhelicalDistance = Gen1Tile.INTERHELICAL_SHEET,
        crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_SHEET_BP,
        coupling = LayerCoupling.INDEPENDENT
    )

    fun dishing(rigidities: MultiLayerRigidities, edgeX: Double): Double {
        val sheet: OrigamiSheet = equivalentSheet(rigidities)
        val edgeY = T196_DUPLEXES * rigidities.interhelicalDistance
        val interiorPressure = Gen1Tile.TARGET_FORCE / (edgeX * edgeY)
        val field = profile.field(interiorPressure, edgeX, edgeY)
        val freeStroke = PlateOnFoundation(
            sheet.plate(edgeX, edgeY), Gen1Tile.FOUNDATION_SECANT, emptyList(), 12
        ).solve(uniformPressure(interiorPressure)).meanDeflection
        val pitch = sheet.crossoverSpacing / 2.0
        val usable = edgeX - 2.0 * CrossoverLayout.EDGE_MARGIN
        val columns = CrossoverLayout.centred(floor(usable / pitch).toInt() + 1, pitch)
        val lattice = OrigamiGrillage(
            sheet = sheet,
            lengthX = edgeX,
            beamCount = T196_DUPLEXES,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            columns = columns,
            subdivisions = subdivisions
        )
        return lattice.solve(field).peakDishing(samples) / freeStroke
    }

    fun dishingAtFraction(fraction: Double, layerSpacing: Double? = null): Double =
        dishing(rigiditiesAt(fraction, layerSpacing), T196_BUILDABLE_EDGE_X)
}

@Suppress("LongMethod")
fun main() {
    val profile = t196Profile(File("gpd/results/T-3b-tile-edge-load-profile.json"))
    val tile = T196FreeTile(profile)

    // ---------------------------------------------------------------- the cheap bound, first
    val geometry = tile.rigiditiesAt(0.0)
    val factor = geometry.parallelAxisFactor
    println("T-196 — the cheap bound, before any sweep")
    println("  parallel-axis factor (pure geometry, independent of f): %.6f".format(factor))
    println("  so the enhancement runs 1.0 at f = 0 to %.4f at f = 1, affine in f".format(factor))

    // ---------------------------------------------------------------- the sweep
    val fractions = listOf(
        0.0, 0.005, 0.01, 0.02, 0.03, 0.05, 0.075, 0.10, 0.15, 0.20,
        MeasuredBundleRigidity.COMPOSITE_FRACTION_MIN,
        MeasuredBundleRigidity.COMPOSITE_FRACTION,
        MeasuredBundleRigidity.COMPOSITE_FRACTION_MAX,
        0.50, 0.74, 1.0
    ).distinct().sorted()
    val samples = fractions.map { fraction ->
        val rigidities = tile.rigiditiesAt(fraction)
        val dishing = tile.dishing(rigidities, T196_BUILDABLE_EDGE_X)
        println(
            "  f = %.4f  enh %.4f  D_par %10.2f  D_perp %8.3f  dishing %.9f  %s".format(
                fraction, rigidities.realisedEnhancement, rigidities.alongHelixRigidity,
                rigidities.acrossHelixRigidity, dishing,
                if (dishing < T196_TOLERANCE) "flat" else "NOT flat"
            )
        )
        T196Sample(
            compositeFraction = fraction,
            realisedEnhancement = rigidities.realisedEnhancement,
            alongHelixRigidity = rigidities.alongHelixRigidity,
            acrossHelixRigidity = rigidities.acrossHelixRigidity,
            reachAlong = winklerBendingLength(
                rigidities.alongHelixRigidity, Gen1Tile.FOUNDATION_SECANT
            ),
            reachAcross = winklerBendingLength(
                rigidities.acrossHelixRigidity, Gen1Tile.FOUNDATION_SECANT
            ),
            freeTileDishingOverStroke = dishing,
            flat = dishing < T196_TOLERANCE
        )
    }

    // ---------------------------------------------------------------- the crossing
    val crossing = firstCrossing(0.0, 1.0, 40, T196_TOLERANCE, T196_BISECTION) {
        tile.dishingAtFraction(it)
    }
    val thresholdText = HashMap<String, String>()
    if (crossing == null) {
        thresholdText["verdict"] = "NO CROSSING in [0, 1]"
        thresholdText["meaning"] =
            "the free-tile dishing does not cross T-5b's 0.10 anywhere in the coupling range, " +
                    "so the flatness verdict does not depend on f at all"
        println("  NO CROSSING: the dishing does not reach %.2f anywhere in [0, 1]".format(
            T196_TOLERANCE
        ))
    } else {
        val f = crossing.root
        thresholdText["compositeFractionAtCrossing"] = f.emitted()
        thresholdText["bracketLow"] = crossing.bracketLow.emitted()
        thresholdText["bracketHigh"] = crossing.bracketHigh.emitted()
        thresholdText["signChanges"] = crossing.signChanges.toString()
        thresholdText["monotone"] = crossing.monotone.toString()
        thresholdText["enhancementAtCrossing"] =
            enhancementForFraction(f, factor).emitted()
        thresholdText["dishingAtCrossing"] = tile.dishingAtFraction(f).emitted()
        thresholdText["measuredBandLow"] = MeasuredBundleRigidity.COMPOSITE_FRACTION_MIN.emitted()
        thresholdText["marginAtTheBandLowEnd"] =
            (MeasuredBundleRigidity.COMPOSITE_FRACTION_MIN / f).emitted()
        thresholdText["marginAtTheBandCentre"] =
            (MeasuredBundleRigidity.COMPOSITE_FRACTION / f).emitted()
        println(
            "  CROSSING at f = %.9f (bracket %.9f..%.9f, %d sign change(s))".format(
                f, crossing.bracketLow, crossing.bracketHigh, crossing.signChanges
            )
        )
        println(
            "  the measured band's LOW end clears it by %.4fx, the centre by %.4fx".format(
                MeasuredBundleRigidity.COMPOSITE_FRACTION_MIN / f,
                MeasuredBundleRigidity.COMPOSITE_FRACTION / f
            )
        )
    }

    // ------------------------------------------------- CH-0124's geometry, its own threshold
    //
    // The true honeycomb array stacks its rows at `d*sqrt(3)/2`, which scales the second moment
    // by 3/4 and therefore moves `factor` — so the threshold in `f` is a DIFFERENT number on that
    // geometry, not the same one read at a different dishing. `CH-0124` is open, so both are
    // carried rather than one chosen.
    val trueSpacing = Gen1Tile.INTERHELICAL_HONEYCOMB * sqrt(3.0) / 2.0
    val trueFactor = tile.rigiditiesAt(0.0, trueSpacing).parallelAxisFactor
    val trueCrossing = firstCrossing(0.0, 1.0, 40, T196_TOLERANCE, T196_BISECTION) {
        tile.dishingAtFraction(it, trueSpacing)
    }
    println(
        "  CH-0124's true honeycomb spacing: factor %.6f, crossing at f = %s".format(
            trueFactor, trueCrossing?.root?.emitted() ?: "NONE"
        )
    )

    // ---------------------------------------------------------------- convergence
    val convergence = ArrayList<T196Convergence>()
    val meshResults = listOf(1, 2, 4).map { subdivisions ->
        T196FreeTile(profile, subdivisions).dishingAtFraction(
            MeasuredBundleRigidity.COMPOSITE_FRACTION
        )
    }
    convergence += T196Convergence(
        axis = "beam subdivisions per node interval, free-tile dishing at the measured f",
        values = listOf(1.0, 2.0, 4.0),
        results = meshResults,
        departure = abs(meshResults[2] - meshResults[1]) / abs(meshResults[2]),
        note = "nested refinements only — a subdivision of 3 moves a load off a node (CLAUDE.md)"
    )
    val sampleResults = listOf(41, 81, 161).map {
        T196FreeTile(profile, 2, it).dishingAtFraction(MeasuredBundleRigidity.COMPOSITE_FRACTION)
    }
    convergence += T196Convergence(
        axis = "dishing grid samples per side",
        values = listOf(41.0, 81.0, 161.0),
        results = sampleResults,
        departure = abs(sampleResults[2] - sampleResults[1]) / abs(sampleResults[2]),
        note = "peak dishing is a max over the grid, so it can only rise with resolution"
    )
    if (crossing != null) {
        val roots = listOf(1e-6, 1e-9, 1e-12).map {
            firstCrossing(0.0, 1.0, 40, T196_TOLERANCE, it) { f -> tile.dishingAtFraction(f) }!!
                .root
        }
        convergence += T196Convergence(
            axis = "bisection exit width on the fraction axis",
            values = listOf(1e-6, 1e-9, 1e-12),
            results = roots,
            departure = abs(roots[2] - roots[1]),
            note = "an ABSOLUTE departure in the fraction — the abscissa is dimensionless, and " +
                    "a bisection on a solved field has its own few-ulp noise floor"
        )
        val scanRoots = listOf(20, 40, 80).map {
            firstCrossing(0.0, 1.0, it, T196_TOLERANCE, T196_BISECTION) { f ->
                tile.dishingAtFraction(f)
            }!!.root
        }
        convergence += T196Convergence(
            axis = "scan resolution before the bisection",
            values = listOf(20.0, 40.0, 80.0),
            results = scanRoots,
            departure = abs(scanRoots[2] - scanRoots[1]),
            note = "refining the scan must NOT move the root: the bisection owns the precision, " +
                    "and a scan that changes the answer is a scan that missed a feature"
        )
    }

    // ---------------------------------------------------------------- reproductions
    val atMeasured = tile.dishingAtFraction(MeasuredBundleRigidity.COMPOSITE_FRACTION)
    val singleLayer = tile.dishing(tile.singleLayerRigidities(), 40.0)
    val reproductions = listOf(
        T196Reproduction(
            "C-0109", "four-layer free-tile dishing at the measured f", 0.0577199433,
            atMeasured, abs(atMeasured - 0.0577199433) / 0.0577199433, true
        ),
        T196Reproduction(
            "C-0109 / C-0063", "single-layer free-tile dishing", 0.307902368,
            singleLayer, abs(singleLayer - 0.307902368) / 0.307902368, false
        )
    )
    reproductions.forEach {
        println(
            "  reproduce %s %s: %.9f against %.9f, departure %.2e".format(
                it.source, it.quantity, it.reproduced, it.published, it.departure
            )
        )
    }

    // ---------------------------------------------------------------- the record
    val trueHoneycomb = tile.dishingAtFraction(
        MeasuredBundleRigidity.COMPOSITE_FRACTION,
        Gen1Tile.INTERHELICAL_HONEYCOMB * sqrt(3.0) / 2.0
    )
    val findings = HashMap<String, String>()
    findings["theCheapBoundRemovesTheSweep"] =
        ("f enters only through realised = 1 + f(factor - 1), and that one number multiplies " +
                "D_par and D_perp alike, so f is a pure SCALE on the plate and the threshold is " +
                "a scalar inversion rather than a two-dimensional search. factor = %.6f."
            ).format(factor)
    findings["trueHoneycombThreshold"] =
        if (trueCrossing == null) {
            "At the true honeycomb layer spacing d*sqrt(3)/2 the dishing does not cross 0.10 " +
                    "anywhere in [0, 1]."
        } else {
            ("At the true honeycomb layer spacing d*sqrt(3)/2 the parallel-axis factor is %s " +
                    "rather than %s, so the threshold is its OWN number: f = %s, cleared by the " +
                    "measured band's low end 0.26 by %sx. CH-0124's geometry is carried beside " +
                    "the default rather than instead of it, because the challenge is open."
                ).format(
                    trueFactor.emitted(), factor.emitted(), trueCrossing.root.emitted(),
                    (MeasuredBundleRigidity.COMPOSITE_FRACTION_MIN / trueCrossing.root).emitted()
                )
        }
    findings["trueHoneycombSpacing"] =
        ("At the true honeycomb layer spacing d*sqrt(3)/2 the same f gives %.9f, against %.9f " +
                "at C-0109's default spacing d — CH-0124's geometry, carried beside the default " +
                "rather than instead of it."
            ).format(trueHoneycomb, atMeasured)

    val result = T196Result(
        task = "T-196",
        leaf = "A8.2",
        title = "Where the four-layer tile stops being flat: the composite-fraction threshold",
        verificationType = "in-silico (beam-and-hinge grillage) + logical (the scale inversion)",
        maturity = "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated.",
        units = mapOf(
            "rigidity" to "pN*nm",
            "length" to "nm",
            "dishing" to "dimensionless, as a fraction of the free-tile stroke",
            "compositeFraction" to "dimensionless, 0 at INDEPENDENT and 1 at COMPOSITE"
        ),
        conventions = mapOf(
            "load" to "C-0022's solved collar at 2 mM / 10 nm / 0.192 V",
            "foundation" to "Gen1Tile.FOUNDATION_SECANT, C-0001's secant",
            "flat" to "peak dishing below T-5b's 0.10 convention",
            "tile" to "four honeycomb layers, 15 duplex rows, C-0086's buildable 38.08 nm width",
            "coupling" to "NONE — this is the uncoupled free tile throughout"
        ),
        parameters = mapOf(
            "duplexRows" to T196_DUPLEXES.toString(),
            "edgeX" to T196_BUILDABLE_EDGE_X.toString(),
            "rowBasePairs" to T196_BUILDABLE_ROW_BP.toString(),
            "dishingSamplesPerSide" to T196_SAMPLES.toString(),
            "tolerance" to T196_TOLERANCE.toString(),
            "bisectionExitWidth" to T196_BISECTION.toString(),
            "parallelAxisFactor" to factor.emitted(),
            "layerSpacingDefault" to Gen1Tile.INTERHELICAL_HONEYCOMB.emitted()
        ),
        citedInputs = listOf(
            "C-0109 — the four-layer tile, its calibration and its stated open threshold",
            "C-0022 — the solved edge collar this dishing is read under",
            "C-0086 — the buildable 38.08 nm seamless raster width",
            "MeasuredBundleRigidity — f = 0.26-0.33 from four measured bundles, DERIVED there",
            "CH-0124 — the true honeycomb layer spacing, carried beside the default"
        ),
        cheapBound = mapOf(
            "parallelAxisFactor" to factor.emitted(),
            "enhancementAtFractionZero" to "1.0",
            "enhancementAtFractionOne" to factor.emitted(),
            "whyItRemovesTheSweep" to findings["theCheapBoundRemovesTheSweep"]!!
        ),
        samples = samples,
        threshold = thresholdText,
        convergence = convergence,
        reproductions = reproductions,
        falsifiers = listOf(
            "F1 — the dishing is NOT monotone in f, so no threshold exists and the sweep must " +
                    "report the alternation instead. Tested by counting EVERY sign change over " +
                    "the whole interval rather than bisecting the first one found.",
            "F2 — the crossing lies ABOVE the measured band's low end 0.26, which would put " +
                    "C-0109's uncoupled verdict inside the measurement's own uncertainty.",
            "F3 — C-0109's own numbers do not reproduce, in which case no comparison against " +
                    "them is licensed."
        ),
        findings = findings,
        validity = listOf(
            "The body is a SMEARED equivalent sheet: OrigamiGrillage never reads `layers`, so " +
                    "the four-layer tile enters as one orthotropic sheet carrying the multi-layer " +
                    "rigidities. C-0109 asserts that reduction at 1e-12 and it is inherited here.",
            "RIGID-LIMIT READING. C-0093 found a buildable four-layer body reads 0.100166871 " +
                    "where its rigid limit reads 0.0344013403, so body rigidity is FIRST ORDER. " +
                    "This study sweeps the interlayer coupling of a smeared sheet and does not " +
                    "model a buildable body's own compliance.",
            "The measured f = 0.26-0.33 is calibrated on RODS — bundles crossovered around a " +
                    "closed ring. A 15-wide x 4-deep slab has a different crossover topology and " +
                    "a far larger second moment, so 0.30 is plausibly an UPPER bound there.",
            "The staple-dropout statistics are measured on a SINGLE-LAYER rectangle and this " +
                    "study is of the UNCOUPLED tile, so they do not enter at all.",
            "Kirchhoff: the four-layer thickness over span is 0.252, where transverse shear is " +
                    "not safe, so D_par is again an upper bound (C-0109's own caveat)."
        ),
        openQuestions = listOf(
            "What f a 15-wide x 4-deep SLAB realises, as against the measured rods. That is a " +
                    "simulation or a measurement, not a solve — and it is the one number this " +
                    "verdict now rests on.",
            "Whether a BUILDABLE four-layer body clears the threshold, which needs C-0093's " +
                    "Ritz body rather than a smeared sheet.",
            "The honeycomb raster width (T-198) and the attachment lattice of a four-layer top " +
                    "face, neither of which this study needs but both of which a design does."
        )
    )
    // Rounded at the SERIALISATION boundary so nothing can be emitted unrounded by omission,
    // and the departures are emitted at TWO significant digits because a convergence departure is
    // a dimensionless difference of two nearly equal numbers and `RESULT_ABSOLUTE_FLOOR` — a
    // claim in the LOCKED UNITS — does not reach it (`CLAUDE.md`, three recorded instances).
    val output = File("gpd/results/T-196-composite-fraction-threshold.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digits = 9,
                digitsByKey = mapOf("departure" to 2)
            ) as JsonObject)
        ) + "\n"
    )
    println("T-196 — wrote ${output.path}")
}
