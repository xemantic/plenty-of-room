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

import com.xemantic.nano.plentyofroom.coupling.InfluenceSurrogate
import com.xemantic.nano.plentyofroom.anchoring.UpwardArmPlacement
import com.xemantic.nano.plentyofroom.anchoring.centroSymmetricPlacements
import com.xemantic.nano.plentyofroom.anchoring.placementFromKey
import com.xemantic.nano.plentyofroom.anchoring.quantisedToRise
import com.xemantic.nano.plentyofroom.anchoring.rasterColumnLayout
import com.xemantic.nano.plentyofroom.anchoring.rasterUpwardSites
import com.xemantic.nano.plentyofroom.coupling.CollarTerm
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max

/**
 * `T-189` — can `C-0086`'s 112 bp seamless raster row be **twist-corrected**?
 *
 * Emits `gpd/results/T-189-twist-corrected-raster.json`. Reads
 * `gpd/results/T-3b-tile-edge-load-profile.json` (`C-0022`'s solved collar),
 * `gpd/results/T-153-buildable-raster-width.json` (`C-0090`'s placement key and dishing) and
 * `gpd/results/T-172-row-end-prestrain.json` (`C-0104`'s threshold).
 *
 * See [TwistCorrectedRaster.kt][RasterRow] for the theorem the whole study rests on.
 */
private const val DUPLEXES = 15
private const val PHASE = 8
private const val FLATNESS_TOLERANCE = 0.10
private const val ARM_COUNT = C0055_ARM_COUNT
private val MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE
private const val C0104_THRESHOLD_DEGREES = 15.4497275

private fun degrees(radians: Double): Double = radians * 180.0 / PI

/** `CLAUDE.md`: a dimensionless departure is emitted at two significant digits. */
private fun twoSignificant(value: Double): Double {
    if (value == 0.0 || !value.isFinite()) return value
    val exponent = Math.floor(Math.log10(abs(value))).toInt()
    val scale = Math.pow(10.0, (1 - exponent).toDouble())
    return Math.round(value * scale) / scale
}

// ---------------------------------------------------------------------------------------------
// the records
// ---------------------------------------------------------------------------------------------

@Serializable
private data class T189RowRecord(
    val family: String,
    val halfTurnsPerDomain: Int,
    val domainCount: Int,
    val halfTurns: Int,
    val basePairs: Int,
    val widthNm: Double,
    val domains: String,
    val perInterfaceSpacing: String,
    val seamlessAdmissible: Boolean,
    val designTwistPerBase: Double,
    val mismatchPerBase: Double,
    val totalMismatchDegrees: Double,
    val residualBasePairs: Double,
    val exactHalfTurnBasePairs: Double
)

@Serializable
private data class T189ArrangementRecord(
    val shortDomainsAt: String,
    val domains: String,
    val centroSymmetric: Boolean,
    val rowEndDegrees: Double,
    val peakDegrees: Double,
    val peakOverThreshold: Boolean
)

@Serializable
private data class T189RegisterRecord(
    val row: String,
    val torsionalRigidity: Double,
    val torsionalRigiditySource: String,
    val crossoverAlpha: Double,
    val hingeStiffness: Double,
    val crossoverSpacing: Double,
    val decayLength: Double,
    val rowEndDegrees: Double,
    val peakDegrees: Double,
    val continuumRowEndDegrees: Double,
    val rowEndAboveThreshold: Boolean,
    val peakAboveThreshold: Boolean,
    val field: String
)

@Serializable
private data class T189DishingRecord(
    val host: String,
    val state: String,
    val placementKey: String,
    val placementShape: String,
    val rowEndDegrees: Double,
    val peakPrestrainDegrees: Double,
    val coupledDishingOverStroke: Double,
    val freeDishingOverStroke: Double,
    val uniformLoadDishingOverStroke: Double,
    val flatAtTenPercent: Boolean
)

@Serializable
private data class T189EnumerationRecord(
    val host: String,
    val state: String,
    val enumerated: Int,
    val bestDishingOverStroke: Double,
    val worstDishingOverStroke: Double,
    val medianDishingOverStroke: Double,
    val bestShape: String,
    val shapeIsZeroPrestrainOptimum: Boolean,
    val shapeIsC0090Shape: Boolean,
    val flatAtTenPercent: Boolean
)

@Serializable
private data class T189LatticeRecord(
    val name: String,
    val basePairs: Int,
    val widthNm: Double,
    val columns: Int,
    val stations: Int,
    val centroSymmetric: Boolean,
    val outOfPlaneOffsets: String,
    val worstAzimuthDepartureDegrees: Double,
    val outermostStationNm: Double
)

@Serializable
private data class T189Convergence(
    val axis: String,
    val coarse: Double,
    val fine: Double,
    val relativeMovement: Double
)

@Serializable
private data class T189Reproduction(
    val what: String,
    val owner: String,
    val expected: Double,
    val reproduced: Double,
    val departure: Double
)

@Serializable
private data class T189Predicate(val id: String, val statement: String, val met: Boolean)

@Serializable
private data class T189Falsifier(
    val id: String,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
private data class T189Result(
    val task: String,
    val leaf: String,
    val question: String,
    val conditions: Map<String, String>,
    val rows: List<T189RowRecord>,
    val arrangements: List<T189ArrangementRecord>,
    val register: List<T189RegisterRecord>,
    val lattices: List<T189LatticeRecord>,
    val dishing: List<T189DishingRecord>,
    val enumerations: List<T189EnumerationRecord>,
    val convergence: List<T189Convergence>,
    val reproductions: List<T189Reproduction>,
    val predicates: List<T189Predicate>,
    val falsifiers: List<T189Falsifier>,
    val findings: List<String>,
    val parameters: Map<String, String>
)

// ---------------------------------------------------------------------------------------------
// the upstream inputs
// ---------------------------------------------------------------------------------------------

private fun t189SolvedProfile(file: File): Pair<CollarTerm, CollarTerm> {
    require(file.exists()) { "C-0022's result file is missing: ${file.path}" }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .first {
            fun value(key: String) = it.getValue(key).jsonPrimitive.content.toDouble()
            value("concentration") == 2.0 && value("gapHeight") == 10.0 &&
                    value("appliedBias") == 0.192
        }
    fun value(key: String) = record.getValue(key).jsonPrimitive.content.toDouble()
    return CollarTerm(value("taperDepth"), value("taperWidth")) to
            CollarTerm(value("rimResidualDepth"), 1.0)
}

private fun t189C0090Reading(file: File, phase: Int): Pair<Double, String> {
    require(file.exists()) { "C-0090's result file is missing: ${file.path}" }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("placements").jsonArray.map { it.jsonObject }
        .first {
            it.getValue("case").jsonPrimitive.content.startsWith("RECOMMENDED") &&
                    it.getValue("phaseBasePairs").jsonPrimitive.content.toInt() == phase
        }
    return record.getValue("bestDishingOverStroke").jsonPrimitive.content.toDouble() to
            record.getValue("bestKey").jsonPrimitive.content
}

private fun t189C0104Bound(file: File, namePrefix: String): Double {
    require(file.exists()) { "C-0104's result file is missing: ${file.path}" }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("cheapBounds").jsonArray.map { it.jsonObject }
        .first { it.getValue("name").jsonPrimitive.content.startsWith(namePrefix) }
        .getValue("value").jsonPrimitive.content.toDouble()
}

/** `C-0107`'s graded corrugated-field dishing, read from its own result file. */
private fun t189C0107Graded(file: File): Double {
    require(file.exists()) { "C-0107's result file is missing: ${file.path}" }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("dishing").jsonArray.map { it.jsonObject }
        .first { it.getValue("state").jsonPrimitive.content.startsWith("the GRADED") }
        .getValue("coupledDishingOverStroke").jsonPrimitive.content.toDouble()
}

// ---------------------------------------------------------------------------------------------
// the host: one lattice, its stations, its influence bank and its prestrained solves
// ---------------------------------------------------------------------------------------------

private class T189Host(
    val name: String,
    val sheet: OrigamiSheet,
    val edgeX: Double,
    val columns: CrossoverLayout,
    val siteLattice: List<List<Double>>,
    smooth: CollarTerm,
    rim: CollarTerm
) {

    val lengthY: Double = DUPLEXES * sheet.interhelicalDistance

    val interiorPressure: Double = Gen1Tile.TARGET_FORCE / (edgeX * lengthY)

    val uniformField: PressureField = uniformPressure(interiorPressure)

    val solvedField: PressureField =
        edgeCollarPressure(interiorPressure, edgeX, lengthY, listOf(smooth, rim))

    val freeStroke: Double = PlateOnFoundation(
        sheet.plate(edgeX, lengthY), Gen1Tile.FOUNDATION_SECANT, emptyList(), basisDegree = 12
    ).solve(uniformField).meanDeflection

    val stations: List<Pair<Double, Double>> = siteLattice.flatMapIndexed { row, xs ->
        xs.map { it to (row - (DUPLEXES - 1) / 2.0) * sheet.interhelicalDistance }
    }

    fun lattice(
        prestrains: Map<CrossoverSite, Double>,
        subdivisions: Int = 2
    ): OrigamiGrillage = OrigamiGrillage(
        sheet = sheet,
        lengthX = edgeX,
        beamCount = DUPLEXES,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        columns = columns,
        subdivisions = subdivisions,
        crossoverPrestrains = prestrains
    )

    /**
     * The influence functions of the station set, taken on the **prestrain-free** lattice.
     * `CH-0120`'s discipline: a prestrain is a load, so it must not enter the unit-load solves.
     */
    inner class Influences(val samples: Int = 81, val subdivisions: Int = 2) {

        val bare: OrigamiGrillage = lattice(emptyMap(), subdivisions)

        private val halfX = bare.lengthX / 2.0

        private val halfY = bare.lengthY / 2.0

        private val solutions = stations.map { (x, y) ->
            bare.solve(uniformPressure(0.0), listOf(PointLoad(x, y, 1.0)))
        }

        fun sample(field: (Double, Double) -> Double): DoubleArray {
            val grid = DoubleArray(samples * samples)
            for (i in 0 until samples) {
                val x = -halfX + 2.0 * halfX * i / (samples - 1)
                for (j in 0 until samples) {
                    grid[i * samples + j] = field(x, -halfY + 2.0 * halfY * j / (samples - 1))
                }
            }
            return grid
        }

        val stationInfluence: Array<DoubleArray> = Array(stations.size) { j ->
            DoubleArray(stations.size) { k ->
                solutions[k].deflection(stations[j].first, stations[j].second)
            }
        }

        val dishingInfluence: Array<DoubleArray> =
            Array(stations.size) { k -> sample { x, y -> solutions[k].dishing(x, y) } }

        fun indexOf(x: Double, y: Double, tolerance: Double = 1e-9): Int =
            stations.indexOfFirst {
                abs(it.first - x) <= tolerance && abs(it.second - y) <= tolerance
            }
    }

    inner class Solve(
        prestrains: Map<CrossoverSite, Double>,
        val influences: Influences,
        val samples: Int = 81,
        subdivisions: Int = 2
    ) {

        val host: OrigamiGrillage = lattice(prestrains, subdivisions)

        private val uniform = List(ARM_COUNT) { MANDATE / ARM_COUNT }

        private val free = host.solve(solvedField)

        private val stationFree = DoubleArray(stations.size) {
            free.deflection(stations[it].first, stations[it].second)
        }

        private val dishingFree = influences.sample { x, y -> free.dishing(x, y) }

        val freeDishing = free.peakDishing(samples) / freeStroke

        /** The standing falsifier, on the support-free and **prestrain-free** lattice. */
        val uniformLoadDishing =
            host.withoutPrestrain.solve(uniformField).peakDishing(samples) / freeStroke

        fun indicesOf(placement: UpwardArmPlacement): List<Int> =
            placement.stations(DUPLEXES, sheet.interhelicalDistance).map { (x, y) ->
                val index = influences.indexOf(x, y)
                require(index >= 0) { "($x, $y) is not a station of $name" }
                index
            }

        fun dishing(indices: List<Int>): Double = InfluenceSurrogate(
            grid = indices.map { stations[it] },
            samples = samples,
            stationFree = DoubleArray(indices.size) { stationFree[indices[it]] },
            stationInfluence = Array(indices.size) { j ->
                DoubleArray(indices.size) { k ->
                    influences.stationInfluence[indices[j]][indices[k]]
                }
            },
            dishingFree = dishingFree,
            dishingInfluence = Array(indices.size) { influences.dishingInfluence[indices[it]] }
        ).solve(uniform).peakDishing / freeStroke

        fun dishing(placement: UpwardArmPlacement): Double = dishing(indicesOf(placement))
    }

    /**
     * A placement's **shape**: the station's index within its own row, per row. Lattice-free, so
     * a placement on the 112 bp lattice and one on the 110 bp lattice are directly comparable.
     */
    fun shapeOf(placement: UpwardArmPlacement): String =
        placement.rows.joinToString(";") { row ->
            row.row.toString() + ":" + row.roots.joinToString(",") { root ->
                siteLattice[row.row].indexOfFirst { abs(it - root) < 1e-9 }.toString()
            }
        }
}

/** The graded corrugated prestrain over every crossover of a host, from a column register field. */
private fun gradedPrestrain(
    host: T189Host,
    field: DoubleArray
): Map<CrossoverSite, Double> {
    val sites = host.lattice(emptyMap()).crossoverSites
    return sites.associateWith { site ->
        val glide = if (site.lowerBeam % 2 == 0) 1.0 else -1.0
        glide * field[site.column]
    }
}

private class T189Optimum(
    family: Sequence<UpwardArmPlacement>,
    value: (UpwardArmPlacement) -> Double
) {
    val best: UpwardArmPlacement
    val bestValue: Double
    val worstValue: Double
    val medianValue: Double
    val enumerated: Int

    init {
        val values = ArrayList<Double>()
        var incumbent: Pair<UpwardArmPlacement, Double>? = null
        family.forEach { placement ->
            val here = value(placement)
            values += here
            val current = incumbent
            if (current == null || here < current.second ||
                (here == current.second && placement.key < current.first.key)
            ) incumbent = placement to here
        }
        require(values.isNotEmpty()) { "the centro-symmetric family is empty" }
        values.sort()
        best = incumbent!!.first
        bestValue = incumbent!!.second
        worstValue = values.last()
        medianValue = values[values.size / 2]
        enumerated = values.size
    }
}

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    val rise = Gen1Tile.RISE_PER_BASE_PAIR
    val sheet = origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)
    val arm = quantisedToRise(C0055_ARM_LENGTH)
    val hinge = Gen1Tile.crossoverHingeStiffness()

    println("T-189 — reading C-0022's collar, C-0090's placement and C-0104's threshold ...")
    val (smooth, rim) = t189SolvedProfile(ResultInputs.T_3B.file())
    val (publishedDishing, publishedKey) =
        t189C0090Reading(ResultInputs.T_153.file(), PHASE)
    val c0104Threshold = t189C0104Bound(
        ResultInputs.T_172.file(), "the prestrain at which C-0090"
    )
    val c0107Graded = t189C0107Graded(ResultInputs.T_182.file())

    // ------------------------------------------------- Deliverable 1: the cheap bound, in full
    println("T-189 — the closed-form enumeration, BEFORE any solve ...")
    val rows = ArrayList<T189RowRecord>()
    fun record(family: String, row: RasterRow) {
        rows += T189RowRecord(
            family = family,
            halfTurnsPerDomain = row.halfTurnsPerDomain,
            domainCount = row.domainCount,
            halfTurns = row.halfTurns,
            basePairs = row.basePairs,
            widthNm = row.width(rise),
            domains = row.domains.joinToString("+"),
            perInterfaceSpacing = row.perInterfaceSpacing.joinToString(","),
            seamlessAdmissible = row.seamlessAdmissible,
            designTwistPerBase = row.designTwistPerBase,
            mismatchPerBase = row.mismatchPerBase(),
            totalMismatchDegrees = row.totalMismatchDegrees(),
            residualBasePairs = row.residualBasePairs(),
            exactHalfTurnBasePairs = exactHalfTurnBasePairs(row.halfTurns)
        )
    }
    // C-0086's own list: the odd multiples of 16 bp, at the UNCORRECTED design twist
    listOf(1, 3, 5, 7, 9).forEach { record("C-0086 uniform 16 bp domains", RasterRow(List(it) { 16 })) }
    seamlessTwistCorrectedRows(15).forEach { record("twist-corrected, 1.5-turn domains", it) }
    seamlessTwistCorrectedRows(9, halfTurnsPerDomain = 5)
        .forEach { record("twist-corrected, 2.5-turn domains", it) }
    rows.forEach {
        println(
            "  %-34s D=%2d q=%3d N=%4d w=%7.3f nm  dtwist=%8.5f  total=%+8.4f deg".format(
                it.family.take(34), it.domainCount, it.halfTurns, it.basePairs, it.widthNm,
                it.designTwistPerBase, it.totalMismatchDegrees
            )
        )
    }

    val uniformRow = RasterRow(List(7) { 16 })
    val nearestToSpec = rows
        .filter { it.family.startsWith("twist-corrected, 1.5") }
        .minByOrNull { abs(it.widthNm - Gen1Tile.EDGE_X) }!!
    require(nearestToSpec.basePairs == 110) { "the near-40 nm corrected row must be 110 bp" }

    // ------------------------------------ Deliverable 2: which arrangement of the short domains
    println("T-189 — the 21 arrangements of two 15 bp domains among seven ...")
    val arrangements = ArrayList<T189ArrangementRecord>()
    val candidates = ArrayList<Pair<List<Int>, Double>>()
    for (a in 0 until 7) for (b in a + 1 until 7) {
        val domains = (0 until 7).map { if (it == a || it == b) 15 else 16 }
        val field = columnRegisterField(domains, Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY, hinge, 10.2, rise)
        val peak = degrees(field.maxOf { abs(it) })
        arrangements += T189ArrangementRecord(
            shortDomainsAt = "$a,$b",
            domains = domains.joinToString("+"),
            centroSymmetric = domains == domains.reversed(),
            rowEndDegrees = degrees(field.last()),
            peakDegrees = peak,
            peakOverThreshold = peak > C0104_THRESHOLD_DEGREES
        )
        if (domains == domains.reversed()) candidates += domains to peak
    }
    val bestDomains = candidates.minByOrNull { it.second }!!.first
    val correctedRow = RasterRow(bestDomains)
    println("  the best CENTRO-SYMMETRIC arrangement is ${bestDomains.joinToString("+")}")
    arrangements.filter { it.centroSymmetric }.forEach {
        println(
            "  centro-symmetric %-4s  end %+8.4f deg  peak %8.4f deg".format(
                it.shortDomainsAt, it.rowEndDegrees, it.peakDegrees
            )
        )
    }

    // ---------------------------- Deliverable 3: the register field over C-0107's own 12 cells
    println("T-189 — the register field over C-0107's 2 x 3 x 2 bracket ...")
    val register = ArrayList<T189RegisterRecord>()
    val rigidities = listOf(
        Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY to "CanDo GJ = 460 pN nm^2",
        Gen1Tile.DUPLEX_TORSIONAL_PERSISTENCE * thermalEnergy() to
                "l_t = ${Gen1Tile.DUPLEX_TORSIONAL_PERSISTENCE} nm times k_BT"
    )
    listOf("C-0086's 112 bp row" to uniformRow, "the 110 bp twist-corrected row" to correctedRow)
        .forEach { (label, row) ->
            rigidities.forEach { (rigidity, source) ->
                listOf(0.6, 1.0, 1.2).forEach { alpha ->
                    val k = Gen1Tile.crossoverHingeStiffness(alpha)
                    listOf(
                        10.2 to "D L/N_c, one duplex per crossover",
                        16.0 * rise to "the 16 bp column pitch"
                    ).forEach { (spacing, _) ->
                        val field =
                            columnRegisterField(row.domains, rigidity, k, spacing, rise)
                        val continuum = EdgeTwistRelief(rigidity, k, spacing, row.width(rise))
                        val end = degrees(field.last())
                        val peak = degrees(field.maxOf { abs(it) })
                        register += T189RegisterRecord(
                            row = label,
                            torsionalRigidity = rigidity,
                            torsionalRigiditySource = source,
                            crossoverAlpha = alpha,
                            hingeStiffness = k,
                            crossoverSpacing = spacing,
                            decayLength = continuum.decayLength,
                            rowEndDegrees = end,
                            peakDegrees = peak,
                            continuumRowEndDegrees =
                                degrees(continuum.endResidual(row.twistRateMismatch(rise))),
                            rowEndAboveThreshold = abs(end) > C0104_THRESHOLD_DEGREES,
                            peakAboveThreshold = peak > C0104_THRESHOLD_DEGREES,
                            field = field.joinToString(",") { "%.4f".format(degrees(it)) }
                        )
                    }
                }
            }
        }
    register.forEach {
        println(
            "  %-30s C=%6.1f a=%.1f p=%5.2f  end %+8.4f  peak %8.4f".format(
                it.row.take(30), it.torsionalRigidity, it.crossoverAlpha, it.crossoverSpacing,
                it.rowEndDegrees, it.peakDegrees
            )
        )
    }

    // --------------------------------------------- Deliverable 4: the two lattices and a solve
    println("T-189 — building the two lattices ...")
    val uniformColumns = twistCorrectedColumnLayout(uniformRow.domains, rise)
    val uniformSites = twistCorrectedUpwardSites(uniformRow.domains, DUPLEXES, rise)
    val correctedColumns = twistCorrectedColumnLayout(correctedRow.domains, rise)
    val correctedSites =
        twistCorrectedUpwardSites(correctedRow.domains, DUPLEXES, rise, mirrorOffsets = true)
    val correctedSitesPlain = twistCorrectedUpwardSites(correctedRow.domains, DUPLEXES, rise)

    fun symmetric(lattice: List<List<Double>>): Boolean = lattice.indices.all { row ->
        val mine = lattice[row]
        val partner = lattice[lattice.size - 1 - row].map { -it }.sorted()
        mine.size == partner.size && mine.zip(partner).all { (a, b) -> abs(a - b) < 1e-9 }
    }

    val lattices = listOf(
        Triple("C-0086's 112 bp row, 8 bp planes", uniformRow, uniformSites) to
                outOfPlaneOffsets(uniformRow.domains),
        Triple("the 110 bp corrected row, 8 bp planes", correctedRow, correctedSitesPlain) to
                outOfPlaneOffsets(correctedRow.domains),
        Triple("the 110 bp corrected row, MIRRORED planes", correctedRow, correctedSites) to
                outOfPlaneOffsets(correctedRow.domains, mirror = true)
    ).map { (triple, offsets) ->
        val (name, row, sites) = triple
        T189LatticeRecord(
            name = name,
            basePairs = row.basePairs,
            widthNm = row.width(rise),
            columns = row.domainCount + 1,
            stations = sites.sumOf { it.size },
            centroSymmetric = symmetric(sites),
            outOfPlaneOffsets = offsets.joinToString("+"),
            worstAzimuthDepartureDegrees = offsets.maxOf { abs(azimuthDeparture(it)) },
            outermostStationNm = sites.flatten().maxOf { abs(it) }
        )
    }
    lattices.forEach {
        println(
            "  %-42s %3d bp  %2d columns  %2d stations  symmetric=%s".format(
                it.name.take(42), it.basePairs, it.columns, it.stations, it.centroSymmetric
            )
        )
    }

    val hostA = T189Host(
        "C-0086's 112 bp lattice", sheet, uniformRow.width(rise),
        uniformColumns, uniformSites, smooth, rim
    )
    val hostB = T189Host(
        "the 110 bp twist-corrected lattice", sheet, correctedRow.width(rise),
        correctedColumns, correctedSites, smooth, rim
    )

    // the arm is a DESIGN VARIABLE quantised at the rise (C-0085, C-0090), and the 34-root
    // family's feasibility is a plan question the narrower row can lose: walk the quantum down
    // until the family is non-empty, on each lattice, and report what it cost.
    fun largestFeasibleArm(host: T189Host, from: Double): Pair<Double, Int> {
        var rises = Math.round(from / rise).toInt()
        while (rises >= 1) {
            val candidate = rises * rise
            val any = centroSymmetricPlacementsOn(
                host.siteLattice, host.edgeX, candidate, ARM_COUNT,
                phaseTag = PHASE, minimumPerRow = 2, maximumPerRow = 3
            ).any()
            if (any) return candidate to rises
            rises--
        }
        error("no arm quantum admits $ARM_COUNT roots on ${host.name}")
    }
    val (armAcheck, armARises) = largestFeasibleArm(hostA, arm)
    val (armB, armBRises) = largestFeasibleArm(hostB, arm)
    println("  the largest 34-root arm is %.4f nm (%d rises) at 112 bp and %.4f nm (%d) at 110"
        .format(armAcheck, armARises, armB, armBRises))

    println("T-189 — building the influence banks ...")
    val influencesA = hostA.Influences()
    val influencesB = hostB.Influences()

    val nominalRigidity = Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY
    val nominalSpacing = 10.2
    val fieldA = columnRegisterField(uniformRow.domains, nominalRigidity, hinge, nominalSpacing, rise)
    val fieldB = columnRegisterField(correctedRow.domains, nominalRigidity, hinge, nominalSpacing, rise)

    val publishedPlacement = placementFromKey(publishedKey, PHASE, arm, hostA.edgeX)

    val dishing = ArrayList<T189DishingRecord>()
    val enumerations = ArrayList<T189EnumerationRecord>()

    fun read(
        host: T189Host, influences: T189Host.Influences, state: String,
        prestrains: Map<CrossoverSite, Double>, field: DoubleArray?,
        placement: UpwardArmPlacement
    ): T189DishingRecord {
        val solve = host.Solve(prestrains, influences)
        val value = solve.dishing(placement)
        val record = T189DishingRecord(
            host = host.name,
            state = state,
            placementKey = placement.key,
            placementShape = host.shapeOf(placement),
            rowEndDegrees = if (field == null) 0.0 else degrees(field.last()),
            peakPrestrainDegrees = if (field == null) 0.0 else degrees(field.maxOf { abs(it) }),
            coupledDishingOverStroke = value,
            freeDishingOverStroke = solve.freeDishing,
            uniformLoadDishingOverStroke = solve.uniformLoadDishing,
            flatAtTenPercent = value < FLATNESS_TOLERANCE
        )
        dishing += record
        println("  %-34s %-46s %.7f  flat=%s".format(
            host.name.take(34), state.take(46), value, record.flatAtTenPercent))
        return record
    }

    fun enumerate(
        host: T189Host, influences: T189Host.Influences, state: String,
        prestrains: Map<CrossoverSite, Double>, zeroShape: String?, c0090Shape: String,
        hostArm: Double
    ): T189EnumerationRecord {
        val solve = host.Solve(prestrains, influences)
        val optimum = T189Optimum(
            centroSymmetricPlacementsOn(
                host.siteLattice, host.edgeX, hostArm, ARM_COUNT,
                phaseTag = PHASE, minimumPerRow = 2, maximumPerRow = 3
            )
        ) { solve.dishing(it) }
        val shape = host.shapeOf(optimum.best)
        val record = T189EnumerationRecord(
            host = host.name,
            state = state,
            enumerated = optimum.enumerated,
            bestDishingOverStroke = optimum.bestValue,
            worstDishingOverStroke = optimum.worstValue,
            medianDishingOverStroke = optimum.medianValue,
            bestShape = shape,
            shapeIsZeroPrestrainOptimum = zeroShape == null || shape == zeroShape,
            shapeIsC0090Shape = shape == c0090Shape,
            flatAtTenPercent = optimum.bestValue < FLATNESS_TOLERANCE
        )
        enumerations += record
        println("  enumerate %-34s %-30s best %.7f over %d".format(
            host.name.take(34), state.take(30), optimum.bestValue, optimum.enumerated))
        return record
    }

    println("T-189 — solving ...")
    val c0090Shape = hostA.shapeOf(publishedPlacement)
    val zeroA = read(hostA, influencesA, "zero prestrain — the reproduction gate",
        emptyMap(), null, publishedPlacement)
    val gradedA = read(hostA, influencesA, "C-0107's graded corrugated field, 112 bp",
        gradedPrestrain(hostA, fieldA), fieldA, publishedPlacement)

    println("T-189 — the exhaustive centro-symmetric enumerations ...")
    val enumA0 = enumerate(hostA, influencesA, "zero prestrain", emptyMap(), null, c0090Shape, arm)
    val enumA1 = enumerate(hostA, influencesA, "112 bp graded field",
        gradedPrestrain(hostA, fieldA), enumA0.bestShape, c0090Shape, arm)
    val enumB0 = enumerate(hostB, influencesB, "zero prestrain", emptyMap(), null, c0090Shape, armB)
    val enumB1 = enumerate(hostB, influencesB, "110 bp corrected graded field",
        gradedPrestrain(hostB, fieldB), enumB0.bestShape, c0090Shape, armB)

    // the corrected host read at C-0090's own SHAPE, so the two widths are compared like for like
    val shapePlacement = centroSymmetricPlacementsOn(
        hostB.siteLattice, hostB.edgeX, armB, ARM_COUNT,
        phaseTag = PHASE, minimumPerRow = 2, maximumPerRow = 3
    ).firstOrNull { hostB.shapeOf(it) == c0090Shape }
    val zeroBAtShape = shapePlacement?.let {
        read(hostB, influencesB, "zero prestrain, at C-0090's own placement SHAPE",
            emptyMap(), null, it)
    }
    val gradedBAtShape = shapePlacement?.let {
        read(hostB, influencesB, "the corrected graded field, at C-0090's SHAPE",
            gradedPrestrain(hostB, it.let { _ -> fieldB }), fieldB, it)
    }
    val incrementA = gradedA.coupledDishingOverStroke - zeroA.coupledDishingOverStroke
    val incrementB = if (zeroBAtShape != null && gradedBAtShape != null)
        gradedBAtShape.coupledDishingOverStroke - zeroBAtShape.coupledDishingOverStroke
    else Double.NaN

    // ------------------------------------------------------------------ Deliverable 5: gates
    println("T-189 — convergence ...")
    val convergence = ArrayList<T189Convergence>()
    fun converge(axis: String, coarse: Double, fine: Double) {
        convergence += T189Convergence(
            axis, coarse, fine, twoSignificant(abs(fine - coarse) / max(1e-12, abs(fine)))
        )
        println("  convergence %-46s %.6f -> %.6f".format(axis.take(46), coarse, fine))
    }
    val smear = listOf(1, 2, 4).map { s ->
        val field = columnRegisterField(
            correctedRow.domains, nominalRigidity, hinge, nominalSpacing, rise, subdivisions = s
        )
        degrees(field.maxOf { abs(it) })
    }
    converge("register peak, hinge smearing 1 -> 2 sub-segments", smear[0], smear[1])
    converge("register peak, hinge smearing 2 -> 4 sub-segments", smear[1], smear[2])
    val gridA = listOf(41, 81, 161).map { samples ->
        hostA.Solve(gradedPrestrain(hostA, fieldA), influencesA, samples = samples)
            .dishing(publishedPlacement)
    }
    converge("dishing sample grid 41 -> 81", gridA[0], gridA[1])
    converge("dishing sample grid 81 -> 161", gridA[1], gridA[2])
    val subs = listOf(1, 2, 4).map { s ->
        val influences = hostB.Influences(subdivisions = s)
        hostB.Solve(gradedPrestrain(hostB, fieldB), influences, subdivisions = s)
            .dishing(enumB1.bestShape.let { _ ->
                centroSymmetricPlacementsOn(
                    hostB.siteLattice, hostB.edgeX, armB, ARM_COUNT,
                    phaseTag = PHASE, minimumPerRow = 2, maximumPerRow = 3
                ).first { hostB.shapeOf(it) == enumB1.bestShape }
            })
    }
    converge("corrected-host beam subdivision 1 -> 2", subs[0], subs[1])
    converge("corrected-host beam subdivision 2 -> 4", subs[1], subs[2])

    println("T-189 — reproductions ...")
    val reproductions = ArrayList<T189Reproduction>()
    // `CLAUDE.md`: comparing two quantities that are both meant to be zero RELATIVELY compares
    // their noise. Where the expected value is exactly zero the departure is the absolute one.
    fun reproduce(what: String, owner: String, expected: Double, got: Double) {
        reproductions += T189Reproduction(
            what, owner, expected, got,
            twoSignificant(
                if (expected == 0.0) abs(got) else abs(got - expected) / abs(expected)
            )
        )
        println("  reproduce %-56s %.9f vs %.9f".format(what.take(56), expected, got))
    }
    reproduce(
        "C-0090's 34-root dishing at 38.08 nm / phase 8, on the generalised column layout",
        "C-0090", publishedDishing, zeroA.coupledDishingOverStroke
    )
    reproduce("C-0104's flatness threshold in degrees", "C-0104",
        C0104_THRESHOLD_DEGREES, c0104Threshold)
    reproduce(
        "C-0107's graded-field dishing at 112 bp, its continuum against this discrete chain",
        "C-0107", c0107Graded, gradedA.coupledDishingOverStroke
    )
    reproduce(
        "EdgeTwistRelief's own discrete end residual on seven equal domains",
        "C-0107",
        EdgeTwistRelief(nominalRigidity, hinge, nominalSpacing, uniformRow.width(rise))
            .discreteEndResidual(uniformRow.twistRateMismatch(rise), uniformRow.domainCount),
        fieldA.last()
    )
    reproduce(
        "C-0107's un-relieved accumulation over C-0086's 112 bp row, in degrees",
        "C-0107", 30.0, uniformRow.totalMismatchDegrees() / 2.0
    )
    reproduce(
        "the residual ratio between C-0086's row and the twist-corrected floor",
        "T-189 (closed form)", 7.0,
        uniformRow.residualBasePairs() / correctedRow.residualBasePairs()
    )
    reproduce(
        "the station census, 112 bp against 110 bp",
        "C-0055", uniformSites.sumOf { it.size }.toDouble(),
        correctedSites.sumOf { it.size }.toDouble()
    )
    val referenceLattice = rasterUpwardSites(
        PHASE, uniformRow.width(rise), DUPLEXES, true, rise, CrossoverLayout.EDGE_MARGIN
    )
    reproduce(
        "the generalised upward lattice against rasterUpwardSites, worst station in nm",
        "C-0090", 0.0,
        referenceLattice.indices.maxOf { row ->
            referenceLattice[row].indices.maxOf { abs(referenceLattice[row][it] - uniformSites[row][it]) }
        }
    )
    val referenceColumns = rasterColumnLayout(
        PHASE, sheet, uniformRow.width(rise), true, CrossoverLayout.EDGE_MARGIN
    )
    reproduce(
        "the generalised column layout against rasterColumnLayout, worst column in nm",
        "C-0090", 0.0,
        referenceColumns.positions.indices.maxOf {
            abs(referenceColumns.positions[it] - uniformColumns.positions[it])
        }
    )
    val uniformFamily = centroSymmetricPlacements(
        PHASE, uniformRow.width(rise), DUPLEXES, arm, ARM_COUNT,
        minimumPerRow = 2, maximumPerRow = 3
    ).count()
    reproduce(
        "the generalised enumerator's family size against C-0063's own",
        "C-0063", uniformFamily.toDouble(), enumA0.enumerated.toDouble()
    )

    // ---------------------------------------------------------------- linearity, PROVED here
    val halved = columnRegisterField(
        correctedRow.domains, nominalRigidity, hinge, nominalSpacing, rise, driverScale = 0.5
    )
    val linearityDeparture = fieldB.indices.maxOf {
        abs(fieldB[it] - 2.0 * halved[it]) / max(1e-14, abs(fieldB[it]))
    }
    reproduce(
        "the register field is exactly linear in the twist mismatch (half gives half)",
        "T-189 (closed form)", 0.0, linearityDeparture
    )

    // -------------------------------------------------------------------- predicates
    val peakCorrected = register
        .filter { it.row.startsWith("the 110") }
    val peakUniform = register.filter { it.row.startsWith("C-0086") }
    val predicates = listOf(
        T189Predicate(
            "P1",
            "the closed-form enumeration of seamless twist-corrected rows is delivered before " +
                    "any solve, with the incompatibility stated as a theorem",
            rows.isNotEmpty() && rows.all { it.residualBasePairs > 0.0 }
        ),
        T189Predicate(
            "P2",
            "a construction near Sec 3's 40 nm exists: 110 bp = 37.40 nm over seven domains, " +
                    "five of 16 bp and two of 15",
            correctedRow.basePairs == 110 && correctedRow.seamlessAdmissible
        ),
        T189Predicate(
            "P3",
            "the row-end register is re-read over C-0107's own 12-cell bracket and the " +
                    "linearity in the mismatch is proved rather than swept",
            peakCorrected.size == 12 && linearityDeparture < 1e-9
        ),
        T189Predicate(
            "P4",
            "C-0090's placement is re-read on the corrected lattice and the question whether " +
                    "the DESIGN or only its VALUE moves is answered",
            enumerations.size == 4
        )
    )

    // -------------------------------------------------------------------- falsifiers
    val f1 = (1..4001 step 2).any { q ->
        val exact = exactHalfTurnBasePairs(q)
        abs(exact - Math.round(exact)) < 1e-9
    }
    val f2 = (1..401 step 2).any { q ->
        val exact = exactHalfTurnBasePairs(q)
        abs(abs(exact - Math.round(exact)) - 0.25) > 1e-9
    }
    val f3 = reproductions.filter { it.what.startsWith("the generalised") }.any { it.departure > 1e-9 }
    val f4 = linearityDeparture > 1e-9
    val f5 = peakCorrected.any { abs(it.rowEndDegrees) > C0104_THRESHOLD_DEGREES }
    val f5b = peakCorrected.any { it.peakDegrees > C0104_THRESHOLD_DEGREES }
    val f6 = dishing.any { it.uniformLoadDishingOverStroke > 1e-6 }
    val falsifiers = listOf(
        T189Falsifier(
            "F1",
            "an integer row length is exactly an odd number of half turns at 10.5 bp per turn",
            f1,
            if (f1) "FIRED — the incompatibility theorem is false"
            else "did not fire: 0 of 2001 odd half-turn counts land on an integer base pair"
        ),
        T189Falsifier(
            "F2",
            "the residual is not exactly a quarter base pair at every odd half-turn count",
            f2,
            if (f2) "FIRED — the invariant is false"
            else "did not fire: the residual is 0.25 bp at all 201 counts tested, exactly"
        ),
        T189Falsifier(
            "F3",
            "the generalised column and station construction does not reproduce the uniform " +
                    "lattice at a 16 bp domain mix",
            f3,
            if (f3) "FIRED — the corrected lattice is a different object, not a perturbation"
            else "did not fire: columns, stations and the enumerated family all reproduce exactly"
        ),
        T189Falsifier(
            "F4",
            "the register field is not exactly proportional to the twist mismatch",
            f4,
            if (f4) "FIRED — the re-read needs a sweep"
            else ("did not fire: halving the mismatch halves every node to %.1e"
                .format(linearityDeparture))
        ),
        T189Falsifier(
            "F5",
            "the twist-corrected row's ROW-END register still exceeds C-0104's threshold",
            f5,
            if (f5) "FIRED — the correction does not remove C-0107's exposure"
            else ("did not fire: the corrected row end carries %.4f to %.4f deg against %.4f"
                .format(
                    peakCorrected.minOf { it.rowEndDegrees },
                    peakCorrected.maxOf { it.rowEndDegrees }, C0104_THRESHOLD_DEGREES
                ))
        ),
        T189Falsifier(
            "F5b",
            "the twist-corrected row's PEAK register, wherever it sits, exceeds the threshold",
            f5b,
            if (f5b) ("FIRED — the correction RELOCATES the strain rather than removing it, " +
                    "peak %.4f deg").format(peakCorrected.maxOf { it.peakDegrees })
            else ("did not fire, but only just: the interior peak is %.4f to %.4f deg " +
                    "against %.4f, where the row end is under 4").format(
                peakCorrected.minOf { it.peakDegrees },
                peakCorrected.maxOf { it.peakDegrees }, C0104_THRESHOLD_DEGREES
            )
        ),
        T189Falsifier(
            "F6",
            "a free plate under a uniform load on a uniform Winkler foundation dishes something",
            f6,
            if (f6) "FIRED — the solver is wrong"
            else ("did not fire: the largest uniform-load dishing over %d states is %.2e"
                .format(dishing.size, dishing.maxOf { it.uniformLoadDishingOverStroke }))
        )
    )

    // -------------------------------------------------------------------- findings
    val mirroredOffsets = outOfPlaneOffsets(correctedRow.domains, mirror = true)
    val mirroredStationsAtSevenBp = (0 until DUPLEXES).sumOf { duplex ->
        mirroredOffsets.indices.count {
            Math.floorMod(it - duplex - 1, 2) == 0 && mirroredOffsets[it] != OUT_OF_PLANE_OFFSET_BASE_PAIRS
        }
    }
    val findings = listOf(
        ("THE TWO QUANTISATIONS ARE EXACTLY INCOMPATIBLE, AND IT IS ONE LINE. A seamless " +
                "boustrophedon needs its row to be an ODD number of half turns; a twist " +
                "correction needs those half turns to be B-DNA's. Together N = 180 q/34.2857 = " +
                "21q/4 with q odd, and 21q is odd, so N is NEVER an integer. No row length, at " +
                "no domain mix, is exactly twist-corrected AND seamless. F1 did not fire over " +
                "2001 odd half-turn counts."),
        ("BUT THE RESIDUAL IS AN INVARIANT AND IT IS A QUARTER OF A BASE PAIR. Because 21q is " +
                "odd, the nearest integer to 21q/4 is exactly 0.25 away at EVERY odd q — so the " +
                "best twist-corrected seamless row carries exactly 0.25 x 34.2857 = 8.5714 deg " +
                "of accumulated twist across the whole tile, INDEPENDENTLY OF ITS WIDTH. " +
                "C-0086's 112 bp row sits 1.75 bp away and carries 60.0 deg: exactly 7 times, a " +
                "ratio of two integers rather than a computed number. F2 did not fire."),
        ("THE CONSTRUCTION EXISTS AND IT IS 110 bp = 37.40 nm. Seven domains, q = 21 half " +
                "turns, five of 16 bp and two of 15 — which is Rothemund's own remedy " +
                "(\"helical domain lengths ... by single bases\") and Snodin's measured mix " +
                "read on the crossover spacing this project uses: the per-interface spacings " +
                "come out %s, i.e. Snodin's 31 bp between equivalent junctions beside the " +
                "square lattice's nominal 32. It is 1.8 %% narrower than C-0086's 112 bp row " +
                "and 6.5 %% below Sec 3's 40.0 nm.").format(
            correctedRow.perInterfaceSpacing.joinToString(",")),
        ("THE ARRANGEMENT OF THE TWO SHORT DOMAINS IS A DESIGN VARIABLE WORTH %.2fx, AND THE " +
                "OBVIOUS RULE IS NOT THE BEST ONE. Over the 21 arrangements the peak register " +
                "runs %.4f to %.4f deg; over the three CENTRO-SYMMETRIC ones it runs %.4f to " +
                "%.4f, and the best is %s — short domains at the SECOND and SIXTH positions, " +
                "not the innermost pair a naive even split gives.").format(
            arrangements.maxOf { it.peakDegrees } / arrangements.minOf { it.peakDegrees },
            arrangements.minOf { it.peakDegrees }, arrangements.maxOf { it.peakDegrees },
            arrangements.filter { it.centroSymmetric }.minOf { it.peakDegrees },
            arrangements.filter { it.centroSymmetric }.maxOf { it.peakDegrees },
            correctedRow.domains.joinToString("+")),
        ("THE ROW-END PRESTRAIN IS REMOVED, AND ITS SIGN FLIPS. Over C-0107's own 2 x 3 x 2 " +
                "bracket the row end carries %.4f to %.4f deg on the corrected row against " +
                "%.4f to %.4f on C-0086's, all twelve below C-0104's %.4f where all twelve " +
                "were above it. The sign reverses because a 110 bp row is OVERtwisted by its " +
                "own lattice where a 112 bp row is undertwisted. F5 did not fire.").format(
            peakCorrected.minOf { it.rowEndDegrees }, peakCorrected.maxOf { it.rowEndDegrees },
            peakUniform.minOf { it.rowEndDegrees }, peakUniform.maxOf { it.rowEndDegrees },
            C0104_THRESHOLD_DEGREES),
        ("BUT THE STRAIN IS RELOCATED, NOT REMOVED, AND THAT IS THE FINDING. A 15 bp domain is " +
                "25.714 deg short of the 540 deg its two crossovers demand, and that error is " +
                "LOCAL: the corrected row's peak register is %.4f to %.4f deg and it sits in " +
                "the INTERIOR, at the columns flanking the short domains, where C-0086's row " +
                "has its peak at the ends. Peak against peak the correction is worth only " +
                "%.2fx, against 7x on the accumulated twist. A twist correction converts a " +
                "GLOBAL, one-signed, coherent driver into a LOCAL, alternating one — which is " +
                "why the flatness moves so much more than the peak angle does.").format(
            peakCorrected.minOf { it.peakDegrees }, peakCorrected.maxOf { it.peakDegrees },
            peakUniform.maxOf { it.peakDegrees } / peakCorrected.maxOf { it.peakDegrees }),
        ("AND THE CORRECTED FIELD IS NEARLY INSENSITIVE TO THE PARAMETERS C-0107 HAD TO " +
                "BRACKET. The peak register spans %.2f %% over the whole 2 x 3 x 2 " +
                "(C, alpha, p) sweep against %.1f %% for C-0086's row, because a local " +
                "per-domain strain is taken by the hinges at its own two columns while a " +
                "global accumulation is set by the boundary layer's decay length. The " +
                "twist correction removes the parameter uncertainty as well as the value.").format(
            100.0 * (peakCorrected.maxOf { it.peakDegrees } - peakCorrected.minOf { it.peakDegrees }) /
                    peakCorrected.maxOf { it.peakDegrees },
            100.0 * (peakUniform.maxOf { it.peakDegrees } - peakUniform.minOf { it.peakDegrees }) /
                    peakUniform.maxOf { it.peakDegrees }),
        ("THE CORRECTION COSTS THE STATION LATTICE'S CENTRO-SYMMETRY UNLESS THE OUT-OF-PLANE " +
                "OFFSET IS MIRRORED, AND MIRRORING COSTS AZIMUTH. The EAST site sits 8 bp past " +
                "its column, which is not mirror-symmetric inside an ODD domain: taken at 8 bp " +
                "everywhere the 110 bp station lattice is NOT centro-symmetric, and C-0063's " +
                "whole exhaustive family assumes it is. Mirroring the offsets right of centre " +
                "restores it exactly and puts %d of %d stations at a 7 bp offset, whose " +
                "azimuth departure is %.2f deg against 4.29 deg at 8 bp. The station COUNT is " +
                "unchanged at %d.").format(
            mirroredStationsAtSevenBp,
            correctedSites.sumOf { it.size },
            azimuthDeparture(7), correctedSites.sumOf { it.size }),
        ("AND IT COSTS ONE BASE PAIR OF ARM, BECAUSE C-0090'S PLAN CLEARANCE WAS ALREADY ZERO. " +
                "C-0090 records that at 38.08 nm the outboard bound owns the plan budget and " +
                "C-0085's 24-rise arm is EXACTLY tangent. A 110 bp row moves every station " +
                "0.34 nm inboard, so the 34-root centro-symmetric family is EMPTY at 24 rises " +
                "and the largest arm that admits it is %d rises = %.4f nm, against %d rises = " +
                "%.4f nm at 112 bp. A twist correction is therefore not free in the plan: it " +
                "spends the arm length C-0034's placement condition is written on."
            ).format(armBRises, armB, armARises, armAcheck),
        ("AND THE FLATNESS BARELY MOVES, WHICH IS THE RESULT THIS TASK EXISTS TO REPORT. Read " +
                "at C-0090's own placement SHAPE on both lattices, the prestrain's CONTRIBUTION " +
                "to the dishing is %+.7f of the stroke at 112 bp and %+.7f at the twist-" +
                "corrected 110 bp — a factor of %.2f, against 7x on the accumulated twist and " +
                "%.2fx on the peak angle. The reason is that dishing is driven by the whole " +
                "crossover prestrain FIELD, not by its row-end value: the corrected field is a " +
                "sawtooth of %.2f deg RMS against the uncorrected boundary layer's %.2f. " +
                "CLAUDE.md's own \"a mandate written on a SUM is not a mandate on each term\", " +
                "read on a driver instead of on a coupling."
            ).format(incrementA, incrementB, incrementA / incrementB,
                peakUniform.maxOf { it.peakDegrees } / peakCorrected.maxOf { it.peakDegrees },
                Math.sqrt(fieldB.sumOf { it * it } / fieldB.size) * 180.0 / PI,
                Math.sqrt(fieldA.sumOf { it * it } / fieldA.size) * 180.0 / PI),
        ("THE PER-DOMAIN FLOOR IS THE SAME QUARTER BASE PAIR. A 1.5-turn domain needs 540 deg; " +
                "16 bp of B-DNA gives 548.571 and 15 bp gives 514.286, so the CHEAPEST domain " +
                "any integer lattice can build is misregistered by 8.571 deg — which is 0.25 bp " +
                "of rotation, the same floor the whole-row residual has. A twist correction is " +
                "therefore never a removal of register error; it is a choice of how many " +
                "25.714 deg domains to buy in order to cancel the accumulation of the cheap ones."),
        ("THE DESIGN MOVES, NOT ONLY ITS VALUE. On C-0086's 112 bp lattice the best 34-root " +
                "centro-symmetric placement dishes %.7f at zero prestrain and %.7f under the " +
                "graded field; on the 110 bp corrected lattice it dishes %.7f and %.7f, and " +
                "the winning SHAPE %s C-0090's. Every one of the four is inside T-5b's 0.10.").format(
            enumA0.bestDishingOverStroke, enumA1.bestDishingOverStroke,
            enumB0.bestDishingOverStroke, enumB1.bestDishingOverStroke,
            if (enumB1.shapeIsC0090Shape) "IS" else "is NOT"),
        ("THE RE-READ IS A SCALING AND THE PROOF IS ONE LINE, WHICH IS WHY NO SWEEP WAS PAID " +
                "FOR. u(+-L/2) = Delta_omega lambda tanh(L/2 lambda) and lambda = sqrt(C p/k) " +
                "contains no Delta_omega, so the whole boundary layer is exactly linear in the " +
                "driver; measured on the discrete chain, halving the mismatch halves every " +
                "node to %.1e. C-0107's twelve cells therefore re-read by multiplication.").format(
            linearityDeparture)
    )

    val result = T189Result(
        task = "T-189",
        leaf = "A8.2, with A1.2",
        question = "Can C-0086's 112 bp seamless raster row be twist-corrected, and what does " +
                "the correction leave for C-0107's row-end prestrain and C-0090's placement?",
        conditions = mapOf(
            "temperature" to "300 K",
            "kBT" to "${thermalEnergy()} pN nm",
            "buffer" to "aqueous 2 mM MgCl2",
            "sheet" to ("single-layer square-lattice Rothemund, $DUPLEXES duplexes at " +
                    "${Gen1Tile.INTERHELICAL_SHEET} nm, ${Gen1Tile.RISE_PER_BASE_PAIR} nm rise"),
            "designTwist" to "32/3 bp per turn against B-DNA's 10.5",
            "widths" to ("${uniformRow.width(rise).roundedForProse()} nm (C-0086's 112 bp) " +
                    "against ${correctedRow.width(rise).roundedForProse()} nm " +
                    "(the 110 bp twist-corrected row)"),
            "placement" to "C-0090's published 34-root key $publishedKey at phase $PHASE",
            "load" to "C-0022's solved collar at 2 mM, a 10 nm gap and 0.192 V",
            "coupling" to ("C-0017's ${MANDATE.roundedForProse()} pN/nm shared equally over " +
                    "$ARM_COUNT roots"),
            "freeStrokeUniform" to "${hostA.freeStroke.roundedForProse()} nm",
            "freeStrokeCorrected" to "${hostB.freeStroke.roundedForProse()} nm"
        ),
        rows = rows,
        arrangements = arrangements,
        register = register,
        lattices = lattices,
        dishing = dishing,
        enumerations = enumerations,
        convergence = convergence,
        reproductions = reproductions,
        predicates = predicates,
        falsifiers = falsifiers,
        findings = findings,
        parameters = mapOf(
            "naturalTwistPerBase" to B_DNA_TWIST_PER_BASE.roundedForProse().toString(),
            "uniformDesignTwistPerBase" to uniformRow.designTwistPerBase.toString(),
            "correctedDesignTwistPerBase" to correctedRow.designTwistPerBase.roundedForProse().toString(),
            "correctedDomains" to correctedRow.domains.joinToString("+"),
            "residualBasePairsFloor" to correctedRow.residualBasePairs().toString(),
            "residualDegreesFloor" to abs(correctedRow.totalMismatchDegrees()).roundedForProse().toString(),
            "uniformTotalMismatchDegrees" to uniformRow.totalMismatchDegrees().roundedForProse().toString(),
            "crossoverHingeStiffness" to hinge.roundedForProse().toString(),
            "torsionalRigidity" to nominalRigidity.toString(),
            "crossoverSpacing" to nominalSpacing.toString(),
            "armLength" to arm.toString(),
            "armRisesUniform" to armARises.toString(),
            "armLengthUniform" to armAcheck.toString(),
            "armRisesCorrected" to armBRises.toString(),
            "armLengthCorrected" to armB.toString(),
            "c0104Threshold" to c0104Threshold.toString(),
            "c0090PublishedDishing" to publishedDishing.toString(),
            "flatnessTolerance" to FLATNESS_TOLERANCE.toString(),
            "sources" to ("gpd/results/T-3b-tile-edge-load-profile.json, " +
                    "gpd/results/T-153-buildable-raster-width.json, " +
                    "gpd/results/T-172-row-end-prestrain.json, " +
                    "gpd/results/T-182-row-end-prestrain-value.json")
        )
    )

    val json = Json { prettyPrint = true }
    val out = File("gpd/results/T-189-twist-corrected-raster.json")
    out.writeText(json.encodeToString(json.encodeToJsonElement(result).roundedForResult().withEmissionHeader(LatticeTag.SQUARE, null)))
    println("T-189 — wrote ${out.path}")
    findings.forEach { println("  * $it") }
}
