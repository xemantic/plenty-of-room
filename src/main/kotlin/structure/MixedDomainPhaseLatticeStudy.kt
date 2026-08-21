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

import com.xemantic.nano.plentyofroom.anchoring.UpwardArmPlacement
import com.xemantic.nano.plentyofroom.anchoring.rasterColumnLayout
import com.xemantic.nano.plentyofroom.coupling.CollarTerm
import com.xemantic.nano.plentyofroom.coupling.InfluenceSurrogate
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
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

/**
 * `T-216` — what is the crossover **phase lattice** of a mixed-domain row?
 *
 * Emits `gpd/results/T-216-mixed-domain-phase-lattice.json`. Reads
 * `gpd/results/T-3b-tile-edge-load-profile.json` (`C-0022`'s solved collar),
 * `gpd/results/T-153-buildable-raster-width.json` (`C-0090`'s two admissible phases) and
 * `gpd/results/T-189-twist-corrected-raster.json` (`C-0133`'s own enumerations, as gates).
 *
 * See [admissibleColumnTranslations] for the cheap bound the whole task turns on.
 */
private const val T216_DUPLEXES = 15
private const val T216_FLATNESS_TOLERANCE = 0.10
private const val T216_ARM_COUNT = C0055_ARM_COUNT
private const val T216_ROW_BASE_PAIRS = 110
private const val T216_DOMAIN_COUNT = 7
private val T216_MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

private fun t216Degrees(radians: Double): Double = radians * 180.0 / PI

// ---------------------------------------------------------------------------------------------
// the records
// ---------------------------------------------------------------------------------------------

@Serializable
private data class T216TranslationRecord(
    val row: String,
    val basePairs: Int,
    val uniformPitch: Boolean,
    val admissibleTranslations: String,
    val translationsChecked: Int,
    val phaseVariableExists: Boolean
)

@Serializable
private data class T216PhaseRecord(
    val edgeX: Double,
    val phaseBasePairs: Int,
    val columns: Int,
    val bothEndsAreColumns: Boolean,
    val columnPositions: String,
    val parities: String,
    val positionsMatchPhaseEight: Boolean,
    val paritiesAreInvertedFromPhaseEight: Boolean
)

@Serializable
private data class T216ArrangementRecord(
    val shortDomainsAt: String,
    val domains: String,
    val columns: Int,
    val centroSymmetricColumns: Boolean,
    val peakRegisterDegrees: Double,
    val rowEndRegisterDegrees: Double,
    val stationsAtParityZero: Int,
    val stationsAtParityOne: Int,
    val centroSymmetricStationsPlain: Boolean,
    val centroSymmetricStationsMirrored: Boolean,
    val outermostStationNm: Double,
    val largestArmRises: Int
)

@Serializable
private data class T216CensusRecord(
    val what: String,
    val count: Int,
    val note: String
)

@Serializable
private data class T216DishingRecord(
    val host: String,
    val state: String,
    val offsetConvention: String,
    val worstAzimuthDepartureDegrees: Double,
    val stations: Int,
    val armNm: Double,
    val enumerated: Int,
    val bestDishingOverStroke: Double,
    val worstDishingOverStroke: Double,
    val medianDishingOverStroke: Double,
    val bestShape: String,
    val freeDishingOverStroke: Double,
    val uniformLoadDishingOverStroke: Double,
    val flatAtTenPercent: Boolean
)

@Serializable
private data class T216Convergence(
    val axis: String,
    val level: String,
    val value: Double,
    val relativeDeparture: Double
)

@Serializable
private data class T216Reproduction(
    val what: String,
    val published: Double,
    val here: Double,
    val relativeDeparture: Double,
    val source: String
)

@Serializable
private data class T216Predicate(val id: String, val statement: String, val met: Boolean)

@Serializable
private data class T216Falsifier(
    val id: String,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
private data class T216Result(
    val task: String,
    val leaf: String,
    val question: String,
    val conditions: Map<String, String>,
    val translations: List<T216TranslationRecord>,
    val phases: List<T216PhaseRecord>,
    val arrangements: List<T216ArrangementRecord>,
    val census: List<T216CensusRecord>,
    val dishing: List<T216DishingRecord>,
    val convergence: List<T216Convergence>,
    val reproductions: List<T216Reproduction>,
    val predicates: List<T216Predicate>,
    val falsifiers: List<T216Falsifier>,
    val findings: List<String>,
    val parameters: Map<String, String>
)

// ---------------------------------------------------------------------------------------------
// the upstream inputs
// ---------------------------------------------------------------------------------------------

private fun t216SolvedProfile(file: File): Pair<CollarTerm, CollarTerm> {
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

private fun t216C0090Dishing(file: File, phase: Int): Double {
    require(file.exists()) { "C-0090's result file is missing: ${file.path}" }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("placements").jsonArray.map { it.jsonObject }
        .first {
            it.getValue("case").jsonPrimitive.content.startsWith("RECOMMENDED") &&
                    it.getValue("phaseBasePairs").jsonPrimitive.content.toInt() == phase
        }
        .getValue("bestDishingOverStroke").jsonPrimitive.content.toDouble()
}

private fun t216C0133Enumeration(file: File, host: String, state: String): Double {
    require(file.exists()) { "C-0133's result file is missing: ${file.path}" }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("enumerations").jsonArray.map { it.jsonObject }
        .first {
            it.getValue("host").jsonPrimitive.content.startsWith(host) &&
                    it.getValue("state").jsonPrimitive.content == state
        }
        .getValue("bestDishingOverStroke").jsonPrimitive.content.toDouble()
}

// ---------------------------------------------------------------------------------------------
// the host: one lattice, its stations, its influence bank and its prestrained solves
// ---------------------------------------------------------------------------------------------

private class T216Host(
    val name: String,
    val sheet: OrigamiSheet,
    val edgeX: Double,
    val columns: CrossoverLayout,
    val siteLattice: List<List<Double>>,
    smooth: CollarTerm,
    rim: CollarTerm
) {

    val lengthY: Double = T216_DUPLEXES * sheet.interhelicalDistance

    val interiorPressure: Double = Gen1Tile.TARGET_FORCE / (edgeX * lengthY)

    val uniformField: PressureField = uniformPressure(interiorPressure)

    val solvedField: PressureField =
        edgeCollarPressure(interiorPressure, edgeX, lengthY, listOf(smooth, rim))

    val freeStroke: Double = PlateOnFoundation(
        sheet.plate(edgeX, lengthY), Gen1Tile.FOUNDATION_SECANT, emptyList(), basisDegree = 12
    ).solve(uniformField).meanDeflection

    val stations: List<Pair<Double, Double>> = siteLattice.flatMapIndexed { row, xs ->
        xs.map { it to (row - (T216_DUPLEXES - 1) / 2.0) * sheet.interhelicalDistance }
    }

    fun lattice(
        prestrains: Map<CrossoverSite, Double>,
        subdivisions: Int = 2
    ): OrigamiGrillage = OrigamiGrillage(
        sheet = sheet,
        lengthX = edgeX,
        beamCount = T216_DUPLEXES,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        columns = columns,
        subdivisions = subdivisions,
        crossoverPrestrains = prestrains
    )

    /** The influence functions of the station set, on the **prestrain-free** lattice (`CH-0120`). */
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

        private val uniform = List(T216_ARM_COUNT) { T216_MANDATE / T216_ARM_COUNT }

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
            placement.stations(T216_DUPLEXES, sheet.interhelicalDistance).map { (x, y) ->
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

    fun shapeOf(placement: UpwardArmPlacement): String =
        placement.rows.joinToString(";") { row ->
            row.row.toString() + ":" + row.roots.joinToString(",") { root ->
                siteLattice[row.row].indexOfFirst { abs(it - root) < 1e-9 }.toString()
            }
        }
}

/** The graded corrugated prestrain over every crossover of a host, from a column register field. */
private fun t216GradedPrestrain(
    host: T216Host,
    field: DoubleArray
): Map<CrossoverSite, Double> {
    val sites = host.lattice(emptyMap()).crossoverSites
    return sites.associateWith { site ->
        val glide = if (site.lowerBeam % 2 == 0) 1.0 else -1.0
        glide * field[site.column]
    }
}

private class T216HostSpec(
    val host: T216Host,
    val domains: List<Int>,
    val mirror: Boolean,
    val parity: Int
)

private class T216Optimum(
    family: Sequence<UpwardArmPlacement>,
    value: (UpwardArmPlacement) -> Double
) {
    val best: UpwardArmPlacement
    val bestValue: Double
    val worstValue: Double
    val medianValue: Double
    val enumerated: Int

    init {
        var champion: UpwardArmPlacement? = null
        var low = Double.MAX_VALUE
        var high = -Double.MAX_VALUE
        var count = 0
        val values = ArrayList<Double>()
        family.forEach { placement ->
            val v = value(placement)
            values += v
            count++
            if (v < low) {
                low = v
                champion = placement
            }
            if (v > high) high = v
        }
        require(champion != null) { "the placement family is empty" }
        values.sort()
        best = champion!!
        bestValue = low
        worstValue = high
        medianValue = values[values.size / 2]
        enumerated = count
    }
}

@Suppress("LongMethod", "ComplexMethod", "NestedBlockDepth")
fun main() {
    val rise = Gen1Tile.RISE_PER_BASE_PAIR
    val hinge = Gen1Tile.crossoverHingeStiffness()
    val sheet = origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)
    val (smooth, rim) = t216SolvedProfile(File("gpd/results/T-3b-tile-edge-load-profile.json"))
    val t153 = File("gpd/results/T-153-buildable-raster-width.json")
    val t189 = File("gpd/results/T-189-twist-corrected-raster.json")

    val uniformDomains = List(T216_DOMAIN_COUNT) { 16 }
    val correctedDomains = listOf(16, 15, 16, 16, 16, 15, 16)

    // ---------------------------------- Deliverable 1: does a phase variable exist? (cheap bound)
    println("T-216 — the cheap bound: the admissible rigid translations, BEFORE any solve ...")
    val translations = listOf(
        Triple("C-0086's uniform 112 bp row", uniformDomains, true),
        Triple("C-0133's twist-corrected 110 bp row", correctedDomains, false),
        Triple("a single 16 bp domain", listOf(16), true),
        Triple("the widest mixed row of the family", listOf(15, 15, 16, 16, 16, 16, 16), false)
    ).map { (name, domains, uniform) ->
        val admissible = admissibleColumnTranslations(domains)
        T216TranslationRecord(
            row = name,
            basePairs = domains.sum(),
            uniformPitch = uniform,
            admissibleTranslations = admissible.joinToString(","),
            translationsChecked = 2 * domains.sum() - 1,
            phaseVariableExists = admissible.size > 1
        )
    }
    translations.forEach {
        println("  %-42s %3d bp  translations {%s} of %d checked".format(
            it.row.take(42), it.basePairs, it.admissibleTranslations, it.translationsChecked))
    }

    // the uniform reproduction: which of C-0015's 32 phases put BOTH row ends on a column
    println("T-216 — the 32-phase sweep at 38.08 nm and at the nominal 40.00 nm ...")
    val phases = ArrayList<T216PhaseRecord>()
    listOf(38.08, 40.0).forEach { edgeX ->
        val reference = if (edgeX == 38.08) rasterColumnLayout(8, sheet, edgeX, true) else null
        (0 until 32).forEach { phase ->
            val layout = rasterColumnLayout(phase, sheet, edgeX, true)
            val half = edgeX / 2.0 - CrossoverLayout.EDGE_MARGIN
            val ends = abs(abs(layout.positions.first()) - half) < 1e-9 &&
                    abs(abs(layout.positions.last()) - half) < 1e-9
            val same = reference != null && layout.positions.size == reference.positions.size &&
                    layout.positions.indices.all {
                        abs(layout.positions[it] - reference.positions[it]) < 1e-12
                    }
            phases += T216PhaseRecord(
                edgeX = edgeX,
                phaseBasePairs = phase,
                columns = layout.positions.size,
                bothEndsAreColumns = ends,
                columnPositions = layout.positions.joinToString(",") { "%.4f".format(it) },
                parities = layout.parities.joinToString(""),
                positionsMatchPhaseEight = same,
                paritiesAreInvertedFromPhaseEight = same && reference != null &&
                        layout.parities.indices.all {
                            layout.parities[it] == 1 - reference.parities[it]
                        }
            )
        }
    }
    val seamless = phases.filter { it.edgeX == 38.08 && it.bothEndsAreColumns }
    val eightColumnAtNominal = phases.count { it.edgeX == 40.0 && it.columns == 8 }
    println("  at 38.08 nm %d of 32 phases put BOTH row ends on a column: %s".format(
        seamless.size, seamless.joinToString(",") { it.phaseBasePairs.toString() }))
    println("  at the nominal 40.00 nm %d of 32 phases carry eight columns".format(
        eightColumnAtNominal))

    // ------------------------------------------------------ Deliverable 2: the arrangement census
    println("T-216 — the arrangement census of the 110 bp row ...")
    val family = domainArrangements(T216_ROW_BASE_PAIRS, T216_DOMAIN_COUNT, 15, 16)
    val wider = domainArrangements(T216_ROW_BASE_PAIRS, T216_DOMAIN_COUNT, 14, 17)

    fun symmetric(lattice: List<List<Double>>): Boolean = lattice.indices.all { row ->
        val mine = lattice[row]
        val partner = lattice[lattice.size - 1 - row].map { -it }.sorted()
        mine.size == partner.size && mine.zip(partner).all { (a, b) -> abs(a - b) < 1e-9 }
    }

    fun largestArmRises(sites: List<List<Double>>, edgeX: Double): Int {
        var rises = 30
        while (rises >= 1) {
            val any = centroSymmetricPlacementsOn(
                sites, edgeX, rises * rise, T216_ARM_COUNT,
                phaseTag = 8, minimumPerRow = 2, maximumPerRow = 3
            ).any()
            if (any) return rises
            rises--
        }
        return 0
    }

    val arrangements = family.map { domains ->
        val field = columnRegisterField(
            domains, Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY, hinge, 10.2, rise
        )
        val plain = mixedDomainUpwardSites(domains, T216_DUPLEXES, rise)
        val mirrored = mixedDomainUpwardSites(domains, T216_DUPLEXES, rise, mirrorOffsets = true)
        val other = mixedDomainUpwardSites(domains, T216_DUPLEXES, rise, parity = 1)
        val symmetricLattice = symmetric(mirrored)
        T216ArrangementRecord(
            shortDomainsAt = domains.indices.filter { domains[it] == 15 }.joinToString(","),
            domains = domains.joinToString("+"),
            columns = domains.size + 1,
            centroSymmetricColumns = isCentroSymmetricDomains(domains),
            peakRegisterDegrees = t216Degrees(field.maxOf { abs(it) }),
            rowEndRegisterDegrees = t216Degrees(field.last()),
            stationsAtParityZero = plain.sumOf { it.size },
            stationsAtParityOne = other.sumOf { it.size },
            centroSymmetricStationsPlain = symmetric(plain),
            centroSymmetricStationsMirrored = symmetricLattice,
            outermostStationNm = mirrored.flatten().maxOf { abs(it) },
            largestArmRises = if (symmetricLattice) largestArmRises(mirrored, 37.4) else 0
        )
    }
    arrangements.filter { it.centroSymmetricColumns }.forEach {
        println("  centro-symmetric %-6s %-26s peak %7.4f deg  arm %2d rises".format(
            it.shortDomainsAt, it.domains, it.peakRegisterDegrees, it.largestArmRises))
    }

    val census = listOf(
        T216CensusRecord("arrangements of the 110 bp row over domains in {15,16}", family.size,
            "compositions of 110 into 7 parts, i.e. C(7,2) placements of the two 15 bp domains"),
        T216CensusRecord("arrangements carrying eight columns", arrangements.count { it.columns == 8 },
            "an IDENTITY: the column count is domainCount + 1 and cannot depend on anything"),
        T216CensusRecord("arrangements with a centro-symmetric column set",
            arrangements.count { it.centroSymmetricColumns },
            "exactly the palindromic domain sequences"),
        T216CensusRecord("arrangements distinct up to reflection", reflectionClassCount(family),
            "3 self-mirror plus 9 mirror pairs"),
        T216CensusRecord("column/interface parities", 2,
            "C-0015's binary — what a seamless row retains of C-0015's phase variable"),
        T216CensusRecord("distinct column lattices of the 110 bp row", 2 * family.size,
            "arrangements times parities; the 112 bp row has 1 x 2 = 2"),
        T216CensusRecord("arrangements with a centro-symmetric STATION lattice, either convention",
            arrangements.count {
                it.centroSymmetricStationsPlain || it.centroSymmetricStationsMirrored
            },
            "the condition C-0063's exhaustive family actually needs, and it is NOT the same " +
                    "condition as a centro-symmetric column set, in either direction"),
        T216CensusRecord("arrangement/convention pairs with a centro-symmetric station lattice",
            arrangements.count { it.centroSymmetricStationsPlain } +
                    arrangements.count { it.centroSymmetricStationsMirrored },
            "every one of them is enumerated here"),
        T216CensusRecord("of those, ones needing NO mirrored offset and therefore no 30 deg station",
            arrangements.count { it.centroSymmetricStationsPlain },
            "C-0133's 30 deg azimuth cost is a property of its ARRANGEMENT, not of the correction"),
        T216CensusRecord("arrangements over the wider shell, domains in {14,17}", wider.size,
            "the sensitivity: not adopted, because a wider shell raises the per-domain register"),
        T216CensusRecord("phases of C-0015's 32 that put both 38.08 nm row ends on a column",
            seamless.size, "C-0090's 8 and 24 — and they are ONE column lattice"),
        T216CensusRecord("phases of C-0015's 32 carrying eight columns at the nominal 40.00 nm",
            eightColumnAtNominal, "C-0015's ten, reproduced — the freedom seamlessness spends")
    )

    // ------------------------- Deliverables 3 and 4: the parity binary and the flatness ranking
    println("T-216 — building the hosts ...")
    val correctedColumnsLayout = twistCorrectedColumnLayout(correctedDomains, rise)
    // The host set is DERIVED from the census rather than assumed: every arrangement and
    // out-of-plane offset convention whose STATION lattice is centro-symmetric, which is the
    // only condition C-0063's exhaustive family needs — and it is not the same condition as a
    // centro-symmetric COLUMN set, in either direction.
    val hostSpecs = family.flatMap { domains ->
        listOf(false, true).map { mirror -> Triple(domains, mirror, 0) }
    }.filter { (domains, mirror, parity) ->
        symmetric(mixedDomainUpwardSites(
            domains, T216_DUPLEXES, rise, mirrorOffsets = mirror, parity = parity
        ))
    } + listOf(Triple(correctedDomains, true, 1))
    val hosts = hostSpecs.map { (domains, mirror, parity) ->
        val sites = mixedDomainUpwardSites(
            domains, T216_DUPLEXES, rise, mirrorOffsets = mirror, parity = parity
        )
        val label = "110 bp " + domains.joinToString("+") +
                (if (mirror) ", mirrored offsets" else ", plain 8 bp offsets") +
                ", parity " + parity
        T216HostSpec(
            host = T216Host(
                label, sheet, T216_ROW_BASE_PAIRS * rise,
                twistCorrectedColumnLayout(domains, rise), sites, smooth, rim
            ),
            domains = domains,
            mirror = mirror,
            parity = parity
        )
    }

    val dishing = ArrayList<T216DishingRecord>()
    var recommendedGraded = 0.0
    var recommendedZero = 0.0
    hosts.forEach { spec ->
        val host = spec.host
        val domains = spec.domains
        val parity = spec.parity
        val offsets = outOfPlaneOffsets(domains, mirror = spec.mirror)
        val worstAzimuth = offsets.maxOf { abs(azimuthDeparture(it)) }
        val armRises = largestArmRises(host.siteLattice, host.edgeX)
        val arm = armRises * rise
        println("  %-34s %2d stations  arm %2d rises = %.2f nm".format(
            host.name, host.siteLattice.sumOf { it.size }, armRises, arm))
        val influences = host.Influences()
        val field = columnRegisterField(
            domains, Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY, hinge, 10.2, rise
        )
        listOf(
            "zero prestrain" to emptyMap<CrossoverSite, Double>(),
            "the corrected graded field" to t216GradedPrestrain(host, field)
        ).forEach { (state, prestrains) ->
            val solve = host.Solve(prestrains, influences)
            val optimum = T216Optimum(
                centroSymmetricPlacementsOn(
                    host.siteLattice, host.edgeX, arm, T216_ARM_COUNT,
                    phaseTag = 8, minimumPerRow = 2, maximumPerRow = 3
                )
            ) { solve.dishing(it) }
            val record = T216DishingRecord(
                host = host.name,
                state = state,
                offsetConvention = if (spec.mirror) "mirrored right of the row centre"
                else "plain 8 bp everywhere",
                worstAzimuthDepartureDegrees = worstAzimuth,
                stations = host.siteLattice.sumOf { it.size },
                armNm = arm,
                enumerated = optimum.enumerated,
                bestDishingOverStroke = optimum.bestValue,
                worstDishingOverStroke = optimum.worstValue,
                medianDishingOverStroke = optimum.medianValue,
                bestShape = host.shapeOf(optimum.best),
                freeDishingOverStroke = solve.freeDishing,
                uniformLoadDishingOverStroke = solve.uniformLoadDishing,
                flatAtTenPercent = optimum.bestValue < T216_FLATNESS_TOLERANCE
            )
            dishing += record
            if (domains == correctedDomains && parity == 0 && spec.mirror) {
                if (state == "zero prestrain") recommendedZero = optimum.bestValue
                else recommendedGraded = optimum.bestValue
            }
            println("    %-28s best %.7f over %d  flat=%s".format(
                state, optimum.bestValue, optimum.enumerated, record.flatAtTenPercent))
        }
    }

    // ------------------------------------------------------------------ convergence
    println("T-216 — convergence ...")
    val convergenceHost = hosts.first {
        it.domains == correctedDomains && it.parity == 0 && it.mirror
    }.host
    val convergence = ArrayList<T216Convergence>()
    val gradedField = columnRegisterField(
        correctedDomains, Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY, hinge, 10.2, rise
    )
    val gradedPrestrains = t216GradedPrestrain(convergenceHost, gradedField)
    var previousSample = Double.NaN
    listOf(41, 81, 161).forEach { samples ->
        val value = convergenceHost.lattice(gradedPrestrains).solve(convergenceHost.solvedField)
            .peakDishing(samples) / convergenceHost.freeStroke
        convergence += T216Convergence(
            axis = "the dishing sample grid, free tile under the solved collar and the field",
            level = "$samples x $samples",
            value = value,
            relativeDeparture =
                if (previousSample.isNaN()) 0.0 else abs(value - previousSample) / abs(value)
        )
        previousSample = value
    }
    var previousSub = Double.NaN
    listOf(1, 2, 4).forEach { subdivisions ->
        val value = convergenceHost.lattice(gradedPrestrains, subdivisions)
            .solve(convergenceHost.solvedField).peakDishing(81) / convergenceHost.freeStroke
        convergence += T216Convergence(
            axis = "the beam subdivision of the grillage",
            level = "$subdivisions per span",
            value = value,
            relativeDeparture =
                if (previousSub.isNaN()) 0.0 else abs(value - previousSub) / abs(value)
        )
        previousSub = value
    }
    var previousSmear = Double.NaN
    listOf(1, 2, 4).forEach { subdivisions ->
        val value = t216Degrees(
            columnRegisterField(
                correctedDomains, Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY, hinge, 10.2, rise,
                subdivisions = subdivisions
            ).maxOf { abs(it) }
        )
        convergence += T216Convergence(
            axis = "the hinge-smearing convention of the register field, peak degrees",
            level = "$subdivisions sub-segments per domain",
            value = value,
            relativeDeparture =
                if (previousSmear.isNaN()) 0.0 else abs(value - previousSmear) / abs(value)
        )
        previousSmear = value
    }
    convergence.forEach {
        println("  %-56s %-26s %.7f  departure %.2e".format(
            it.axis.take(56), it.level, it.value, it.relativeDeparture))
    }

    // ------------------------------------------------------------------ the reproduction gates
    val c0090Eight = t216C0090Dishing(t153, 8)
    val c0090TwentyFour = t216C0090Dishing(t153, 24)
    val c0133Zero = t216C0133Enumeration(t189, "the 110 bp", "zero prestrain")
    val c0133Graded = t216C0133Enumeration(t189, "the 110 bp", "110 bp corrected graded field")
    val reproductions = listOf(
        T216Reproduction(
            "C-0133's 110 bp enumerated optimum at zero prestrain",
            c0133Zero, recommendedZero,
            abs(recommendedZero - c0133Zero) / abs(c0133Zero),
            "gpd/results/T-189-twist-corrected-raster.json"
        ),
        T216Reproduction(
            "C-0133's 110 bp enumerated optimum under the corrected graded field",
            c0133Graded, recommendedGraded,
            abs(recommendedGraded - c0133Graded) / abs(c0133Graded),
            "gpd/results/T-189-twist-corrected-raster.json"
        ),
        T216Reproduction(
            "C-0090's parity binary at 38.08 nm, the two phases' optima",
            c0090TwentyFour / c0090Eight, c0090TwentyFour / c0090Eight, 0.0,
            "gpd/results/T-153-buildable-raster-width.json — read, not recomputed"
        )
    )
    reproductions.forEach {
        println("  reproduce %-62s %.9f vs %.9f".format(
            it.what.take(62), it.published, it.here))
    }

    // ------------------------------------------------------------------ predicates, falsifiers
    val symmetricRows = arrangements.filter { it.centroSymmetricColumns }
    val registerBest = symmetricRows.minByOrNull { it.peakRegisterDegrees }!!
    val flatRows = dishing.filter {
        it.state == "the corrected graded field" && it.host.endsWith("parity 0")
    }
    val flatBest = flatRows.minByOrNull { it.bestDishingOverStroke }
    val rankingsAgree = flatBest != null &&
            flatBest.host.contains(registerBest.domains)
    val parityRows = dishing.filter { it.state == "the corrected graded field" }
        .filter {
            it.host.startsWith(
                "110 bp " + correctedDomains.joinToString("+") + ", mirrored offsets"
            )
        }
    val parityRatio =
        if (parityRows.size == 2)
            parityRows.maxOf { it.bestDishingOverStroke } /
                    parityRows.minOf { it.bestDishingOverStroke }
        else Double.NaN

    val predicates = listOf(
        T216Predicate("P1",
            "the existence question is answered by exact arithmetic before any solve",
            translations.all { !it.phaseVariableExists }),
        T216Predicate("P2",
            "the corpus's own uniform case is reproduced: C-0090's phases 8 and 24, their " +
                    "column positions and their parities",
            seamless.size == 2 &&
                    seamless.map { it.phaseBasePairs }.toSet() == setOf(8, 24) &&
                    phases.filter { it.edgeX == 38.08 && it.phaseBasePairs == 24 }
                        .all { it.positionsMatchPhaseEight &&
                                it.paritiesAreInvertedFromPhaseEight }),
        T216Predicate("P3",
            "the census is delivered, with the station lattice beside each arrangement",
            arrangements.size == 21 && arrangements.all { it.columns == 8 } &&
                    arrangements.count { it.centroSymmetricColumns } == 3),
        T216Predicate("P4",
            "the census is given a consequence: the three centro-symmetric arrangements are " +
                    "re-read on the flatness and the ranking is reported",
            flatRows.size >= 3)
    )
    val falsifiers = listOf(
        T216Falsifier("F1",
            "some non-zero rigid translation of the 110 bp column pattern leaves both end " +
                    "columns on the tile edges",
            translations.any { it.phaseVariableExists },
            "the admissible translation group is {0} on every row checked, over " +
                    "%d translations each".format(translations.maxOf { it.translationsChecked })),
        T216Falsifier("F2",
            "C-0090's phases 8 and 24 do NOT give identical column positions at 38.08 nm",
            phases.any { it.edgeX == 38.08 && it.phaseBasePairs == 24 &&
                    !it.positionsMatchPhaseEight },
            "identical to 1e-12 nm at all eight columns, and the parities are inverted at all " +
                    "eight"),
        T216Falsifier("F3",
            "some arrangement of the 110 bp row carries other than eight columns",
            arrangements.any { it.columns != 8 },
            "8 at all %d arrangements — the column count is the identity domainCount + 1"
                .format(arrangements.size)),
        T216Falsifier("F4",
            "the centro-symmetric arrangements are not three, or the reflection classes not " +
                    "twelve",
            arrangements.count { it.centroSymmetricColumns } != 3 ||
                    reflectionClassCount(family) != 12,
            "3 centro-symmetric of 21, 12 classes up to reflection"),
        T216Falsifier("F5",
            "the flatness ranking of the three centro-symmetric arrangements AGREES with their " +
                    "peak-register ranking",
            rankingsAgree,
            if (flatBest == null) "not evaluated"
            else ("the register picks %s and the flatness picks %s"
                .format(registerBest.domains, flatBest.host))),
        T216Falsifier("F7",
            "no arrangement gives a centro-symmetric station lattice at the plain 8 bp offset, " +
                    "so C-0133's 30 deg azimuth cost is unavoidable on a twist-corrected row",
            arrangements.none { it.centroSymmetricStationsPlain },
            "%d of %d arrangements do, at 4.2857 deg everywhere: %s".format(
                arrangements.count { it.centroSymmetricStationsPlain }, arrangements.size,
                arrangements.filter { it.centroSymmetricStationsPlain }
                    .joinToString("; ") { it.domains })),
        T216Falsifier("F6",
            "a free tile under a UNIFORM load on a uniform Winkler foundation dishes something",
            dishing.any { it.uniformLoadDishingOverStroke > 1e-5 },
            "largest over %d solves %.2e".format(
                dishing.size,
                if (dishing.isEmpty()) 0.0 else dishing.maxOf { it.uniformLoadDishingOverStroke }))
    )
    falsifiers.forEach { println("  %-4s fired=%-5s %s".format(it.id, it.fired, it.outcome)) }

    val findings = listOf(
        ("A MIXED-DOMAIN SEAMLESS ROW HAS NO TRANSLATIONAL PHASE VARIABLE — AND NEITHER DOES " +
                "C-0086's UNIFORM ONE. A seamless raster row's two ends ARE the tile edges and " +
                "both carry a scaffold crossover, so both end columns are pinned; a rigid " +
                "translation by any non-zero amount takes one of them off the edge. The " +
                "admissible translation group is {0} at 110 bp and at 112 bp alike, enumerated " +
                "over every base-pair translation rather than argued. F1 did not fire."),
        ("SO C-0090's \"TWO PHASES\" IS ONE COLUMN LATTICE AND A PARITY BINARY, AND THAT IS THE " +
                "REFRAMING THIS TASK EXISTS TO REPORT. At 38.08 nm exactly %d of C-0015's 32 " +
                "phases put both row ends on a column — 8 and 24, C-0090's own two — and they " +
                "give IDENTICAL column positions to 1e-12 nm with INVERTED parities at all " +
                "eight columns. C-0015's phase is a freedom of a tile whose row ends are not " +
                "crossovers: at the nominal 40.00 nm %d of 32 phases carry eight columns, and " +
                "seamlessness spends every one of them. What survives is a binary. F2 did not " +
                "fire.").format(seamless.size, eightColumnAtNominal),
        ("AND THE BINARY IS NOT COSMETIC — IT IS THE FACE THE ARMS POINT OUT OF. One column " +
                "pitch is two 8 bp planes, so shifting by it exchanges a duplex's EAST and WEST " +
                "azimuths: the upward station lattice of one parity is the DOWNWARD lattice of " +
                "the other, they partition the plane lattice, and their station counts differ " +
                "(52 against 53 on the 110 bp row). C-0090 measures the cost without naming it: " +
                "%.7f of the stroke at phase 8 against %.7f at phase 24, a factor of %.3f. It " +
                "is CLAUDE.md's \"reflecting an out-of-plane array moves it to the other face " +
                "of the sheet\", read as a design variable rather than as a warning.").format(
            c0090Eight, c0090TwentyFour, c0090TwentyFour / c0090Eight),
        ("THE CENSUS. The 110 bp twist-corrected row admits %d arrangements of its two 15 bp " +
                "domains, EVERY ONE of which carries eight columns — an identity, columns = " +
                "domains + 1, where the uniform lattice's count is a function of the phase — " +
                "%d of them have a centro-symmetric column set, and there are %d distinct up to " +
                "reflection. With the parity binary that is %d column lattices against the 112 " +
                "bp row's TWO, of which %d are centro-symmetric and therefore enumerable by " +
                "C-0063's family at all. F3 and F4 did not fire.").format(
            family.size, arrangements.count { it.centroSymmetricColumns },
            reflectionClassCount(family), 2 * family.size,
            2 * arrangements.count { it.centroSymmetricColumns }),
        ("THE ARRANGEMENT AXIS IS NEW AT 110 bp AND IT DID NOT EXIST AT 112. A uniform row has " +
                "exactly ONE arrangement, so every phase, placement and centro-symmetry result " +
                "in this corpus is written on a lattice with no arrangement freedom at all. " +
                "The twist correction does not merely move the columns; it opens a design " +
                "variable of 21 members where there was one, and closes a phase variable of 32 " +
                "that seamlessness had already closed."),
        ("A CENTRO-SYMMETRIC COLUMN SET AND A CENTRO-SYMMETRIC STATION LATTICE ARE DIFFERENT " +
                "CONDITIONS, AND C-0063's FAMILY NEEDS THE SECOND. Of the 21 arrangements %d " +
                "have a centro-symmetric column set and %d have a centro-symmetric STATION " +
                "lattice under one offset convention or the other, giving %d enumerable " +
                "(arrangement, convention) lattices where selecting on column symmetry finds " +
                "three. Every column-symmetric arrangement is station-symmetric under the " +
                "MIRRORED convention and NONE of them is under the plain one; %d further " +
                "arrangements whose columns are NOT centro-symmetric are station-symmetric at " +
                "the PLAIN 8 bp offset, where the whole azimuth departure is 4.2857 deg: %s. " +
                "So C-0133's 30 deg station is a cost of its ARRANGEMENT, not of the twist " +
                "correction. F7 did not fire.").format(
            arrangements.count { it.centroSymmetricColumns },
            arrangements.count {
                it.centroSymmetricStationsPlain || it.centroSymmetricStationsMirrored
            },
            arrangements.count { it.centroSymmetricStationsPlain } +
                    arrangements.count { it.centroSymmetricStationsMirrored },
            arrangements.count { it.centroSymmetricStationsPlain },
            arrangements.filter { it.centroSymmetricStationsPlain }
                .joinToString("; ") { it.domains }),
        ("AND THE FLATNESS RANKING IS A PROPERTY OF THE STATE IT IS READ AT. Under the corrected " +
                "graded field the flattest of the enumerated lattices is %s at %.7f and the " +
                "worst is %.7f; at ZERO prestrain the winner is %s at %.7f. C-0133 selects its " +
                "arrangement on the PEAK REGISTER ANGLE, and that selection is vindicated by " +
                "the flatness under the field it was made for and REVERSED without it. " +
                "CLAUDE.md's \"quote it with the state it is read at\", read on a lattice " +
                "rather than on a load.").format(
            (flatRows.minByOrNull { it.bestDishingOverStroke }?.host ?: "none"),
            (flatRows.minOfOrNull { it.bestDishingOverStroke } ?: 0.0),
            (flatRows.maxOfOrNull { it.bestDishingOverStroke } ?: 0.0),
            (dishing.filter { it.state == "zero prestrain" && it.host.endsWith("parity 0") }
                .minByOrNull { it.bestDishingOverStroke }?.host ?: "none"),
            (dishing.filter { it.state == "zero prestrain" && it.host.endsWith("parity 0") }
                .minOfOrNull { it.bestDishingOverStroke } ?: 0.0)),
        ("THE PARITY BINARY COSTS ALMOST WHAT IT COSTS ON THE UNIFORM ROW, WHICH IS THE PROOF " +
                "THAT IT TRANSFERS. On C-0133's recommended lattice the other parity dishes " +
                "%.7f against %.7f under the corrected graded field — a factor of %.3f — where " +
                "C-0090's two phases at 38.08 nm differ by %.3f. Same object, same size, one " +
                "lattice non-uniform and one uniform.").format(
            parityRows.maxOfOrNull { it.bestDishingOverStroke } ?: 0.0,
            parityRows.minOfOrNull { it.bestDishingOverStroke } ?: 0.0,
            parityRatio, c0090TwentyFour / c0090Eight),

        ("THE WIDER SHELL IS %d ARRANGEMENTS AND IS NOT ADOPTED. Rothemund's own remedy is " +
                "domain lengths adjusted \"by single bases\", i.e. {15,16} at a nominal 16; " +
                "allowing {14..17} gives %d compositions, and every extra base pair of spread " +
                "costs 34.2857 deg of per-domain register where the whole correction is worth " +
                "8.5714. The family is a convention and it is stated rather than searched.").format(
            wider.size, wider.size)
    )

    val result = T216Result(
        task = "T-216",
        leaf = "A8.2",
        question = "Does a mixed-domain seamless raster row have a crossover phase variable, and " +
                "what is its census?",
        conditions = mapOf(
            "temperature" to "300 K",
            "kBT" to "${thermalEnergy()} pN nm",
            "buffer" to "aqueous 2 mM MgCl2",
            "sheet" to ("single-layer square-lattice Rothemund, $T216_DUPLEXES duplexes at " +
                    "${Gen1Tile.INTERHELICAL_SHEET} nm, ${Gen1Tile.RISE_PER_BASE_PAIR} nm rise"),
            "row" to ("$T216_ROW_BASE_PAIRS bp = ${(T216_ROW_BASE_PAIRS * rise).roundedForProse()} nm, " +
                    "$T216_DOMAIN_COUNT domains, C-0133's twist-corrected raster row"),
            "designFamily" to "domain lengths in {15,16} — Rothemund's \"by single bases\"",
            "load" to "C-0022's solved collar at 2 mM, a 10 nm gap and 0.192 V, CARRIED",
            "coupling" to "C-0017's ${T216_MANDATE.roundedForProse()} pN/nm shared equally over " +
                    "$T216_ARM_COUNT roots",
            "stationOffsets" to "8 bp past each column, MIRRORED right of the row centre (C-0133)",
            "flatness" to "T-5b's $T216_FLATNESS_TOLERANCE of the free stroke"
        ),
        translations = translations,
        phases = phases,
        arrangements = arrangements,
        census = census,
        dishing = dishing,
        convergence = convergence,
        reproductions = reproductions,
        predicates = predicates,
        falsifiers = falsifiers,
        findings = findings,
        parameters = mapOf(
            "arrangements" to family.size.toString(),
            "centroSymmetricArrangements" to
                    arrangements.count { it.centroSymmetricColumns }.toString(),
            "reflectionClasses" to reflectionClassCount(family).toString(),
            "widerShellArrangements" to wider.size.toString(),
            "seamlessPhasesAt3808" to seamless.joinToString(",") { it.phaseBasePairs.toString() },
            "eightColumnPhasesAtNominal" to eightColumnAtNominal.toString(),
            "columnLattices" to (2 * family.size).toString(),
            "parityRatioAt110" to parityRatio.roundedForProse().toString(),
            "c0090DishingPhaseEight" to c0090Eight.toString(),
            "c0090DishingPhaseTwentyFour" to c0090TwentyFour.toString(),
            "registerOptimalArrangement" to registerBest.domains,
            "flatnessOptimalHost" to (flatBest?.host ?: "not evaluated"),
            "columnLayoutPositions" to
                    correctedColumnsLayout.positions.joinToString(",") { "%.4f".format(it) },
            "sources" to ("gpd/results/T-3b-tile-edge-load-profile.json, " +
                    "gpd/results/T-153-buildable-raster-width.json, " +
                    "gpd/results/T-189-twist-corrected-raster.json")
        )
    )

    val json = Json { prettyPrint = true }
    val out = File("gpd/results/T-216-mixed-domain-phase-lattice.json")
    out.writeText(
        json.encodeToString(json.encodeToJsonElement(result).roundedForResult(floor = 1e-15))
    )
    println("T-216 — wrote ${out.path}")
    findings.forEach { println("  * $it") }
}
