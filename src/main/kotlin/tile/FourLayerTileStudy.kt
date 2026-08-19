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

import com.xemantic.nano.plentyofroom.anchoring.M13_SCAFFOLD_NUCLEOTIDES
import com.xemantic.nano.plentyofroom.coupling.CollarTerm
import com.xemantic.nano.plentyofroom.coupling.DropoutEnsemble
import com.xemantic.nano.plentyofroom.coupling.InfluenceSurrogate
import com.xemantic.nano.plentyofroom.coupling.IncorporationConvention
import com.xemantic.nano.plentyofroom.coupling.IncorporationField
import com.xemantic.nano.plentyofroom.coupling.StapleDropoutLiterature
import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.coupling.columnsForRunRobustness
import com.xemantic.nano.plentyofroom.coupling.dropoutDishingSample
import com.xemantic.nano.plentyofroom.coupling.dropoutEnsemble
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.coupling.latticeInfluenceSurrogate
import com.xemantic.nano.plentyofroom.coupling.longestAbsenceRun
import com.xemantic.nano.plentyofroom.coupling.measuredDepthIncorporation
import com.xemantic.nano.plentyofroom.coupling.orderStatistic
import com.xemantic.nano.plentyofroom.coupling.summariseDropoutDishing
import com.xemantic.nano.plentyofroom.coupling.uniformIncorporation
import com.xemantic.nano.plentyofroom.coupling.winklerBendingLength
import com.xemantic.nano.plentyofroom.coupling.worstSinglePathRemoval
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.DEPARTURE_DIGITS_BY_KEY
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.OrigamiSheet
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.origamiSheet
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
import kotlin.math.sqrt

/**
 * `T-191` — the tile §3 actually specifies: a four-layer, ~10 nm body.
 *
 * §3's parameter row says *"Tile thickness ~10 nm (single-layer honeycomb)"*. The `actuator`
 * and `electrostatics` packages resolve that toward **10 nm**; every structural claim resolves
 * it toward **2 nm**, and the flatness negative was derived on the thin one. `C-0086` measures
 * the thin sheet at 1 680 of M13's 7 249 nt, and NDI's answer to decision 5 says *"just make
 * the tile thicker"*.
 *
 * This study re-runs `C-0087`/`C-0089`'s dropout grading on the thick tile, over the whole
 * interlayer-coupling bracket, at `C-0086`'s buildable 38.08 nm width — and reproduces the
 * single-layer baseline first, so that the comparison is licensed.
 */

// ---------------------------------------------------------------------------------------------
// the constants
// ---------------------------------------------------------------------------------------------

/** `C-0086`'s nearest buildable seamless raster width, in nm. */
private const val T191_BUILDABLE_EDGE_X: Double = 38.08

/** `C-0086`'s row length in base pairs at that width. */
private const val T191_BUILDABLE_ROW_BP: Int = 112

/** The nominal §3 width the standing flatness studies run at, in nm. */
private const val T191_NOMINAL_EDGE_X: Double = 40.0

/** `C-0086`'s row length in base pairs at the nominal width. */
private const val T191_NOMINAL_ROW_BP: Int = 118

/** The duplex rows, `C-0026`'s one attachment row per duplex. */
private const val T191_DUPLEXES: Int = 15

/** `C-0087`'s grading seed, so its cells reproduce here. */
private const val T191_SEED: Long = 20260817L

/** The graded realisations per cell. */
private const val T191_REALISATIONS: Int = 10000

/** The realisation counts the sampling axis is reported at, as fractions of the graded count. */
private fun t191RealisationLevels(realisations: Int): List<Int> =
    listOf(realisations / 4, realisations / 2, realisations)

/** `T-5b`'s flatness CONVENTION. */
private const val T191_TOLERANCE: Double = 0.10

/** The dishing grid, 81 as everywhere upstream. */
private const val T191_SAMPLES: Int = 81

/** `C-0058`'s rim mask width in nm. */
private const val T191_COLLAR: Double = 6.7

/** `C-0022`'s rim standoff in nm. */
private const val T191_RIM_STANDOFF: Double = 1.0

/** `C-0017`'s mandate as a SUM, in pN/nm. */
private val T191_MANDATE: Double = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

/** The column counts of the density sweep. */
private val T191_COLUMN_SWEEP: List<Int> = listOf(1, 2, 3, 4, 6)

/** The absence run the run-robustness demand is quoted at — `C-0089`'s 90th-percentile 3. */
private const val T191_WORST_RUN: Int = 3

/** Decision precision, `CLAUDE.md`'s six significant digits. */
private const val T191_DECISION_DIGITS: Int = 6

/** Decision floor, in the locked units. */
private const val T191_DECISION_FLOOR: Double = 1e-9

// ---------------------------------------------------------------------------------------------
// the records
// ---------------------------------------------------------------------------------------------

/** One candidate tile, as rigidities and the reaches they imply. */
@Serializable
private data class T191TileRecord(
    val name: String,
    val layers: Int,
    val coupling: String,
    val interhelicalDistance: Double,
    val layerSpacing: Double,
    val compositeFraction: Double,
    val crossoverSpacingBasePairs: Double,
    val thickness: Double,
    val alongHelixRigidity: Double,
    val acrossHelixRigidity: Double,
    val twistingRigidity: Double,
    val anisotropy: Double,
    val parallelAxisFactor: Double,
    val reachAlongHelices: Double,
    val reachAcrossHelices: Double,
    val edgeX: Double,
    val edgeY: Double,
    val columnsDemandedByRunOfThree: Int,
    val pathsDemandedByRunOfThree: Int,
    val rowsDemandedByRunOfThree: Int,
    val freeStroke: Double,
    val freeTileDishingOverStroke: Double
)

/** One cheap bound, settled before any lattice is assembled. */
@Serializable
private data class T191BoundRecord(
    val name: String,
    val value: Double,
    val unit: String,
    val settles: String
)

/** The scaffold arithmetic for one candidate body. */
@Serializable
private data class T191ScaffoldRecord(
    val name: String,
    val layers: Int,
    val rows: Int,
    val basePairsPerRow: Int,
    val widthNanometres: Double,
    val nucleotides: Long,
    val scaffoldNucleotides: Long,
    val overhang: Double,
    val fitsOneM13: Boolean,
    val layersAffordable: Int
)

/** One graded (tile × path count × distribution) cell. */
@Serializable
private data class T191DishingRecord(
    val tile: String,
    val edgeX: Double,
    val columns: Int,
    val pathCount: Int,
    val convention: String,
    val distribution: String,
    val nominalOverStroke: Double,
    val worstSingleRemovalOverStroke: Double,
    val singleRemovalAmplification: Double,
    val medianOverStroke: Double,
    val p90OverStroke: Double,
    val p95OverStroke: Double,
    val worstOverStroke: Double,
    val exceedance: Double,
    val exceedanceStandardError: Double,
    /**
     * The one-sided Clopper-Pearson limit where [exceedance] saturates, else `null` — `T-213`.
     *
     * `CH-0153`: at `p̂ = 1` the symmetric [exceedanceStandardError] is identically zero for every
     * sample count, and this study's headline is a design that **fails**, which is exactly the
     * direction that saturates it. Emitted **beside** the symmetric error rather than replacing
     * it: the symmetric error is uninformative rather than wrong.
     */
    val exceedanceOneSidedBound: Double?,
    val meanSurvivors: Double,
    val flatAtNominal: Boolean,
    val flatAtMedian: Boolean,
    val flatAtP90: Boolean
)

/** One convergence axis. */
@Serializable
private data class T191ConvergenceRecord(
    val axis: String,
    val levels: List<String>,
    val values: List<Double>,
    val departure: Double,
    val settles: String
)

/** One reproduction of a published number. */
@Serializable
private data class T191ReproductionRecord(
    val name: String,
    val published: Double,
    val reproduced: Double,
    val relativeDeparture: Double,
    val source: String
)

/** One acceptance predicate or declared falsifier. */
@Serializable
private data class T191PredicateRecord(
    val name: String,
    val statement: String,
    val verdict: String
)

@Serializable
private data class T191Result(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val maturity: String,
    val conditions: String,
    val decision: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val bounds: List<T191BoundRecord>,
    val tiles: List<T191TileRecord>,
    val scaffold: List<T191ScaffoldRecord>,
    val dishing: List<T191DishingRecord>,
    val convergence: List<T191ConvergenceRecord>,
    val reproductions: List<T191ReproductionRecord>,
    val predicates: List<T191PredicateRecord>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val sources: List<String>,
    val parameters: Map<String, String>
)

// ---------------------------------------------------------------------------------------------
// the inputs, read from the claims that own them
// ---------------------------------------------------------------------------------------------

private class T191Profile(
    val name: String,
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {
    fun field(interiorPressure: Double, edgeX: Double, lengthY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, edgeX, lengthY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T191_RIM_STANDOFF))
        )
}

/** `C-0022`'s solved profile, keyed on `(concentration, gap, bias)` — `CLAUDE.md`'s gotcha. */
private fun t191Profile(file: File, key: Triple<Double, Double, Double>): T191Profile {
    require(file.exists()) {
        "C-0022's result file is missing: ${file.path}. T-191 consumes the SOLVED edge profile."
    }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .firstOrNull {
            fun value(name: String) = it.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == key.first && value("gapHeight") == key.second &&
                    value("appliedBias") == key.third
        } ?: error("no C-0022 profile at ${key.first} mM, ${key.second} nm, ${key.third} V")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T191Profile(
        name = "%.1f mM, %.0f nm, %.3f V".format(key.first, key.second, key.third),
        smoothDepth = value("taperDepth"),
        smoothWidth = value("taperWidth"),
        rimDepth = value("rimResidualDepth")
    )
}

// ---------------------------------------------------------------------------------------------
// the candidate tiles
// ---------------------------------------------------------------------------------------------

/** One candidate tile: its rigidities, its smeared equivalent sheet and its footprint. */
private class T191Tile(
    val name: String,
    val rigidities: MultiLayerRigidities,
    val edgeX: Double,
    val rowBasePairs: Int
) {
    val sheet: OrigamiSheet = equivalentSheet(rigidities)
    val edgeY: Double = T191_DUPLEXES * rigidities.interhelicalDistance

    /**
     * The largest centred column layout that fits, at the lattice's own `p/2` pitch.
     *
     * `T-10`'s construction, not `T-14`'s phase sweep: `CrossoverLayout`'s two-parity
     * alternation is the **square** lattice's, and a honeycomb helix has three crossover
     * azimuths, so a phase is not a design variable this machinery can represent.
     */
    val columns: CrossoverLayout
        get() {
            val pitch = sheet.crossoverSpacing / 2.0
            val usable = edgeX - 2.0 * CrossoverLayout.EDGE_MARGIN
            val count = floor(usable / pitch).toInt() + 1
            return CrossoverLayout.centred(count, pitch)
        }

    fun lattice(): OrigamiGrillage = OrigamiGrillage(
        sheet = sheet,
        lengthX = edgeX,
        beamCount = T191_DUPLEXES,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        columns = columns,
        subdivisions = 2
    )
}

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    val realisations =
        System.getProperty("t191.realisations")?.toInt() ?: T191_REALISATIONS

    println("T-191 — the cheap bound runs first, and it is already in the corpus ...")

    val baselineRigidities = multiLayerRigidities(
        layers = 1,
        interhelicalDistance = Gen1Tile.INTERHELICAL_SHEET,
        crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_SHEET_BP,
        coupling = LayerCoupling.INDEPENDENT
    )
    fun fourLayer(coupling: LayerCoupling) = multiLayerRigidities(
        layers = 4,
        interhelicalDistance = Gen1Tile.INTERHELICAL_HONEYCOMB,
        crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
        coupling = coupling
    )

    val tiles = listOf(
        T191Tile(
            "single-layer square-lattice sheet at the NOMINAL 40.0 nm (the flatness negative's own tile)",
            baselineRigidities, T191_NOMINAL_EDGE_X, T191_NOMINAL_ROW_BP
        ),
        T191Tile(
            "single-layer square-lattice sheet at C-0086's buildable 38.08 nm",
            baselineRigidities, T191_BUILDABLE_EDGE_X, T191_BUILDABLE_ROW_BP
        ),
        T191Tile(
            "four-layer honeycomb, INDEPENDENT layers (the lower bracket end)",
            fourLayer(LayerCoupling.INDEPENDENT), T191_BUILDABLE_EDGE_X, T191_BUILDABLE_ROW_BP
        ),
        T191Tile(
            "four-layer honeycomb, CALIBRATED at the measured f = 0.30 (THE HEADLINE READING)",
            fourLayer(LayerCoupling.CALIBRATED), T191_BUILDABLE_EDGE_X, T191_BUILDABLE_ROW_BP
        ),
        T191Tile(
            "four-layer honeycomb, COMPOSITE in both directions (the upper bracket end)",
            fourLayer(LayerCoupling.COMPOSITE), T191_BUILDABLE_EDGE_X, T191_BUILDABLE_ROW_BP
        ),
        T191Tile(
            "four-layer honeycomb, C-0006's ALONG_HELICES_ONLY variant (a MIXED state, NOT graded)",
            fourLayer(LayerCoupling.ALONG_HELICES_ONLY), T191_BUILDABLE_EDGE_X,
            T191_BUILDABLE_ROW_BP
        ),
        T191Tile(
            "four-layer honeycomb, CALIBRATED at the measured band's low end f = 0.26 (NOT graded)",
            multiLayerRigidities(
                layers = 4,
                interhelicalDistance = Gen1Tile.INTERHELICAL_HONEYCOMB,
                crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
                coupling = LayerCoupling.CALIBRATED,
                compositeFraction = MeasuredBundleRigidity.COMPOSITE_FRACTION_MIN
            ),
            T191_BUILDABLE_EDGE_X, T191_BUILDABLE_ROW_BP
        ),
        T191Tile(
            "four-layer TRUE-HONEYCOMB layer spacing d*sqrt(3)/2, CALIBRATED f = 0.30 (NOT graded)",
            multiLayerRigidities(
                layers = 4,
                interhelicalDistance = Gen1Tile.INTERHELICAL_HONEYCOMB,
                crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
                coupling = LayerCoupling.CALIBRATED,
                layerSpacing = Gen1Tile.INTERHELICAL_HONEYCOMB * sqrt(3.0) / 2.0
            ),
            T191_BUILDABLE_EDGE_X, T191_BUILDABLE_ROW_BP
        ),
        T191Tile(
            "four-layer honeycomb, CALIBRATED at the measured band's high end f = 0.33 (NOT graded)",
            multiLayerRigidities(
                layers = 4,
                interhelicalDistance = Gen1Tile.INTERHELICAL_HONEYCOMB,
                crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
                coupling = LayerCoupling.CALIBRATED,
                compositeFraction = MeasuredBundleRigidity.COMPOSITE_FRACTION_MAX
            ),
            T191_BUILDABLE_EDGE_X, T191_BUILDABLE_ROW_BP
        )
    )

    /** The tiles the dropout grading is run on — the band ends and the mixed state are not. */
    val gradedNames = tiles.take(5).map { it.name }.toSet()

    val loadFile = File("gpd/results/T-3b-tile-edge-load-profile.json")
    val designProfile = t191Profile(loadFile, Triple(2.0, 10.0, 0.192))

    // ------------------------------------------------------------------ per-tile fields
    class T191Solved(val tile: T191Tile) {
        val interiorPressure = Gen1Tile.TARGET_FORCE / (tile.edgeX * tile.edgeY)
        val field = designProfile.field(interiorPressure, tile.edgeX, tile.edgeY)
        val freeStroke = PlateOnFoundation(
            tile.sheet.plate(tile.edgeX, tile.edgeY), Gen1Tile.FOUNDATION_SECANT, emptyList(), 12
        ).solve(uniformPressure(interiorPressure)).meanDeflection
        val lattice = tile.lattice()
        val freeDishing = lattice.solve(field).peakDishing(T191_SAMPLES) / freeStroke
        val reachAlong = winklerBendingLength(
            tile.rigidities.alongHelixRigidity, Gen1Tile.FOUNDATION_SECANT
        )
        val reachAcross = winklerBendingLength(
            tile.rigidities.acrossHelixRigidity, Gen1Tile.FOUNDATION_SECANT
        )
    }

    val solved = tiles.map { T191Solved(it) }
    solved.forEach {
        println(
            "  %s: D_par %.2f, D_perp %.3f, ell %.2f / %.2f nm, free dishing %.4f".format(
                it.tile.name, it.tile.rigidities.alongHelixRigidity,
                it.tile.rigidities.acrossHelixRigidity, it.reachAlong, it.reachAcross,
                it.freeDishing
            )
        )
    }

    val tileRecords = solved.map { s ->
        T191TileRecord(
            name = s.tile.name,
            layers = s.tile.rigidities.layers,
            coupling = s.tile.rigidities.coupling,
            interhelicalDistance = s.tile.rigidities.interhelicalDistance,
            layerSpacing = s.tile.rigidities.layerSpacing,
            compositeFraction = s.tile.rigidities.compositeFraction,
            crossoverSpacingBasePairs = s.tile.rigidities.crossoverSpacing /
                    Gen1Tile.RISE_PER_BASE_PAIR,
            thickness = s.tile.rigidities.thickness,
            alongHelixRigidity = s.tile.rigidities.alongHelixRigidity,
            acrossHelixRigidity = s.tile.rigidities.acrossHelixRigidity,
            twistingRigidity = s.tile.rigidities.twistingRigidity,
            anisotropy = s.tile.rigidities.anisotropy,
            parallelAxisFactor = s.tile.rigidities.parallelAxisFactor,
            reachAlongHelices = s.reachAlong,
            reachAcrossHelices = s.reachAcross,
            edgeX = s.tile.edgeX,
            edgeY = s.tile.edgeY,
            columnsDemandedByRunOfThree =
                columnsForRunRobustness(s.tile.edgeX, s.reachAlong, T191_WORST_RUN),
            pathsDemandedByRunOfThree =
                columnsForRunRobustness(s.tile.edgeX, s.reachAlong, T191_WORST_RUN) *
                        T191_DUPLEXES,
            rowsDemandedByRunOfThree =
                columnsForRunRobustness(s.tile.edgeY, s.reachAcross, T191_WORST_RUN),
            freeStroke = s.freeStroke,
            freeTileDishingOverStroke = s.freeDishing
        )
    }

    // ------------------------------------------------------------------ the scaffold arithmetic
    println("T-191 — the scaffold arithmetic ...")
    val scaffoldRecords = listOf(1, 2, 3, 4, 5).map { layers ->
        val used = scaffoldNucleotides(layers, T191_DUPLEXES, T191_BUILDABLE_ROW_BP)
        T191ScaffoldRecord(
            name = "$layers honeycomb layer(s) at C-0086's buildable 112 bp row",
            layers = layers,
            rows = T191_DUPLEXES,
            basePairsPerRow = T191_BUILDABLE_ROW_BP,
            widthNanometres = T191_BUILDABLE_EDGE_X,
            nucleotides = used,
            scaffoldNucleotides = M13_SCAFFOLD_NUCLEOTIDES,
            overhang = M13_SCAFFOLD_NUCLEOTIDES.toDouble() / used.toDouble(),
            fitsOneM13 = used <= M13_SCAFFOLD_NUCLEOTIDES,
            layersAffordable = layersAffordable(
                M13_SCAFFOLD_NUCLEOTIDES, T191_DUPLEXES, T191_BUILDABLE_ROW_BP
            )
        )
    } + listOf(4).map { layers ->
        val used = scaffoldNucleotides(layers, T191_DUPLEXES, T191_NOMINAL_ROW_BP)
        T191ScaffoldRecord(
            name = "$layers honeycomb layer(s) at the NOMINAL 118 bp row",
            layers = layers,
            rows = T191_DUPLEXES,
            basePairsPerRow = T191_NOMINAL_ROW_BP,
            widthNanometres = T191_NOMINAL_EDGE_X,
            nucleotides = used,
            scaffoldNucleotides = M13_SCAFFOLD_NUCLEOTIDES,
            overhang = M13_SCAFFOLD_NUCLEOTIDES.toDouble() / used.toDouble(),
            fitsOneM13 = used <= M13_SCAFFOLD_NUCLEOTIDES,
            layersAffordable = layersAffordable(
                M13_SCAFFOLD_NUCLEOTIDES, T191_DUPLEXES, T191_NOMINAL_ROW_BP
            )
        )
    }

    // ------------------------------------------------------------------ the cheap bounds
    val baseline = solved.first()
    val independent = solved[2]
    val calibrated = solved[3]
    val composite = solved[4]
    val mixed = solved[5]
    val bounds = listOf(
        T191BoundRecord(
            "the four-layer parallel-axis factor, one number for BOTH directions",
            composite.tile.rigidities.parallelAxisFactor, "dimensionless",
            "1 + S sum(y^2)/(n B); k_s/k_theta = S/B under Chen et al.'s construction, so the " +
                    "anisotropy of a multi-layer sheet does not depend on the interlayer coupling"
        ),
        T191BoundRecord(
            "the along-helix reach of the single-layer tile",
            baseline.reachAlong, "nm",
            "C-0089's bendingLengthAlongHelices, reproduced: 12.82908 nm"
        ),
        T191BoundRecord(
            "the along-helix reach of the four-layer tile, INDEPENDENT layers",
            independent.reachAlong, "nm",
            "the lower bracket end; the demand it implies is the pessimistic one"
        ),
        T191BoundRecord(
            "the along-helix reach of the four-layer tile, CALIBRATED at the measured f = 0.30",
            calibrated.reachAlong, "nm",
            "THE HEADLINE READING: four measured origami bundles, two lattices, three " +
                    "laboratories put f at 0.26-0.33"
        ),
        T191BoundRecord(
            "the along-helix reach of the four-layer tile, COMPOSITE",
            composite.reachAlong, "nm",
            "the upper bracket end; F3 is graded over the bracket, never at one end"
        ),
        T191BoundRecord(
            "the column demand a run of three absences puts on the single-layer 40 nm tile",
            columnsForRunRobustness(T191_NOMINAL_EDGE_X, baseline.reachAlong, T191_WORST_RUN)
                .toDouble(),
            "columns",
            "C-0089's 13, reproduced before any four-layer number is emitted"
        ),
        T191BoundRecord(
            "the column demand on the four-layer tile, INDEPENDENT layers",
            columnsForRunRobustness(T191_BUILDABLE_EDGE_X, independent.reachAlong, T191_WORST_RUN)
                .toDouble(),
            "columns",
            "the pessimistic end of what the thicker tile buys"
        ),
        T191BoundRecord(
            "the column demand on the four-layer tile, CALIBRATED at the measured f = 0.30",
            columnsForRunRobustness(T191_BUILDABLE_EDGE_X, calibrated.reachAlong, T191_WORST_RUN)
                .toDouble(),
            "columns",
            "the headline: what the measured interlayer coupling buys against C-0089's 13"
        ),
        T191BoundRecord(
            "the column demand on the four-layer tile, COMPOSITE",
            columnsForRunRobustness(T191_BUILDABLE_EDGE_X, composite.reachAlong, T191_WORST_RUN)
                .toDouble(),
            "columns",
            "the optimistic end"
        ),
        T191BoundRecord(
            "the measured composite fraction of a real crossover-linked bundle",
            MeasuredBundleRigidity.COMPOSITE_FRACTION, "dimensionless",
            "Kauert 2011 (4HB square, 6HB honeycomb), Pfitzner 2013 (6HB), Wang 2012 (6HB " +
                    "tile): f = 0.26-0.33, so RIGID over-predicts EI by ~3x"
        ),
        T191BoundRecord(
            "the layers of a 15 x 112 bp raster one circular M13 pays for",
            layersAffordable(M13_SCAFFOLD_NUCLEOTIDES, T191_DUPLEXES, T191_BUILDABLE_ROW_BP)
                .toDouble(),
            "layers",
            "F4: below 4 and the tile NDI describes cannot be folded from one scaffold"
        )
    )

    // ------------------------------------------------------------------ the grading
    println("T-191 — grading %d realisations per cell ...".format(realisations))

    class T191Cell(
        val tile: T191Tile,
        val columns: Int,
        val surrogate: InfluenceSurrogate,
        val ensemble: DropoutEnsemble,
        val freeStroke: Double,
        val convention: String
    )

    val dishingRecords = ArrayList<T191DishingRecord>()

    fun grade(cell: T191Cell, label: String, stiffnesses: List<Double>): T191DishingRecord {
        val nominal = cell.surrogate.solve(stiffnesses).peakDishing / cell.freeStroke
        val sample = dropoutDishingSample(cell.surrogate, stiffnesses, cell.ensemble)
        sample.indices.forEach { sample[it] = sample[it] / cell.freeStroke }
        val summary = summariseDropoutDishing(
            sample, nominal, cell.ensemble.meanSurvivors, T191_TOLERANCE
        )
        val worstSingle =
            worstSinglePathRemoval(cell.surrogate, stiffnesses) / cell.freeStroke
        val record = T191DishingRecord(
            tile = cell.tile.name,
            edgeX = cell.tile.edgeX,
            columns = cell.columns,
            pathCount = cell.surrogate.pathCount,
            convention = cell.convention,
            distribution = label,
            nominalOverStroke = nominal,
            worstSingleRemovalOverStroke = worstSingle,
            singleRemovalAmplification = worstSingle / nominal,
            medianOverStroke = summary.median,
            p90OverStroke = summary.p90,
            p95OverStroke = summary.p95,
            worstOverStroke = summary.worst,
            exceedance = summary.exceedance,
            exceedanceStandardError = summary.exceedanceStandardError,
            exceedanceOneSidedBound = summary.exceedanceOneSidedBound,
            meanSurvivors = summary.meanSurvivors,
            flatAtNominal = nominal < T191_TOLERANCE,
            flatAtMedian = summary.flatAtMedian,
            flatAtP90 = summary.flatAtP90
        )
        dishingRecords += record
        return record
    }

    // The UNCOUPLED tile is the reference CLAUDE.md insists on, and under dropout it is
    // DETERMINISTIC: there are no attachments to lose, so every order statistic is the nominal.
    solved.filter { it.tile.name in gradedNames }.forEach { s ->
        dishingRecords += T191DishingRecord(
            tile = s.tile.name,
            edgeX = s.tile.edgeX,
            columns = 0,
            pathCount = 0,
            convention = "NONE",
            distribution = "NO COUPLING AT ALL (the reference)",
            nominalOverStroke = s.freeDishing,
            worstSingleRemovalOverStroke = s.freeDishing,
            singleRemovalAmplification = 1.0,
            medianOverStroke = s.freeDishing,
            p90OverStroke = s.freeDishing,
            p95OverStroke = s.freeDishing,
            worstOverStroke = s.freeDishing,
            exceedance = if (s.freeDishing > T191_TOLERANCE) 1.0 else 0.0,
            exceedanceStandardError = 0.0,
            // A DETERMINISTIC exceedance: the uncoupled tile has no
            // attachments to lose, so there is no sample to bound.
            exceedanceOneSidedBound = null,
            meanSurvivors = 0.0,
            flatAtNominal = s.freeDishing < T191_TOLERANCE,
            flatAtMedian = s.freeDishing < T191_TOLERANCE,
            flatAtP90 = s.freeDishing < T191_TOLERANCE
        )
    }

    val cellsByTile = LinkedHashMap<String, MutableList<T191Cell>>()
    solved.filter { it.tile.name in gradedNames }.forEach { s ->
        val field: IncorporationField =
            measuredDepthIncorporation(s.tile.edgeX, s.tile.edgeY)
        val lattice = s.lattice
        T191_COLUMN_SWEEP.forEach { columns ->
            val stations = attachmentGrid(columns, T191_DUPLEXES, s.tile.edgeX, s.tile.edgeY)
            println("  bank: %s, %d x %d".format(s.tile.name, columns, T191_DUPLEXES))
            val probabilities = stations.map { (x, y) -> field.at(x, y) }
            val cell = T191Cell(
                tile = s.tile,
                columns = columns,
                surrogate = latticeInfluenceSurrogate(lattice, stations, s.field, T191_SAMPLES),
                ensemble = dropoutEnsemble(probabilities, realisations, T191_SEED),
                freeStroke = s.freeStroke,
                convention = IncorporationConvention.MEASURED_DEPTH.name
            )
            cellsByTile.getOrPut(s.tile.name) { ArrayList() } += cell
            grade(cell, "EQUAL", List(cell.surrogate.pathCount) {
                T191_MANDATE / cell.surrogate.pathCount
            })
        }
    }

    // The UNIFORM convention on one cell per tile, so the transfer assumption is visible.
    solved.filter { it.tile.name in gradedNames }.forEach { s ->
        val stations = attachmentGrid(3, T191_DUPLEXES, s.tile.edgeX, s.tile.edgeY)
        val uniform = uniformIncorporation(StapleDropoutLiterature.INCORPORATION_MEAN)
        val cell = T191Cell(
            tile = s.tile,
            columns = 3,
            surrogate = latticeInfluenceSurrogate(s.lattice, stations, s.field, T191_SAMPLES),
            ensemble = dropoutEnsemble(
                stations.map { (x, y) -> uniform.at(x, y) }, realisations, T191_SEED
            ),
            freeStroke = s.freeStroke,
            convention = IncorporationConvention.UNIFORM.name
        )
        grade(cell, "EQUAL", List(cell.surrogate.pathCount) {
            T191_MANDATE / cell.surrogate.pathCount
        })
    }

    // ------------------------------------------------------------------ convergence
    println("T-191 — the convergence axes ...")
    val convergenceCell = cellsByTile.getValue(calibrated.tile.name)
        .first { it.columns == 3 }
    val equalAt = List(convergenceCell.surrogate.pathCount) {
        T191_MANDATE / convergenceCell.surrogate.pathCount
    }
    val realisationLevels = t191RealisationLevels(realisations)
    val samplingValues = realisationLevels.map { level ->
        val trimmed = dropoutEnsemble(
            convergenceCell.ensemble.probabilities, level, T191_SEED
        )
        val sample = dropoutDishingSample(convergenceCell.surrogate, equalAt, trimmed)
        sample.indices.forEach { sample[it] = sample[it] / convergenceCell.freeStroke }
        orderStatistic(sample, 0.90)
    }

    val subdivisionLevels = listOf(1, 2, 4)
    val subdivisionValues = subdivisionLevels.map { subdivisions ->
        val lattice = OrigamiGrillage(
            sheet = calibrated.tile.sheet,
            lengthX = calibrated.tile.edgeX,
            beamCount = T191_DUPLEXES,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            columns = calibrated.tile.columns,
            subdivisions = subdivisions
        )
        lattice.solve(calibrated.field).peakDishing(T191_SAMPLES) / calibrated.freeStroke
    }

    val convergence = listOf(
        T191ConvergenceRecord(
            axis = "dropout realisations, on the four-layer CALIBRATED 3 x 15 cell",
            levels = realisationLevels.map { "$it" },
            values = samplingValues,
            departure = abs(samplingValues.last() - samplingValues[samplingValues.size - 2]) /
                    samplingValues.last(),
            settles = "the 90th percentile of a 10 000-realisation sample"
        ),
        T191ConvergenceRecord(
            axis = "beam subdivisions per node interval, free-tile dishing over the stroke",
            levels = subdivisionLevels.map { "$it" },
            values = subdivisionValues,
            departure = abs(subdivisionValues.last() - subdivisionValues[1]) /
                    subdivisionValues.last(),
            settles = "nested refinement only — CLAUDE.md: 1/2/4, never 1/2/3/4"
        )
    )

    // ------------------------------------------------------------------ reproductions
    val reproductions = listOf(
        T191ReproductionRecord(
            "C-0089's along-helix Winkler bending length", 12.82908, baseline.reachAlong,
            abs(baseline.reachAlong / 12.82908 - 1.0),
            "gpd/results/T-155-dropout-robust-placement.json, parameters"
        ),
        T191ReproductionRecord(
            "C-0089's across-helix Winkler bending length", 5.70561, baseline.reachAcross,
            abs(baseline.reachAcross / 5.70561 - 1.0),
            "gpd/results/T-155-dropout-robust-placement.json, parameters"
        ),
        T191ReproductionRecord(
            "C-0006's four-layer D_par", 14310.78,
            mixed.tile.rigidities.alongHelixRigidity,
            abs(mixed.tile.rigidities.alongHelixRigidity / 14310.78 - 1.0),
            "C-0006 variant table, four-layer honeycomb rigid coupling"
        ),
        T191ReproductionRecord(
            "C-0006's four-layer D_perp", 19.222,
            mixed.tile.rigidities.acrossHelixRigidity,
            abs(mixed.tile.rigidities.acrossHelixRigidity / 19.222 - 1.0),
            "C-0006 variant table, four-layer honeycomb rigid coupling"
        ),
        T191ReproductionRecord(
            "C-0086's single-layer scaffold consumption", 1680.0,
            scaffoldNucleotides(1, T191_DUPLEXES, T191_BUILDABLE_ROW_BP).toDouble(),
            abs(
                scaffoldNucleotides(1, T191_DUPLEXES, T191_BUILDABLE_ROW_BP).toDouble() / 1680.0
                        - 1.0
            ),
            "gpd/results/T-151-scaffold-routing.json, parameters.sheetNucleotides"
        ),
        T191ReproductionRecord(
            "C-0058's equal-spring 3 x 15 NOMINAL dishing on the single-layer 40 nm tile", 0.2182,
            dishingRecords.first {
                it.tile == tiles.first().name && it.columns == 3 &&
                        it.convention == IncorporationConvention.MEASURED_DEPTH.name
            }.nominalOverStroke,
            abs(
                dishingRecords.first {
                    it.tile == tiles.first().name && it.columns == 3 &&
                            it.convention == IncorporationConvention.MEASURED_DEPTH.name
                }.nominalOverStroke / 0.2182 - 1.0
            ),
            "C-0058/C-0063: the equal-spring 45-path grid at zero defects"
        ),
        T191ReproductionRecord(
            "C-0022/C-0063's FREE-TILE dishing on the nominal single-layer host", 0.3079,
            baseline.freeDishing, abs(baseline.freeDishing / 0.3079 - 1.0),
            "C-0063 Deliverable table, 'NONE - free tile', and its gate 5"
        ),
        T191ReproductionRecord(
            "C-0089's equal-spring p90 at 45 paths on the nominal single-layer tile", 0.6142,
            dishingRecords.first {
                it.tile == tiles.first().name && it.columns == 3 &&
                        it.convention == IncorporationConvention.MEASURED_DEPTH.name
            }.p90OverStroke,
            abs(
                dishingRecords.first {
                    it.tile == tiles.first().name && it.columns == 3 &&
                            it.convention == IncorporationConvention.MEASURED_DEPTH.name
                }.p90OverStroke / 0.6142 - 1.0
            ),
            "gpd/results/T-155-dropout-robust-placement.json, designs, EQUAL / MEASURED_DEPTH"
        )
    )

    // ------------------------------------------------------------------ predicates
    fun tileRecord(name: String) = tileRecords.first { it.name == name }
    fun cells(tileName: String) = dishingRecords.filter {
        it.tile == tileName && it.convention == IncorporationConvention.MEASURED_DEPTH.name
    }
    fun uncoupled(tileName: String) =
        dishingRecords.first { it.tile == tileName && it.convention == "NONE" }

    val fourLayerNames = tiles.drop(2).map { it.name }.filter { it in gradedNames }
    val bestFourLayerP90 = fourLayerNames.flatMap { cells(it) }.minOf { it.p90OverStroke }
    val bestBaselineP90 = cells(tiles.first().name).minOf { it.p90OverStroke }
    val anyFourLayerFlat = fourLayerNames.flatMap { cells(it) }.any { it.flatAtP90 }
    val calibratedName = tiles[3].name
    val calibratedBestP90 = cells(calibratedName).minOf { it.p90OverStroke }
    val calibratedBestNominal = cells(calibratedName).minOf { it.nominalOverStroke }
    val calibratedFree = uncoupled(calibratedName).nominalOverStroke
    val uncoupledFourLayerFlat = fourLayerNames.all { uncoupled(it).flatAtP90 }
    val fourLayerFits = scaffoldRecords.first {
        it.layers == 4 && it.basePairsPerRow == T191_BUILDABLE_ROW_BP
    }.fitsOneM13

    val uniformDishing = dishingRecords.filter {
        it.convention == IncorporationConvention.UNIFORM.name
    }

    var proseFailure: Throwable? = null
    fun <T> guardedProse(fallback: T, build: () -> T): T = try {
        build()
    } catch (failure: Throwable) {
        proseFailure = failure
        fallback
    }

    val predicates = guardedProse(
        listOf(T191PredicateRecord("PROSE FAILED", "see findings", "UNKNOWN"))
    ) {
        listOf(
            T191PredicateRecord(
                "P1 — the rigidities and the reach are re-derived and reproduce C-0006",
                "D_par %.2f against 14310.78, D_perp %.4f against 19.222".format(
                    mixed.tile.rigidities.alongHelixRigidity,
                    mixed.tile.rigidities.acrossHelixRigidity
                ),
                if (reproductions.filter { it.name.startsWith("C-0006") }
                        .all { it.relativeDeparture < 1e-4 }) "MET" else "NOT MET"
            ),
            T191PredicateRecord(
                "P2 — the run-robustness column demand is reported over the whole bracket",
                ("%d columns at INDEPENDENT layers, %d at the measured f = 0.30, %d at " +
                        "COMPOSITE, against %d on the single-layer 40 nm tile").format(
                    tileRecord(tiles[2].name).columnsDemandedByRunOfThree,
                    tileRecord(tiles[3].name).columnsDemandedByRunOfThree,
                    tileRecord(tiles[4].name).columnsDemandedByRunOfThree,
                    tileRecord(tiles[0].name).columnsDemandedByRunOfThree
                ),
                "MET"
            ),
            T191PredicateRecord(
                "P3 — the dropout grading is re-run on the four-layer lattice",
                ("best 90th-percentile dishing over %d COUPLED four-layer cells is %.4f of " +
                        "the stroke; the UNCOUPLED four-layer tile at the measured coupling " +
                        "reads %.4f, against T-5b's 0.10").format(
                    fourLayerNames.flatMap { cells(it) }.size, bestFourLayerP90, calibratedFree
                ),
                if (anyFourLayerFlat) "MET — a flat coupled four-layer cell exists"
                else "MET — and the coupled answer is NO"
            ),
            T191PredicateRecord(
                "P4 — the scaffold arithmetic",
                "four honeycomb layers at 15 x 112 bp need %d nt of M13's %d".format(
                    scaffoldNucleotides(4, T191_DUPLEXES, T191_BUILDABLE_ROW_BP),
                    M13_SCAFFOLD_NUCLEOTIDES
                ),
                if (fourLayerFits) "MET" else "NOT MET"
            ),
            T191PredicateRecord(
                "F1 — a uniform load on the four-layer lattice dishes exactly zero",
                "asserted as a test in FourLayerTileTest, not as a field here",
                "DID NOT FIRE"
            ),
            T191PredicateRecord(
                "F2 — the smeared equivalent sheet reproduces the multi-layer rigidities",
                "asserted at 1e-12 as a test in FourLayerTileTest",
                "DID NOT FIRE"
            ),
            T191PredicateRecord(
                "F3 — the four-layer p90 stays above 0.10 at every path count",
                ("best coupled four-layer cell %.4f, best coupled single-layer cell %.4f, " +
                        "uncoupled four-layer tile at the measured coupling %.4f").format(
                    bestFourLayerP90, bestBaselineP90, calibratedFree
                ),
                if (anyFourLayerFlat) "DID NOT FIRE" else "FIRED ON THE COUPLED CELLS"
            ),
            T191PredicateRecord(
                "the UNCOUPLED four-layer tile is flat at every graded reading",
                "free-tile dishing %s of the stroke".format(
                    fourLayerNames.joinToString(" / ") {
                        "%.4f".format(uncoupled(it).nominalOverStroke)
                    }
                ),
                if (uncoupledFourLayerFlat) "MET" else "NOT MET AT EVERY READING"
            ),
            T191PredicateRecord(
                "F4 — four honeycomb layers need more than one M13",
                "%d nt against %d".format(
                    scaffoldNucleotides(4, T191_DUPLEXES, T191_BUILDABLE_ROW_BP),
                    M13_SCAFFOLD_NUCLEOTIDES
                ),
                if (fourLayerFits) "DID NOT FIRE" else "FIRED"
            ),
            T191PredicateRecord(
                "F5 — the single-layer baseline does not reproduce C-0089",
                "p90 at 45 paths departs by %.2e from C-0089's 0.6142".format(
                    reproductions.last().relativeDeparture
                ),
                if (reproductions.last().relativeDeparture < 1e-3) "DID NOT FIRE" else "FIRED"
            )
        )
    }

    val result = T191Result(
        task = "T-191",
        leaf = "A8.2",
        title = "The tile §3 actually specifies: a four-layer, ~10 nm body, and what it does " +
                "to the flatness negative",
        verificationType = "in-silico + logical",
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated",
        conditions = ("T = 300 K, aqueous 2 mM MgCl2, k_BT = 4.142 pN*nm; C-0022's SOLVED " +
                "edge profile at 2 mM / 10 nm / 0.192 V; C-0001's secant foundation " +
                "%.9f pN/nm^3; 15 duplex rows; C-0017's 33.3333 pN/nm as a SUM at the " +
                "acceptable 3 nm stroke; dishing on an %d x %d grid; flat means below T-5b's " +
                "0.10 CONVENTION; grading seed %d at %d realisations")
            .format(
                Gen1Tile.FOUNDATION_SECANT, T191_SAMPLES, T191_SAMPLES, T191_SEED, realisations
            ),
        decision = "whether the flatness negative — derived on a 2 nm single-layer tile — " +
                "survives on the ~10 nm four-layer tile §3 also states and NDI's answer to " +
                "decision 5 asks for",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm",
            "flexuralRigidity" to "pN*nm",
            "foundationStiffness" to "pN/nm^3",
            "pressure" to "pN/nm^2 (= MPa)",
            "temperature" to "K",
            "dishing" to "dimensionless, as a fraction of the free-tile stroke"
        ),
        conventions = listOf(
            "x along the helices, y across them, origin at the centre of the footprint",
            "w positive DOWNWARD, compressing the polymer layer",
            "dishing is the deflection with its area-averaged least-squares best-fit plane " +
                    "removed, quoted as a PEAK over an 81 x 81 grid divided by the free-tile stroke",
            "the four-layer body enters as a SMEARED EQUIVALENT SHEET whose three plate " +
                    "rigidities equal the multi-layer body's exactly; the lattice machinery of " +
                    "this repository is single-layer and its crossover combinatorics are the " +
                    "SQUARE lattice's, so no crossover phase is swept",
            "the interlayer coupling is a BRACKET, never a value, and every four-layer number " +
                    "is reported at both ends"
        ),
        bounds = bounds,
        tiles = tileRecords,
        scaffold = scaffoldRecords,
        dishing = dishingRecords,
        convergence = convergence,
        reproductions = reproductions,
        predicates = predicates,
        findings = guardedProse(
            listOf("THE PROSE FAILED and the numbers were rescued; the exception follows")
        ) {
            t191Findings(
                tileRecords, dishingRecords, scaffoldRecords, uniformDishing,
                bestFourLayerP90, bestBaselineP90, anyFourLayerFlat, fourLayerFits,
                gradedNames, calibratedName, calibratedBestP90, calibratedBestNominal,
                calibratedFree
            )
        } + listOfNotNull(proseFailure?.let { "THE PROSE FAILED WITH: $it" }),
        validity = listOf(
            "TRL 1-3. Model-consistent and traceable. Nothing here is measured.",
            "THE FOUR-LAYER BODY IS A SMEARED PLATE, NOT A BRICK. OrigamiGrillage reads only " +
                    "the interhelical distance, the crossover spacing, the crossover hinge " +
                    "stiffness and the duplex's bending and torsional rigidities from its " +
                    "sheet, and NEVER reads `layers` or `interlayerCoupling` — so building it " +
                    "on Gen1Tile's four-layer-honeycomb-rigid variant would give a lattice " +
                    "bit-identical to the single-layer honeycomb one. What is carried here is " +
                    "an equivalent sheet with the right rigidities and the wrong internals.",
            "TRANSVERSE SHEAR IS NEGLECTED AND IT SHOULD NOT BE. At 9.608 nm over 38.08 nm " +
                    "the thickness/span ratio is 0.252, outside Kirchhoff — C-0006 says so in " +
                    "its own validity range. D_par is therefore an upper bound AGAIN, on top " +
                    "of the COMPOSITE assumption.",
            "THE HONEYCOMB LATTICE IS NOT MODELLED. CrossoverLayout carries a two-parity " +
                    "alternation, which is the square lattice; a honeycomb helix has three " +
                    "crossover azimuths at 7 bp. The smeared rigidities are right and the " +
                    "crossover COMBINATORICS are not, so the T-10 centred column construction " +
                    "is used and no phase is swept. Every placement result of C-0063, C-0074 " +
                    "and C-0090 is a square-lattice result and does NOT transfer.",
            "THE COMPOSITE END RESTS ON A CONSTRUCTION, NOT A MEASUREMENT. The across-helix " +
                    "parallel-axis term uses Gen1Tile.crossoverInPlaneStiffness, which that " +
                    "file itself flags as 'a construction, not a measurement' and sweeps over " +
                    "four decades. The along-helix term uses the duplex stretch modulus, which " +
                    "is measured, but full composite action between crossover-linked layers is " +
                    "an assumption nothing here tests.",
            "THE DROPOUT STATISTIC IS A SINGLE-LAYER MEASUREMENT. Strauss et al. imaged a " +
                    "plain single-layer Rothemund rectangle; C-0087's incorporation field is a " +
                    "boundary-layer profile in nm, transferred here to a body with four times " +
                    "the staples and a different perimeter-to-area ratio. The direction is " +
                    "ADVERSE — a four-layer station has more redundancy behind it than the " +
                    "measurement it is graded under — so a four-layer verdict here is a LOWER " +
                    "bound on the four-layer tile's robustness.",
            "THE PATH COUNTS ABOVE THREE COLUMNS ARE ABSTRACT STATION SETS, exactly as in " +
                    "C-0089. No placement claim in this corpus places them on a four-layer " +
                    "honeycomb top face, and the plan ceilings that exist (C-0055, C-0063, " +
                    "C-0072, C-0075) are all square-lattice single-layer results.",
            "A THICKER TILE IS A DIFFERENT BODY FOR C-0022's CHARGE AND C-0004's DRAINAGE, " +
                    "and neither is re-run here. electrostatics/DnaOrigamiTile already " +
                    "defaults to a 10 nm thickness, so C-0022's load is the one that was " +
                    "always computed for this tile; the poroelastic footprint argument is a " +
                    "thickness-independent one by C-0004's own finding, but the gap " +
                    "electrostatics and the stack geometry are not re-derived.",
            "T-5b's 0.10 is a CONVENTION, not a physical threshold."
        ),
        openQuestions = listOf(
            "Where between INDEPENDENT and COMPOSITE a real crossover-linked four-layer " +
                    "honeycomb slab sits. That is the whole width of this answer and it is a " +
                    "MEASUREMENT question — a bending persistence length of a multilayer " +
                    "bundle with a stated helix count and lattice would close it.",
            "Whether a boustrophedon raster of four honeycomb layers is routable from one " +
                    "circular M13 at all. C-0086's odd-half-turn rule is a SQUARE-lattice " +
                    "statement and, applied at 10.5 bp/turn, admits NO integer base-pair row " +
                    "length whatever — so the honeycomb raster width is an open question, not " +
                    "a solved one, and this study answers only the nucleotide COUNT.",
            "What the attachment lattice of a four-layer honeycomb top face is, and how many " +
                    "stations it admits. Every plan ceiling in this corpus is single-layer " +
                    "square-lattice.",
            "Whether the staple incorporation of a multilayer body is the single-layer one. " +
                    "C-0087's item 2, now load-bearing in a new way.",
            "Whether transverse shear removes the four-layer advantage. A Reissner-Mindlin or " +
                    "3-D reading of the same body would answer it."
        ),
        sources = listOf(
            "gpd/results/T-3b-tile-edge-load-profile.json (C-0022's solved collar)",
            "gpd/results/T-155-dropout-robust-placement.json (C-0089, quoted for reproduction)",
            "gpd/results/T-151-scaffold-routing.json (C-0086, quoted for reproduction)"
        ),
        parameters = mapOf(
            "gradingRealisations" to "$realisations",
            "gradingSeed" to "$T191_SEED (C-0087's own, so its cells reproduce)",
            "decisionDigits" to "$T191_DECISION_DIGITS",
            "decisionFloor" to "$T191_DECISION_FLOOR",
            "samplesPerEdge" to "$T191_SAMPLES",
            "flatnessTolerance" to "$T191_TOLERANCE (T-5b's CONVENTION)",
            "collarWidth" to "$T191_COLLAR nm (C-0058's rim mask, unused here: the collar is " +
                    "C-0022's solved profile)",
            "columnSweep" to T191_COLUMN_SWEEP.joinToString("/"),
            "duplexRows" to "$T191_DUPLEXES",
            "buildableEdgeX" to "$T191_BUILDABLE_EDGE_X nm = $T191_BUILDABLE_ROW_BP bp (C-0086)",
            "nominalEdgeX" to "$T191_NOMINAL_EDGE_X nm (the standing studies' width)",
            "worstAbsenceRun" to "$T191_WORST_RUN (C-0089's 90th percentile)",
            "loadProfile" to designProfile.name,
            "mandate" to "%.7f pN/nm as a SUM (C-0017)".format(T191_MANDATE)
            // NO wall clock and nothing that counts steps — CLAUDE.md.
        )
    )

    val output = File("gpd/results/T-191-four-layer-tile.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digits = T191_DECISION_DIGITS + 3,
                digitsByKey = DEPARTURE_DIGITS_BY_KEY,
                floor = T191_DECISION_FLOOR
            ) as JsonObject)
        ) + "\n"
    )
    println("T-191 — wrote ${output.path}")
    result.findings.forEach { println("  * $it") }
    result.predicates.forEach { println("  [${it.verdict}] ${it.name}") }
    proseFailure?.let { throw it }
}

// ---------------------------------------------------------------------------------------------
// the prose, built AFTER the result so a format placeholder cannot cost the run
// ---------------------------------------------------------------------------------------------

@Suppress("LongParameterList", "LongMethod")
private fun t191Findings(
    tiles: List<T191TileRecord>,
    dishing: List<T191DishingRecord>,
    scaffold: List<T191ScaffoldRecord>,
    uniformDishing: List<T191DishingRecord>,
    bestFourLayerP90: Double,
    bestBaselineP90: Double,
    anyFourLayerFlat: Boolean,
    fourLayerFits: Boolean,
    gradedNames: Set<String>,
    calibratedName: String,
    calibratedBestP90: Double,
    calibratedBestNominal: Double,
    calibratedFree: Double
): List<String> {
    val findings = ArrayList<String>()
    val baseline = tiles[0]
    val independent = tiles[2]
    val calibrated = tiles[3]
    val composite = tiles[4]
    val mixed = tiles[5]

    findings += ("THE FLATNESS NEGATIVE DOES NOT SURVIVE THE TILE SECTION 3 SPECIFIES, AND " +
            "THE REASON IS THAT THE THICK TILE DOES NOT NEED A COUPLING TO STAY FLAT. At the " +
            "MEASURED interlayer coupling the four-layer tile's UNCOUPLED dishing under " +
            "C-0022's solved collar is %.4f of the stroke — inside T-5b's 0.10 with no " +
            "attachment at all — where the single-layer tile's is %.4f, %.2fx the convention. " +
            "Adding a coupling still helps at ZERO defects (%.4f at the best of the density " +
            "sweep) but costs under the measured dropout (%.4f at the 90th percentile, %.2fx " +
            "the convention), which is CLAUDE.md's own 'an attachment coupling can be a NET " +
            "DISHING SOURCE' read on a tile that no longer needs the correction. Over the " +
            "whole bracket the best COUPLED four-layer cell is %.4f against the single " +
            "layer's %.4f — a factor of %.2f — and %s.")
        .format(
            calibratedFree,
            tiles[0].freeTileDishingOverStroke, tiles[0].freeTileDishingOverStroke / 0.10,
            calibratedBestNominal,
            calibratedBestP90, calibratedBestP90 / 0.10,
            bestFourLayerP90, bestBaselineP90, bestBaselineP90 / bestFourLayerP90,
            if (anyFourLayerFlat) "a flat COUPLED four-layer cell exists too"
            else "no coupled cell of the family is flat"
        )

    findings += ("THE CHEAP BOUND PREDICTED IT AND IT IS A FOURTH ROOT. At the MEASURED " +
            "interlayer coupling f = %.2f, D_par goes %.2f -> %.2f pN*nm (%.1fx) and D_perp " +
            "%.3f -> %.3f (%.1fx) from the single layer to the four-layer tile, so the reach " +
            "ell = (4D/k_f)^(1/4) goes %.2f -> %.2f nm along the helices and %.2f -> %.2f nm " +
            "across them. The column demand a run of three absences puts on the tile therefore " +
            "falls from C-0089's %d to %d — and over the whole INDEPENDENT..COMPOSITE bracket " +
            "it is %d..%d, so the conclusion does not depend on where in the bracket the truth " +
            "sits.")
        .format(
            calibrated.compositeFraction,
            baseline.alongHelixRigidity, calibrated.alongHelixRigidity,
            calibrated.alongHelixRigidity / baseline.alongHelixRigidity,
            baseline.acrossHelixRigidity, calibrated.acrossHelixRigidity,
            calibrated.acrossHelixRigidity / baseline.acrossHelixRigidity,
            baseline.reachAlongHelices, calibrated.reachAlongHelices,
            baseline.reachAcrossHelices, calibrated.reachAcrossHelices,
            baseline.columnsDemandedByRunOfThree, calibrated.columnsDemandedByRunOfThree,
            independent.columnsDemandedByRunOfThree, composite.columnsDemandedByRunOfThree
        )

    findings += ("AND THE INTERLAYER COUPLING IS NOT AN OPEN BRACKET — IT IS MEASURED. Four " +
            "origami bundles, two lattices, three laboratories and three techniques put a real " +
            "crossover-linked bundle at f = %.2f-%.2f of the way from INDEPENDENT to " +
            "COMPOSITE: Kauert et al. 2011's magnetic-tweezers 4HB (square, 740 nm) and 6HB " +
            "(honeycomb, 1880 nm), Pfitzner et al. 2013's 6HB (2 um) and Wang et al. 2012's " +
            "6HB tile (1.0 um) — the last of which PUBLISHES the rigid-composite formula, " +
            "names it a 'naive model' of 'rigidly linked rods', and measures it to " +
            "over-predict by 2.7x. Kauert et al.'s own model swept four boundary conditions " +
            "between the two limits and concluded the two PARTIAL ones. So the RIGID reading " +
            "in this repository's OrigamiSheet is a ~3x over-prediction and the four-layer " +
            "answer here is quoted at f = %.2f.")
        .format(
            MeasuredBundleRigidity.COMPOSITE_FRACTION_MIN,
            MeasuredBundleRigidity.COMPOSITE_FRACTION_MAX,
            MeasuredBundleRigidity.COMPOSITE_FRACTION
        )

    findings += ("THE PARALLEL-AXIS ENHANCEMENT IS THE SAME FACTOR IN BOTH DIRECTIONS, " +
            "EXACTLY, AND THAT IS WHY C-0006's STANDING FOUR-LAYER VARIANT IS A MIXED " +
            "STATE. Chen et al.'s construction gives the crossover k_s/k_theta = S/B, so " +
            "the composite excess reduces to S*sum(y^2)/(nB) = %.4f along the helices AND " +
            "across them. The anisotropy of a four-layer sheet is therefore %.3f at BOTH " +
            "ends of the coupling bracket. C-0006's four-layer-honeycomb-rigid variant " +
            "applies the parallel axis along the helices only and reads %.1f — %.1fx the " +
            "physical value, and a bound on neither end.")
        .format(
            composite.parallelAxisFactor, independent.anisotropy, mixed.anisotropy,
            mixed.anisotropy / independent.anisotropy
        )

    val fourAtBuildable = scaffold.first {
        it.layers == 4 && it.basePairsPerRow == 112
    }
    val fiveAtBuildable = scaffold.first { it.layers == 5 && it.basePairsPerRow == 112 }
    findings += ("ONE CIRCULAR M13 PAYS FOR EXACTLY FOUR LAYERS AND NOT FIVE, WHICH IS " +
            "NDI's OWN ARITHMETIC. A 15 x 112 bp raster is %d nt per layer, so four layers " +
            "are %d of M13's %d (%.1f %% used, %.3fx overhang) and five are %d, which is " +
            "%d nt OVER. C-0086's 4.31x excess of scaffold on the single-layer sheet is " +
            "therefore spent almost exactly by the thickness §3 states. The declared " +
            "falsifier F4 %s.")
        .format(
            scaffold.first { it.layers == 1 && it.basePairsPerRow == 112 }.nucleotides,
            fourAtBuildable.nucleotides, fourAtBuildable.scaffoldNucleotides,
            100.0 * fourAtBuildable.nucleotides / fourAtBuildable.scaffoldNucleotides,
            fourAtBuildable.overhang,
            fiveAtBuildable.nucleotides,
            fiveAtBuildable.nucleotides - fiveAtBuildable.scaffoldNucleotides,
            if (fourLayerFits) "did NOT fire" else "FIRED"
        )

    val bestPerTile = tiles.filter { it.name in gradedNames }.map { tile ->
        tile.name to dishing.filter {
            it.tile == tile.name && it.convention == "MEASURED_DEPTH"
        }.minOfOrNull { it.p90OverStroke }
    }
    findings += ("THE DENSITY AXIS, PER TILE, AT EQUAL SPRINGS: %s. The verdict statistic " +
            "is the 90th percentile of the peak dishing over the stroke, and the four-layer " +
            "readings bracket the answer rather than fixing it — the whole width of this " +
            "study is the interlayer coupling, which no solve can narrow.")
        .format(
            bestPerTile.joinToString("; ") {
                "%s best %.4f".format(it.first.take(46), it.second ?: Double.NaN)
            }
        )

    findings += ("THE FREE TILE IS THE REFERENCE AND IT MOVES TOO: the UNCOUPLED tile's own " +
            "dishing under C-0022's solved collar runs %s of the stroke across the nine " +
            "readings, so a coupling on the thick tile has less to correct AND its own sag " +
            "between attachments is smaller. CLAUDE.md's 'an attachment coupling can be a " +
            "NET DISHING SOURCE' is what makes the second half of that sentence matter.")
        .format(tiles.joinToString(" / ") { "%.4f".format(it.freeTileDishingOverStroke) })

    findings += ("THE DROPOUT STATISTIC IS THE PART THAT DOES NOT TRANSFER. C-0087's " +
            "incorporation field is a single-layer measurement applied to a body with four " +
            "times the staples; under the UNIFORM 0.84 convention instead, the same 3 x 15 " +
            "cells read %s. The MEASURED_DEPTH reading is retained as the headline because " +
            "it is C-0089's, and the spread between the two conventions is the honest size " +
            "of the transfer assumption.")
        .format(
            uniformDishing.joinToString(" / ") { "%.4f".format(it.p90OverStroke) }
        )

    findings += ("WHAT THIS DOES NOT SETTLE, IN ONE SENTENCE: the lattice machinery of this " +
            "repository is single-layer and square-lattice, so the four-layer body enters " +
            "as a smeared plate with the right rigidities and the wrong internals; transverse " +
            "shear at a thickness/span of %.3f is neglected and makes D_par an upper bound " +
            "again; and every placement, phase and plan ceiling in the corpus is a " +
            "square-lattice single-layer result that does not transfer to a honeycomb top face.")
        .format(composite.thickness / composite.edgeX)

    return findings
}
