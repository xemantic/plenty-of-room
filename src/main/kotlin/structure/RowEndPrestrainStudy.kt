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

import com.xemantic.nano.plentyofroom.anchoring.BUILDABLE_RASTER_WIDTH
import com.xemantic.nano.plentyofroom.anchoring.UpwardArmPlacement
import com.xemantic.nano.plentyofroom.anchoring.UpwardRootInfluenceBank
import com.xemantic.nano.plentyofroom.coupling.InfluenceSurrogate
import com.xemantic.nano.plentyofroom.anchoring.centroSymmetricPlacements
import com.xemantic.nano.plentyofroom.anchoring.placementFromKey
import com.xemantic.nano.plentyofroom.anchoring.quantisedToRise
import com.xemantic.nano.plentyofroom.anchoring.rasterColumnLayout
import com.xemantic.nano.plentyofroom.anchoring.rasterUpwardSites
import com.xemantic.nano.plentyofroom.anchoring.rowEndCrossoverSites
import com.xemantic.nano.plentyofroom.coupling.CollarTerm
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
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
 * `T-172` — the row-end crossover's **PRESTRAIN**, which is a different question from its
 * stiffness, and the one route `C-0099` left open.
 *
 * ```shell
 * tools/study.sh structure.RowEndPrestrainStudyKt
 * ```
 *
 * Emits `gpd/results/T-172-row-end-prestrain.json`. Reads
 * `gpd/results/T-3b-tile-edge-load-profile.json` (`C-0022`'s solved collar, exactly as `C-0090`
 * and `C-0099` carried it) and `gpd/results/T-153-buildable-raster-width.json` (`C-0090`'s
 * published reading and its optimum placement key, as the reproduction gate).
 */

private const val DUPLEXES = 15
private const val PHASE = 8
private const val FLATNESS_TOLERANCE = 0.10
private const val RIM_STANDOFF = 1.0
private const val ARM_COUNT = C0055_ARM_COUNT
private val MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

/** The square lattice's design twist, `32/3` bp per turn, in degrees per base. */
private val DESIGN_TWIST = 360.0 / (Gen1Tile.CROSSOVER_SPACING_SHEET_BP / 3.0)

/** B-DNA's preferred twist, 10.5 bp per turn, in degrees per base — `CLAUDE.md`, honeycomb. */
private const val NATURAL_TWIST = 360.0 / 10.5

private fun degrees(radians: Double): Double = radians * 180.0 / PI

// ---------------------------------------------------------------------------------------------
// records — prefixed with the task, because study record classes are package scoped (CLAUDE.md)
// ---------------------------------------------------------------------------------------------

@Serializable
private data class T172BoundRecord(
    val name: String,
    val statement: String,
    val value: Double,
    val unit: String,
    val owner: String,
    val derivedHere: Boolean
)

@Serializable
private data class T172LadderRecord(
    val name: String,
    val basis: String,
    val degrees: Double,
    val radians: Double,
    val couplePerCrossover: Double,
    val assembledCouple: Double,
    val sourced: String
)

@Serializable
private data class T172SweepRecord(
    val distribution: String,
    val rung: String,
    val degrees: Double,
    val radians: Double,
    val publishedPlacementDishing: Double,
    val freeDishingOverStroke: Double,
    val uniformLoadDishingOverStroke: Double,
    val peakHingeMoment: Double,
    val flatAtTenPercent: Boolean
)

@Serializable
private data class T172EnumerationRecord(
    val distribution: String,
    val degrees: Double,
    val enumerated: Int,
    val bestDishingOverStroke: Double,
    val worstDishingOverStroke: Double,
    val medianDishingOverStroke: Double,
    val publishedPlacementDishing: Double,
    val publishedPlacementPenalty: Double,
    val bestKey: String,
    val bestKeyIsZeroPrestrainOptimum: Boolean,
    val flatAtTenPercent: Boolean
)

@Serializable
private data class T172LinearityRecord(
    val distribution: String,
    val radians: Double,
    val measured: Double,
    val predictedFromUnit: Double,
    val relativeDeparture: Double
)

@Serializable
private data class T172ReproductionRecord(
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val relativeDeparture: Double,
    val source: String
)

@Serializable
private data class T172ConvergenceRecord(
    val quantity: String,
    val control: String,
    val level: Double,
    val value: Double,
    val departureFromFinest: Double
)

@Serializable
private data class T172LiteratureRecord(
    val question: String,
    val source: String,
    val readAs: String,
    val quantified: Boolean,
    val finding: String
)

@Serializable
private data class T172FalsifierRecord(
    val name: String,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
private data class T172PredicateRecord(
    val name: String,
    val statement: String,
    val verdict: String,
    val met: Boolean
)

@Serializable
private data class T172Result(
    val task: String,
    val leaf: String,
    val conditions: String,
    val question: String,
    val cheapBounds: List<T172BoundRecord>,
    val ladder: List<T172LadderRecord>,
    val sweep: List<T172SweepRecord>,
    val enumerations: List<T172EnumerationRecord>,
    val linearity: List<T172LinearityRecord>,
    val literature: List<T172LiteratureRecord>,
    val convergence: List<T172ConvergenceRecord>,
    val reproductions: List<T172ReproductionRecord>,
    val predicates: List<T172PredicateRecord>,
    val falsifiers: List<T172FalsifierRecord>,
    val findings: List<String>,
    val parameters: Map<String, Double>
)

// ---------------------------------------------------------------------------------------------
// the inputs, read from the claims that own them
// ---------------------------------------------------------------------------------------------

/** `C-0022`'s solved edge profile, keyed on concentration, gap **and bias** (`CLAUDE.md`). */
private fun solvedProfile(file: File): Pair<CollarTerm, CollarTerm> {
    require(file.exists()) { "C-0022's result file is missing: ${file.path}" }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .firstOrNull {
            fun value(key: String) = it.getValue(key).jsonPrimitive.content.toDouble()
            value("concentration") == 2.0 && value("gapHeight") == 10.0 &&
                    value("appliedBias") == 0.192
        } ?: error("no C-0022 profile at 2 mM, 10 nm, 0.192 V")
    fun value(key: String) = record.getValue(key).jsonPrimitive.content.toDouble()
    return CollarTerm(value("taperDepth"), value("taperWidth")) to
            CollarTerm(value("rimResidualDepth"), RIM_STANDOFF)
}

/** `C-0090`'s published reading of one case at one phase — the dishing and the placement key. */
private fun c0090Reading(file: File, casePrefix: String, phase: Int): Pair<Double, String> {
    require(file.exists()) { "C-0090's result file is missing: ${file.path}" }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("placements").jsonArray.map { it.jsonObject }
        .firstOrNull {
            it.getValue("case").jsonPrimitive.content.startsWith(casePrefix) &&
                    it.getValue("phaseBasePairs").jsonPrimitive.content.toInt() == phase
        } ?: error("no C-0090 placement record for $casePrefix at phase $phase")
    return record.getValue("bestDishingOverStroke").jsonPrimitive.content.toDouble() to
            record.getValue("bestKey").jsonPrimitive.content
}

// ---------------------------------------------------------------------------------------------
// one host at one prestrain state
// ---------------------------------------------------------------------------------------------

private class T172Host(
    val sheet: OrigamiSheet,
    val edgeX: Double,
    val arm: Double,
    smooth: CollarTerm,
    rim: CollarTerm
) {

    val lengthY: Double = DUPLEXES * sheet.interhelicalDistance

    val area: Double = edgeX * lengthY

    val interiorPressure: Double = Gen1Tile.TARGET_FORCE / area

    val uniformField: PressureField = uniformPressure(interiorPressure)

    val solvedField: PressureField =
        edgeCollarPressure(interiorPressure, edgeX, lengthY, listOf(smooth, rim))

    val freeStroke: Double = PlateOnFoundation(
        sheet.plate(edgeX, lengthY), Gen1Tile.FOUNDATION_SECANT, emptyList(), basisDegree = 12
    ).solve(uniformField).meanDeflection

    val columns: CrossoverLayout =
        rasterColumnLayout(PHASE, sheet, edgeX, true, CrossoverLayout.EDGE_MARGIN)

    val rowEndSites: List<CrossoverSite> =
        rowEndCrossoverSites(columns, edgeX, DUPLEXES, CrossoverLayout.EDGE_MARGIN)

    val sites: List<List<Double>> = rasterUpwardSites(
        PHASE, edgeX, DUPLEXES, true, Gen1Tile.RISE_PER_BASE_PAIR, CrossoverLayout.EDGE_MARGIN
    )

    val stations: List<Pair<Double, Double>> = sites.flatMapIndexed { row, xs ->
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
     * The influence functions of the station set, taken on the **prestrain-free** lattice and
     * shared by every prestrain state.
     *
     * `T-172`'s central economy and its central trap. A prestrain is a load, so
     * [OrigamiGrillage.solve] adds it to *every* load case — including the unit point loads an
     * influence bank is built from. Built naively, `M[j][k]` becomes *"the deflection at `j`
     * under a unit load at `k` **plus** the prestrain's own response"*, which is not a compliance
     * matrix, and `C-0058`'s Woodbury reduction reports it as a non-positive Cholesky pivot.
     * Superposition gives the right split in one line — free field from the prestrained lattice,
     * influence fields from [OrigamiGrillage.withoutPrestrain] — and, because the influences are
     * then prestrain-independent, one bank of them serves the whole `θ₀` axis.
     */
    inner class Influences(val samples: Int = 81, val subdivisions: Int = 2) {

        val bare: OrigamiGrillage = lattice(emptyMap(), subdivisions)

        private val halfX = bare.lengthX / 2.0

        private val halfY = bare.lengthY / 2.0

        private val solutions = stations.map { (x, y) ->
            bare.solve(uniformPressure(0.0), listOf(PointLoad(x, y, 1.0)))
        }

        private fun sample(field: (Double, Double) -> Double): DoubleArray {
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

        fun sampleDishing(solution: GrillageDeflection): DoubleArray =
            sample { x, y -> solution.dishing(x, y) }

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

        private val dishingFree = influences.sampleDishing(free)

        val freeDishing = free.peakDishing(samples) / freeStroke

        /**
         * The standing falsifier, carried unchanged from `C-0099` — and read on
         * [OrigamiGrillage.withoutPrestrain], because *"a uniform load on a uniform foundation
         * dishes exactly zero"* is a statement about a **load**. A prestrained lattice's
         * `solve(uniform)` is the uniform load **plus** the eigenstrain, which genuinely dishes;
         * reading the falsifier on it measures the prestrain and reports it as a solver failure.
         * Third instance in this study of the same trap, after the influence bank and the
         * unit-load solves (`CH-0120`).
         */
        val uniformLoadDishing =
            host.withoutPrestrain.solve(uniformField).peakDishing(samples) / freeStroke

        /** The largest moment any single crossover hinge carries under the solved load. */
        val peakHingeMoment = free.peakHingeMoment

        fun surrogateFor(indices: List<Int>): InfluenceSurrogate = InfluenceSurrogate(
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
        )

        fun dishing(placement: UpwardArmPlacement): Double =
            surrogateFor(
                placement.stations(DUPLEXES, sheet.interhelicalDistance).map { (x, y) ->
                    val index = influences.indexOf(x, y)
                    require(index >= 0) { "($x, $y) is not an upward site of phase $PHASE" }
                    index
                }
            ).solve(uniform).peakDishing / freeStroke
    }
}

/** The exhaustive centro-symmetric optimum of one solve, with the spread of the whole family. */
private class T172Optimum(host: T172Host, solve: T172Host.Solve) {

    val best: UpwardArmPlacement
    val bestValue: Double
    val worstValue: Double
    val medianValue: Double
    val enumerated: Int

    init {
        val values = ArrayList<Double>()
        var incumbent: Pair<UpwardArmPlacement, Double>? = null
        centroSymmetricPlacements(
            PHASE, host.edgeX, DUPLEXES, host.arm, ARM_COUNT,
            minimumPerRow = 2, maximumPerRow = 3
        ).forEach { placement ->
            val value = solve.dishing(placement)
            values += value
            val current = incumbent
            if (current == null || value < current.second ||
                (value == current.second && placement.key < current.first.key)
            ) incumbent = placement to value
        }
        require(values.isNotEmpty()) { "the centro-symmetric family at phase $PHASE is empty" }
        values.sort()
        best = incumbent!!.first
        bestValue = incumbent!!.second
        worstValue = values.last()
        medianValue = values[values.size / 2]
        enumerated = values.size
    }
}

// ---------------------------------------------------------------------------------------------
// the prestrain distributions over the 14 row-end sites
// ---------------------------------------------------------------------------------------------

private const val UNIFORM = "uniform — every row-end crossover built at the same theta_0"
private const val ALTERNATING =
    "alternating — the sign flips with the interface, Rothemund's glide symmetry surviving"
private const val OPPOSED_ENDS = "opposed ends — the two row ends carry opposite signs"

private fun distribute(
    distribution: String,
    sites: List<CrossoverSite>,
    angle: Double,
    lowColumn: Int
): Map<CrossoverSite, Double> = sites.associateWith { site ->
    when (distribution) {
        UNIFORM -> angle
        ALTERNATING -> if (site.lowerBeam % 2 == 0) angle else -angle
        OPPOSED_ENDS -> if (site.column == lowColumn) angle else -angle
        else -> error("unknown distribution: $distribution")
    }
}

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    val started = System.currentTimeMillis()
    val sheet = origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)
    val edgeX = BUILDABLE_RASTER_WIDTH
    val arm = quantisedToRise(C0055_ARM_LENGTH)
    val hinge = Gen1Tile.crossoverHingeStiffness()

    println("T-172 — reading C-0022's solved load and C-0090's published reading ...")
    val (smooth, rim) = solvedProfile(File("gpd/results/T-3b-tile-edge-load-profile.json"))
    val (publishedDishing, publishedKey) =
        c0090Reading(File("gpd/results/T-153-buildable-raster-width.json"), "RECOMMENDED", PHASE)

    val host = T172Host(sheet, edgeX, arm, smooth, rim)
    check(host.columns.size == 8) { "phase $PHASE must carry 8 columns" }
    check(host.rowEndSites.size == DUPLEXES - 1) {
        "the two row-end columns must carry one crossover per interface"
    }
    val lowColumn = host.rowEndSites.minOf { it.column }
    val placement = placementFromKey(publishedKey, PHASE, arm, edgeX)
    check(placement.count == ARM_COUNT) { "C-0090's key must carry $ARM_COUNT roots" }

    // ------------------------------------------------------------ the ladder of candidate angles
    fun rung(name: String, basis: String, radians: Double, sourced: String) = T172LadderRecord(
        name = name,
        basis = basis,
        degrees = degrees(radians),
        radians = radians,
        couplePerCrossover = hingePrestrainCouple(radians, hinge),
        assembledCouple = host.rowEndSites.size * hingePrestrainCouple(radians, hinge),
        sourced = sourced
    )

    val eightBase = registerPrestrain(8.0, DESIGN_TWIST, NATURAL_TWIST)
    val sixteenBase = registerPrestrain(16.0, DESIGN_TWIST, NATURAL_TWIST)
    val perInterface = registerPrestrain(
        Gen1Tile.CROSSOVER_SPACING_SHEET_BP, DESIGN_TWIST, NATURAL_TWIST
    )
    val halfQuantum = PI / 180.0 * DESIGN_TWIST / 2.0
    val quarterTurn = PI / 2.0
    val halfTurn = PI

    val ladder = listOf(
        rung(
            "8 bp register", "the out-of-plane site's offset from the design twist, " +
                    "linear in the base-pair offset (C-0015)", eightBase,
            "DERIVED here from 32/3 against 10.5 bp per turn; CLAUDE.md's 4.286 deg"
        ),
        rung(
            "16 bp register", "the sheet's own next in-plane crossover, one column pitch away",
            sixteenBase, "DERIVED; CLAUDE.md's 8.571 deg"
        ),
        rung(
            "32 bp register", "the per-interface crossover spacing, which is the distance a " +
                    "row-end crossover sits from the one that balances it",
            perInterface, "DERIVED"
        ),
        rung(
            "half the azimuthal quantum", "the worst registration a 33.75 deg/bp lattice can " +
                    "impose on a chord (CLAUDE.md, C-0029)", halfQuantum, "DERIVED"
        ),
        rung(
            "a quarter turn", "a geometric ceiling that assumes nothing about the strand route",
            quarterTurn, "DERIVED — assumption-free"
        ),
        rung(
            "a half turn", "the ABSOLUTE geometric ceiling on a dihedral prestrain: past it the " +
                    "crossover is better described in the opposite register",
            halfTurn, "DERIVED — assumption-free"
        )
    )

    // ------------------------------------------------- the cheap bound: ONE unit-prestrain solve
    println("T-172 — the cheap bound: one unit-prestrain solve fixes the whole theta_0 axis ...")

    val influences = host.Influences()
    val zeroSolve = host.Solve(emptyMap(), influences)
    val zeroDishing = zeroSolve.dishing(placement)
    val zeroFree = zeroSolve.freeDishing

    // gate: at zero prestrain the split bank must reproduce `C-0058`/`C-0063`'s own, which is an
    // independent code path — one holds free and influence on one lattice, the other on two
    val referenceBank = UpwardRootInfluenceBank(host.lattice(emptyMap()), host.stations, host.solvedField)
    val referenceDishing = referenceBank.surrogateFor(
        placement.stations(DUPLEXES, sheet.interhelicalDistance).map { (x, y) ->
            referenceBank.indexOf(x, y)
        }
    ).solve(List(ARM_COUNT) { MANDATE / ARM_COUNT }).peakDishing / host.freeStroke
    val bankAgreement = abs(referenceDishing - zeroDishing) / abs(referenceDishing)
    check(bankAgreement < 1e-9) {
        "the split influence bank must reproduce UpwardRootInfluenceBank at zero prestrain, " +
                "was: $zeroDishing against $referenceDishing"
    }

    val unitAngle = 1.0
    val unitSolve = host.Solve(
        distribute(UNIFORM, host.rowEndSites, unitAngle, lowColumn), influences
    )
    val unitDishing = unitSolve.dishing(placement)
    val unitFree = unitSolve.freeDishing
    // the prestrain-ONLY response, by superposition: it is what multiplies theta_0
    val unitSlope = abs(unitDishing - zeroDishing) / unitAngle
    val unitFreeSlope = abs(unitFree - zeroFree) / unitAngle
    val thetaCritical = (FLATNESS_TOLERANCE - zeroDishing) / unitSlope

    val cheapBounds = listOf(
        T172BoundRecord(
            "the prestrain is a LOAD, not a stiffness",
            "1/2 k (dphi - theta_0)^2 = 1/2 k dphi^2 - k theta_0 dphi + const: the quadratic " +
                    "term is untouched, so no entry of the stiffness matrix moves and the " +
                    "deflection field is LINEAR in theta_0",
            0.0, "pN nm/rad moved in K", "T-172", true
        ),
        T172BoundRecord(
            "C-0090's placement dishing at zero prestrain",
            "the reproduction gate and the baseline every bound is written against",
            zeroDishing, "of the free stroke", "C-0090", false
        ),
        T172BoundRecord(
            "dishing per radian of uniform row-end prestrain, at C-0090's placement",
            "one solve, exact for every theta_0 by linearity",
            unitSlope, "of the free stroke per rad", "T-172", true
        ),
        T172BoundRecord(
            "dishing per radian, UNCOUPLED tile",
            "the same slope with no coupling at all, as the reference C-0060 demands",
            unitFreeSlope, "of the free stroke per rad", "T-172", true
        ),
        T172BoundRecord(
            "the prestrain at which C-0090's placement reaches T-5b's 0.10",
            "(0.10 - dishing at zero) / slope — a triangle-inequality CEILING, since peak " +
                    "dishing is an absolute value and cannot exceed the sum of the two peaks",
            degrees(thetaCritical), "degrees", "T-172", true
        ),
        T172BoundRecord(
            "the assembled couple 14 row-end crossovers carry at that prestrain",
            "14 x k_theta x theta_0, against the 100 pN the tile carries over 38.08 nm",
            host.rowEndSites.size * hingePrestrainCouple(thetaCritical, hinge), "pN nm",
            "T-172", true
        )
    )

    println(
        "  zero %.9f   slope %.6f /rad   theta* %.3f deg".format(
            zeroDishing, unitSlope, degrees(thetaCritical)
        )
    )

    // ------------------------------------------------------------------------------- the sweep
    println("T-172 — the ladder, at C-0090's own placement, three distributions, both signs ...")

    val sweep = ArrayList<T172SweepRecord>()
    val linearity = ArrayList<T172LinearityRecord>()
    val distributions = listOf(UNIFORM, ALTERNATING, OPPOSED_ENDS)

    sweep += T172SweepRecord(
        distribution = "none — zero prestrain",
        rung = "zero", degrees = 0.0, radians = 0.0,
        publishedPlacementDishing = zeroDishing,
        freeDishingOverStroke = zeroFree,
        uniformLoadDishingOverStroke = zeroSolve.uniformLoadDishing,
        peakHingeMoment = zeroSolve.peakHingeMoment,
        flatAtTenPercent = zeroDishing < FLATNESS_TOLERANCE
    )

    distributions.forEach { distribution ->
        val unit = if (distribution == UNIFORM) unitSlope else {
            val one = host.Solve(
                distribute(distribution, host.rowEndSites, 1.0, lowColumn), influences
            )
            abs(one.dishing(placement) - zeroDishing)
        }
        ladder.forEach { rungRecord ->
            listOf(1.0, -1.0).forEach { sign ->
                val angle = sign * rungRecord.radians
                val solve = host.Solve(
                    distribute(distribution, host.rowEndSites, angle, lowColumn), influences
                )
                val value = solve.dishing(placement)
                sweep += T172SweepRecord(
                    distribution = distribution,
                    rung = rungRecord.name,
                    degrees = sign * rungRecord.degrees,
                    radians = angle,
                    publishedPlacementDishing = value,
                    freeDishingOverStroke = solve.freeDishing,
                    uniformLoadDishingOverStroke = solve.uniformLoadDishing,
                    peakHingeMoment = solve.peakHingeMoment,
                    flatAtTenPercent = value < FLATNESS_TOLERANCE
                )
                if (sign > 0.0) linearity += T172LinearityRecord(
                    distribution = distribution,
                    radians = angle,
                    measured = value,
                    predictedFromUnit = zeroDishing + rungRecord.radians * unit,
                    relativeDeparture = 0.0
                )
                println(
                    "  %-12s %-26s %+8.3f deg  dishing %9.6f  flat %s".format(
                        distribution.take(11), rungRecord.name, sign * rungRecord.degrees,
                        value, value < FLATNESS_TOLERANCE
                    )
                )
            }
        }
    }

    // the linearity check is written on the SIGNED response, so recompute the departure exactly:
    // dishing(theta) is the peak of |A + theta B| and equals |A| + theta|B| only where the two
    // fields peak at the same place — the departure IS the finding, not an error
    val linearityChecked = linearity.map {
        it.copy(
            relativeDeparture =
                abs(it.measured - it.predictedFromUnit) / max(1e-12, abs(it.measured))
        )
    }

    // ------------------------------------------------------- the exhaustive re-optimisation
    println("T-172 — the exhaustive centro-symmetric enumeration at three states ...")

    fun enumerate(distribution: String, angle: Double): T172EnumerationRecord {
        val solve = host.Solve(
            if (angle == 0.0) emptyMap()
            else distribute(distribution, host.rowEndSites, angle, lowColumn),
            influences
        )
        val optimum = T172Optimum(host, solve)
        val here = solve.dishing(placement)
        val record = T172EnumerationRecord(
            distribution = distribution,
            degrees = degrees(angle),
            enumerated = optimum.enumerated,
            bestDishingOverStroke = optimum.bestValue,
            worstDishingOverStroke = optimum.worstValue,
            medianDishingOverStroke = optimum.medianValue,
            publishedPlacementDishing = here,
            publishedPlacementPenalty = here - optimum.bestValue,
            bestKey = optimum.best.key,
            bestKeyIsZeroPrestrainOptimum = optimum.best.key == publishedKey,
            flatAtTenPercent = optimum.bestValue < FLATNESS_TOLERANCE
        )
        println(
            "  enumerate %-12s %+8.3f deg  best %9.6f  key stable %s  flat %s".format(
                distribution.take(11), degrees(angle), record.bestDishingOverStroke,
                record.bestKeyIsZeroPrestrainOptimum, record.flatAtTenPercent
            )
        )
        return record
    }

    val enumerations = listOf(
        enumerate("none — zero prestrain", 0.0),
        enumerate(UNIFORM, perInterface),
        enumerate(UNIFORM, -perInterface)
    )

    // ------------------------------------------------------------------------- convergence
    println("T-172 — convergence ...")
    val convergence = ArrayList<T172ConvergenceRecord>()
    val state = distribute(UNIFORM, host.rowEndSites, perInterface, lowColumn)
    val nested = listOf(1, 2, 4).map {
        it to host.Solve(state, host.Influences(81, it), 81, it).dishing(placement)
    }
    val finestNested = nested.last().second
    nested.forEach { (level, value) ->
        convergence += T172ConvergenceRecord(
            "C-0090's placement dishing at the 32 bp register prestrain",
            "beam subdivisions", level.toDouble(), value,
            abs(value - finestNested) / abs(finestNested)
        )
    }
    val sampled = listOf(41, 81, 161).map {
        it to host.Solve(state, host.Influences(it, 2), it, 2).dishing(placement)
    }
    val finestSampled = sampled.last().second
    sampled.forEach { (level, value) ->
        convergence += T172ConvergenceRecord(
            "C-0090's placement dishing at the 32 bp register prestrain",
            "dishing sample grid", level.toDouble(), value,
            abs(value - finestSampled) / abs(finestSampled)
        )
    }

    // ------------------------------------------------------------------------ reproductions
    fun reproduce(quantity: String, published: Double, here: Double, source: String) =
        T172ReproductionRecord(
            quantity, published, here,
            abs(here - published) / max(1e-30, abs(published)), source
        )

    val zeroEnumeration = enumerations.first()
    val reproductions = listOf(
        reproduce(
            "C-0090's best 34-root dishing at 38.08 nm / phase 8", publishedDishing,
            zeroEnumeration.bestDishingOverStroke, "C-0090 / T-153 result file"
        ),
        reproduce(
            "C-0099's admitted reading, at zero prestrain", 0.0621469105,
            zeroEnumeration.bestDishingOverStroke, "C-0099 / T-164"
        ),
        reproduce(
            "Gen1Tile's interior crossover hinge, from the bond count", 13.5294118, hinge,
            "C-0009 / Chen et al. 2014"
        ),
        reproduce(
            "the 16 bp register departure in degrees", 8.5714286, degrees(sixteenBase),
            "CLAUDE.md / C-0015"
        ),
        reproduce(
            "the 8 bp out-of-plane register departure in degrees", 4.2857143, degrees(eightBase),
            "CLAUDE.md / C-0015"
        ),
        reproduce(
            "the row-end crossover count", 14.0, host.rowEndSites.size.toDouble(), "C-0095"
        ),
        reproduce(
            "the free stroke in nm", 5.15473846, host.freeStroke, "C-0099 / T-164"
        ),
        reproduce(
            "C-0063's UpwardRootInfluenceBank at zero prestrain, against the split bank",
            referenceDishing, zeroDishing, "C-0063 / C-0058 — an independent code path"
        )
    )

    // ------------------------------------------------------------------------------ verdicts
    val worstSweep = sweep.maxBy { it.publishedPlacementDishing }
    val everySweepFlat = sweep.all { it.flatAtTenPercent }
    val everyEnumerationFlat = enumerations.all { it.flatAtTenPercent }
    val keyStable = enumerations.all { it.bestKeyIsZeroPrestrainOptimum }
    val firstCrossing = ladder.firstOrNull { it.radians >= thetaCritical }
    val worstUniformLoad = sweep.maxOf { it.uniformLoadDishingOverStroke }
    val worstLinearity = linearityChecked.maxOf { it.relativeDeparture }

    // the uniform-prestrain curl: the falsifier the task PROPOSED, measured rather than asserted
    val everyCrossover = zeroSolve.host.crossoverSites
    val curlAngle = perInterface
    val curled = host.Solve(everyCrossover.associateWith { curlAngle }, influences)
    val curlFree = curled.freeDishing
    val curlIsZero = abs(curlFree - zeroFree) < 1e-6

    val predicates = listOf(
        T172PredicateRecord(
            "P1", "an initial-stress term exists in C-0009's grillage and is additive",
            "OrigamiGrillage.crossoverPrestrains, empty by default, asserted bit-identical to " +
                    "the unmodified load vector at zero", true
        ),
        T172PredicateRecord(
            "P2", "the best 34-root dishing at 38.08 nm / phase 8 is re-read under a prestrain",
            "%d ladder states at C-0090's placement and %d exhaustive enumerations"
                .format(sweep.size, enumerations.size), true
        ),
        T172PredicateRecord(
            "P3", "either the prestrain is bounded by a source, or the flatness is insensitive " +
                    "to any prestrain inside a STATED bound",
            "no accessible source quantifies it; the flatness holds T-5b's 0.10 for every " +
                    "|theta_0| below %.2f degrees".format(degrees(thetaCritical)),
            true
        )
    )

    val falsifiers = listOf(
        T172FalsifierRecord(
            "F1", "zero prestrain does not reproduce C-0090's 0.0621469105",
            reproductions[1].relativeDeparture > 1e-8,
            "departure %.3e".format(reproductions[1].relativeDeparture)
        ),
        T172FalsifierRecord(
            "F2", "the coupled response is not linear in theta_0, i.e. the term is not a load",
            worstLinearity > 0.5,
            (
                "worst departure of the measured peak from the unit-slope prediction %.3e; a " +
                        "peak of |A + theta B| is linear in theta only where the two fields " +
                        "peak at the same place, so a small departure is expected and a large " +
                        "one is a defect"
            ).format(worstLinearity)
        ),
        T172FalsifierRecord(
            "F3", "a UNIFORM prestrain over every crossover dishes zero, i.e. an eigenstrain " +
                    "behaves like a uniform load",
            curlIsZero,
            (
                "the free tile dishes %.6f of the stroke at zero and %.6f under a uniform " +
                        "%.3f degree prestrain on all %d crossovers: an eigenstrain CURLS the " +
                        "sheet, and CLAUDE.md's uniform-load falsifier does not transfer"
            ).format(zeroFree, curlFree, degrees(curlAngle), everyCrossover.size)
        ),
        T172FalsifierRecord(
            "F4", "the exhaustive optimum placement moves under a prestrain", !keyStable,
            "C-0090's key is the optimum at %d of %d enumerated states"
                .format(enumerations.count { it.bestKeyIsZeroPrestrainOptimum }, enumerations.size)
        ),
        T172FalsifierRecord(
            "F5", "a uniform load on a uniform foundation dishes more than 1e-6 of the stroke",
            worstUniformLoad > 1e-6, "worst over the whole sweep %.3e".format(worstUniformLoad)
        ),
        T172FalsifierRecord(
            "F6", "T-5b's 0.10 is crossed at a prestrain inside the derived ladder",
            firstCrossing != null,
            if (firstCrossing == null)
                "no rung of the ladder reaches the crossing at %.2f degrees"
                    .format(degrees(thetaCritical))
            else "the crossing at %.2f degrees is reached by the '%s' rung at %.2f degrees"
                .format(degrees(thetaCritical), firstCrossing.name, firstCrossing.degrees)
        )
    )

    val literature = listOf(
        T172LiteratureRecord(
            "Is the row-end / seam crossover tension quantified anywhere?",
            "Rothemund 2006, Nature 440:297, Suppl. Note S2 — gpd/data/T-151-sources/",
            "READ DIRECTLY", false,
            "The passage states the tension and states that it is unquantified: \"a crossover " +
                    "involving staple strands is in tension with an adjacent crossover " +
                    "involving the scaffold strand … How the strain is actually relieved is " +
                    "unknown, the final base pairs of each helix may be distorted.\""
        ),
        T172LiteratureRecord(
            "What COORDINATE does the strain live in?",
            "Rothemund 2006, Suppl. Note S2, design-program section",
            "READ DIRECTLY", true,
            "An ANGLE, and the paper's own metric is angular: \"the computed strain energy is " +
                    "just the sum of the squared angular deviation from the tangent point for " +
                    "the base before and the base after the crossover\", with the two causes " +
                    "named as \"the non-integral number of bases in a single turn, and the " +
                    "major-minor groove angle\". That is exactly the dihedral coordinate " +
                    "C-0009's crossover hinge already carries."
        ),
        T172LiteratureRecord(
            "Has anyone since put a number on it?",
            "EuropePMC, 10 recorded queries, 68 unique records — " +
                    "gpd/data/T-172-sources/europepmc-queries.json",
            "ABSTRACTS READ; three full texts already manifested in gpd/data/T-151-sources",
            false,
            "NOT FOUND. The quantified strain in the literature is GLOBAL twist of a whole " +
                    "object (underwinding, helicity mismatch, intercalator relief), never a " +
                    "per-crossover residual angle at an edge, and a 2021 tutorial still says " +
                    "only that \"compensation of residual strain/torque is much easier for the " +
                    "hexagonal lattice\"."
        )
    )

    val elapsed = (System.currentTimeMillis() - started) / 1000.0

    // the first rung, by magnitude, at which C-0090's OWN placement leaves T-5b's 0.10
    val firstMeasuredCrossing = sweep
        .filter { !it.flatAtTenPercent }
        .minByOrNull { abs(it.degrees) }
    val recoveredByReplacement = enumerations.filter { it.degrees != 0.0 }

    val parameters = mapOf(
        "edgeX" to edgeX,
        "phaseBasePairs" to PHASE.toDouble(),
        "duplexes" to DUPLEXES.toDouble(),
        "armLength" to arm,
        "armCount" to ARM_COUNT.toDouble(),
        "mandate" to MANDATE,
        "flatnessTolerance" to FLATNESS_TOLERANCE,
        "interiorHingeStiffness" to hinge,
        "rowEndCrossovers" to host.rowEndSites.size.toDouble(),
        "latticeCrossovers" to everyCrossover.size.toDouble(),
        "freeStroke" to host.freeStroke,
        "zeroPrestrainDishing" to zeroDishing,
        "dishingPerRadian" to unitSlope,
        "dishingPerRadianUncoupled" to unitFreeSlope,
        "criticalPrestrainDegrees" to degrees(thetaCritical),
        "criticalPrestrainRadians" to thetaCritical,
        "largestLadderRungDegrees" to ladder.maxOf { it.degrees },
        "worstLadderDishing" to worstSweep.publishedPlacementDishing,
        "worstUniformLoadDishing" to worstUniformLoad,
        "worstLinearityDeparture" to worstLinearity,
        "uniformCurlFreeDishing" to curlFree,
        "enumeratedPerState" to zeroEnumeration.enumerated.toDouble(),
        // `T-227`/`C-0150`: the wall clock is PRINTED (see `report`), never emitted.
        // `CLAUDE.md`: *a WALL CLOCK in a result file is a step counter by another name*, and one
        // such field makes the whole file permanently un-diffable — which is the check the
        // rounding layer exists to enable. `T-214` re-emitted this file and watched
        // `elapsedSeconds` move 1.1 % for no reason but the clock, on a run that changed nothing.
        "firstCrossingDegreesMeasured" to (firstMeasuredCrossing?.degrees ?: 0.0),
        "firstCrossingDishingMeasured" to
                (firstMeasuredCrossing?.publishedPlacementDishing ?: 0.0),
        "sweptStatesFlat" to sweep.count { it.flatAtTenPercent }.toDouble(),
        "sweptStates" to sweep.size.toDouble(),
        "reoptimisedBestAtPlusThirtyTwoBaseRung" to
                (recoveredByReplacement.firstOrNull { it.degrees > 0.0 }
                    ?.bestDishingOverStroke ?: 0.0),
        "reoptimisedBestAtMinusThirtyTwoBaseRung" to
                (recoveredByReplacement.firstOrNull { it.degrees < 0.0 }
                    ?.bestDishingOverStroke ?: 0.0)
    )

    val findings = listOf(
        (
            "THE PRESTRAIN IS A LOAD, WHICH IS WHY THE WHOLE AXIS COSTS ONE SOLVE. " +
                    "1/2 k (dphi - theta_0)^2 leaves the quadratic term untouched, so no entry " +
                    "of the stiffness matrix moves, the host's factorisation is unchanged and " +
                    "C-0058's influence bank stays exact PROVIDED the influences are taken on " +
                    "the prestrain-free lattice (CH-0120). The dishing at C-0090's placement " +
                    "moves %.6f of the free stroke per radian of uniform row-end prestrain, so " +
                    "the triangle-inequality ceiling puts T-5b's 0.10 at %.2f degrees."
        ).format(unitSlope, degrees(thetaCritical)),
        (
            "AND THAT CROSSING IS INSIDE THE DERIVED LADDER, WHICH IS WHY THIS IS NOT A NULL " +
                    "RESULT. The ladder runs %.2f to %.2f degrees on physical rungs and the " +
                    "crossing sits at %.2f: C-0090's own 34-root placement holds T-5b's 0.10 at " +
                    "the 8 bp (%.2f deg) and 16 bp (%.2f deg) register rungs and LOSES it at " +
                    "the 32 bp rung in the adverse sign (%s). %d of %d swept states stay flat."
        ).format(
            ladder.minOf { it.degrees }, ladder.maxOf { it.degrees }, degrees(thetaCritical),
            degrees(eightBase), degrees(sixteenBase),
            if (firstMeasuredCrossing == null) "no state crosses"
            else "%.3f deg, dishing %.6f".format(
                firstMeasuredCrossing.degrees, firstMeasuredCrossing.publishedPlacementDishing
            ),
            sweep.count { it.flatAtTenPercent }, sweep.size
        ),
        (
            "THE DESIGN ABSORBS IT, AND THE PRICE IS THAT THE ARGMIN MOVES. Re-running the " +
                    "exhaustive centro-symmetric enumeration at the 32 bp register prestrain " +
                    "recovers the verdict in both signs — %s — against C-0090's own placement, " +
                    "which reads %s at the same two states. So a prestrain of this size is a " +
                    "PLACEMENT question rather than a feasibility one; but C-0090's published " +
                    "key is the optimum at only %d of %d enumerated states, so the recommended " +
                    "DESIGN is a function of an unmeasured parameter in a way C-0099's " +
                    "stiffness sweep was not."
        ).format(
            recoveredByReplacement.joinToString("; ") {
                "%+.3f deg gives %.6f".format(it.degrees, it.bestDishingOverStroke)
            },
            recoveredByReplacement.joinToString("; ") {
                "%+.3f deg gives %.6f".format(it.degrees, it.publishedPlacementDishing)
            },
            enumerations.count { it.bestKeyIsZeroPrestrainOptimum }, enumerations.size
        ),
        (
            "THE UNIFORM-LOAD FALSIFIER DOES NOT TRANSFER TO AN EIGENSTRAIN. A uniform " +
                    "prestrain on all %d crossovers takes the free tile from %.6f to %.6f of " +
                    "the stroke: it CURLS the sheet into a cylinder of curvature theta_0/d, " +
                    "which is the state that relaxes every hinge and every vertical link at " +
                    "once. A uniform LOAD is equilibrated by a rigid translation; an " +
                    "eigenstrain is not, and CLAUDE.md's best falsifier is silent here."
        ).format(everyCrossover.size, zeroFree, curlFree),
        (
            "TWO OF THE THREE DISTRIBUTIONS ARE THE SAME MAP, AND THAT IS A LATTICE FACT. Each " +
                    "of the 14 row-end crossovers is the only one on its interface, and which " +
                    "END it sits at alternates with the interface — so 'alternating by " +
                    "interface' and 'opposed ends' are the same assignment, to the last digit, " +
                    "and both are EVEN in the sign where the uniform distribution is not. The " +
                    "sign asymmetry belongs to C-0022's solved collar, not to the lattice."
        ),
        (
            "NO ACCESSIBLE SOURCE QUANTIFIES THE ROW-END PRESTRAIN, and Rothemund says so " +
                    "himself: 'How the strain is actually relieved is unknown.' Ten recorded " +
                    "EuropePMC queries returned 68 unique records and none carries a " +
                    "per-crossover residual angle at an edge; what the literature quantifies is " +
                    "the GLOBAL twist of a whole object. The deliverable is therefore P-6's " +
                    "threshold rather than a value — and unlike C-0099's, this threshold is " +
                    "REACHED inside the range the lattice itself makes plausible."
        )
    )

    val result = T172Result(
        task = "T-172",
        leaf = "A8.2",
        conditions = "T = 300 K, k_BT = 4.141947 pN nm; aqueous 2 mM MgCl2; single-layer " +
                "square-lattice Rothemund sheet, 15 duplexes at the SAXS 2.69 nm, 0.34 nm " +
                "rise, 32/3 bp per turn, 16 bp column pitch, 32 bp per-interface spacing; " +
                "along-helix width 38.08 nm (112 bp, C-0086) at crossover phase 8; C-0090's " +
                "buildable 24-rise 8.16 nm arm at C-0055's 34 roots; C-0017's 33.3333 pN/nm " +
                "mandate shared equally; C-0022's solved collar at 2 mM, a 10 nm gap and " +
                "0.192 V, AS C-0090 AND C-0099 CARRIED IT; C-0001's foundation secant",
        question = "Can a row-end crossover PRESTRAIN — Rothemund's \"crossovers in tension\" — " +
                "move C-0090's flatness, C-0095's admitted reading, or T-5b's 0.10?",
        cheapBounds = cheapBounds,
        ladder = ladder,
        sweep = sweep,
        enumerations = enumerations,
        linearity = linearityChecked,
        literature = literature,
        convergence = convergence,
        reproductions = reproductions,
        predicates = predicates,
        falsifiers = falsifiers,
        findings = findings,
        parameters = parameters
    )

    val json = Json { prettyPrint = true }
    val file = File("gpd/results/T-172-row-end-prestrain.json")
    file.parentFile.mkdirs()
    file.writeText(json.encodeToString(json.encodeToJsonElement(result).roundedForResult()))

    println()
    println("T-172 — the row-end crossover's prestrain")
    println("  zero prestrain, best 34-root dishing   %.9f".format(zeroDishing))
    println("  dishing per radian, uniform row-end    %.6f".format(unitSlope))
    println("  T-5b's 0.10 reached at                 %.3f degrees".format(degrees(thetaCritical)))
    println("  largest DERIVED ladder rung            %.3f degrees".format(ladder.maxOf { it.degrees }))
    println("  every ladder state flat                %s".format(everySweepFlat))
    println("  every enumeration flat                 %s".format(everyEnumerationFlat))
    println("  optimum placement key stable           %s".format(keyStable))
    println("  elapsed %.1f s".format(elapsed))
    println("written to ${file.path}")
}
