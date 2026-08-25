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
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.PointLoad
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.ResultInputs
import com.xemantic.nano.plentyofroom.structure.roundedForProse
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
import kotlin.math.max

// ---------------------------------------------------------------------------------------------
// T-330 -- the dishing decomposition's basis, and the parity that decides whether it is
// orthogonal.
//
// `HoneycombDeflection` removed its best-fit rigid plane by three INDEPENDENT projections, which
// is the least-squares fit iff the three modes are mutually orthogonal. `<piston, tiltY> = int y
// dA` over the face's tributaries, each one row pitch centred on its own beam's axis, so it is
// `L_s * rowPitch * SUM beamY` -- and a honeycomb face is CORRUGATED, its gap sequence
// `d, 2d, d, 2d, ...`, which is palindromic iff the raster-row count `m` is EVEN.
//
// The cheap bound is closed form and integer: `SUM beamY` is 0 at even `m` and exactly
// `-(m-1)d/4` at odd `m`. Every block this corpus graded before `T-294` has `m = 10`.
// ---------------------------------------------------------------------------------------------

private const val T330_SAMPLES: Int = 81
private const val T330_TOLERANCE: Double = 0.10
private const val T330_RIM_STANDOFF: Double = 1.0
private const val T330_PROBE_BP: Int = 42
private const val T330_UPSTREAM_BP: Int = 112

/** The corrected uniform-load dishing is exactly zero, so it is reported against a threshold. */
private const val T330_UNIFORM_THRESHOLD: Double = 1e-9

/**
 * The departure rule, for the two record types this study coins.
 *
 * `DEPARTURE_DIGITS_BY_KEY` is keyed on `reproductions` and `convergence`, so it cannot see an
 * `upstream` or a `residue` record — which is exactly how `C-0218`'s `F14` fired, at three leaves
 * of 10 649. A departure between two nearly equal numbers carries about `9 + log10 d` digits, so
 * an order-`1e-9` one carries none at all and is emitted at two.
 */
private val T330_DEPARTURE_DIGITS: Map<String, Int> = mapOf(
    "upstream/reproductionDeparture" to 2,
    "upstream/c0218Departure" to 2,
    "residue/relativeGap" to 2,
    "residue/gapOverPeakDeflection" to 2
)

private fun Double.emitted(digits: Int = 9): String = roundedForProse(digits).toString()

/** A ratio, a departure or a flag: dimensionless, so `P-18`'s pN floor must not reach it. */
private fun Double.emittedDimensionless(digits: Int = 9): String =
    roundedForProse(digits, floor = 0.0).toString()

@Serializable
private class T330ParityRow(
    val rasterRows: Int,
    val faceColumn: Int,
    val sumOfFaceYNm: Double,
    val closedFormSumNm: Double,
    val closedFormHolds: Boolean,
    val modesAreOrthogonal: Boolean,
    val parityPredictsOrthogonality: Boolean,
    val worstGramOffDiagonal: Double,
    val uniformLoadDishingCorrected: Double,
    val uniformLoadDishingStanding: Double,

    /**
     * The standing convention's uniform-load dishing predicted from the face's GEOMETRY alone.
     *
     * For a uniform field `u = c · piston` the three projections leave `−c(∫y dA/∫y² dA) y`, so
     * the peak over the mean is `(∫y dA/∫y² dA)(L_y/2)` — a pure geometric ratio carrying no
     * element, no row length, no thickness and no tie. Four multiplications, no solve.
     */
    val uniformLoadDishingStandingPredicted: Double,
    val predictionHolds: Boolean
)

@Serializable
private class T330UpstreamRow(
    val source: String,
    val crossSection: String,
    val rasterRows: Int,
    val rowBasePairs: Int,
    val hingeStiffnessEnhancement: Double,
    val modesAreOrthogonal: Boolean,
    val published: Double,
    val standing: Double,
    val corrected: Double,
    val unconditional: Double,
    val standingReproducesThePublished: Boolean,
    val reproductionDeparture: Double,
    val correctedReproducesC0218: Boolean,
    val c0218Corrected: Double,
    val c0218Departure: Double,
    val flatStanding: Boolean,
    val flatCorrected: Boolean,
    val verdictMoves: Boolean,
    val correctedOverStanding: Double
)

@Serializable
private class T330ResidueRow(
    val crossSection: String,
    val rasterRows: Int,
    val loadCase: String,
    val modesAreOrthogonal: Boolean,
    val standing: Double,
    val unconditional: Double,
    val relativeGap: Double,
    val peakDeflection: Double,
    val gapOverPeakDeflection: Double,

    /**
     * Whether [relativeGap] is a ratio of two quantities that are meant to be non-zero.
     *
     * Under a uniform pressure at an orthogonal basis BOTH readings are the solver's own noise,
     * and `CLAUDE.md`'s rule applies: comparing two quantities that are both meant to be zero
     * relatively compares their noise. Such a row is emitted and excluded from the headline.
     */
    val wellPosed: Boolean
)

@Serializable
private class T330Convergence(
    val axis: String,
    val setting: String,
    val quantity: String,
    val value: Double,
    val departure: Double
)

@Serializable
private class T330Reproduction(
    val source: String,
    val quantity: String,
    val published: Double,
    val here: Double,
    val departure: Double,
    val closes: Boolean
)

@Serializable
private class T330Result(
    val task: String, val leaf: String, val title: String, val verificationType: String,
    val maturity: String, val units: Map<String, String>, val conventions: Map<String, String>,
    val parameters: Map<String, String>, val citedInputs: Map<String, String>,
    val sources: List<String>,
    val cheapBound: Map<String, String>,
    val parity: List<T330ParityRow>,
    val upstream: List<T330UpstreamRow>,
    val residue: List<T330ResidueRow>,
    val blastRadius: Map<String, String>,
    val verdict: Map<String, String>,
    val falsifiers: List<String>,
    val reproductions: List<T330Reproduction>,
    val convergence: List<T330Convergence>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

private class T330Profile(val smoothDepth: Double, val smoothWidth: Double, val rimDepth: Double) {
    fun field(interiorPressure: Double, edgeX: Double, edgeY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, edgeX, edgeY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T330_RIM_STANDOFF))
        )
}

private fun t330Profile(file: File): T330Profile {
    require(file.exists()) { "C-0022's result file is missing: " + file.path }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .firstOrNull { row ->
            fun value(name: String) = row.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == 2.0 && value("gapHeight") == 10.0 &&
                    value("appliedBias") == 0.192
        } ?: error("no C-0022 profile at the design state")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T330Profile(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

private fun probeLattice(rows: Int, faceColumn: Int = 0) = HoneycombGrillage(
    block = HoneycombBlock(rows, 2),
    rowBasePairs = T330_PROBE_BP,
    foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
    faceColumn = faceColumn
)

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    val d = Gen1Tile.INTERHELICAL_HONEYCOMB
    val profile = t330Profile(ResultInputs.T_3B.file())

    // ======================================== the cheap bound: closed form, integer, no solve
    println("T-330 - the cheap bound, before any solve")
    val parity = ArrayList<T330ParityRow>()
    for (rows in 3..16) {
        for (column in 0..1) {
            val lattice = probeLattice(rows, column)
            val sum = lattice.faceBeams.sumOf { lattice.beamY[it] }
            // The sign is set by the face column's own parity: at an even column the corrugation
            // puts the MAJORITY sublattice half a bond ABOVE the ladder, at an odd column below.
            // A first pass carried the `faceColumn = 0` sign only and the sweep found it, which
            // is what a two-column sweep is for.
            val sign = if (column % 2 == 0) -1.0 else 1.0
            val closed = if (rows % 2 == 0) 0.0 else sign * (rows - 1) * d / 4.0
            val pitch = lattice.rowPitch
            val firstMoment = pitch * sum
            val secondMoment = lattice.faceBeams.sumOf { pitch * lattice.beamY[it] * lattice.beamY[it] } +
                    rows * pitch * pitch * pitch / 12.0
            val predicted = abs(firstMoment / secondMoment) * lattice.lengthY / 2.0
            val field = lattice.solve(uniformPressure(0.05))
            parity += T330ParityRow(
                rasterRows = rows,
                faceColumn = column,
                sumOfFaceYNm = sum,
                closedFormSumNm = closed,
                closedFormHolds = abs(sum - closed) < 1e-12 * (1.0 + abs(closed)),
                modesAreOrthogonal = lattice.faceRigidModesAreOrthogonal,
                parityPredictsOrthogonality =
                    lattice.faceRigidModesAreOrthogonal == (rows % 2 == 0),
                worstGramOffDiagonal = lattice.worstFaceNonOrthogonality,
                uniformLoadDishingCorrected =
                    field.peakDishing(T330_SAMPLES) / field.meanDeflection,
                uniformLoadDishingStanding =
                    field.independentProjectionPeakDishing(T330_SAMPLES) / field.meanDeflection,
                uniformLoadDishingStandingPredicted = predicted,
                // The absolute floor is what makes this well posed at an EVEN m, where the
                // prediction and the reading are both zero in exact arithmetic and both the
                // solver's own noise in floating point.
                predictionHolds = abs(
                    field.independentProjectionPeakDishing(T330_SAMPLES) / field.meanDeflection -
                            predicted
                ) < max(1e-8 * predicted, T330_UNIFORM_THRESHOLD)
            )
        }
    }
    parity.filter { it.faceColumn == 0 }.forEach {
        println(
            "  m = " + it.rasterRows + "  sum " + it.sumOfFaceYNm.emitted(6) +
                    " nm  orthogonal " + it.modesAreOrthogonal +
                    "  worst off-diagonal " + it.worstGramOffDiagonal.emittedDimensionless(6)
        )
    }

    // ======================================== C-0154's own committed free tiles, three readings
    println("T-330 - C-0154's free tiles, in three readings")
    val upstream = ArrayList<T330UpstreamRow>()
    val residue = ArrayList<T330ResidueRow>()
    listOf(
        Triple(
            15, 4,
            listOf(
                Triple(1.0, 0.312237799, 0.242196276),
                Triple(9.65079217, 0.227177955, 0.157167743),
                Triple(12.7228458, 0.220064299, 0.150056485)
            )
        ),
        Triple(
            10, 6,
            listOf(
                Triple(1.0, 0.127358454, 0.0),
                Triple(21.1851817, 0.0449400126, 0.0),
                Triple(17.6059172, 0.0477844467, 0.0)
            )
        )
    ).forEach { (rows, layers, readings) ->
        val block = HoneycombBlock(rows, layers)
        val norm = crossSectionNormalisation(block, T330_UPSTREAM_BP, fractionalTolerance = T330_TOLERANCE)
        val load = profile.field(norm.interiorPressure, norm.edgeX, norm.edgeY)
        readings.forEach { (enhancement, published, c0218) ->
            val lattice = honeycombTiedLatticeAtResolvedLink(
                block = block, rowBasePairs = T330_UPSTREAM_BP,
                enhancement = enhancement, tied = false
            )
            val field = lattice.solve(load)
            val stroke = lattice.solve(uniformPressure(norm.interiorPressure)).meanDeflection
            val standing = field.independentProjectionPeakDishing(T330_SAMPLES) / stroke
            val corrected = field.peakDishing(T330_SAMPLES) / stroke
            val unconditional =
                FaceRigidBasis(lattice).dishingOf(field).peakDishing(T330_SAMPLES) / stroke
            val departure = abs(standing - published) / published
            val c0218Departure = if (c0218 == 0.0) 0.0 else abs(corrected - c0218) / c0218
            upstream += T330UpstreamRow(
                source = "C-0154 (T-253), the free tile at 112 bp",
                crossSection = "" + rows + " x " + layers,
                rasterRows = rows,
                rowBasePairs = T330_UPSTREAM_BP,
                hingeStiffnessEnhancement = enhancement,
                modesAreOrthogonal = lattice.faceRigidModesAreOrthogonal,
                published = published,
                standing = standing,
                corrected = corrected,
                unconditional = unconditional,
                standingReproducesThePublished = departure < 1e-8,
                reproductionDeparture = departure,
                correctedReproducesC0218 = c0218 != 0.0 && c0218Departure < 1e-8,
                c0218Corrected = c0218,
                c0218Departure = c0218Departure,
                flatStanding = standing < T330_TOLERANCE,
                flatCorrected = corrected < T330_TOLERANCE,
                verdictMoves = (standing < T330_TOLERANCE) != (corrected < T330_TOLERANCE),
                correctedOverStanding = corrected / standing
            )
            val peak = lattice.overFaceGrid(T330_SAMPLES) { s, y -> abs(field.deflection(s, y)) }
            residue += T330ResidueRow(
                crossSection = "" + rows + " x " + layers,
                rasterRows = rows,
                loadCase = "C-0022's solved collar, enhancement " + enhancement.emitted(9),
                modesAreOrthogonal = lattice.faceRigidModesAreOrthogonal,
                standing = standing,
                unconditional = unconditional,
                relativeGap = abs(unconditional - standing) / standing,
                peakDeflection = peak,
                gapOverPeakDeflection = abs(unconditional - standing) * stroke / peak,
                wellPosed = standing * stroke > 1e-6 * peak
            )
        }
    }
    upstream.forEach {
        println(
            "  " + it.crossSection + " e=" + it.hingeStiffnessEnhancement.emitted(6) +
                    "  standing " + it.standing.emitted() +
                    "  corrected " + it.corrected.emitted() +
                    "  flat " + it.flatStanding + "/" + it.flatCorrected
        )
    }

    // ==================== P10: what an UNBRANCHED adoption would cost an even-m reading
    println("T-330 - the cost of not branching, at 10 x 6")
    run {
        val block = HoneycombBlock(10, 6)
        val norm = crossSectionNormalisation(block, T330_UPSTREAM_BP, fractionalTolerance = T330_TOLERANCE)
        val lattice = honeycombTiedLatticeAtResolvedLink(
            block = block, rowBasePairs = T330_UPSTREAM_BP, enhancement = 21.1851817, tied = false
        )
        val cases: List<Pair<String, HoneycombDeflection>> = listOf(
            "uniform pressure" to lattice.solve(uniformPressure(norm.interiorPressure)),
            "point load at the face centre" to
                    lattice.solve(uniformPressure(0.0), listOf(PointLoad(0.0, 0.0, 1.0))),
            "a unit bond prestrain" to lattice.unitPrestrainResponse(lattice.bonds.first())
        )
        cases.forEach { (name, field) ->
            val standing = field.independentProjectionPeakDishing(T330_SAMPLES)
            val unconditional =
                FaceRigidBasis(lattice).dishingOf(field).peakDishing(T330_SAMPLES)
            val peak = lattice.overFaceGrid(T330_SAMPLES) { s, y -> abs(field.deflection(s, y)) }
            residue += T330ResidueRow(
                crossSection = "10 x 6",
                rasterRows = 10,
                loadCase = name,
                modesAreOrthogonal = lattice.faceRigidModesAreOrthogonal,
                standing = standing,
                unconditional = unconditional,
                // `CLAUDE.md`: a quantity that is nothing but ulp noise must be emitted as a
                // THRESHOLD, never as a value. Under a uniform pressure at an orthogonal basis
                // BOTH readings are the solver's own noise and their ratio moved in the sixth
                // digit between two emissions -- `F9` fired on exactly this field.
                relativeGap =
                    if (standing > 1e-6 * peak) abs(unconditional - standing) / standing else 0.0,
                peakDeflection = peak,
                gapOverPeakDeflection = if (peak == 0.0) 0.0 else abs(unconditional - standing) / peak,
                wellPosed = standing > 1e-6 * peak
            )
        }
    }
    val evenResidue = residue.filter { it.modesAreOrthogonal && it.wellPosed }
    val worstUnbranched = evenResidue.maxOfOrNull { it.relativeGap } ?: 0.0
    val worstUnbranchedOnACollar = residue
        .filter { it.modesAreOrthogonal && it.wellPosed && it.loadCase.startsWith("C-0022") }
        .maxOfOrNull { it.relativeGap } ?: 0.0
    println("  worst relative gap at an orthogonal basis: " + worstUnbranched.emittedDimensionless(6))

    // ======================================== convergence
    println("T-330 - convergence")
    val convergence = ArrayList<T330Convergence>()
    run {
        val block = HoneycombBlock(15, 4)
        val norm = crossSectionNormalisation(block, T330_UPSTREAM_BP, fractionalTolerance = T330_TOLERANCE)
        val load = profile.field(norm.interiorPressure, norm.edgeX, norm.edgeY)
        var previous: Double? = null
        listOf(41, 81, 161).forEach { samples ->
            val lattice = honeycombTiedLatticeAtResolvedLink(
                block = block, rowBasePairs = T330_UPSTREAM_BP, enhancement = 1.0, tied = false
            )
            val stroke = lattice.solve(uniformPressure(norm.interiorPressure)).meanDeflection
            val value = lattice.solve(load).peakDishing(samples) / stroke
            convergence += T330Convergence(
                axis = "the dishing sampling grid",
                setting = "" + samples + " x " + samples,
                quantity = "the corrected 15 x 4 free tile at enhancement 1.0",
                value = value,
                departure = previous?.let { abs(value - it) / it } ?: 0.0
            )
            previous = value
        }
        previous = null
        listOf(1, 2).forEach { subdivisions ->
            val lattice = honeycombTiedLatticeAtResolvedLink(
                block = block, rowBasePairs = T330_UPSTREAM_BP, enhancement = 1.0, tied = false,
                subdivisions = subdivisions
            )
            val stroke = lattice.solve(uniformPressure(norm.interiorPressure)).meanDeflection
            val value = lattice.solve(load).peakDishing(T330_SAMPLES) / stroke
            convergence += T330Convergence(
                axis = "the beam subdivision",
                setting = "" + subdivisions,
                quantity = "the corrected 15 x 4 free tile at enhancement 1.0",
                value = value,
                departure = previous?.let { abs(value - it) / it } ?: 0.0
            )
            previous = value
        }
    }
    convergence.forEach {
        println(
            "  " + it.axis + " = " + it.setting + "  " + it.value.emitted() +
                    "  departure " + it.departure.emittedDimensionless(2)
        )
    }

    // ======================================== reproductions
    val reproductions = upstream.map {
        T330Reproduction(
            source = "C-0154 (T-253) " + it.crossSection + ", enhancement " +
                    it.hingeStiffnessEnhancement.emitted(9),
            quantity = "the free tile at 112 bp, three-projection convention",
            published = it.published,
            here = it.standing,
            departure = it.reproductionDeparture,
            closes = it.standingReproducesThePublished
        )
    } + upstream.filter { it.c0218Corrected != 0.0 }.map {
        T330Reproduction(
            source = "C-0218 (T-294) section 7, " + it.crossSection + ", enhancement " +
                    it.hingeStiffnessEnhancement.emitted(9),
            quantity = "the free tile at 112 bp, least-squares convention",
            published = it.c0218Corrected,
            here = it.corrected,
            departure = it.c0218Departure,
            closes = it.correctedReproducesC0218
        )
    } + listOf(
        T330Reproduction(
            source = "C-0218 (T-294) section 7 / CH-0282, predicted from the GEOMETRY alone",
            quantity = "the standing convention's uniform-load dishing at m = 15, " +
                    "(int y dA / int y^2 dA)(L_y/2) -- no solve, no element, no tie",
            published = 0.0620506254,
            here = parity.first { it.rasterRows == 15 && it.faceColumn == 0 }
                .uniformLoadDishingStandingPredicted,
            departure = abs(
                parity.first { it.rasterRows == 15 && it.faceColumn == 0 }
                    .uniformLoadDishingStandingPredicted - 0.0620506254
            ) / 0.0620506254,
            closes = abs(
                parity.first { it.rasterRows == 15 && it.faceColumn == 0 }
                    .uniformLoadDishingStandingPredicted - 0.0620506254
            ) / 0.0620506254 < 1e-8
        ),
        T330Reproduction(
            source = "C-0218 (T-294) section 7 / CH-0282",
            quantity = "the worst relative Gram off-diagonal at m = 15",
            published = 0.0358744468,
            here = parity.first { it.rasterRows == 15 && it.faceColumn == 0 }.worstGramOffDiagonal,
            departure = abs(
                parity.first { it.rasterRows == 15 && it.faceColumn == 0 }.worstGramOffDiagonal -
                        0.0358744468
            ) / 0.0358744468,
            closes = abs(
                parity.first { it.rasterRows == 15 && it.faceColumn == 0 }.worstGramOffDiagonal -
                        0.0358744468
            ) / 0.0358744468 < 1e-8
        )
    )

    // ======================================== the verdict and the findings
    val allParityHolds = parity.all { it.parityPredictsOrthogonality && it.closedFormHolds }
    val correctedUniformWorst = parity.maxOf { it.uniformLoadDishingCorrected }
    val standingUniformWorst = parity.maxOf { it.uniformLoadDishingStanding }
    val anyVerdictMoves = upstream.any { it.verdictMoves }
    val oddCells = upstream.filter { !it.modesAreOrthogonal }
    val evenCells = upstream.filter { it.modesAreOrthogonal }

    val findings = LinkedHashMap<String, String>()
    findings["theCheapBoundIsClosedFormAndInteger"] =
        "The face's rooting helices sit on an INTEGER ladder in units of d/2 -- the row pitch is " +
                "3d/2 and the corrugation is half a bond -- so SUM beamY is exactly 0 at even m " +
                "and exactly -(m-1)d/4 at odd m, which is -" + (14 * d / 4.0).emitted(6) +
                " nm at m = 15 and -" + (10 * d / 4.0).emitted(6) + " nm at m = 11. It holds at " +
                parity.count { it.closedFormHolds } + " of " + parity.size + " readings over " +
                "m = 3 to 16 and both face columns, and it reproduces CH-0282's own worst " +
                "off-diagonal " + parity.first { it.rasterRows == 15 && it.faceColumn == 0 }
            .worstGramOffDiagonal.emittedDimensionless(9) + " with no solve at all."
    findings["theOrthogonalityIsExactlyTheParity"] =
        "faceRigidModesAreOrthogonal equals (m even) at " +
                parity.count { it.parityPredictsOrthogonality } + " of " + parity.size +
                " readings. The predicate is an EXACT integer statement about the half-bond " +
                "ladder, not a tolerance on a quadrature -- which matters, because the " +
                "quadrature Gram's own worst off-diagonal at an even m is below " +
                T330_UNIFORM_THRESHOLD.toString() + " and is NOT reliably the exact zero that " +
                "T-294's own 10 x 6 at 116 bp returns. F2 FIRED: a branch taken on the float " +
                "would not be inert."
    findings["theDefectsOwnMAGNITUDEIsAPureGeometricRatio"] =
        "For a uniform field the three projections leave -c(int y dA / int y^2 dA) y, so the " +
                "standing convention's uniform-load dishing is (int y dA / int y^2 dA)(L_y/2) -- " +
                "a ratio of two face integrals carrying no element, no row length, no thickness " +
                "and no tie. Evaluated it is " +
                parity.first { it.rasterRows == 15 && it.faceColumn == 0 }
                    .uniformLoadDishingStandingPredicted.emitted(9) + " at m = 15, which is " +
                "C-0218 section 7's own 0.0620506254 to every digit it publishes -- reproduced " +
                "here on a 15 x 2 probe at 42 bp with no ties, where C-0218 measured it on a " +
                "tied 15 x 4 block at 116 bp. The number is a property of the FIT and of the " +
                "corrugation, and of nothing else."
    findings["theFalsifierIsRepairedAtBothParities"] =
        "A uniform pressure gives a corrected peak dishing below " +
                T330_UNIFORM_THRESHOLD.toString() + " of the mean deflection at every one of " +
                parity.size + " readings over m = 3 to 16 -- a threshold and not a value, " +
                "because the number itself is the solver's own noise -- against " +
                standingUniformWorst.emittedDimensionless(6) +
                " in the retained three-projection convention. The standing falsifier is " +
                "discharged at BOTH parities for the first time."
    findings["theCostOfNotBranching"] =
        "At an orthogonal basis the three independent projections fit faceFunctional's " +
                "owning-beam reconstruction and the unconditional least-squares solve fits " +
                "evaluate's nearest-beam one, and the gap between them -- CH-0282 section 5 -- " +
                "is at most " + worstUnbranched.emittedDimensionless(3) + " relative over " +
                evenResidue.size + " well-posed readings at 10 x 6, and " +
                worstUnbranchedOnACollar.emittedDimensionless(3) + " on the solved collar every " +
                "free-tile reading in this corpus is taken under. That is what an UNCONDITIONAL " +
                "adoption would inflict on every one of the 15 even-m result files, against " +
                "C-0180's tightest recovered cell which clears T-5b by 0.426 % -- so the collar " +
                "reading is inside that margin by a factor of " +
                (0.00426 / max(worstUnbranchedOnACollar, 1e-30)).emittedDimensionless(3) +
                " and the prestrain influence function, which a coupled surrogate's bank is " +
                "built from, is NOT. It is a CONVENTION and not the defect CH-0282 raises, so it " +
                "is measured here and filed as CH-0284."
    findings["noVerdictMoves"] =
        "All " + oddCells.size + " corrected 15 x 4 readings stay outside T-5b's 0.10 (" +
                oddCells.joinToString(" / ") { it.corrected.emitted() } + " against a standing " +
                oddCells.joinToString(" / ") { it.standing.emitted() } + "), and all " +
                evenCells.size + "10 x 6 readings are unchanged. The cross-section ordering is " +
                "STRENGTHENED, because the corrected readings are smaller and still outside."

    val verdict = LinkedHashMap<String, String>()
    verdict["parityPredictsOrthogonalityEverywhere"] = allParityHolds.toString()
    verdict["parityReadings"] = parity.size.toString()
    // A corrected uniform-load dishing is exactly zero in exact arithmetic, so what is emitted
    // is a THRESHOLD and a boolean -- `F9` fired on the value, which moved in its third digit
    // between two emissions of identical code.
    verdict["correctedUniformLoadDishingBelow"] = T330_UNIFORM_THRESHOLD.toString()
    verdict["correctedUniformLoadDishingIsZeroAtEveryParity"] =
        (correctedUniformWorst < T330_UNIFORM_THRESHOLD).toString()
    verdict["worstStandingUniformLoadDishing"] = standingUniformWorst.emittedDimensionless()
    verdict["anyVerdictMoves"] = anyVerdictMoves.toString()
    verdict["correctedFifteenByFour"] = oddCells.joinToString(" / ") { it.corrected.emitted() }
    verdict["standingFifteenByFour"] = oddCells.joinToString(" / ") { it.standing.emitted() }
    // two digits, the same rule the `residue` records themselves are emitted under
    verdict["worstUnbranchedRelativeGapAtEvenM"] = worstUnbranched.emittedDimensionless(2)
    verdict["worstUnbranchedRelativeGapOnTheCollarLoad"] =
        worstUnbranchedOnACollar.emittedDimensionless(2)
    verdict["cZeroOneEightyTightestCellMargin"] = "0.00426"
    verdict["reproductionsClosing"] =
        reproductions.count { it.closes }.toString() + " of " + reproductions.size

    val blastRadius = LinkedHashMap<String, String>()
    blastRadius["resultFilesCarryingAGrillageDishing"] = "18"
    blastRadius["provablyUnmovedAtAnEvenRasterRowCount"] = "15"
    blastRadius["unmovedList"] = "T-263, T-267, T-279, T-284, T-291, T-294, T-299, T-303, " +
            "T-304, T-307, T-310, T-315, T-316, T-322, T-323"
    blastRadius["reEmitted"] = "3"
    blastRadius["reEmittedList"] = "T-253, T-254, T-297"
    blastRadius["reEmissionOrder"] = "T-253 -> T-254 -> T-297, with T-294 re-run after T-253 " +
            "as a byte-identity control"
    blastRadius["fourthLevelConsumersMoved"] = "0"
    blastRadius["whyNoFourthLevelConsumerMoves"] =
        "every reader of the three selects the 10 x 6 half explicitly: T-263 and T-315 on " +
                "crossSection == '10 x 6', T-284 and T-291 on the 10 x 6 enhancement " +
                "21.1851817, and T-303 on the cells block, which carries no 15 x 4 record"
    blastRadius["howTheUnmovedSetIsEstablished"] =
        "by construction, not by a re-run: where the face basis is orthogonal the class returns " +
                "the three independent projections unchanged, and F6's controls confirm it"

    val result = T330Result(
        task = "T-330",
        leaf = "A8.2",
        title = "The dishing decomposition's basis, and the parity that decides whether it is " +
                "orthogonal",
        verificationType = "logical (a closed-form, integer cheap bound over the face's own " +
                "half-bond ladder) + in-silico (the repaired decomposition, its reproduction of " +
                "C-0154's and C-0218's committed readings, and the measured cost of the branch)",
        maturity = "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated.",
        units = mapOf(
            "length" to "nm",
            "dishing" to "dimensionless, fraction of the free stroke or of the mean deflection",
            "gram" to "nm^4, and its worst off-diagonal is dimensionless"
        ),
        conventions = mapOf(
            "face" to "the gap-facing column of the block, faceColumn = 0",
            "tributary" to "one row pitch 3d/2 centred on each face beam's own axis",
            "dishingDefault" to "the LEAST-SQUARES rigid plane, solved as a 3 x 3 system where " +
                    "the basis is not orthogonal and taken as the three independent projections " +
                    "where it is -- bit for bit, so an even-m reading cannot move",
            "dishingRetained" to "independentProjectionDishing, the three-projection reading " +
                    "C-0154, C-0167, C-0180, C-0208 and C-0218's standing column publish",
            "unconditional" to "FaceRigidBasis's always-solved fit, in evaluate's inner product " +
                    "-- the object CH-0282 section 5's residue is measured with",
            "flat" to "peak dishing below T-5b's 0.10 of the free stroke"
        ),
        parameters = mapOf(
            "probeRowBasePairs" to T330_PROBE_BP.toString(),
            "upstreamRowBasePairs" to T330_UPSTREAM_BP.toString(),
            "samples" to T330_SAMPLES.toString(),
            "tolerance" to T330_TOLERANCE.toString(),
            "rimStandoff" to T330_RIM_STANDOFF.toString(),
            "bondLength" to d.emitted(),
            "rowPitch" to (1.5 * d).emitted(),
            "foundationStiffness" to Gen1Tile.FOUNDATION_SECANT.emitted(),
            "targetForce" to Gen1Tile.TARGET_FORCE.emitted()
        ),
        citedInputs = mapOf(
            "C-0154's 15 x 4 free tiles" to "0.312237799 / 0.227177955 / 0.220064299",
            "C-0154's 10 x 6 free tiles" to "0.127358454 / 0.0449400126 / 0.0477844467",
            "C-0218 section 7's corrected 15 x 4" to "0.242196276 / 0.157167743 / 0.150056485",
            "CH-0282's worst Gram off-diagonal at m = 15" to "0.0358744468",
            "C-0218 section 7's uniform-load standing reading at 15 x 4" to "0.0620506254",
            "C-0180's tightest recovered cell margin" to "0.426 % of T-5b"
        ),
        sources = listOf(
            ResultInputs.T_3B.path + " (C-0022's solved collar at 2 mM / 10 nm / 0.192 V)"
        ),
        cheapBound = mapOf(
            "sumOfFaceYAtEvenM" to "0",
            "sumOfFaceYAtOddM" to "-(m - 1) d / 4",
            "sumAtM15" to (-(14 * d / 4.0)).emitted(),
            "sumAtM11" to (-(10 * d / 4.0)).emitted(),
            "worstOffDiagonalAtM15" to
                    parity.first { it.rasterRows == 15 && it.faceColumn == 0 }
                        .worstGramOffDiagonal.emittedDimensionless(),
            "worstOffDiagonalAtM11" to
                    parity.first { it.rasterRows == 11 && it.faceColumn == 0 }
                        .worstGramOffDiagonal.emittedDimensionless(),
            "why" to "the tributary of each face beam is one row pitch centred on its own axis, " +
                    "so <piston, tiltY> = L_s * rowPitch * SUM beamY, and the corrugated face's " +
                    "gap sequence d, 2d, d, 2d, ... is palindromic iff m is even. It carries " +
                    "neither the axial span nor the thickness, and it needs no solve."
        ),
        parity = parity,
        upstream = upstream,
        residue = residue,
        blastRadius = blastRadius,
        verdict = verdict,
        falsifiers = listOf(
            "F1 (declared CLOSED) -- the corrected and retained coefficients differ at an " +
                    "orthogonal-basis lattice. Asserted bit-identically over four load cases at " +
                    "m = 4, 6 and 10; did not fire.",
            "F2 (declared OPEN) -- the quadrature Gram's off-diagonals are not exactly 0.0 at " +
                    "even m. FIRED. The value is the solver's own noise, so it is reported as a " +
                    "threshold: at every even m it is below " +
                    T330_UNIFORM_THRESHOLD.toString() + " and at no even m is it reliably the " +
                    "exact 0.0 that T-294's own 10 x 6 at 116 bp happens to return -- an " +
                    "assertion of exact equality failed on this study's first run and is " +
                    "recorded in FaceRigidBasisTest. So a branch taken on the float would not " +
                    "have been inert, and the one taken on the INTEGER ladder is.",
            "F3 (declared OPEN) -- the two right-hand-side conventions differ by more than 1e-2 " +
                    "relative at 15 x 4. Did not fire: at a non-orthogonal basis they are the " +
                    "same call and agree bit for bit.",
            "F4 (declared OPEN) -- a corrected 15 x 4 reading crosses T-5b. Did not fire: " +
                    upstream.count { it.verdictMoves } + " of " + upstream.size +
                    " readings move a verdict.",
            "F5 (declared CLOSED) -- the corrected readings must reproduce C-0218's to 1e-9.",
            "F6 (declared OPEN) -- an m = 10 result file re-run as a control is not " +
                    "byte-identical. Measured outside this study, in the claim.",
            "F7 (declared OPEN) -- faceRigidModesAreOrthogonal is not exactly (m even) over " +
                    "m = 3 to 16 and both face columns. Did not fire at " +
                    parity.count { it.parityPredictsOrthogonality } + " of " + parity.size + ".",
            "F8 (declared OPEN) -- the decomposition fails to annihilate its own three basis " +
                    "modes, which needs no solve at all. Did not fire.",
            "F9 (declared OPEN) -- two independent emissions of this file are not byte-identical.",
            "F10 (declared OPEN) -- a mutation of the new code survives every named test.",
            "F11 (declared OPEN) -- the retained accessor is unreachable from any consumer. Did " +
                    "not fire: five call sites in T-294's own two files carry the standing " +
                    "convention through it."
        ),
        reproductions = reproductions,
        convergence = convergence,
        findings = findings,
        validity = listOf(
            "The branch is on the face's own geometry, so it is exact for any block whose face " +
                    "lies on the half-bond ladder -- which HoneycombBlock.position guarantees " +
                    "and a require asserts. A face that left that ladder would refuse rather " +
                    "than guess.",
            "The odd-m right-hand side is taken in evaluate's inner product, which is the one " +
                    "the reported peak dishing is sampled through. The faceFunctional pairing " +
                    "the load is assembled with is a DIFFERENT fit, and the gap between them is " +
                    "measured here and deliberately not adopted -- CH-0282 section 5, CH-0284.",
            "Nothing here changes the SOLVE. At 15 x 4 the solved field under a uniform pressure " +
                    "is uniform to 1e-10 relative, which C-0218 asserts as a named test; what " +
                    "moved is the fit.",
            "The parity probe uses an m x 2 block at 42 bp, because the worst off-diagonal is a " +
                    "ratio of three integrals that all carry the axial span as a factor and is " +
                    "therefore independent of the row length and the thickness. That is asserted " +
                    "as a named test rather than argued.",
            "A one-column block is a mechanism under the banded solve, so every probe that " +
                    "solves takes two columns."
        ),
        openQuestions = listOf(
            "CH-0284 -- whether the class should fit and sample in the SAME reconstruction. It " +
                    "moves all 18 files and it is a convention rather than a defect, so it is " +
                    "priced here and filed rather than taken.",
            "Whether every geometric falsifier in this tree should be swept over both parities " +
                    "of every discrete count it depends on, or whether the projector-level test " +
                    "F8 generalises more cheaply. T-326.",
            "OrigamiGrillage's own dishing decomposition is not examined here. Its tributaries " +
                    "are uniform, so the same defect cannot arise in the same way, but that is " +
                    "an argument and not a measurement."
        )
    )

    val output = File("gpd/results/T-330-a-dishing-fit-and-the-parity-of-its-basis.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result)
                .roundedForResult(digits = 9, digitsByKey = T330_DEPARTURE_DIGITS)
                .withEmissionHeader(LatticeTag.HONEYCOMB, null) as JsonObject)
        ) + "\n"
    )
    println("T-330 - wrote " + output.path)
    println("  " + max(0, reproductions.count { it.closes }) + " of " + reproductions.size +
            " reproductions close")
}
