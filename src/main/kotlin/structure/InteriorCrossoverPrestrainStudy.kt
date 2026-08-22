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
import com.xemantic.nano.plentyofroom.anchoring.placementFromKey
import com.xemantic.nano.plentyofroom.anchoring.quantisedToRise
import com.xemantic.nano.plentyofroom.anchoring.rasterColumnLayout
import com.xemantic.nano.plentyofroom.anchoring.rowEndCrossoverSites
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
import org.jetbrains.bio.viktor.F64Array
import java.io.File
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * `T-190` — what do the **42 interior** crossovers carry, and does their cancellation hold?
 *
 * ```shell
 * tools/study.sh structure.InteriorCrossoverPrestrainStudyKt
 * ```
 *
 * Emits `gpd/results/T-190-interior-crossover-prestrain.json`. Reads
 * `gpd/results/T-3b-tile-edge-load-profile.json` (`C-0022`'s solved collar),
 * `gpd/results/T-153-buildable-raster-width.json` (`C-0090`'s reading and placement key),
 * `gpd/results/T-172-row-end-prestrain.json` (`C-0104`'s threshold) and
 * `gpd/results/T-182-row-end-prestrain-value.json` (`C-0107`'s eleven solved states, as the
 * reproduction gates).
 *
 * `C-0107` read its boundary layer two ways — as a prestrain on the **14** row-end sites alone
 * (0.1022820 of the free stroke, **not** flat) and as the graded corrugated field `(−1)^b u(x)`
 * over all **56** (0.0922622, flat) — and attributed the difference to the 42 interior sites.
 * That is a verdict decided by a cancellation between two site sets, one of which no claim has
 * posed as a question.
 *
 * A prestrain is a **load** (`C-0104`), so the split is an *identity* and this study is a bank of
 * solves on an already-factorised host. What has to be measured is what the two halves do to a
 * quantity that is **not** additive: peak dishing is a seminorm of the field, so the cancellation
 * is a **cross term** and its convention-free measure is the cosine of the two dishing fields
 * under the lattice's own area inner product.
 *
 * No influence bank is built here, so `CH-0120`'s trap has nothing to contaminate; the standing
 * uniform-load falsifier is read on the **support-free, prestrain-free** lattice, which is where
 * it is a statement about a load rather than about an eigenstrain or about a sparse coupling.
 */

private const val DUPLEXES = 15
private const val PHASE = 8
private const val FLATNESS_TOLERANCE = 0.10
private const val RIM_STANDOFF = 1.0
private const val ARM_COUNT = C0055_ARM_COUNT
private val MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

/** `C-0104`'s threshold, read from its result file and asserted, never transcribed. */
private const val T190_C0104_THRESHOLD_DEGREES = 15.4497275

/** The square lattice's design twist, `32/3` bp per turn, in degrees per base. */
private val T190_DESIGN_TWIST = 360.0 / (Gen1Tile.CROSSOVER_SPACING_SHEET_BP / 3.0)

/** B-DNA's preferred twist, 10.5 bp per turn, in degrees per base. */
private const val T190_NATURAL_TWIST = 360.0 / 10.5

private fun t190Degrees(radians: Double): Double = radians * 180.0 / PI

/** Rounds a dimensionless departure or convergence measure to **two significant digits**. */
private fun t190TwoSignificant(value: Double): Double {
    if (value == 0.0 || !value.isFinite()) return value
    val magnitude = kotlin.math.floor(kotlin.math.log10(abs(value)))
    val scale = Math.pow(10.0, 1.0 - magnitude)
    return kotlin.math.round(value * scale) / scale
}

/** A numeric parameter, rounded at the same boundary the record fields are. */
private fun t190Parameter(value: Double): String = roundForResult(value).toString()

// ---------------------------------------------------------------------------------------------
// records — prefixed with the task, because study record classes are package scoped (CLAUDE.md)
// ---------------------------------------------------------------------------------------------

@Serializable
private data class T190ColumnRecord(
    val columnIndex: Int,
    val x: Double,
    val parity: Int,
    val atRowEnd: Boolean,
    val sites: Int,
    val prestrainDegrees: Double,
    val amplitudeOverRowEnd: Double,
    val absoluteCoupleShare: Double
)

@Serializable
private data class T190StateRecord(
    val state: String,
    val siteSet: String,
    val overallSign: Int,
    val rowEndDegrees: Double,
    val sites: Int,
    val netCouple: Double,
    val absoluteCouple: Double,
    val coupledDishingOverStroke: Double,
    val freeDishingOverStroke: Double,
    val peakHingeMoment: Double,
    val flatAtTenPercent: Boolean
)

@Serializable
private data class T190DecompositionRecord(
    val overallSign: Int,
    val rowEndNorm: Double,
    val interiorNorm: Double,
    val gradedNorm: Double,
    val cosine: Double,
    val quadraticIdentityDeparture: Double,
    val superpositionDeparture: Double,
    val peakRowEndOnly: Double,
    val peakInteriorOnly: Double,
    val peakGraded: Double,
    val peakSumOfParts: Double,
    val nonAdditivity: Double
)

@Serializable
private data class T190FactorialRecord(
    val overallSign: Int,
    val rowEndAngleStation: String,
    val interiorSitesLoaded: Boolean,
    val rowEndDegrees: Double,
    val coupledDishingOverStroke: Double,
    val flatAtTenPercent: Boolean,
    val reproduces: String
)

@Serializable
private data class T190LadderRecord(
    val overallSign: Int,
    val outermostColumnPairs: Int,
    val sites: Int,
    val coupledDishingOverStroke: Double,
    val flatAtTenPercent: Boolean,
    val recoveredFraction: Double
)

@Serializable
private data class T190BracketRecord(
    val torsionalRigidity: Double,
    val crossoverAlpha: Double,
    val crossoverSpacing: Double,
    val latticeHingeConsistent: Boolean,
    val decayLength: Double,
    val endResidualDegrees: Double,
    val overallSign: Int,
    val gradedDishingOverStroke: Double,
    val rowEndOnlyDishingOverStroke: Double,
    val cancellation: Double,
    val gradedFlat: Boolean,
    val rowEndOnlyFlat: Boolean
)

@Serializable
private data class T190ConvergenceRecord(
    val axis: String,
    val coarse: Double,
    val fine: Double,
    val relativeDeparture: Double
)

@Serializable
private data class T190Reproduction(
    val what: String,
    val owner: String,
    val expected: Double,
    val got: Double,
    val relativeDeparture: Double
)

@Serializable
private data class T190Predicate(
    val id: String,
    val statement: String,
    val verdict: String,
    val evidence: String
)

@Serializable
private data class T190Falsifier(
    val id: String,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
private data class T190Result(
    val task: String,
    val leaf: String,
    val question: String,
    val conditions: String,
    val columns: List<T190ColumnRecord>,
    val states: List<T190StateRecord>,
    val decomposition: List<T190DecompositionRecord>,
    val factorial: List<T190FactorialRecord>,
    val ladder: List<T190LadderRecord>,
    val bracket: List<T190BracketRecord>,
    val convergence: List<T190ConvergenceRecord>,
    val reproductions: List<T190Reproduction>,
    val predicates: List<T190Predicate>,
    val falsifiers: List<T190Falsifier>,
    val findings: List<String>,
    val parameters: Map<String, String>
)

// ---------------------------------------------------------------------------------------------
// the inputs, read from the claims that own them
// ---------------------------------------------------------------------------------------------

/** `C-0022`'s solved edge profile, keyed on concentration, gap **and bias** (`CLAUDE.md`). */
private fun t190SolvedProfile(file: File): Pair<CollarTerm, CollarTerm> {
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
private fun t190C0090Reading(file: File, casePrefix: String, phase: Int): Pair<Double, String> {
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

/** One named cheap bound of `C-0104`, read from its own result file rather than transcribed. */
private fun t190C0104Bound(file: File, namePrefix: String): Double {
    require(file.exists()) { "C-0104's result file is missing: ${file.path}" }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("cheapBounds").jsonArray.map { it.jsonObject }
        .firstOrNull { it.getValue("name").jsonPrimitive.content.startsWith(namePrefix) }
        ?: error("no C-0104 cheap bound named $namePrefix")
    return record.getValue("value").jsonPrimitive.content.toDouble()
}

/** One field of one of `C-0107`'s eleven solved dishing states, by state-name prefix. */
private fun t190C0107Dishing(file: File, statePrefix: String, key: String): Double {
    require(file.exists()) { "C-0107's result file is missing: ${file.path}" }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("dishing").jsonArray.map { it.jsonObject }
        .firstOrNull { it.getValue("state").jsonPrimitive.content.startsWith(statePrefix) }
        ?: error("no C-0107 dishing state named $statePrefix")
    return record.getValue(key).jsonPrimitive.content.toDouble()
}

/** `C-0107`'s nominal boundary-layer cell — CanDo `GJ`, `α = 1`, the derived spacing. */
private fun t190C0107NominalEndResidual(file: File, spacing: Double): Double {
    require(file.exists()) { "C-0107's result file is missing: ${file.path}" }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("boundaryLayer").jsonArray.map { it.jsonObject }
        .firstOrNull {
            fun value(key: String) = it.getValue(key).jsonPrimitive.content.toDouble()
            value("crossoverAlpha") == 1.0 &&
                    value("torsionalRigidity") == Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY &&
                    abs(value("crossoverSpacing") - spacing) < 1.0e-6
        } ?: error("no C-0107 nominal boundary-layer cell")
    return record.getValue("endResidualDegrees").jsonPrimitive.content.toDouble()
}

// ---------------------------------------------------------------------------------------------
// the host — `C-0107`'s EXPLICIT elastic-support grillage, reproduced rather than re-invented
// ---------------------------------------------------------------------------------------------

private class T190Host(
    val sheet: OrigamiSheet,
    val edgeX: Double,
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

    val columns: CrossoverLayout =
        rasterColumnLayout(PHASE, sheet, edgeX, true, CrossoverLayout.EDGE_MARGIN)

    val rowEndSites: List<CrossoverSite> =
        rowEndCrossoverSites(columns, edgeX, DUPLEXES, CrossoverLayout.EDGE_MARGIN)

    fun siteX(site: CrossoverSite): Double = columns.positions[site.column]

    fun lattice(
        prestrains: Map<CrossoverSite, Double>,
        supports: List<PointSupport>,
        subdivisions: Int = 2
    ): OrigamiGrillage = OrigamiGrillage(
        sheet = sheet,
        lengthX = edgeX,
        beamCount = DUPLEXES,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        columns = columns,
        subdivisions = subdivisions,
        supports = supports,
        crossoverPrestrains = prestrains
    )
}

/** [sites] built at the same [angle] — `C-0104`'s uniform distribution. */
private fun t190Uniform(sites: List<CrossoverSite>, angle: Double): Map<CrossoverSite, Double> =
    sites.associateWith { angle }

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    val sheet = origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)
    val edgeX = BUILDABLE_RASTER_WIDTH
    val arm = quantisedToRise(C0055_ARM_LENGTH)
    val hinge = Gen1Tile.crossoverHingeStiffness()

    println("T-190 — reading C-0022's collar, C-0090's key, C-0104's threshold, C-0107's states ...")
    val (smooth, rim) = t190SolvedProfile(ResultInputs.T_3B.file())
    val (publishedDishing, publishedKey) =
        t190C0090Reading(ResultInputs.T_153.file(), "RECOMMENDED", PHASE)
    val c0104File = ResultInputs.T_172.file()
    val c0104Threshold = t190C0104Bound(c0104File, "the prestrain at which C-0090")
    check(abs(c0104Threshold - T190_C0104_THRESHOLD_DEGREES) < 1e-6) {
        "C-0104's threshold moved: $c0104Threshold"
    }
    val c0107File = ResultInputs.T_182.file()

    val host = T190Host(sheet, edgeX, smooth, rim)
    check(host.columns.size == 8) { "phase $PHASE must carry 8 columns" }
    check(host.rowEndSites.size == DUPLEXES - 1) { "one row-end crossover per interface" }
    val placement = placementFromKey(publishedKey, PHASE, arm, edgeX)
    val stations = placement.stations(DUPLEXES, sheet.interhelicalDistance)
    check(stations.size == ARM_COUNT) { "C-0090's key must carry $ARM_COUNT roots" }
    val supports = stations.map { (x, y) -> PointSupport(x, y, MANDATE / ARM_COUNT) }

    val bareLattice = host.lattice(emptyMap(), emptyList())
    val allSites = bareLattice.crossoverSites
    val partition = partitionRowEnd(allSites, host.rowEndSites)
    check(allSites.size == 56) { "C-0015's 56 crossovers, and there are ${allSites.size}" }
    check(partition.interior.size == 42) { "42 interior sites, and there are ${partition.interior.size}" }

    val mismatch = twistRateMismatch(
        T190_DESIGN_TWIST, T190_NATURAL_TWIST, Gen1Tile.RISE_PER_BASE_PAIR
    )
    // C-0107's principled smearing convention: the mean contour of one duplex per crossover
    val derivedSpacing = DUPLEXES * edgeX / allSites.size
    val nominalModel = EdgeTwistRelief(
        Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY, hinge, derivedSpacing, edgeX
    )
    val nominalEndDegrees = t190Degrees(nominalModel.endResidual(mismatch))

    // ------------------------------------------------------ Deliverable 1: the census, no solve
    println("T-190 — the census of the graded field, before any solve ...")
    val gradedPlus = corrugatedPrestrainField(nominalModel, mismatch, allSites, host::siteX, 1.0)
    val wholeLedger = prestrainLedger(gradedPlus, hinge)
    val rowEndLedgerPlus = prestrainLedger(restrictPrestrains(gradedPlus, partition.rowEnd), hinge)
    val interiorLedgerPlus =
        prestrainLedger(restrictPrestrains(gradedPlus, partition.interior), hinge)
    val gradedRowEndAngle = uniformValueOrNull(restrictPrestrains(gradedPlus, partition.rowEnd))
        ?: error("the graded field's row-end restriction is not uniform")
    val rowEndAmplitude = abs(gradedRowEndAngle)

    val columnRecords = host.columns.positions.indices.map { column ->
        val sites = allSites.filter { it.column == column }
        val angles = sites.map { gradedPlus.getValue(it) }
        val uniform = uniformValueOrNull(sites.associateWith { gradedPlus.getValue(it) })
            ?: error("column $column is not uniform over its own sites")
        T190ColumnRecord(
            columnIndex = column,
            x = host.columns.positions[column],
            parity = host.columns.parities[column],
            atRowEnd = sites.any { it in partition.rowEnd.toSet() },
            sites = sites.size,
            prestrainDegrees = t190Degrees(uniform),
            amplitudeOverRowEnd = abs(uniform) / rowEndAmplitude,
            absoluteCoupleShare = hinge * angles.sumOf { abs(it) } / wholeLedger.absoluteCouple
        )
    }
    columnRecords.forEach {
        println(
            "  column ${it.columnIndex} at x=${"%.2f".format(it.x)} nm, ${it.sites} sites, " +
                    "${"%+.3f".format(it.prestrainDegrees)} deg, " +
                    "${"%.3f".format(it.amplitudeOverRowEnd)} of the row end"
        )
    }

    // ------------------------------------------------------------ the solves, one bank of them
    println("T-190 — solving ...")
    val uniformLoadFalsifier = bareLattice.solve(host.uniformField)
        .peakDishing(81) / host.freeStroke

    fun coupled(prestrains: Map<CrossoverSite, Double>, subdivisions: Int = 2): GrillageDeflection =
        host.lattice(prestrains, supports, subdivisions).solve(host.solvedField)

    fun coupledDishing(prestrains: Map<CrossoverSite, Double>): Double =
        coupled(prestrains).peakDishing(81) / host.freeStroke

    val states = ArrayList<T190StateRecord>()
    fun read(
        state: String, siteSet: String, sign: Int, rowEndDegrees: Double,
        prestrains: Map<CrossoverSite, Double>
    ): T190StateRecord {
        val solution = coupled(prestrains)
        val ledger = prestrainLedger(prestrains, hinge)
        val record = T190StateRecord(
            state = state, siteSet = siteSet, overallSign = sign,
            rowEndDegrees = rowEndDegrees, sites = ledger.sites,
            netCouple = ledger.netCouple, absoluteCouple = ledger.absoluteCouple,
            coupledDishingOverStroke = solution.peakDishing(81) / host.freeStroke,
            freeDishingOverStroke = host.lattice(prestrains, emptyList())
                .solve(host.solvedField).peakDishing(81) / host.freeStroke,
            peakHingeMoment = solution.peakHingeMoment,
            flatAtTenPercent = solution.peakDishing(81) / host.freeStroke < FLATNESS_TOLERANCE
        )
        states += record
        println(
            "  ${state.take(52).padEnd(52)} ${"%9.7f".format(record.coupledDishingOverStroke)}" +
                    "  flat=${record.flatAtTenPercent}"
        )
        return record
    }

    val zero = read("zero prestrain — the reproduction gate", "none", 0, 0.0, emptyMap())
    val gradedMinus = corrugatedPrestrainField(nominalModel, mismatch, allSites, host::siteX, -1.0)
    listOf(1.0, -1.0).forEach { sign ->
        val field = if (sign > 0) gradedPlus else gradedMinus
        val end = uniformValueOrNull(restrictPrestrains(field, partition.rowEnd))
            ?: error("the graded field's row-end restriction is not uniform")
        val signLabel = if (sign > 0) "+" else "-"
        read(
            "the GRADED field over all 56, overall sign $signLabel",
            "all 56", sign.toInt(), t190Degrees(end), field
        )
        read(
            "the 14 row-end sites of that same field, sign $signLabel",
            "14 row-end", sign.toInt(), t190Degrees(end),
            restrictPrestrains(field, partition.rowEnd)
        )
        read(
            "the 42 interior sites of that same field, sign $signLabel",
            "42 interior", sign.toInt(), t190Degrees(end),
            restrictPrestrains(field, partition.interior)
        )
    }
    // C-0107's own two row-end-only states, at the ROW END station rather than at the column
    listOf(1.0, -1.0).forEach { sign ->
        read(
            "C-0107's row-end-only idealisation at " +
                    "${if (sign > 0) "+" else "-"}${"%.4f".format(nominalEndDegrees)} deg",
            "14 row-end", sign.toInt(), sign * nominalEndDegrees,
            t190Uniform(host.rowEndSites, sign * nominalEndDegrees * PI / 180.0)
        )
    }

    // --------------------------------- Deliverable 2: the decomposition, which is an IDENTITY
    println("T-190 — decomposing ...")
    val decomposition = listOf(1.0, -1.0).map { sign ->
        val field = if (sign > 0) gradedPlus else gradedMinus
        val rowEndField = restrictPrestrains(field, partition.rowEnd)
        val interiorField = restrictPrestrains(field, partition.interior)
        // prestrain-ONLY responses, so the decomposition is of the eigenstrain and not of the load
        val zeroPressure = uniformPressure(0.0)
        val dR = host.lattice(rowEndField, supports).solve(zeroPressure)
        val dI = host.lattice(interiorField, supports).solve(zeroPressure)
        val dG = host.lattice(field, supports).solve(zeroPressure)
        val basis = host.lattice(field, supports)
        fun inner(a: F64Array, b: F64Array) = basis.areaInnerProduct(a, b)
        val rr = inner(dR.dishingCoefficients, dR.dishingCoefficients)
        val ii = inner(dI.dishingCoefficients, dI.dishingCoefficients)
        val gg = inner(dG.dishingCoefficients, dG.dishingCoefficients)
        val ri = inner(dR.dishingCoefficients, dI.dishingCoefficients)
        val residual = dG.dishingCoefficients.copy()
        residual -= dR.dishingCoefficients
        residual -= dI.dishingCoefficients
        val superposition = sqrt(abs(inner(residual, residual)) / gg)
        val peakR = dR.peakDishing(81) / host.freeStroke
        val peakI = dI.peakDishing(81) / host.freeStroke
        val peakG = dG.peakDishing(81) / host.freeStroke
        T190DecompositionRecord(
            overallSign = sign.toInt(),
            rowEndNorm = sqrt(rr), interiorNorm = sqrt(ii), gradedNorm = sqrt(gg),
            cosine = cosineFromInnerProducts(rr, ii, ri),
            quadraticIdentityDeparture = t190TwoSignificant(abs(gg - (rr + ii + 2.0 * ri)) / gg),
            superpositionDeparture = t190TwoSignificant(superposition),
            peakRowEndOnly = peakR, peakInteriorOnly = peakI, peakGraded = peakG,
            peakSumOfParts = peakR + peakI,
            nonAdditivity = peakG / (peakR + peakI)
        )
    }
    decomposition.forEach {
        println(
            "  sign ${it.overallSign}: cosine ${"%.6f".format(it.cosine)}, " +
                    "|I|/|R| ${"%.4f".format(it.interiorNorm / it.rowEndNorm)}, " +
                    "superposition ${"%.2e".format(it.superpositionDeparture)}"
        )
    }

    // ------------------------- Deliverable 3: the 2x2x2 factorial of C-0107's own comparison
    println("T-190 — the factorial ...")
    val c0107Cells = listOf(
        "C-0107's row-end-only NOMINAL, +theta_0" to
                t190C0107Dishing(c0107File, "derived boundary layer, NOMINAL, +",
                    "coupledDishingOverStroke"),
        "C-0107's row-end-only NOMINAL, -theta_0" to
                t190C0107Dishing(c0107File, "derived boundary layer, NOMINAL, -",
                    "coupledDishingOverStroke"),
        "C-0107's GRADED field over all 56" to
                t190C0107Dishing(c0107File, "the GRADED", "coupledDishingOverStroke")
    )
    val factorial = ArrayList<T190FactorialRecord>()
    listOf(1.0, -1.0).forEach { sign ->
        listOf("row end (19.04 nm)" to nominalEndDegrees * PI / 180.0,
            "column (18.99 nm)" to rowEndAmplitude).forEach { (station, magnitude) ->
            listOf(false, true).forEach { interior ->
                // the graded field's row-end sign at overall sign `sign` is -sign, because the
                // parity rule puts every interface's row-end crossover at the end where
                // (-1)^b u(x) = -u(|x|). Measured above; used here.
                val rowEndAngle = if (gradedRowEndAngle < 0.0) -sign * magnitude
                else sign * magnitude
                val field = HashMap<CrossoverSite, Double>()
                partition.rowEnd.forEach { field[it] = rowEndAngle }
                if (interior) {
                    val graded = if (sign > 0) gradedPlus else gradedMinus
                    partition.interior.forEach { field[it] = graded.getValue(it) }
                }
                val value = coupledDishing(field)
                val matched = c0107Cells.firstOrNull { abs(it.second - value) < 1.0e-8 }
                factorial += T190FactorialRecord(
                    overallSign = sign.toInt(),
                    rowEndAngleStation = station,
                    interiorSitesLoaded = interior,
                    rowEndDegrees = t190Degrees(rowEndAngle),
                    coupledDishingOverStroke = value,
                    flatAtTenPercent = value < FLATNESS_TOLERANCE,
                    reproduces = matched?.first ?: "not read by C-0107"
                )
            }
        }
    }

    // -------------------------------------- Deliverable 4: the cumulative column-pair ladder
    println("T-190 — the ladder ...")
    val ladder = ArrayList<T190LadderRecord>()
    val pairs = host.columns.positions.indices
        .groupBy { Math.round(abs(host.columns.positions[it]) * 1.0e6) }
        .toList().sortedByDescending { it.first }.map { it.second }
    listOf(1.0, -1.0).forEach { sign ->
        val field = if (sign > 0) gradedPlus else gradedMinus
        val gradedValue = coupledDishing(field)
        val rowEndOnly = coupledDishing(restrictPrestrains(field, partition.rowEnd))
        var included = emptySet<Int>()
        pairs.forEachIndexed { index, pair ->
            included = included + pair
            val sites = allSites.filter { it.column in included }
            val value = coupledDishing(restrictPrestrains(field, sites))
            ladder += T190LadderRecord(
                overallSign = sign.toInt(),
                outermostColumnPairs = index + 1,
                sites = sites.size,
                coupledDishingOverStroke = value,
                flatAtTenPercent = value < FLATNESS_TOLERANCE,
                recoveredFraction = (rowEndOnly - value) / (rowEndOnly - gradedValue)
            )
        }
    }

    // ----------------------- Deliverable 5: the cancellation over C-0107's own 12-cell bracket
    println("T-190 — the bracket ...")
    val bracket = ArrayList<T190BracketRecord>()
    val spacings = listOf(derivedSpacing, 16.0 * Gen1Tile.RISE_PER_BASE_PAIR)
    val rigidities = listOf(
        Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY,
        Gen1Tile.DUPLEX_TORSIONAL_PERSISTENCE * thermalEnergy()
    )
    val hostByAlpha = HashMap<Double, T190Host>()
    fun hostAt(alpha: Double): T190Host = hostByAlpha.getOrPut(alpha) {
        if (alpha == 1.0) host else T190Host(
            origamiSheet(
                Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP,
                crossoverAlpha = alpha
            ),
            edgeX, smooth, rim
        )
    }
    rigidities.forEach { c ->
        listOf(0.6, 1.0, 1.2).forEach { alpha ->
            spacings.forEach { p ->
                listOf(true, false).forEach { consistent ->
                    // `consistent` moves the LATTICE's own hinge with alpha as well as the
                    // boundary layer's; C-0107 swept alpha in the field only, which is the
                    // `false` row here and is why both are carried.
                    if (!consistent && alpha == 1.0) return@forEach
                    val fieldHinge = Gen1Tile.crossoverHingeStiffness(alpha)
                    val model = EdgeTwistRelief(c, fieldHinge, p, edgeX)
                    val cellHost = if (consistent) hostAt(alpha) else host
                    listOf(1.0, -1.0).forEach { sign ->
                        val field = corrugatedPrestrainField(
                            model, mismatch, allSites, cellHost::siteX, sign
                        )
                        val gradedValue = cellHost.lattice(field, supports)
                            .solve(cellHost.solvedField).peakDishing(81) / cellHost.freeStroke
                        val rowEndValue =
                            cellHost.lattice(restrictPrestrains(field, partition.rowEnd), supports)
                                .solve(cellHost.solvedField)
                                .peakDishing(81) / cellHost.freeStroke
                        bracket += T190BracketRecord(
                            torsionalRigidity = c, crossoverAlpha = alpha, crossoverSpacing = p,
                            latticeHingeConsistent = consistent,
                            decayLength = model.decayLength,
                            endResidualDegrees = t190Degrees(model.endResidual(mismatch)),
                            overallSign = sign.toInt(),
                            gradedDishingOverStroke = gradedValue,
                            rowEndOnlyDishingOverStroke = rowEndValue,
                            cancellation = rowEndValue - gradedValue,
                            gradedFlat = gradedValue < FLATNESS_TOLERANCE,
                            rowEndOnlyFlat = rowEndValue < FLATNESS_TOLERANCE
                        )
                    }
                }
            }
        }
    }

    // --------------------------------------------------------------------------- convergence
    println("T-190 — convergence ...")
    val convergence = ArrayList<T190ConvergenceRecord>()
    fun converge(axis: String, coarse: Double, fine: Double) {
        convergence += T190ConvergenceRecord(
            axis, coarse, fine, t190TwoSignificant(abs(fine - coarse) / abs(fine))
        )
        println("  ${axis.take(50).padEnd(50)} ${"%.6f".format(coarse)} -> ${"%.6f".format(fine)}")
    }
    val sub1 = coupled(gradedPlus, 1).peakDishing(81) / host.freeStroke
    val sub2 = coupled(gradedPlus, 2).peakDishing(81) / host.freeStroke
    val sub4 = coupled(gradedPlus, 4).peakDishing(81) / host.freeStroke
    converge("beam subdivisions 1 -> 2, the graded field", sub1, sub2)
    converge("beam subdivisions 2 -> 4, the graded field", sub2, sub4)
    val solved = coupled(gradedPlus)
    converge(
        "dishing sample grid 41 -> 81",
        solved.peakDishing(41) / host.freeStroke, solved.peakDishing(81) / host.freeStroke
    )
    converge(
        "dishing sample grid 81 -> 161",
        solved.peakDishing(81) / host.freeStroke, solved.peakDishing(161) / host.freeStroke
    )
    val cosine1 = decomposition.first { it.overallSign == 1 }.cosine
    val cosineFine = run {
        val zeroPressure = uniformPressure(0.0)
        val dR = host.lattice(restrictPrestrains(gradedPlus, partition.rowEnd), supports, 4)
            .solve(zeroPressure)
        val dI = host.lattice(restrictPrestrains(gradedPlus, partition.interior), supports, 4)
            .solve(zeroPressure)
        val basis = host.lattice(gradedPlus, supports, 4)
        cosineFromInnerProducts(
            basis.areaInnerProduct(dR.dishingCoefficients, dR.dishingCoefficients),
            basis.areaInnerProduct(dI.dishingCoefficients, dI.dishingCoefficients),
            basis.areaInnerProduct(dR.dishingCoefficients, dI.dishingCoefficients)
        )
    }
    converge("the row-end/interior cosine, subdivisions 2 -> 4", cosine1, cosineFine)

    // ------------------------------------------------------------------------- reproductions
    val reproductions = ArrayList<T190Reproduction>()
    fun reproduce(what: String, owner: String, expected: Double, got: Double) {
        val departure = t190TwoSignificant(
            if (expected == 0.0) abs(got) else abs(got - expected) / abs(expected)
        )
        reproductions += T190Reproduction(what, owner, expected, got, departure)
        println("  reproduce ${what.take(56).padEnd(56)} ${"%.2e".format(departure)}")
    }
    reproduce(
        "C-0090's 34-root dishing at 38.08 nm / phase 8", "C-0090",
        publishedDishing, zero.coupledDishingOverStroke
    )
    reproduce(
        "C-0107's zero-prestrain baseline", "C-0107",
        t190C0107Dishing(c0107File, "zero prestrain", "coupledDishingOverStroke"),
        zero.coupledDishingOverStroke
    )
    reproduce(
        "C-0107's GRADED field over all 56 crossovers", "C-0107",
        t190C0107Dishing(c0107File, "the GRADED", "coupledDishingOverStroke"),
        states.first { it.state.startsWith("the GRADED field over all 56, overall sign +") }
            .coupledDishingOverStroke
    )
    reproduce(
        "C-0107's row-end-only NOMINAL, +theta_0", "C-0107",
        t190C0107Dishing(c0107File, "derived boundary layer, NOMINAL, +", "coupledDishingOverStroke"),
        states.first { it.state.startsWith("C-0107's row-end-only idealisation at +") }
            .coupledDishingOverStroke
    )
    reproduce(
        "C-0107's row-end-only NOMINAL, -theta_0", "C-0107",
        t190C0107Dishing(c0107File, "derived boundary layer, NOMINAL, -", "coupledDishingOverStroke"),
        states.first { it.state.startsWith("C-0107's row-end-only idealisation at -") }
            .coupledDishingOverStroke
    )
    reproduce(
        "C-0107's nominal end residual in degrees", "C-0107",
        t190C0107NominalEndResidual(c0107File, derivedSpacing), nominalEndDegrees
    )
    reproduce(
        "C-0107's uniform-load falsifier on the support-free prestrain-free lattice", "C-0107",
        t190C0107Dishing(c0107File, "zero prestrain", "uniformLoadDishingOverStroke"),
        uniformLoadFalsifier
    )
    reproduce("C-0104's threshold in degrees", "C-0104", c0104Threshold, T190_C0104_THRESHOLD_DEGREES)
    reproduce("C-0095's row-end crossover count", "C-0095", 14.0, host.rowEndSites.size.toDouble())
    reproduce("C-0015's crossover inventory", "C-0015", 56.0, allSites.size.toDouble())

    // ------------------------------------------------------------- predicates and falsifiers
    val gradedPlusRecord = states.first { it.state.startsWith("the GRADED field over all 56, overall sign +") }
    val gradedMinusRecord = states.first { it.state.startsWith("the GRADED field over all 56, overall sign -") }
    val rowEndPlusRecord = states.first { it.state.startsWith("the 14 row-end sites of that same field, sign +") }
    val rowEndMinusRecord = states.first { it.state.startsWith("the 14 row-end sites of that same field, sign -") }
    val interiorPlusRecord = states.first { it.state.startsWith("the 42 interior sites of that same field, sign +") }
    val c0107Plus = states.first { it.state.startsWith("C-0107's row-end-only idealisation at +") }
    val c0107Minus = states.first { it.state.startsWith("C-0107's row-end-only idealisation at -") }
    val decompositionPlus = decomposition.first { it.overallSign == 1 }
    val worstSuperposition = decomposition.maxOf { it.superpositionDeparture }
    val bracketGradedFlat = bracket.count { it.gradedFlat }
    val signMatchesC0107Plus = gradedRowEndAngle > 0.0

    val predicates = listOf(
        T190Predicate(
            "P1", "the decomposition is an identity: load + row-end response + interior " +
                    "response = the graded response, to 1e-10 in the coefficient vector",
            if (worstSuperposition < 1.0e-10) "PASS" else "FAIL",
            "worst superposition departure over both overall signs " +
                    "%.2e".format(worstSuperposition)
        ),
        T190Predicate(
            "P2", "the interior contribution is quantified against the row-end one on a " +
                    "convention-free measure and both peaks are reported separately",
            "PASS",
            "cosine ${"%.6f".format(decompositionPlus.cosine)}, " +
                    "|interior|/|row end| " +
                    "%.4f".format(decompositionPlus.interiorNorm / decompositionPlus.rowEndNorm)
        ),
        T190Predicate(
            "P3", "the cancellation is tested over the overall sign, the 12-cell bracket and " +
                    "the column structure",
            "PASS",
            "${bracket.size} bracket cells, ${ladder.size} ladder rungs, both overall signs"
        ),
        T190Predicate(
            "P4", "every re-read upstream number is reproduced from its own result file",
            if (reproductions.all { it.relativeDeparture < 1.0e-6 }) "PASS" else "FAIL",
            "${reproductions.size} reproductions, worst " +
                    "%.2e".format(reproductions.maxOf { it.relativeDeparture })
        )
    )

    val falsifiers = listOf(
        T190Falsifier(
            "F1", "the three solved fields do not superpose to 1e-10",
            worstSuperposition > 1.0e-10,
            "worst ${"%.2e".format(worstSuperposition)} over both signs"
        ),
        T190Falsifier(
            "F2", "the graded field's restriction to the 14 row-end sites is C-0107's NOMINAL " +
                    "+22.6184533 deg map",
            !signMatchesC0107Plus,
            "the graded field at overall sign + puts " +
                    "${"%+.4f".format(t190Degrees(gradedRowEndAngle))} deg on every row-end site, " +
                    "against C-0107's stated +${"%.4f".format(nominalEndDegrees)}"
        ),
        T190Falsifier(
            "F3", "the interior contribution is a small correction, |d_I| < 0.2 |d_R|",
            decompositionPlus.interiorNorm / decompositionPlus.rowEndNorm < 0.2,
            "|interior|/|row end| = " +
                    "%.4f".format(decompositionPlus.interiorNorm / decompositionPlus.rowEndNorm)
        ),
        T190Falsifier(
            "F4", "the graded field at the OTHER overall sign is also flat",
            gradedMinusRecord.flatAtTenPercent,
            "sign - reads ${"%.7f".format(gradedMinusRecord.coupledDishingOverStroke)}, " +
                    "flat=${gradedMinusRecord.flatAtTenPercent}"
        ),
        T190Falsifier(
            "F5", "every cell of the boundary-layer bracket keeps the graded field flat",
            bracketGradedFlat == bracket.size,
            "$bracketGradedFlat of ${bracket.size} bracket cells are flat under the graded field"
        ),
        T190Falsifier(
            "F6", "a uniform load on a uniform foundation dishes more than 1e-6 of the free " +
                    "stroke, on the support-free prestrain-free lattice",
            uniformLoadFalsifier > 1.0e-6,
            "%.3e".format(uniformLoadFalsifier)
        )
    )

    // ------------------------------------------------------------------------------ findings
    val findings = ArrayList<String>()
    var proseFailure: Throwable? = null
    try {
        findings += "THE FIELD IS EXACTLY SEPARABLE AND THE VERDICT IS NOT. A prestrain is a " +
                "load, so the solved field under the graded corrugated map is the load-only " +
                "field plus the 14-site response plus the 42-site response, to " +
                "${"%.2e".format(worstSuperposition)} in the coefficient vector at both overall " +
                "signs. Peak dishing is a seminorm of that field and does NOT add: the graded " +
                "peak is ${"%.4f".format(decompositionPlus.nonAdditivity)} of the sum of its " +
                "two parts' peaks. So no part of the verdict can be assigned to either site set."
        findings += "THE 42 INTERIOR SITES ARE NOT A CORRECTION, THEY ARE THE SAME ORDER AND " +
                "THE OPPOSITE SIGN. Their dishing field has " +
                "${"%.4f".format(decompositionPlus.interiorNorm / decompositionPlus.rowEndNorm)} " +
                "of the row-end field's area norm and a cosine of " +
                "${"%.4f".format(decompositionPlus.cosine)} against it. The cancellation is a " +
                "cross term, not a difference of two peaks, and it is structural: the column " +
                "next inboard of the row end carries " +
                "${"%.3f".format(columnRecords[1].amplitudeOverRowEnd)} of the row end's " +
                "amplitude on the same number of sites at the opposite sign, which is " +
                "arithmetic and needs no solve."
        findings += "THE GRADED FIELD'S OWN ROW-END RESTRICTION IS UNIFORM AT " +
                "${"%+.4f".format(t190Degrees(gradedRowEndAngle))} deg AT OVERALL SIGN +, " +
                "which is the sign C-0107 calls ADVERSE. The lattice's parity rule puts every " +
                "interface's row-end crossover at the end where (-1)^b u(x) = -u(|x|), so the " +
                "consistent row-end-only counterpart of the graded " +
                "${"%.7f".format(gradedPlusRecord.coupledDishingOverStroke)} is " +
                "${"%.7f".format(rowEndPlusRecord.coupledDishingOverStroke)} and NOT C-0107's " +
                "${"%.7f".format(c0107Plus.coupledDishingOverStroke)}: the published comparison " +
                "pairs the graded field at one overall sign with the row-end-only idealisation " +
                "at the other. The cancellation is therefore LARGER than published, " +
                "${"%.4f".format(rowEndPlusRecord.coupledDishingOverStroke - gradedPlusRecord.coupledDishingOverStroke)} " +
                "of the stroke against " +
                "${"%.4f".format(c0107Plus.coupledDishingOverStroke - gradedPlusRecord.coupledDishingOverStroke)}."
        findings += "THE OVERALL SIGN OF THE CORRUGATION IS UNDETERMINED AND C-0107 READ ONE OF " +
                "TWO. The glide symmetry says the field alternates with the interface index and " +
                "does not say which parity folds which way, so both signs are admissible. They " +
                "read ${"%.7f".format(gradedPlusRecord.coupledDishingOverStroke)} " +
                "(flat=${gradedPlusRecord.flatAtTenPercent}) and " +
                "${"%.7f".format(gradedMinusRecord.coupledDishingOverStroke)} " +
                "(flat=${gradedMinusRecord.flatAtTenPercent})."
        findings += "THE CANCELLATION IS COMPLETE BY THE FIRST INTERIOR COLUMN PAIR. Adding the " +
                "column pair next inboard of the row ends recovers " +
                "${"%.1f".format(100.0 * ladder.first { it.overallSign == 1 && it.outermostColumnPairs == 2 }.recoveredFraction)} % " +
                "of the whole row-end-to-graded move; the two innermost pairs together are " +
                "worth the remainder."
        findings += "OVER C-0107'S OWN 12-CELL BRACKET AND BOTH SIGNS, " +
                "$bracketGradedFlat of ${bracket.size} cells keep the graded field flat, " +
                "against ${bracket.count { it.rowEndOnlyFlat }} of ${bracket.size} for the " +
                "row-end-only idealisation. The cancellation is a property of the field's " +
                "SHAPE, and the shape is set by lambda/L, which the bracket moves by " +
                "${"%.2f".format(bracket.maxOf { it.decayLength } / bracket.minOf { it.decayLength })}x."
        findings += "THE ROW-END-ONLY IDEALISATION IS THE lambda -> 0 LIMIT OF THE GRADED FIELD, " +
                "and the Gen-1 tile is nowhere near it: the derived decay length is " +
                "${"%.2f".format(nominalModel.decayLength)} nm against a " +
                "${"%.2f".format(edgeX / 2.0)} nm half-row, so the field is graded over the " +
                "whole tile and the 14-site map has nothing behind it. C-0104's three " +
                "distributions are maps on 14 sites because C-0104 had no field; C-0107 has one."
        findings += "THE STANDING UNIFORM-LOAD FALSIFIER READS " +
                "${"%.3e".format(uniformLoadFalsifier)} on the support-free, prestrain-free " +
                "lattice. CLAUDE.md's best falsifier is NOT declared against the eigenstrain " +
                "here: a uniform prestrain is an eigenstrain and the relaxed state is a " +
                "cylinder, which C-0104 Deliverable 5 measured, so asserting the zero would " +
                "report a correct solver as broken."
    } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
        proseFailure = e
        findings += "PROSE CONSTRUCTION FAILED: $e"
    }

    val result = T190Result(
        task = "T-190",
        leaf = "A8.2 (the plan and lattice model the anchoring array is written on), with A1.2",
        question = "What do the 42 interior crossovers of C-0107's graded corrugated field " +
                "carry, and does the cancellation that keeps C-0090's placement flat hold?",
        conditions = "T = 300 K, k_BT = 4.141947 pN nm; aqueous 2 mM MgCl2; single-layer " +
                "square-lattice Rothemund sheet, 15 duplexes at 2.69 nm, 0.34 nm rise, 32/3 bp " +
                "per turn design against B-DNA's 10.5, 16 bp column pitch; 38.08 nm along the " +
                "helices at crossover phase 8; C-0090's published 34-root key; C-0017's " +
                "33.3333 pN/nm as 34 explicit elastic supports; C-0022's solved collar at 2 mM, " +
                "a 10 nm gap and 0.192 V; C-0001's foundation secant; free stroke 5.15473846 nm",
        columns = columnRecords,
        states = states,
        decomposition = decomposition,
        factorial = factorial,
        ladder = ladder,
        bracket = bracket,
        convergence = convergence,
        reproductions = reproductions,
        predicates = predicates,
        falsifiers = falsifiers,
        findings = findings,
        parameters = mapOf(
            "duplexes" to DUPLEXES.toString(),
            "phaseBasePairs" to PHASE.toString(),
            "edgeX" to t190Parameter(edgeX),
            "armCount" to ARM_COUNT.toString(),
            "armLength" to t190Parameter(arm),
            "mandate" to t190Parameter(MANDATE),
            "crossoverHingeStiffness" to t190Parameter(hinge),
            "crossovers" to allSites.size.toString(),
            "rowEndCrossovers" to partition.rowEnd.size.toString(),
            "interiorCrossovers" to partition.interior.size.toString(),
            "twistRateMismatch" to t190Parameter(mismatch),
            "derivedCrossoverSpacing" to t190Parameter(derivedSpacing),
            "nominalDecayLength" to t190Parameter(nominalModel.decayLength),
            "nominalEndResidualDegrees" to t190Parameter(nominalEndDegrees),
            "gradedRowEndDegreesAtSignPlus" to t190Parameter(t190Degrees(gradedRowEndAngle)),
            "freeStroke" to t190Parameter(host.freeStroke),
            "flatnessTolerance" to t190Parameter(FLATNESS_TOLERANCE),
            "c0104ThresholdDegrees" to t190Parameter(c0104Threshold),
            "wholeFieldAbsoluteCouple" to t190Parameter(wholeLedger.absoluteCouple),
            "rowEndAbsoluteCoupleShare" to
                    t190Parameter(rowEndLedgerPlus.absoluteCouple / wholeLedger.absoluteCouple),
            "interiorAbsoluteCoupleShare" to
                    t190Parameter(interiorLedgerPlus.absoluteCouple / wholeLedger.absoluteCouple),
            "sources" to "gpd/results/T-3b-tile-edge-load-profile.json, " +
                    "gpd/results/T-153-buildable-raster-width.json, " +
                    "gpd/results/T-172-row-end-prestrain.json, " +
                    "gpd/results/T-182-row-end-prestrain-value.json"
        )
    )

    val json = Json { prettyPrint = true }
    val out = File("gpd/results/T-190-interior-crossover-prestrain.json")
    // `floor = 0.0`, deliberately. `RESULT_ABSOLUTE_FLOOR` is a claim in the LOCKED UNITS —
    // that no force below a nanopiconewton is of interest — and the load-bearing numbers of this
    // study are **dimensionless**: a superposition departure of `2.1e-15` is the evidence for
    // `P1` and the default floor emits it as exactly `0.0`, which reads as a stronger claim than
    // the truth. Every dimensionless departure here is pre-rounded to two significant digits by
    // [t190TwoSignificant], which is what makes an unfloored emission reproducible.
    out.writeText(
        json.encodeToString(json.encodeToJsonElement(result).roundedForResult(floor = 0.0).withEmissionHeader(LatticeTag.SQUARE, null))
    )
    println("T-190 — wrote ${out.path}")
    findings.forEach { println("  * $it") }
    println("  states: graded+ ${gradedPlusRecord.coupledDishingOverStroke}, " +
            "graded- ${gradedMinusRecord.coupledDishingOverStroke}, " +
            "rowEnd+ ${rowEndPlusRecord.coupledDishingOverStroke}, " +
            "rowEnd- ${rowEndMinusRecord.coupledDishingOverStroke}, " +
            "interior+ ${interiorPlusRecord.coupledDishingOverStroke}, " +
            "C-0107 minus row ${c0107Minus.coupledDishingOverStroke}")
    if (proseFailure != null) throw proseFailure
}
