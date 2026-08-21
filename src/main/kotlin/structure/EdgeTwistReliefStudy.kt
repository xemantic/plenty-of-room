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

import com.xemantic.nano.plentyofroom.anchoring.BForm
import com.xemantic.nano.plentyofroom.anchoring.BUILDABLE_RASTER_WIDTH
import com.xemantic.nano.plentyofroom.anchoring.placementFromKey
import com.xemantic.nano.plentyofroom.anchoring.quantisedToRise
import com.xemantic.nano.plentyofroom.anchoring.rasterColumnLayout
import com.xemantic.nano.plentyofroom.anchoring.rowEndCrossoverSites
import com.xemantic.nano.plentyofroom.coupling.CollarTerm
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
 * `T-182` — what prestrain does a row-end crossover actually **carry**?
 *
 * ```shell
 * tools/study.sh structure.EdgeTwistReliefStudyKt
 * ```
 *
 * Emits `gpd/results/T-182-row-end-prestrain-value.json`. Reads
 * `gpd/results/T-3b-tile-edge-load-profile.json` (`C-0022`'s solved collar, as `C-0090`,
 * `C-0099` and `C-0104` carried it), `gpd/results/T-153-buildable-raster-width.json` (`C-0090`'s
 * published reading and optimum placement key) and `gpd/results/T-172-row-end-prestrain.json`
 * (`C-0104`'s threshold and unit slope, as the reproduction gate).
 *
 * `C-0104` fixed the **threshold** at 15.4497275° and left the value open. This study asks what
 * bounds the value: four independent ceilings, one of which — the duplex's own **twist boundary
 * layer** at a free row end ([EdgeTwistRelief]) — is derived here and is the only one that
 * produces a *number* rather than a limit.
 *
 * The coupled dishing is re-read through an **explicit elastic-support** grillage rather than
 * through `C-0058`'s Woodbury surrogate, which is a genuinely independent code path and is why
 * the reproduction of `C-0090`'s 0.0621469105 is a gate rather than a tautology. It also side-
 * steps `CH-0120` entirely: no influence bank is built, so no bank can be contaminated.
 */

private const val DUPLEXES = 15
private const val PHASE = 8
private const val FLATNESS_TOLERANCE = 0.10
private const val RIM_STANDOFF = 1.0
private const val ARM_COUNT = C0055_ARM_COUNT
private val MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

/** `C-0104`'s threshold, read from its result file and asserted, never transcribed. */
private const val C0104_THRESHOLD_DEGREES = 15.4497275

/** The square lattice's design twist, `32/3` bp per turn, in degrees per base. */
private val DESIGN_TWIST = 360.0 / (Gen1Tile.CROSSOVER_SPACING_SHEET_BP / 3.0)

/** B-DNA's preferred twist, 10.5 bp per turn, in degrees per base. */
private const val NATURAL_TWIST = 360.0 / 10.5

private fun degrees(radians: Double): Double = radians * 180.0 / PI

/**
 * Rounds a dimensionless departure or convergence measure to **two significant digits**.
 *
 * `CLAUDE.md`, twice: `RESULT_ABSOLUTE_FLOOR` is a claim about the locked *units* and emits a
 * dimensionless `1e-10` as `0.0`, and a reproducibility measure should be emitted coarsely
 * enough that a last-ulp difference cannot move it. `C-0102` made its result file byte-identical
 * across two runs by exactly this rounding.
 */
private fun twoSignificant(value: Double): Double {
    if (value == 0.0 || !value.isFinite()) return value
    val magnitude = kotlin.math.floor(kotlin.math.log10(abs(value)))
    val scale = Math.pow(10.0, 1.0 - magnitude)
    return kotlin.math.round(value * scale) / scale
}

// ---------------------------------------------------------------------------------------------
// records — prefixed with the task, because study record classes are package scoped (CLAUDE.md)
// ---------------------------------------------------------------------------------------------

@Serializable
private data class T182CeilingRecord(
    val name: String,
    val statement: String,
    val degrees: Double,
    val owner: String,
    val readStatus: String,
    val derivedHere: Boolean,
    val closesTheQuestion: Boolean
)

@Serializable
private data class T182BoundaryLayerRecord(
    val torsionalRigidity: Double,
    val torsionalRigiditySource: String,
    val crossoverAlpha: Double,
    val hingeStiffness: Double,
    val crossoverSpacing: Double,
    val crossoverSpacingBasis: String,
    val decayLength: Double,
    val decayOverHalfRow: Double,
    val endResidualDegrees: Double,
    val rigidLimitDegrees: Double,
    val reliefFraction: Double,
    val aboveThreshold: Boolean
)

@Serializable
private data class T182DishingRecord(
    val state: String,
    val distribution: String,
    val degrees: Double,
    val coupledDishingOverStroke: Double,
    val freeDishingOverStroke: Double,
    val uniformLoadDishingOverStroke: Double,
    val peakHingeMoment: Double,
    val flatAtTenPercent: Boolean
)

@Serializable
private data class T182LiteratureRecord(
    val question: String,
    val source: String,
    val locator: String,
    val readStatus: String,
    val quantified: Boolean,
    val degrees: Double?,
    val note: String
)

@Serializable
private data class T182ConvergenceRecord(
    val axis: String,
    val coarse: Double,
    val fine: Double,
    val relativeMovement: Double
)

@Serializable
private data class T182Reproduction(
    val what: String,
    val owner: String,
    val expected: Double,
    val reproduced: Double,
    val departure: Double
)

@Serializable
private data class T182Predicate(val id: String, val statement: String, val met: Boolean)

@Serializable
private data class T182Falsifier(
    val id: String,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
private data class T182OxdnaCost(
    val item: String,
    val value: String,
    val basis: String
)

@Serializable
private data class T182Result(
    val task: String,
    val leaf: String,
    val question: String,
    val conditions: Map<String, String>,
    val ceilings: List<T182CeilingRecord>,
    val boundaryLayer: List<T182BoundaryLayerRecord>,
    val dishing: List<T182DishingRecord>,
    val literature: List<T182LiteratureRecord>,
    val convergence: List<T182ConvergenceRecord>,
    val reproductions: List<T182Reproduction>,
    val oxdnaCost: List<T182OxdnaCost>,
    val predicates: List<T182Predicate>,
    val falsifiers: List<T182Falsifier>,
    val findings: List<String>,
    val parameters: Map<String, String>
)

// ---------------------------------------------------------------------------------------------
// the inputs, read from the claims that own them
// ---------------------------------------------------------------------------------------------

/** `C-0022`'s solved edge profile, keyed on concentration, gap **and bias** (`CLAUDE.md`). */
private fun t182SolvedProfile(file: File): Pair<CollarTerm, CollarTerm> {
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
private fun t182C0090Reading(file: File, casePrefix: String, phase: Int): Pair<Double, String> {
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
private fun t182C0104Bound(file: File, namePrefix: String): Double {
    require(file.exists()) { "C-0104's result file is missing: ${file.path}" }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("cheapBounds").jsonArray.map { it.jsonObject }
        .firstOrNull { it.getValue("name").jsonPrimitive.content.startsWith(namePrefix) }
        ?: error("no C-0104 cheap bound named $namePrefix")
    return record.getValue("value").jsonPrimitive.content.toDouble()
}

// ---------------------------------------------------------------------------------------------
// the host — an EXPLICIT elastic-support grillage, an independent path from C-0058's surrogate
// ---------------------------------------------------------------------------------------------

private class T182Host(
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

    /** Every crossover of the lattice, with the `x` of its column measured from the row centre. */
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

/** [sites] built at the same [angle], the uniform distribution. */
private fun t182Uniform(sites: List<CrossoverSite>, angle: Double): Map<CrossoverSite, Double> =
    sites.associateWith { angle }

/** Rothemund's glide symmetry alone: the sign flips with the interface index. */
private fun t182Alternating(
    sites: List<CrossoverSite>,
    angle: Double
): Map<CrossoverSite, Double> =
    sites.associateWith { if (it.lowerBeam % 2 == 0) angle else -angle }

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    val sheet = origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)
    val edgeX = BUILDABLE_RASTER_WIDTH
    val arm = quantisedToRise(C0055_ARM_LENGTH)
    val hinge = Gen1Tile.crossoverHingeStiffness()

    println("T-182 — reading C-0022's collar, C-0090's placement and C-0104's threshold ...")
    val (smooth, rim) = t182SolvedProfile(File("gpd/results/T-3b-tile-edge-load-profile.json"))
    val (publishedDishing, publishedKey) =
        t182C0090Reading(File("gpd/results/T-153-buildable-raster-width.json"), "RECOMMENDED", PHASE)
    val c0104File = File("gpd/results/T-172-row-end-prestrain.json")
    val c0104Threshold = t182C0104Bound(c0104File, "the prestrain at which C-0090")
    val c0104Slope = t182C0104Bound(c0104File, "dishing per radian of uniform row-end prestrain")
    val c0104Baseline = t182C0104Bound(c0104File, "C-0090's placement dishing at zero prestrain")
    check(abs(c0104Threshold - C0104_THRESHOLD_DEGREES) < 1e-6) {
        "C-0104's threshold moved: $c0104Threshold"
    }

    val host = T182Host(sheet, edgeX, smooth, rim)
    check(host.columns.size == 8) { "phase $PHASE must carry 8 columns" }
    check(host.rowEndSites.size == DUPLEXES - 1) { "one row-end crossover per interface" }
    val placement = placementFromKey(publishedKey, PHASE, arm, edgeX)
    val stations = placement.stations(DUPLEXES, sheet.interhelicalDistance)
    check(stations.size == ARM_COUNT) { "C-0090's key must carry $ARM_COUNT roots" }
    val supports = stations.map { (x, y) -> PointSupport(x, y, MANDATE / ARM_COUNT) }

    // -------------------------------------------------------------------- Deliverable 1: ceilings
    val ceilings = ArrayList<T182CeilingRecord>()
    fun ceiling(
        name: String, statement: String, radians: Double, owner: String,
        readStatus: String, derivedHere: Boolean
    ) {
        val d = degrees(radians)
        ceilings += T182CeilingRecord(
            name = name, statement = statement, degrees = d, owner = owner,
            readStatus = readStatus, derivedHere = derivedHere,
            closesTheQuestion = d < C0104_THRESHOLD_DEGREES
        )
        println("  ceiling %-38s %9.3f deg  closes=%s".format(name.take(38), d,
            d < C0104_THRESHOLD_DEGREES))
    }
    listOf(
        "unzip allowable, 10 pN" to 10.0, "unzip allowable, 15 pN" to 15.0,
        "duplex shear allowable, 48 pN" to 48.0, "the nicked ceiling, 65 pN" to 65.0
    ).forEach { (label, force) ->
        ceiling(
            "rupture — $label",
            "k_theta = 2 k_bond a^2 on C-0029's two termini at the measured phosphate radius, " +
                    "so theta_0 = 2 a F / k_theta",
            prestrainAtBondForce(force, hinge, BForm.PHOSPHATE_RADIUS),
            "T-182", "DERIVED (allowable CITED, CLAUDE.md)", true
        )
    }
    ceiling(
        "register quantum — half a base-pair step",
        "the edge domain length is an INTEGER of base pairs and Rothemund minimises the strain " +
                "over it by hand, so the residual after optimal rounding is at most half a step",
        0.5 * NATURAL_TWIST * PI / 180.0, "T-182 / C-0104", "DERIVED", true
    )
    ceiling(
        "Rothemund's own remedy — one unpaired base",
        "\"one or two scaffold bases could be left unpaired ... that should relax the crossover\" " +
                "read as a SCALE: a remedy sized at one base prices the strain at one base of twist",
        unpairedBaseRelief(1.0, NATURAL_TWIST),
        "Rothemund 2006 Suppl. Note S2", "CITED, READ DIRECTLY", false
    )
    ceiling(
        "Rothemund's own remedy — two unpaired bases",
        "the upper end of the same clause",
        unpairedBaseRelief(2.0, NATURAL_TWIST),
        "Rothemund 2006 Suppl. Note S2", "CITED, READ DIRECTLY", false
    )

    // --------------------------------------------- Deliverable 2: the twist boundary layer bracket
    val mismatch = twistRateMismatch(DESIGN_TWIST, NATURAL_TWIST, Gen1Tile.RISE_PER_BASE_PAIR)
    val crossoverCount = host.lattice(emptyMap(), emptyList()).crossoverSites.size
    // the mean contour of ONE duplex per crossover it carries, the hinge energy halved because a
    // crossover is shared by two duplexes: p = D L / N_c
    val derivedSpacing = DUPLEXES * edgeX / crossoverCount
    val spacings = listOf(
        derivedSpacing to "D L / N_c, the mean contour per crossover with the hinge shared",
        16.0 * Gen1Tile.RISE_PER_BASE_PAIR to "the 16 bp column pitch, every crossover unshared"
    )
    val rigidities = listOf(
        Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY to "CanDo GJ = 460 pN nm^2",
        Gen1Tile.DUPLEX_TORSIONAL_PERSISTENCE * thermalEnergy() to
                "torsional persistence 100 nm x k_BT, magnetic tweezers"
    )
    val boundaryLayer = ArrayList<T182BoundaryLayerRecord>()
    rigidities.forEach { (c, cSource) ->
        listOf(0.6, 1.0, 1.2).forEach { alpha ->
            spacings.forEach { (p, pBasis) ->
                val k = Gen1Tile.crossoverHingeStiffness(alpha)
                val model = EdgeTwistRelief(c, k, p, edgeX)
                val end = model.endResidual(mismatch)
                val rigid = model.rigidLimit(mismatch)
                boundaryLayer += T182BoundaryLayerRecord(
                    torsionalRigidity = c, torsionalRigiditySource = cSource,
                    crossoverAlpha = alpha, hingeStiffness = k,
                    crossoverSpacing = p, crossoverSpacingBasis = pBasis,
                    decayLength = model.decayLength,
                    decayOverHalfRow = model.decayLength / (edgeX / 2.0),
                    endResidualDegrees = degrees(end),
                    rigidLimitDegrees = degrees(rigid),
                    reliefFraction = 1.0 - end / rigid,
                    aboveThreshold = degrees(end) > C0104_THRESHOLD_DEGREES
                )
            }
        }
    }
    val derived = boundaryLayer.filter { it.crossoverSpacing == derivedSpacing }
    val lowDegrees = derived.minOf { it.endResidualDegrees }
    val highDegrees = derived.maxOf { it.endResidualDegrees }
    val nominal = boundaryLayer.first {
        it.crossoverAlpha == 1.0 && it.crossoverSpacing == derivedSpacing &&
                it.torsionalRigidity == Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY
    }
    println("  boundary layer: %.3f - %.3f deg (nominal %.3f, rigid limit %.3f)".format(
        lowDegrees, highDegrees, nominal.endResidualDegrees, nominal.rigidLimitDegrees))
    ceiling(
        "the twist boundary layer at a free row end",
        "u(L/2) = mismatch x lambda x tanh(L/2 lambda), lambda = sqrt(C p / k_theta) — the only " +
                "ceiling of the four that is a VALUE, and it is the largest",
        highDegrees * PI / 180.0, "T-182", "DERIVED", true
    )

    // ------------------------------------------------ Deliverable 3: the coupled dishing re-read
    println("T-182 — solving the coupled tile at the derived prestrains ...")
    val dishing = ArrayList<T182DishingRecord>()
    fun read(
        state: String, distribution: String, angleDegrees: Double,
        prestrains: Map<CrossoverSite, Double>, samples: Int = 81, subdivisions: Int = 2
    ): T182DishingRecord {
        val coupled = host.lattice(prestrains, supports, subdivisions)
        val free = host.lattice(prestrains, emptyList(), subdivisions)
        val solution = coupled.solve(host.solvedField)
        val record = T182DishingRecord(
            state = state, distribution = distribution, degrees = angleDegrees,
            coupledDishingOverStroke = solution.peakDishing(samples) / host.freeStroke,
            freeDishingOverStroke = free.solve(host.solvedField)
                .peakDishing(samples) / host.freeStroke,
            // The standing falsifier is a statement about a FREE plate under a uniform load, so
            // it is read on the SUPPORT-FREE, prestrain-free lattice. Read on the 34-anchor
            // coupled lattice it fires at 0.17 — correctly, because a sparse coupling is itself
            // a dishing source (C-0060), which is not what the falsifier is about. Fourth
            // instance of this trap around this term, after C-0104's three (CH-0120).
            uniformLoadDishingOverStroke = free.withoutPrestrain
                .solve(host.uniformField).peakDishing(samples) / host.freeStroke,
            peakHingeMoment = solution.peakHingeMoment,
            flatAtTenPercent = solution.peakDishing(samples) / host.freeStroke <=
                    FLATNESS_TOLERANCE
        )
        dishing += record
        println("  %-46s %8.3f deg  %.7f  flat=%s".format(
            state.take(46), angleDegrees, record.coupledDishingOverStroke,
            record.flatAtTenPercent))
        return record
    }

    val zero = read("zero prestrain — the reproduction gate", "none", 0.0, emptyMap())
    val states = listOf(
        "derived boundary layer, low end" to lowDegrees,
        "derived boundary layer, NOMINAL" to nominal.endResidualDegrees,
        "derived boundary layer, high end" to highDegrees,
        "the un-relieved rigid limit" to nominal.rigidLimitDegrees
    )
    states.forEach { (label, deg) ->
        listOf(1.0, -1.0).forEach { sign ->
            val angle = sign * deg * PI / 180.0
            read(
                "$label, ${if (sign > 0) "+" else "-"}theta_0",
                "uniform — what the sign composition selects", sign * deg,
                t182Uniform(host.rowEndSites, angle)
            )
        }
    }
    // the distribution the glide symmetry gives BEFORE composing with the alternating row ends,
    // carried so the composition can be priced rather than asserted
    read(
        "NOMINAL on the glide-alternating distribution",
        "alternating — the glide symmetry alone, C-0104's even-in-sign map",
        nominal.endResidualDegrees,
        t182Alternating(host.rowEndSites, nominal.endResidualDegrees * PI / 180.0)
    )
    // the GRADED corrugated field over every crossover, which is what the boundary layer really is
    val gradedModel = EdgeTwistRelief(
        Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY, hinge, derivedSpacing, edgeX
    )
    val allSites = host.lattice(emptyMap(), emptyList()).crossoverSites
    val graded = allSites.associateWith { site ->
        corrugatedPrestrain(gradedModel, mismatch, site.lowerBeam, host.siteX(site))
    }
    read(
        "the GRADED corrugated field over all $crossoverCount crossovers",
        "graded — (-1)^b u(x) at every site, the field the boundary layer actually produces",
        nominal.endResidualDegrees, graded
    )
    // the unit-prestrain response, two ways. C-0104's `dishingPerRadian` is the SECANT of the
    // peak — |D(1 rad, loaded) - D(0, loaded)| — and not the peak of the prestrain-only field.
    // The two differ because peak dishing is a peak of |A + theta B|, which is convex in theta
    // and not linear. C-0104's ceiling is still an upper bound on the crossing, by convexity on
    // [0, 1 rad]; the TRIANGLE-inequality ceiling it names is the other one, and it is tighter.
    val unit = host.lattice(t182Uniform(host.rowEndSites, 1.0), supports)
    val unitSlope = unit.solve(uniformPressure(0.0)).peakDishing(81) / host.freeStroke
    val unitSecant = abs(
        unit.solve(host.solvedField).peakDishing(81) / host.freeStroke -
                zero.coupledDishingOverStroke
    )
    val trueTriangleCeiling =
        degrees((FLATNESS_TOLERANCE - zero.coupledDishingOverStroke) / unitSlope)
    println("  C-0104's secant %.9f here; true triangle ceiling %.4f deg".format(
        unitSecant, trueTriangleCeiling))

    // -------------------------------------------------------------- Deliverable 4: convergence
    val convergence = ArrayList<T182ConvergenceRecord>()
    fun converge(axis: String, coarse: Double, fine: Double) {
        convergence += T182ConvergenceRecord(
            axis, coarse, fine, twoSignificant(abs(fine - coarse) / abs(fine))
        )
        println("  convergence %-42s %.6f -> %.6f".format(axis.take(42), coarse, fine))
    }
    val nominalField = t182Uniform(host.rowEndSites, -nominal.endResidualDegrees * PI / 180.0)
    val sub1 = host.lattice(nominalField, supports, 1).solve(host.solvedField)
        .peakDishing(81) / host.freeStroke
    val sub2 = host.lattice(nominalField, supports, 2).solve(host.solvedField)
        .peakDishing(81) / host.freeStroke
    val sub4 = host.lattice(nominalField, supports, 4).solve(host.solvedField)
        .peakDishing(81) / host.freeStroke
    converge("beam subdivisions 1 -> 2, at the adverse nominal", sub1, sub2)
    converge("beam subdivisions 2 -> 4, at the adverse nominal", sub2, sub4)
    val solved2 = host.lattice(nominalField, supports, 2).solve(host.solvedField)
    converge("dishing sample grid 41 -> 81",
        solved2.peakDishing(41) / host.freeStroke, solved2.peakDishing(81) / host.freeStroke)
    converge("dishing sample grid 81 -> 161",
        solved2.peakDishing(81) / host.freeStroke, solved2.peakDishing(161) / host.freeStroke)
    converge("boundary layer, discrete chain 64 -> 256 elements",
        degrees(gradedModel.discreteEndResidual(mismatch, 64)),
        degrees(gradedModel.discreteEndResidual(mismatch, 256)))

    // -------------------------------------------------------------- Deliverable 5: reproductions
    val reproductions = ArrayList<T182Reproduction>()
    fun reproduce(what: String, owner: String, expected: Double, got: Double) {
        val departure = twoSignificant(
            if (expected == 0.0) abs(got) else abs(got - expected) / abs(expected)
        )
        reproductions += T182Reproduction(what, owner, expected, got, departure)
        println("  reproduce %-52s %.6e".format(what.take(52), departure))
    }
    reproduce(
        "C-0090's 34-root dishing at 38.08 nm / phase 8, on an EXPLICIT-SUPPORT grillage",
        "C-0090", publishedDishing, zero.coupledDishingOverStroke
    )
    reproduce("C-0104's zero-prestrain baseline", "C-0104",
        c0104Baseline, zero.coupledDishingOverStroke)
    reproduce("C-0104's dishing per radian of uniform row-end prestrain, ON ITS OWN " +
            "DEFINITION (the secant of the peak at 1 rad)", "C-0104", c0104Slope, unitSecant)
    reproduce("Gen1Tile's crossover hinge stiffness from the two-bond count", "C-0009",
        13.5294118, hinge)
    reproduce("CLAUDE.md's 16 bp register departure", "C-0015",
        8.5714286, degrees(registerPrestrain(16.0, DESIGN_TWIST, NATURAL_TWIST)))
    reproduce("C-0095's row-end crossover count", "C-0095",
        (DUPLEXES - 1).toDouble(), host.rowEndSites.size.toDouble())
    reproduce("C-0015's crossover inventory at eight columns", "C-0015",
        56.0, crossoverCount.toDouble())

    // -------------------------------------------------------------- Deliverable 6: the literature
    val literature = listOf(
        T182LiteratureRecord(
            "Is the residual angle at a row-end / edge crossover quantified anywhere?",
            "EuropePMC, 20 queries over 156 unique records, plus 10 targeted title queries",
            "gpd/data/T-182-sources/europepmc-queries.json, targeted-queries.json",
            "abstracts read; five full texts fetched and read", false, null,
            "NOT FOUND. Extends C-0104's 10 queries onto oxDNA, all-atom MD, cryo-EM, the " +
                    "global-twist measurement line and the corrugation/tube literature."
        ),
        T182LiteratureRecord(
            "Has an oxDNA study measured the interduplex roll at origami crossovers?",
            "Snodin, Schreck, Romano, Louis & Doye, Nucleic Acids Res. 47:1585 (2019)",
            "gpd/data/T-151-sources/PMC6379721-fullTextXML.xml (already in the repository) " +
                    "and gpd/data/T-182-sources/PMC6379721-snodin2019.pdf, Figure 5",
            "READ DIRECTLY (text); figure digitised against its own axis", true, 9.0,
            "YES, and it is the same coordinate: \"the angle between the inter-helix vector and " +
                    "the average inter-helix vector at the nearest junction ... projected onto " +
                    "the plane perpendicular to the average helix axis\". Figure 5's thick black " +
                    "AVERAGE peaks at about +8 deg and -9 deg either side of a junction; " +
                    "individual junctions reach about +-16 deg."
        ),
        T182LiteratureRecord(
            "Does that study cover the ROW-END crossover?",
            "Snodin et al. (2019), same paragraph",
            "gpd/data/T-151-sources/PMC6379721-fullTextXML.xml",
            "READ DIRECTLY", false, null,
            "NO, and it says so: \"we do not include data for all junctions, but only those that " +
                    "have the canonical pattern of neighbouring junctions; thus, we EXCLUDE THE " +
                    "OUTERMOST JUNCTIONS ON THE TILE, and the junctions next to the scaffold " +
                    "seam as well as the seam itself.\" The one published measurement of this " +
                    "quantity excludes exactly the sites T-182 asks about."
        ),
        T182LiteratureRecord(
            "Is the accumulating-register mechanism this task derives the published one?",
            "Snodin et al. (2019)",
            "gpd/data/T-151-sources/PMC6379721-fullTextXML.xml",
            "READ DIRECTLY", true, 17.142857,
            "YES: \"Rothemund's original 2D origami tiles have been shown to be somewhat (right) " +
                    "twisted, because of a slight mismatch between the pitch of DNA (~10.5 bp) " +
                    "and the separation between the junctions (32 bp for three helical turns)\", " +
                    "and the cure is \"a suitable number of sections with 31 base pairs between " +
                    "equivalent junctions in order to remove this net twist\" — a ONE BASE PAIR " +
                    "correction per 32 bp domain, which is exactly the 17.14 deg per-domain " +
                    "register error C-0104's 32 bp rung carries (31 and 32 bp domains are -17.14 " +
                    "and +17.14 deg, so an even mix cancels)."
        ),
        T182LiteratureRecord(
            "Do the free junction's own preferred angles bound the out-of-plane prestrain?",
            "Snodin et al. (2019), Figure 1C and the oxDNA free-junction landscape",
            "gpd/data/T-151-sources/PMC6379721-fullTextXML.xml",
            "READ DIRECTLY", true, 4.5,
            "Partly. The free junction's minimum is (phi, theta) = (95.5, 2.5) deg and \"the " +
                    "minimum at theta = 4.5 deg for the subset 160 <= phi <= 180, the region " +
                    "likely to be relevant within origami\". theta is the arm-to-junction-plane " +
                    "bend, not the interduplex roll, so it is a SECOND and additive out-of-plane " +
                    "term of a few degrees, not a bound on the register term."
        ),
        T182LiteratureRecord(
            "Is the accumulation over a whole row observed experimentally?",
            "Ni, Fan, Zhou, Guo, Lee, Seeman, Kim & Yao, iScience 25:104373 (2022)",
            "gpd/data/T-182-sources/PMC9127610-fullTextXML.xml", "READ DIRECTLY", true, null,
            "YES, as a global twist: \"2D origami uses ~10.7 base pairs for every turn ... while " +
                    "natural B-DNA has around 10.5 ... The 0.2 base pair per turn difference " +
                    "leads to a dramatic change in geometry ... owing to the LARGE NUMBER OF BASE " +
                    "PAIRS included in the sample (~288 bps in one helix)\", and the same paper " +
                    "images \"the cross-tile edges, where there is maximum flexibility\"."
        ),
        T182LiteratureRecord(
            "Is the strain unrelieved at an edge, and does anyone know how it relieves?",
            "Rothemund, Nature 440:297 (2006), Supplementary Note S2",
            "gpd/data/T-151-sources/DNAorigami-supp1.linux.txt", "READ DIRECTLY", false, null,
            "\"How the strain is actually relieved is unknown\" — still the state of the art. " +
                    "The same note names the two causes as ANGLES, gives the glide symmetry that " +
                    "balances them in the bulk (\"cause them to be, on average, flat\"), states " +
                    "that the balance fails \"at seams and edges\", and prices the remedy at " +
                    "\"one or two scaffold bases\"."
        ),
        T182LiteratureRecord(
            "Is the edge domain length a design variable Rothemund tunes by hand?",
            "Rothemund (2006), Supplementary Note S1 step 4",
            "gpd/data/T-151-sources/DNAorigami-supp1.linux.txt", "READ DIRECTLY", true, 34.285714,
            "YES: \"Crossovers along the edges of the shape, in particular, must be adjusted to " +
                    "minimize strain ... By hand, helical domain lengths are changed by single " +
                    "bases until the strain energy is minimized\", and the worked example moves " +
                    "an edge crossover from 5 to 6 bases: \"The 6-base distance creates the " +
                    "least strain.\" The design variable is quantised at ONE BASE, 34.29 deg."
        )
    )

    // ------------------------------------------------------------- Deliverable 7: the oxDNA cost
    val nucleotides = 2 * DUPLEXES * (edgeX / Gen1Tile.RISE_PER_BASE_PAIR)
    val oxdnaCost = listOf(
        T182OxdnaCost(
            "system size",
            "%.0f nucleotides for the whole 38.08 nm x 15-duplex tile".format(nucleotides),
            "2 strands x 15 duplexes x ${(edgeX / Gen1Tile.RISE_PER_BASE_PAIR).toInt()} bp"
        ),
        T182OxdnaCost(
            "T-9's own cost model", "2-5 k nucleotides, microsecond-scale sampling on 8 cores, " +
                    "\"days not weeks — it fits this box\"",
            "TASKS.md T-9, the cost estimate the iteration that raised it recorded"
        ),
        T182OxdnaCost(
            "verdict against that model",
            "the whole tile is ${"%.0f".format(nucleotides)} nt, at the TOP of T-9's own range; " +
                    "the row-end crossover alone is a 2-duplex, 2 x 32 bp motif of ~256 nt, an " +
                    "order of magnitude inside it",
            "the measurement is per-junction (Snodin's Figure 5 protocol), so the minimal system " +
                    "is one row end plus its two neighbouring junctions, not the tile"
        ),
        T182OxdnaCost(
            "what it must resolve to be worth running",
            ("the row-end residual to better than +-3 deg, because the derived bracket is " +
                    "%.1f-%.1f deg against a %.2f deg threshold").format(
                        lowDegrees, highDegrees, C0104_THRESHOLD_DEGREES),
            "T-182's own bracket and C-0104's threshold"
        ),
        T182OxdnaCost(
            "the protocol is published",
            "Snodin et al. (2019) define and apply the exact measurement — the angle between " +
                    "inter-helix vectors near a junction — and EXCLUDE the outermost junctions",
            "gpd/data/T-151-sources/PMC6379721-fullTextXML.xml"
        ),
        T182OxdnaCost(
            "toolchain", "oxDNA needs g++/cmake, absent by default on this box and installable " +
                    "(CLAUDE.md); no GPU is required at this system size",
            "CLAUDE.md Environment"
        )
    )

    // ------------------------------------------------------------------ predicates and falsifiers
    val anyCeilingCloses = ceilings.any { it.closesTheQuestion && it.derivedHere }
    val adverseNominal = dishing.first { it.state.startsWith("derived boundary layer, NOMINAL, -") }
    val favourableNominal =
        dishing.first { it.state.startsWith("derived boundary layer, NOMINAL, +") }
    val gradedRecord = dishing.first { it.distribution.startsWith("graded") }
    val worstUniformLoad = dishing.maxOf { abs(it.uniformLoadDishingOverStroke) }

    val predicates = listOf(
        T182Predicate("P1",
            "every accessible ceiling is stated, derived here and compared against C-0104's " +
                    "15.4497275 deg, with the closes/does-not-close verdict said explicitly",
            ceilings.size >= 7),
        T182Predicate("P2",
            "the literature is searched on the modalities C-0104 did not use, with a read " +
                    "status per load-bearing source and every query string recorded",
            literature.all { it.readStatus.isNotBlank() }),
        T182Predicate("P3",
            "a value, a bound or a threshold is delivered, and the sign is decided as far as " +
                    "symmetry allows",
            boundaryLayer.isNotEmpty() && lowDegrees > 0.0),
        T182Predicate("P4",
            "C-0099's recommendation against an oxDNA edge-crossover run is upheld or reversed, " +
                    "and if reversed the run is costed",
            oxdnaCost.size >= 5)
    )

    val falsifiers = listOf(
        T182Falsifier("F1",
            "the twist-boundary-layer model fails its own limits", false,
            "NO — asserted as gate 2 in EdgeTwistReliefTest: k_theta -> 0 returns the free-duplex " +
                    "mismatch x L/2 and k_theta -> infinity returns zero, both to 1e-6"),
        T182Falsifier("F2",
            "THE DECLARED ONE — every ceiling lands below 15.4497275 deg, so the question closes " +
                    "on the cheap bound and C-0099's recommendation stands",
            !ceilings.all { it.closesTheQuestion },
            if (ceilings.all { it.closesTheQuestion })
                "FIRED: every ceiling closes the question"
            else ("FIRED IN THE OTHER DIRECTION: %d of %d ceilings lie ABOVE the threshold, " +
                    "and the one that is a value rather than a limit — the twist boundary layer " +
                    "at %.1f-%.1f deg — is among them")
                .format(ceilings.count { !it.closesTheQuestion }, ceilings.size,
                    lowDegrees, highDegrees)),
        T182Falsifier("F3",
            "the independent explicit-support path does not reproduce C-0090's 0.0621469105 and " +
                    "C-0104's slope 0.140379322 on C-0104's own definition of it",
            reproductions.take(3).any { it.departure > 1e-3 },
            "departures: " + reproductions.take(3).joinToString(", ") {
                "%s %.3e".format(it.owner, it.departure)
            }),
        T182Falsifier("F4",
            "a source QUANTIFIES the residual angle at a row-end crossover, in which case this " +
                    "is a value and not a bound",
            literature.any { it.quantified && it.question.contains("row-end") },
            "NO for the row end itself — Snodin et al. quantify the INTERIOR corrugation at " +
                    "about 8-9 deg average and +-16 deg per junction, and exclude the outermost " +
                    "junctions in as many words"),
        T182Falsifier("F5",
            "the sign composition does not select one of C-0104's three distributions", false,
            "NO — (-1)^b from the glide symmetry composes with the (-1)^b of the alternating " +
                    "raster ends to +1 at every interface, which is C-0104's UNIFORM " +
                    "distribution, its ADVERSE one; asserted as gate 3"),
        T182Falsifier("F6",
            "the standing one: a uniform load on a uniform foundation dishes more than 1e-6 of " +
                    "the free stroke, read on withoutPrestrain",
            worstUniformLoad > 1e-6,
            ("worst over all states %.3e, read on the SUPPORT-FREE prestrain-free " +
                    "lattice; read on the 34-anchor coupled lattice instead it reads 0.170 and " +
                    "fires, correctly, because a sparse coupling is a dishing source (C-0060) " +
                    "and that is not what this falsifier is about").format(worstUniformLoad))
    )

    val findings = listOf(
        ("THE QUESTION DOES NOT CLOSE ON THE CHEAP BOUND, AND THE DECLARED FALSIFIER FIRED THE " +
                "OTHER WAY. Of %d ceilings, %d lie ABOVE C-0104's %.4f deg threshold. Rupture " +
                "does not bind at all: the unzip allowable inverts to %.1f deg and the nicked " +
                "ceiling to %.1f deg, because a crossover's own hinge is soft (%.3f pN nm/rad) " +
                "and the couple at the threshold is only %.2f pN nm."
            ).format(ceilings.size, ceilings.count { !it.closesTheQuestion },
                C0104_THRESHOLD_DEGREES,
                ceilings.first { it.name.contains("10 pN") }.degrees,
                ceilings.first { it.name.contains("65 pN") }.degrees,
                hinge, hinge * C0104_THRESHOLD_DEGREES * PI / 180.0),
        ("C-0104'S LADDER IS THE WRONG LADDER, AND IT IS TOO SMALL. Its rungs are PER-DOMAIN " +
                "register offsets, but every domain's error carries the same sign, so it " +
                "ACCUMULATES along a duplex and what limits it is the duplex's own torsion. The " +
                "boundary layer u(L/2) = mismatch x lambda x tanh(L/2 lambda) has lambda = " +
                "%.2f nm against a %.2f nm half-row, and leaves %.2f-%.2f deg at a free row end " +
                "— against an un-relieved %.2f deg and C-0104's largest rung of 17.14 deg."
            ).format(nominal.decayLength, edgeX / 2.0, lowDegrees, highDegrees,
                nominal.rigidLimitDegrees),
        "THE SIGN COMPOSES TO THE ADVERSE DISTRIBUTION, AND THAT IS A LATTICE FACT. Rothemund's " +
                "glide symmetry flips the crossover type with the interface parity and a " +
                "boustrophedon's raster turns alternate ends, so the row-end crossover of " +
                "interface b sits where u = (-1)^b u_max and is flipped by (-1)^b: the two " +
                "cancel and every row-end crossover carries the SAME sign. That is C-0104's " +
                "UNIFORM distribution, the only one of its three that crosses 0.10.",
        ("READ AS C-0104 READS IT — THE 14 ROW-END SITES ALONE — THE PUBLISHED PLACEMENT LOSES " +
                "T-5b's 0.10 AT THE DERIVED VALUE IN BOTH SIGNS: %.7f at -%.2f deg and %.7f at " +
                "+%.2f deg, on C-0090's own 34 roots at 38.08 nm / phase 8. The verdict survives " +
                "only at the soft end of the bracket and only in the favourable sign (%.7f at " +
                "+%.2f deg)."
            ).format(adverseNominal.coupledDishingOverStroke, nominal.endResidualDegrees,
                favourableNominal.coupledDishingOverStroke, nominal.endResidualDegrees,
                dishing.first { it.state.startsWith("derived boundary layer, low end, +") }
                    .coupledDishingOverStroke, lowDegrees),
        ("AND C-0104'S THRESHOLD IS A SECANT, NOT THE TRIANGLE INEQUALITY IT IS NAMED AS. Its " +
                "0.140379322 is |D(1 rad) - D(0)|, reproduced here at %.9f; the peak of the " +
                "prestrain-ONLY field is %.9f, and the triangle ceiling it gives is %.4f deg " +
                "rather than 15.4497 deg. C-0104's number is still an upper bound on the " +
                "crossing — peak dishing is convex in theta_0, so a chord from zero lies above " +
                "it on [0, 1 rad] — but the bound it claims is tighter than the bound it quotes, " +
                "which moves the threshold DOWN and away from the derived value. CH-0122."
            ).format(unitSecant, unitSlope, trueTriangleCeiling),
        ("BUT THE BOUNDARY LAYER IS A FIELD, NOT A SET OF 14 NUMBERS, AND THE COMPLETE FIELD " +
                "KEEPS THE VERDICT. Every crossover at x carries (-1)^b u(x), not only the 14 at " +
                "the row ends; over all %d sites that reads %.7f (flat=%s), because the interior " +
                "sites partly cancel the row-end ones. So the answer at the derived value is a " +
                "verdict that survives with %.1f %% of the convention unused where zero prestrain " +
                "leaves %.1f %% — and the difference between the two readings is the 42 interior " +
                "crossovers, which nobody has measured either."
            ).format(crossoverCount, gradedRecord.coupledDishingOverStroke,
                gradedRecord.flatAtTenPercent,
                100.0 * (FLATNESS_TOLERANCE - gradedRecord.coupledDishingOverStroke) /
                        FLATNESS_TOLERANCE,
                100.0 * (FLATNESS_TOLERANCE - zero.coupledDishingOverStroke) /
                        FLATNESS_TOLERANCE),
        "THE ONE PUBLISHED MEASUREMENT OF THIS QUANTITY EXCLUDES EXACTLY THESE SITES. Snodin et " +
                "al. (2019) define the interduplex corrugation angle, measure it over a 2D " +
                "origami by oxDNA, report about 8-9 deg in the average and +-16 deg per " +
                "junction — and write that they \"exclude the outermost junctions on the tile\". " +
                "The protocol exists, the machinery exists, and the number does not.",
        ("SO C-0099'S RECOMMENDATION IS REVERSED. It refused an oxDNA edge crossover because it " +
                "would refine a STIFFNESS worth 2.85 %% of an interval the verdict does not " +
                "cross; the prestrain is a LOAD, the verdict crosses at %.4f deg, and every " +
                "route to a value lands in 8-%.0f deg. The run is warranted, it is one row end " +
                "and its two neighbours rather than a tile, and it fits this box."
            ).format(C0104_THRESHOLD_DEGREES, nominal.rigidLimitDegrees)
    )

    val result = T182Result(
        task = "T-182",
        leaf = "A1.2, with A8.2",
        question = "What prestrain does a row-end crossover actually carry, against C-0104's " +
                "15.4497275 degree threshold?",
        conditions = mapOf(
            "temperature" to "300 K",
            "kBT" to "${thermalEnergy()} pN nm",
            "buffer" to "aqueous 2 mM MgCl2",
            "sheet" to "single-layer square-lattice Rothemund, $DUPLEXES duplexes at " +
                    "${Gen1Tile.INTERHELICAL_SHEET} nm, 0.34 nm rise, 32/3 bp per turn",
            "width" to "${edgeX.roundedForProse()} nm at crossover phase $PHASE (C-0086, C-0090)",
            "placement" to "C-0090's published 34-root key $publishedKey",
            "load" to "C-0022's solved collar at 2 mM, a 10 nm gap and 0.192 V",
            "coupling" to "C-0017's ${MANDATE.roundedForProse()} pN/nm shared equally over " +
                    "$ARM_COUNT roots, " +
                    "as EXPLICIT elastic supports",
            "freeStroke" to "${host.freeStroke.roundedForProse()} nm"
        ),
        ceilings = ceilings,
        boundaryLayer = boundaryLayer,
        dishing = dishing,
        literature = literature,
        convergence = convergence,
        reproductions = reproductions,
        oxdnaCost = oxdnaCost,
        predicates = predicates,
        falsifiers = falsifiers,
        findings = findings,
        parameters = mapOf(
            "designTwistPerBase" to DESIGN_TWIST.toString(),
            "naturalTwistPerBase" to NATURAL_TWIST.roundedForProse().toString(),
            "twistRateMismatchPerNm" to mismatch.roundedForProse().toString(),
            "crossoverHingeStiffness" to hinge.roundedForProse().toString(),
            "phosphateRadius" to BForm.PHOSPHATE_RADIUS.toString(),
            "derivedCrossoverSpacing" to derivedSpacing.roundedForProse().toString(),
            "crossoverCount" to crossoverCount.toString(),
            "rowEndCrossovers" to host.rowEndSites.size.toString(),
            "c0104Threshold" to c0104Threshold.toString(),
            "c0104UnitSlope" to c0104Slope.toString(),
            "unitPrestrainPeakHere" to unitSlope.roundedForProse().toString(),
            "c0104SecantReproducedHere" to unitSecant.roundedForProse().toString(),
            "trueTriangleCeilingDegrees" to trueTriangleCeiling.roundedForProse().toString(),
            "flatnessTolerance" to FLATNESS_TOLERANCE.toString(),
            "sources" to "gpd/results/T-3b-tile-edge-load-profile.json, " +
                    "gpd/results/T-153-buildable-raster-width.json, " +
                    "gpd/results/T-172-row-end-prestrain.json"
        )
    )

    val json = Json { prettyPrint = true }
    val out = File("gpd/results/T-182-row-end-prestrain-value.json")
    out.writeText(json.encodeToString(json.encodeToJsonElement(result).roundedForResult()))
    println("T-182 — wrote ${out.path}")
    findings.forEach { println("  * $it") }
}
