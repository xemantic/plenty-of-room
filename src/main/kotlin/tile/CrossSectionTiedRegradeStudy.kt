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
import com.xemantic.nano.plentyofroom.coupling.DropoutEnsemble
import com.xemantic.nano.plentyofroom.coupling.InfluenceSurrogate
import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.coupling.dropoutDishingSample
import com.xemantic.nano.plentyofroom.coupling.dropoutEnsemble
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.coupling.measuredDepthIncorporation
import com.xemantic.nano.plentyofroom.coupling.summariseDropoutDishing
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
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

// ---------------------------------------------------------------------------------------------
// T-294 -- the 15 x 4 block graded coupled on the TIED lattice, so the two cross-sections can be
// compared like for like.
//
// C-0180 re-graded C-0167's 64 coupled cells on the tied honeycomb lattice at 10 x 6 ONLY, and
// C-0186 section 1 records the consequence: one comparison passage of DECISIONS-FOR-NDI.md is
// left un-annotated, because giving one cross-section its tied numbers while the other has none
// would read as a measured ordering when only one side moved. C-0191 has since stated that leave
// in the document. This study closes it with a measurement.
//
// Nothing turns on the answer today -- the ordering already stands on the UNCOUPLED tiles. What
// a run buys is a like-for-like ordering in one state, plus three things a caveat cannot give:
// the tie set's worth at a DIFFERENT in-plane / through-thickness split, the ordering read in
// the ABSOLUTE convention as well as the fractional one, and a 15 x 4 reading at the resolved
// per-bond link the document now quotes 10 x 6 in.
// ---------------------------------------------------------------------------------------------

private const val T294_SAMPLES: Int = 81
private const val T294_TOLERANCE: Double = 0.10
private const val T294_RIM_STANDOFF: Double = 1.0
private const val T294_RIM_BAND: Double = 6.7
private const val T294_SEED: Long = 197_197L
private const val T294_BLOCK_EXTENT_BP: Int = 116
private const val T294_LADDER_PHASE: Int = 16
private const val T294_LADDER_OFFSET: Int = 14
private const val T294_RECOMMENDED_ONE: Int = 102
private const val T294_RECOMMENDED_TWO: Int = 109

/** `C-0205`'s transverse ceiling, which `C-0208` pins the in-plane bonds at. */
private const val T294_SHEAR_CEILING: Double = 254.808095

/** `C-0208`'s softest rung -- radial = transverse, every bond at the shear ceiling. */
private const val T294_RADIAL_CONTROL: Double = 254.808095

/** `C-0208`'s middle rung -- the connector candidate PLUS the measured duplex-pair term. */
private const val T294_RADIAL_MEASURED: Double = 754.005141

/** The relative tolerance every same-quantity identity is asserted at. */
private const val T294_IDENTITY: Double = 1e-9

/** The study runs at 4 000 realisations; `T294_SMOKE=1` drops it to 150 for a plumbing pass. */
private val t294Realisations: Int =
    if (System.getenv("T294_SMOKE") == "1") 150 else 4000

private fun Double.emitted(digits: Int = 9): String = roundedForProse(digits).toString()

// ------------------------------------------------------------------------------ the records

@Serializable
private class T294CheapBoundRow(
    val question: String,
    val answer: String,
    val consequence: String
)

@Serializable
private class T294Census(
    val crossSection: String,
    val rowBasePairs: Int,
    val helices: Int,
    val faceHelices: Int,
    val bonds: Int,
    val bondsInPlane: Int,
    val bondsThroughThickness: Int,
    val turnTies: Int,
    val tiesThroughThickness: Int,
    val tiesInPlane: Int,
    val tiesAtHighRim: Int,
    val tiesAtLowRim: Int,
    val everyTurnIsBonded: Boolean,
    val degreesOfFreedom: Int,
    val bandwidth: Int,
    val edgeX: Double,
    val edgeY: Double,
    val interiorPressure: Double,
    val freeStroke: Double,
    val absoluteToleranceNm: Double,
    val enhancementAt030: Double,
    val enhancementAt026: Double,
    val faceModesAreOrthogonal: Boolean,
    val worstNonOrthogonality: Double
)

@Serializable
private class T294Geometry(
    val crossSection: String,
    val linkState: String,
    val tieState: String,
    val compositeFraction: Double?,
    val hingeStiffnessEnhancement: Double,
    val bonds: Int,
    val turnTies: Int,
    val freeStroke: Double,
    val closedFormStroke: Double,
    val strokeMatchesClosedForm: Boolean,
    val strokeIdentityTolerance: Double,
    val uncoupledStandingOverStroke: Double,
    val uncoupledCorrectedOverStroke: Double,
    val uncoupledCorrectedNm: Double,
    val uncoupledFlatStanding: Boolean,
    val uncoupledFlatCorrected: Boolean
)

@Serializable
private class T294Cell(
    val crossSection: String,
    val linkState: String,
    val tieState: String,
    val compositeFraction: Double,
    val placement: String,
    val columns: Int,
    val rows: Int,
    val pathCount: Int,
    val distribution: String,
    val nominalStandingOverStroke: Double,
    val nominalCorrectedOverStroke: Double,
    val p90StandingOverStroke: Double,
    val p90CorrectedOverStroke: Double,
    val p90CorrectedNm: Double,
    val medianCorrectedOverStroke: Double,
    val worstCorrectedOverStroke: Double,
    val exceedance: Double,
    val exceedanceStandardError: Double,
    val exceedanceOneSidedBound: Double?,
    val meanSurvivors: Double,
    val uncoupledCorrectedOverStroke: Double,
    val flatAtNominal: Boolean,
    val flatAtP90Standing: Boolean,
    val flatAtP90Corrected: Boolean,
    val beatsUncoupledAtP90: Boolean,
    val conventionRatio: Double
)

@Serializable
private class T294Paired(
    val comparison: String,
    val crossSection: String,
    val linkState: String,
    val compositeFraction: Double,
    val placement: String,
    val columns: Int,
    val pathCount: Int,
    val distribution: String,
    val realisations: Int,
    val medianRatio: Double,
    val p90Ratio: Double,
    val bestRatio: Double,
    val worstRatio: Double,
    val fractionTiedIsWorse: Double,
    val ratioOfPercentiles: Double,
    val untiedP90OverStroke: Double,
    val tiedP90OverStroke: Double,
    val untiedFlatAtP90: Boolean,
    val tiedFlatAtP90: Boolean,
    val verdictMoved: Boolean,
    val insideTenBySixMedianBand: Boolean
)

@Serializable
private class T294Ordering(
    val reading: String,
    val linkState: String,
    val compositeFraction: Double,
    val placement: String,
    val columns: Int,
    val distribution: String,
    val tenBySixPaths: Int,
    val fifteenByFourPaths: Int,
    val tenBySixP90OverStroke: Double,
    val fifteenByFourP90OverStroke: Double,
    val tenBySixP90Nm: Double,
    val fifteenByFourP90Nm: Double,
    val tenBySixFlat: Boolean,
    val fifteenByFourFlat: Boolean,
    val orderingAgreesWithTheFractionalReading: Boolean,
    val source: String
)

/** One committed upstream free-tile reading, re-taken in BOTH dishing conventions. */
@Serializable
private class T294UpstreamReading(
    val source: String,
    val crossSection: String,
    val rowBasePairs: Int,
    val hingeStiffnessEnhancement: Double,
    val published: Double,
    val standing: Double,
    val corrected: Double,
    val standingReproducesThePublished: Boolean,
    val reproductionDeparture: Double,
    val correctedOverStanding: Double,
    val flatStanding: Boolean,
    val flatCorrected: Boolean,
    val verdictMoves: Boolean
)

@Serializable
private class T294Convergence(
    val axis: String,
    val cell: String,
    val quantity: String,
    val coarse: String,
    val fine: String,
    val coarseValue: Double,
    val fineValue: Double,
    val departure: Double,
    val verdictAtCoarse: Boolean?,
    val verdictAtFine: Boolean?,
    val verdictSurvives: Boolean?
)

@Serializable
private class T294Reproduction(
    val source: String,
    val quantity: String,
    val published: Double,
    val here: Double,
    val departure: Double,
    val closes: Boolean
)

/**
 * One declared falsifier.
 *
 * [fired] is **nullable** because `CH-0281` (`C-0217`, `T-328`) is right: a verdict a run cannot
 * take must not be emitted as a verdict the run took. `F2`, `F3` and `F14` are measured **outside**
 * this study — two in the named test suite and one by diffing two independent emissions — so they
 * emit `null` and their notes name the artifact the measurement lives in.
 */
@Serializable
private class T294Falsifier(
    val name: String,
    val statement: String,
    val fired: Boolean?,
    val note: String,
    val measuredIn: String
)

@Serializable
private class T294Result(
    val task: String,
    val claim: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: Map<String, String>,
    val parameters: Map<String, String>,
    val sources: List<String>,
    val citedInputs: Map<String, String>,
    val cheapBound: List<T294CheapBoundRow>,
    val census: List<T294Census>,
    val geometries: List<T294Geometry>,
    val cells: List<T294Cell>,
    val paired: List<T294Paired>,
    val ordering: List<T294Ordering>,
    val upstream: List<T294UpstreamReading>,
    val verdict: Map<String, String>,
    val convergence: List<T294Convergence>,
    val reproductions: List<T294Reproduction>,
    val falsifiers: List<T294Falsifier>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val proseFailure: String
)

// ------------------------------------------------------------------------------ the geometry

private class T294Profile(
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {
    fun field(interiorPressure: Double, edgeX: Double, edgeY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, edgeX, edgeY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T294_RIM_STANDOFF))
        )
}

private fun t294Profile(file: File): T294Profile {
    require(file.exists()) { "C-0022's result file is missing: " + file.path }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray
        .map { it.jsonObject }
        .firstOrNull { record ->
            fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == 2.0 && value("gapHeight") == 10.0 &&
                    value("appliedBias") == 0.192
        } ?: error("no C-0022 profile at the design state")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T294Profile(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

/** One cross-section's shared geometry — everything but the lattice's own elements. */
private class T294Shared(val profile: T294Profile, val rasterRows: Int, val helicesPerRow: Int) {
    val rowBasePairs: Int = T294_BLOCK_EXTENT_BP
    val block: HoneycombBlock = HoneycombBlock(rasterRows, helicesPerRow)
    val normalisation: CrossSectionNormalisation =
        crossSectionNormalisation(block, rowBasePairs, fractionalTolerance = T294_TOLERANCE)
    val edgeX: Double = normalisation.edgeX
    val edgeY: Double = normalisation.edgeY
    val interiorPressure: Double = normalisation.interiorPressure
    val closedFormStroke: Double = normalisation.freeStroke
    val pressureField: PressureField = profile.field(interiorPressure, edgeX, edgeY)
    val crossSection: String = "$rasterRows x $helicesPerRow"

    fun enhancementAt(fraction: Double): Double =
        honeycombCompositeEnhancement(block, fraction)
}

/** One `(cross-section, link state, tie state, enhancement)` — one factorisation, many solves. */
private class T294Tile(
    val shared: T294Shared,
    val linkState: String,
    val enhancement: Double,
    val tied: Boolean,
    val radial: Double?,
    val subdivisions: Int = 1
) {

    val lattice: HoneycombGrillage = honeycombTiedLatticeAtResolvedLink(
        block = shared.block,
        rowBasePairs = shared.rowBasePairs,
        enhancement = enhancement,
        tied = tied,
        transverseLinkStiffness =
            if (radial == null) HoneycombGrillage.RIGID_LINK_STIFFNESS else T294_SHEAR_CEILING,
        radialLinkStiffness = radial,
        subdivisions = subdivisions
    )

    val basis: FaceRigidBasis = FaceRigidBasis(lattice)

    private val uncoupledField by lazy { lattice.solve(shared.pressureField) }

    val freeStroke: Double by lazy {
        lattice.solve(uniformPressure(shared.interiorPressure)).meanDeflection
    }

    val uncoupledStanding: Double by lazy {
        uncoupledField.peakDishing(T294_SAMPLES) / freeStroke
    }

    val uncoupledCorrected: Double by lazy {
        basis.dishingOf(uncoupledField).peakDishing(T294_SAMPLES) / freeStroke
    }

    fun surrogates(grid: List<Pair<Double, Double>>, samples: Int = T294_SAMPLES):
            DualConventionSurrogates =
        crossSectionSurrogates(lattice, basis, grid, shared.pressureField, samples)
}

// ------------------------------------------------------------------------------ the grading

private class T294Graded(val cell: T294Cell, val corrected: DoubleArray, val standing: DoubleArray)

@Suppress("LongParameterList")
private fun gradeT294Cell(
    tile: T294Tile,
    fraction: Double,
    placement: String,
    columns: Int,
    grid: List<Pair<Double, Double>>,
    distribution: String,
    stiffnesses: List<Double>,
    pair: DualConventionSurrogates,
    ensemble: DropoutEnsemble
): T294Graded {
    val stroke = tile.freeStroke
    fun sampleOf(surrogate: InfluenceSurrogate): Pair<Double, DoubleArray> {
        val nominal = surrogate.solve(stiffnesses).peakDishing / stroke
        val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
        sample.indices.forEach { sample[it] = sample[it] / stroke }
        return nominal to sample
    }
    val (nominalStanding, standing) = sampleOf(pair.standing)
    val (nominalCorrected, corrected) = sampleOf(pair.corrected)
    val summaryStanding =
        summariseDropoutDishing(standing, nominalStanding, ensemble.meanSurvivors, T294_TOLERANCE)
    val summary = summariseDropoutDishing(
        corrected, nominalCorrected, ensemble.meanSurvivors, T294_TOLERANCE
    )
    return T294Graded(
        T294Cell(
            crossSection = tile.shared.crossSection,
            linkState = tile.linkState,
            tieState = if (tile.tied) "tied" else "untied",
            compositeFraction = fraction,
            placement = placement,
            columns = columns,
            rows = tile.shared.rasterRows,
            pathCount = grid.size,
            distribution = distribution,
            nominalStandingOverStroke = nominalStanding,
            nominalCorrectedOverStroke = nominalCorrected,
            p90StandingOverStroke = summaryStanding.p90,
            p90CorrectedOverStroke = summary.p90,
            p90CorrectedNm = summary.p90 * stroke,
            medianCorrectedOverStroke = summary.median,
            worstCorrectedOverStroke = summary.worst,
            exceedance = summary.exceedance,
            exceedanceStandardError = summary.exceedanceStandardError,
            exceedanceOneSidedBound = summary.exceedanceOneSidedBound,
            meanSurvivors = summary.meanSurvivors,
            uncoupledCorrectedOverStroke = tile.uncoupledCorrected,
            flatAtNominal = nominalCorrected < T294_TOLERANCE,
            flatAtP90Standing = summaryStanding.flatAtP90,
            flatAtP90Corrected = summary.flatAtP90,
            beatsUncoupledAtP90 = summary.p90 < tile.uncoupledCorrected,
            conventionRatio = summary.p90 / summaryStanding.p90
        ),
        corrected,
        standing
    )
}

private fun t294Distributions(
    grid: List<Pair<Double, Double>>,
    edgeX: Double,
    edgeY: Double
): List<Pair<String, List<Double>>> = listOf(
    "equal springs" to equalShareOfMandate(grid.size),
    "rim-graded 5:1" to rimGradedShareOfMandate(
        grid.map { (x, y) ->
            val onRim = abs(x) > edgeX / 2.0 - T294_RIM_BAND || abs(y) > edgeY / 2.0 - T294_RIM_BAND
            if (onRim) 5.0 else 1.0
        }
    )
)

/** `C-0167`'s four placements at whatever cross-section [shared] names. */
private fun t294Placements(
    shared: T294Shared,
    rootingHelixY: List<Double>,
    columns: Int
): List<Pair<String, List<Pair<Double, Double>>>> {
    val abstract = attachmentGrid(columns, shared.rasterRows, shared.edgeX, shared.edgeY)
    val raster = twoLengthRaster(
        shared.rasterRows, shared.helicesPerRow, T294_RECOMMENDED_ONE, T294_RECOMMENDED_TWO
    )
    val determined = twoLengthSnappedGrid(
        raster, columns, shared.edgeY, T294_LADDER_PHASE, T294_LADDER_OFFSET
    )
    fun onHelices(grid: List<Pair<Double, Double>>) = grid.mapIndexed { index, (x, _) ->
        x to rootingHelixY[index / columns]
    }
    return listOf(
        "abstract grid" to abstract,
        "abstract grid on the rooting helices" to onHelices(abstract),
        "determined station lattice" to determined,
        "determined station lattice on the rooting helices" to onHelices(determined)
    )
}

private fun t294PublishedCell(
    file: File,
    filter: (JsonObject) -> Boolean,
    key: String
): Double {
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("cells").jsonArray.map { it.jsonObject }
        .first(filter)
    return record.getValue(key).jsonPrimitive.content.toDouble()
}

// ------------------------------------------------------------------------------ the study

@Suppress("LongMethod", "ComplexMethod", "NestedBlockDepth")
fun main() {
    val t279 = ResultInputs.T_279.file()
    val t310 = ResultInputs.T_310.file()
    val profile = t294Profile(ResultInputs.T_3B.file())
    val tall = T294Shared(profile, 15, 4)
    val flat = T294Shared(profile, 10, 6)
    val fractions = listOf(0.30, 0.26)
    val gradedColumns = listOf(1, 2, 3, 5)
    val links = listOf(
        Triple("penalty 1e4 pN/nm (C-0180, C-0167)", null as Double?, true),
        Triple("resolved, radial 254.808095 (C-0208 control)", T294_RADIAL_CONTROL, true),
        Triple("resolved, radial 754.005141 (C-0208 measured)", T294_RADIAL_MEASURED, true)
    )

    // ============================================ Deliverable 1 -- the cheap bound, no solver
    println("T-294 - the cheap bound")
    val closing = closingResiduePairs(15, 4)
    val closingFlat = closingResiduePairs(10, 6)
    val recommendedCloses = (T294_RECOMMENDED_ONE % 21 to T294_RECOMMENDED_TWO % 21) in closing
    val publishedTiedP90 = Json.parseToJsonElement(t279.readText())
        .jsonObject.getValue("cells").jsonArray.map { it.jsonObject }
        .filter { it.getValue("tieState").jsonPrimitive.content == "tied" }
        .map { it.getValue("p90OverStroke").jsonPrimitive.content.toDouble() }
    val publishedResolvedTightest = Json.parseToJsonElement(t310.readText())
        .jsonObject.getValue("census").jsonArray.map { it.jsonObject }
        .map { it.getValue("tightestP90").jsonPrimitive.content.toDouble() }
    val rungSpread = publishedResolvedTightest.max() - publishedResolvedTightest.min()
    val cheapBound = listOf(
        T294CheapBoundRow(
            question = "can the recommended 102 / 109 raster be DRAWN on 15 x 4 at all?",
            answer = "yes. C-0151's closure sweep is exhaustive over the 441 residue pairs and " +
                    "it returns the SAME three at both 60-helix cross-sections -- " +
                    closing.joinToString(", ") { "(" + it.first + ", " + it.second + ")" } +
                    " at 15 x 4 against " +
                    closingFlat.joinToString(", ") { "(" + it.first + ", " + it.second + ")" } +
                    " at 10 x 6 -- and 102 mod 21 = " + (T294_RECOMMENDED_ONE % 21) +
                    ", 109 mod 21 = " + (T294_RECOMMENDED_TWO % 21) + " is one of them: " +
                    recommendedCloses,
            consequence = "the 'or, failing that' branch of this row's acceptance clause is " +
                    "CLOSED before any code: same extent, same ladder phase 16, same 14 bp " +
                    "inter-row offset, and a sparsest row of 5 stations, so all four column " +
                    "counts and all four C-0167 placements exist on 15 x 4"
        ),
        T294CheapBoundRow(
            question = "what does NOT transfer from 10 x 6?",
            answer = "four things, all derived from the block: the bond count and its direction " +
                    "split, the tie SPLIT (the COUNT H - 1 = 59 does transfer), the composite " +
                    "enhancement at four layers instead of six, and the normalising stroke",
            consequence = "the stroke ratio is a theorem rather than a measurement -- the " +
                    "interior pressure is F/(edgeX * edgeY), edgeX is shared and " +
                    "edgeY = m * 3d/2, so the free stroke goes as 1/m and the ratio is 10/15 " +
                    "EXACTLY. This row's acceptance clause asks for the 'same normalising " +
                    "stroke' and the lattice refuses it: " +
                    tall.closedFormStroke.emitted(9) + " against " +
                    flat.closedFormStroke.emitted(9) + " nm, so T-5b's 0.10 is 1.5x TIGHTER in " +
                    "absolute nm on the taller block"
        ),
        T294CheapBoundRow(
            question = "how many resolved-link rungs are owed?",
            answer = "C-0208's own census column moves its tightest p90 by " +
                    rungSpread.emitted(3) + " over all five rungs, which is " +
                    (rungSpread / publishedResolvedTightest.min() * 100.0).emitted(3) +
                    " per cent, against a 15 x 4 margin the bound below puts at 100 per cent or " +
                    "more",
            consequence = "TWO rungs are graded here -- the ladder's softest (the control, " +
                    "every bond at the shear ceiling) and its measured middle one -- and " +
                    "because that per-cent is measured on the OTHER cross-section, the rung " +
                    "sensitivity is measured again at 15 x 4 and F11 fires if it exceeds 5 per " +
                    "cent"
        ),
        T294CheapBoundRow(
            question = "how far could a 15 x 4 coupled cell possibly be from T-5b?",
            answer = "C-0154's uncoupled 15 x 4 readings are 0.220064299 to 0.312237799 BEFORE " +
                    "any coupling, and C-0109's 'every coupled cell is worse than the " +
                    "uncoupled tile' reproduces at 64 of 64 on C-0180's tied 10 x 6 lattice, " +
                    "16 of 16 on C-0142's and 0 of 32 on C-0212's searched cells. C-0180's own " +
                    "64 tied cells run " + publishedTiedP90.min().emitted(9) + " to " +
                    publishedTiedP90.max().emitted(9),
            consequence = "the bound says the answer is almost certainly 0 of 64 and it cannot " +
                    "say so: a free-tile ratio is a CEILING the coupled cells never reach " +
                    "(C-0180 section 3) and C-0109's statement is an empirical regularity. What " +
                    "it does decide is the SIZE of the margin the run looks for -- a factor of " +
                    "two, not the 0.198 and 0.426 per cent the 10 x 6 verdicts turn on"
        ),
        T294CheapBoundRow(
            question = "what did the cheap bound NOT foresee?",
            answer = "that the standing dishing decomposition is not a least-squares fit at an " +
                    "ODD raster-row count. HoneycombDeflection removes its rigid plane by three " +
                    "INDEPENDENT projections, which is the best fit only if the three modes are " +
                    "orthogonal; on a corrugated face <piston, tiltY> = integral of y over the " +
                    "tributaries, and that vanishes iff the gap sequence d, 2d, d, 2d, ... is " +
                    "palindromic, i.e. iff m is EVEN",
            consequence = "F1 FIRED. Every block this corpus has graded has m = 10; 15 x 4 is " +
                    "the first with m odd, and there a perfectly uniform solved field reports " +
                    "6 per cent of the stroke as dishing. Both readings are emitted at every " +
                    "cell and the correction is CH-0282"
        )
    )
    cheapBound.forEach { println("  " + it.question + " -> " + it.consequence.take(110)) }

    // ============================================ Deliverable 2 -- the censuses and the tiles
    println("T-294 - the censuses")
    val census = listOf(tall, flat).map { shared ->
        val probe = T294Tile(shared, "penalty", 1.0, tied = true, radial = null)
        val bonds = honeycombBondCensus(probe.lattice)
        val ties = honeycombTieCensus(shared.block)
        T294Census(
            crossSection = shared.crossSection,
            rowBasePairs = shared.rowBasePairs,
            helices = shared.block.helices,
            faceHelices = probe.lattice.faceBeams.size,
            bonds = bonds.bonds,
            bondsInPlane = bonds.inPlane,
            bondsThroughThickness = bonds.throughThickness,
            turnTies = ties.turns,
            tiesThroughThickness = ties.throughThickness,
            tiesInPlane = ties.inPlane,
            tiesAtHighRim = ties.atHighRim,
            tiesAtLowRim = ties.atLowRim,
            everyTurnIsBonded = ties.everyTurnIsBonded,
            degreesOfFreedom = probe.lattice.degreesOfFreedom,
            bandwidth = probe.lattice.bandwidth,
            edgeX = shared.edgeX,
            edgeY = shared.edgeY,
            interiorPressure = shared.interiorPressure,
            freeStroke = shared.closedFormStroke,
            absoluteToleranceNm = shared.normalisation.absoluteToleranceNm,
            enhancementAt030 = shared.enhancementAt(0.30),
            enhancementAt026 = shared.enhancementAt(0.26),
            faceModesAreOrthogonal = probe.basis.modesAreOrthogonal,
            worstNonOrthogonality = probe.basis.worstNonOrthogonality
        )
    }
    census.forEach {
        println("  " + it.crossSection + ": " + it.bonds + " bonds (" + it.bondsInPlane + " / " +
                it.bondsThroughThickness + "), " + it.turnTies + " ties (" + it.tiesInPlane +
                " / " + it.tiesThroughThickness + "), stroke " + it.freeStroke.emitted(9) +
                ", modes orthogonal " + it.faceModesAreOrthogonal)
    }

    println("T-294 - the tiles")
    val tiles = HashMap<Triple<String, Double, Boolean>, T294Tile>()
    val geometries = ArrayList<T294Geometry>()
    links.forEach { (label, radial, _) ->
        val tieStates = if (radial == null) listOf(false, true) else listOf(true)
        (fractions + 1.0).forEach { fraction ->
            val enhancement = if (fraction == 1.0) 1.0 else tall.enhancementAt(fraction)
            tieStates.forEach { tied ->
                val tile = T294Tile(tall, label, enhancement, tied, radial)
                if (fraction != 1.0) tiles[Triple(label, fraction, tied)] = tile
                geometries += T294Geometry(
                    crossSection = tall.crossSection,
                    linkState = label,
                    tieState = if (tied) "410 staple bonds + 59 raster turn ties"
                    else "410 staple bonds, no turn ties",
                    compositeFraction = if (fraction == 1.0) null else fraction,
                    hingeStiffnessEnhancement = enhancement,
                    bonds = tile.lattice.bonds.size,
                    turnTies = tile.lattice.turnElements.size,
                    freeStroke = tile.freeStroke,
                    closedFormStroke = tall.closedFormStroke,
                    strokeMatchesClosedForm = abs(tile.freeStroke - tall.closedFormStroke) <
                            T294_IDENTITY * tall.closedFormStroke,
                    strokeIdentityTolerance = T294_IDENTITY,
                    uncoupledStandingOverStroke = tile.uncoupledStanding,
                    uncoupledCorrectedOverStroke = tile.uncoupledCorrected,
                    uncoupledCorrectedNm = tile.uncoupledCorrected * tile.freeStroke,
                    uncoupledFlatStanding = tile.uncoupledStanding < T294_TOLERANCE,
                    uncoupledFlatCorrected = tile.uncoupledCorrected < T294_TOLERANCE
                )
            }
        }
    }
    geometries.forEach {
        println("  " + it.linkState.take(28) + "  f=" +
                (it.compositeFraction?.emitted(3) ?: "none") + "  " + it.tieState.take(24) +
                "  standing " + it.uncoupledStandingOverStroke.emitted(9) +
                "  corrected " + it.uncoupledCorrectedOverStroke.emitted(9) +
                (if (it.uncoupledFlatCorrected) "  flat" else "  NOT FLAT"))
    }

    // ============================================ Deliverable 3 and 4 -- the cells
    println("T-294 - the re-grade, " + t294Realisations + " realisations on one common stream")
    val probeTall = T294Tile(tall, "penalty", 1.0, tied = false, radial = null)
    val rootingHelixY = probeTall.lattice.faceBeams.map { probeTall.lattice.beamY[it] }
    val incorporation = measuredDepthIncorporation(tall.edgeX, tall.edgeY)
    val cells = ArrayList<T294Cell>()
    val samples = HashMap<String, DoubleArray>()
    gradedColumns.forEach { columns ->
        t294Placements(tall, rootingHelixY, columns).forEach { (placement, grid) ->
            val ensemble = dropoutEnsemble(
                grid.map { (x, y) -> incorporation.at(x, y) }, t294Realisations, T294_SEED
            )
            val distributions = t294Distributions(grid, tall.edgeX, tall.edgeY)
            links.forEach { (label, radial, _) ->
                val tieStates = if (radial == null) listOf(false, true) else listOf(true)
                fractions.forEach { fraction ->
                    tieStates.forEach { tied ->
                        val tile = tiles.getValue(Triple(label, fraction, tied))
                        val pair = tile.surrogates(grid)
                        distributions.forEach { (name, stiffnesses) ->
                            val graded = gradeT294Cell(
                                tile, fraction, placement, columns, grid, name, stiffnesses,
                                pair, ensemble
                            )
                            cells += graded.cell
                            val key = label + "|" + (if (tied) "tied" else "untied") + "|" +
                                    fraction + "|" + placement + "|" + columns + "|" + name
                            samples[key] = graded.corrected
                            samples["standing|" + key] = graded.standing
                        }
                    }
                }
            }
        }
    }
    println("  " + cells.size + " cells graded")

    // ============================================ Deliverable 5 -- the paired tied / untied
    println("T-294 - the paired reading at 15 x 4, per realisation on the shared stream")
    val penaltyLabel = links.first().first
    val paired = ArrayList<T294Paired>()
    gradedColumns.forEach { columns ->
        t294Placements(tall, rootingHelixY, columns).forEach { (placement, grid) ->
            t294Distributions(grid, tall.edgeX, tall.edgeY).forEach { (name, _) ->
                fractions.forEach { fraction ->
                    val key = { state: String ->
                        penaltyLabel + "|" + state + "|" + fraction + "|" + placement + "|" +
                                columns + "|" + name
                    }
                    val summary = pairedRatioSummary(
                        samples.getValue(key("tied")), samples.getValue(key("untied"))
                    )
                    fun cellOf(state: String) = cells.first {
                        it.linkState == penaltyLabel && it.tieState == state &&
                                it.compositeFraction == fraction && it.placement == placement &&
                                it.columns == columns && it.distribution == name
                    }
                    val tied = cellOf("tied")
                    val untied = cellOf("untied")
                    paired += T294Paired(
                        comparison = "the tied 15 x 4 lattice over its own untied one",
                        crossSection = tall.crossSection,
                        linkState = penaltyLabel,
                        compositeFraction = fraction,
                        placement = placement,
                        columns = columns,
                        pathCount = grid.size,
                        distribution = name,
                        realisations = summary.realisations,
                        medianRatio = summary.median,
                        p90Ratio = summary.p90,
                        bestRatio = summary.best,
                        worstRatio = summary.worst,
                        fractionTiedIsWorse = summary.fractionAbove,
                        ratioOfPercentiles = summary.ratioOfPercentiles,
                        untiedP90OverStroke = untied.p90CorrectedOverStroke,
                        tiedP90OverStroke = tied.p90CorrectedOverStroke,
                        untiedFlatAtP90 = untied.flatAtP90Corrected,
                        tiedFlatAtP90 = tied.flatAtP90Corrected,
                        verdictMoved = untied.flatAtP90Corrected != tied.flatAtP90Corrected,
                        insideTenBySixMedianBand =
                            summary.median >= 0.902845544 && summary.median <= 0.988116016
                    )
                }
            }
        }
    }

    // ============================================ Deliverable 6 -- the cross-section ordering
    //
    // UNPAIRED, deliberately and in the field name: the two blocks carry different path counts
    // at the same column count, so no realisation of one corresponds to a realisation of the
    // other and sharing a seed would not make the defect patterns the same. What is quoted is an
    // ORDERING and a MARGIN, in BOTH normalising conventions, never a paired ratio.
    println("T-294 - the cross-section ordering, unpaired, against C-0180 and C-0208")
    val ordering = ArrayList<T294Ordering>()
    val tenBySixStroke = flat.closedFormStroke
    fun tenBySixCell(file: File, fraction: Double, placement: String, columns: Int, dist: String,
                     extra: (JsonObject) -> Boolean, key: String): Double? = runCatching {
        t294PublishedCell(file, { record ->
            record["compositeFraction"]?.jsonPrimitive?.content?.toDoubleOrNull() == fraction &&
                    record["placement"]?.jsonPrimitive?.content == placement &&
                    record["columns"]?.jsonPrimitive?.content?.toInt() == columns &&
                    record["distribution"]?.jsonPrimitive?.content == dist && extra(record)
        }, key)
    }.getOrNull()
    gradedColumns.forEach { columns ->
        listOf(
            "abstract grid", "abstract grid on the rooting helices",
            "determined station lattice", "determined station lattice on the rooting helices"
        ).forEach { placement ->
            listOf("equal springs", "rim-graded 5:1").forEach { dist ->
                fractions.forEach { fraction ->
                    listOf(
                        Triple(penaltyLabel, t279, "C-0180 (T-279), tied, penalty link"),
                        Triple(
                            links[2].first, t310,
                            "C-0208 (T-310), tied, radial 754.005141"
                        )
                    ).forEach { (label, file, source) ->
                        val here = cells.firstOrNull {
                            it.linkState == label && it.tieState == "tied" &&
                                    it.compositeFraction == fraction &&
                                    it.placement == placement && it.columns == columns &&
                                    it.distribution == dist
                        } ?: return@forEach
                        val there = tenBySixCell(file, fraction, placement, columns, dist, {
                            if (file === t279) {
                                it["tieState"]?.jsonPrimitive?.content == "tied"
                            } else {
                                it["radialLinkStiffness"]?.jsonPrimitive?.content
                                    ?.toDoubleOrNull() == T294_RADIAL_MEASURED
                            }
                        }, "p90OverStroke") ?: return@forEach
                        val fractionalAgrees =
                            (here.p90StandingOverStroke > there) ==
                                    (here.p90StandingOverStroke * tall.closedFormStroke >
                                            there * tenBySixStroke)
                        ordering += T294Ordering(
                            reading = "the STANDING dishing convention, so that C-0180's and " +
                                    "C-0208's committed values are quoted unaltered",
                            linkState = label,
                            compositeFraction = fraction,
                            placement = placement,
                            columns = columns,
                            distribution = dist,
                            tenBySixPaths = columns * 10,
                            fifteenByFourPaths = here.pathCount,
                            tenBySixP90OverStroke = there,
                            fifteenByFourP90OverStroke = here.p90StandingOverStroke,
                            tenBySixP90Nm = there * tenBySixStroke,
                            fifteenByFourP90Nm =
                                here.p90StandingOverStroke * tall.closedFormStroke,
                            tenBySixFlat = there < T294_TOLERANCE,
                            fifteenByFourFlat = here.flatAtP90Standing,
                            orderingAgreesWithTheFractionalReading = fractionalAgrees,
                            source = source
                        )
                    }
                }
            }
        }
    }
    println("  " + ordering.size + " matched comparison rows")

    // ============================ C-0154's own committed free tiles, in BOTH conventions
    //
    // C-0154 reads its three 15 x 4 free tiles at its own 112 bp row and its own three
    // enhancements, and BOTH deliverables quote them. They are the numbers the odd-m defect
    // reaches, so they are re-taken here in both conventions -- the standing one to prove the
    // machinery is C-0154's, and the corrected one to say what they are.
    println("T-294 - C-0154's own free tiles, corrected")
    val upstream = ArrayList<T294UpstreamReading>()
    listOf(
        Triple(15, 4, listOf(1.0 to 0.312237799, 9.65079217 to 0.227177955,
            12.7228458 to 0.220064299)),
        Triple(10, 6, listOf(1.0 to 0.127358454, 21.1851817 to 0.0449400126,
            17.6059172 to 0.0477844467))
    ).forEach { (rows, layers, readings) ->
        val block = HoneycombBlock(rows, layers)
        val norm = crossSectionNormalisation(block, 112, fractionalTolerance = T294_TOLERANCE)
        val load = profile.field(norm.interiorPressure, norm.edgeX, norm.edgeY)
        readings.forEach { (enhancement, published) ->
            val lattice = honeycombTiedLatticeAtResolvedLink(
                block = block, rowBasePairs = 112, enhancement = enhancement, tied = false
            )
            val field = lattice.solve(load)
            val stroke = lattice.solve(uniformPressure(norm.interiorPressure)).meanDeflection
            val standing = field.peakDishing(T294_SAMPLES) / stroke
            val corrected =
                FaceRigidBasis(lattice).dishingOf(field).peakDishing(T294_SAMPLES) / stroke
            val departure = abs(standing - published) / published
            upstream += T294UpstreamReading(
                source = "C-0154 (T-253), the free tile at 112 bp",
                crossSection = "$rows x $layers",
                rowBasePairs = 112,
                hingeStiffnessEnhancement = enhancement,
                published = published,
                standing = standing,
                corrected = corrected,
                standingReproducesThePublished = departure < 1e-8,
                reproductionDeparture = departure,
                correctedOverStanding = corrected / standing,
                flatStanding = standing < T294_TOLERANCE,
                flatCorrected = corrected < T294_TOLERANCE,
                verdictMoves = (standing < T294_TOLERANCE) != (corrected < T294_TOLERANCE)
            )
        }
    }
    upstream.forEach {
        println("  " + it.crossSection + " enh " + it.hingeStiffnessEnhancement.emitted(9) +
                ": published " + it.published.emitted(9) + ", standing " +
                it.standing.emitted(9) + " (departure " + it.reproductionDeparture.emitted(3) +
                "), corrected " + it.corrected.emitted(9) + ", verdict moves " + it.verdictMoves)
    }

    // ============================================ the verdict
    val tiedPenalty = cells.filter { it.linkState == penaltyLabel && it.tieState == "tied" }
    val untiedPenalty = cells.filter { it.linkState == penaltyLabel && it.tieState == "untied" }
    val resolved = cells.filter { it.linkState != penaltyLabel }
    val bestTied = tiedPenalty.minByOrNull { it.p90CorrectedOverStroke }
    val rungByCell = resolved.groupBy {
        listOf(it.compositeFraction, it.placement, it.columns, it.distribution).joinToString("|")
    }
    val worstRungMove = rungByCell.values.maxOf { group ->
        val values = group.map { it.p90CorrectedOverStroke }
        if (values.size < 2) 0.0 else (values.max() - values.min()) / values.min()
    }
    val verdict = linkedMapOf(
        "cellsGraded" to (cells.size.toString() + " over three link states and two tie states, " +
                "one common dropout stream restricted per cell, both dishing conventions"),
        "tiedPenaltyFlatAtP90Corrected" to
                (tiedPenalty.count { it.flatAtP90Corrected }.toString() + " of " +
                        tiedPenalty.size + " -- against C-0180's 2 of 64 at 10 x 6"),
        "tiedPenaltyFlatAtP90Standing" to
                (tiedPenalty.count { it.flatAtP90Standing }.toString() + " of " +
                        tiedPenalty.size),
        "untiedPenaltyFlatAtP90Corrected" to
                (untiedPenalty.count { it.flatAtP90Corrected }.toString() + " of " +
                        untiedPenalty.size),
        "resolvedFlatAtP90Corrected" to (resolved.count { it.flatAtP90Corrected }.toString() +
                " of " + resolved.size + " -- against C-0208's 0 of 64 at 10 x 6"),
        "tightestTiedCell" to ((bestTied?.p90CorrectedOverStroke?.emitted(9) ?: "none") +
                " of the stroke = " + (bestTied?.p90CorrectedNm?.emitted(9) ?: "none") +
                " nm, at " + (bestTied?.placement ?: "-") + ", " + (bestTied?.columns ?: 0) +
                " x 15 = " + (bestTied?.pathCount ?: 0) + " paths, " +
                (bestTied?.distribution ?: "-") + ", f = " +
                (bestTied?.compositeFraction?.emitted(3) ?: "-")),
        "tightestTiedCellOverTheTolerance" to
                ((bestTied?.p90CorrectedOverStroke?.div(T294_TOLERANCE))?.emitted(9) ?: "none"),
        "medianRatioRange" to (paired.minOf { it.medianRatio }.emitted(9) + " to " +
                paired.maxOf { it.medianRatio }.emitted(9)),
        "pairedCellsInsideTheTenBySixMedianBand" to
                (paired.count { it.insideTenBySixMedianBand }.toString() + " of " + paired.size),
        "cellsAtWhichTheTiesAreADISHINGSOURCE" to
                (paired.count { it.medianRatio > 1.0 }.toString() + " of " + paired.size),
        "verdictsMovedByTheTies" to
                (paired.count { it.verdictMoved }.toString() + " of " + paired.size),
        "coupledCellsWORSEThanTheirOwnUncoupledTile" to
                (cells.count { !it.beatsUncoupledAtP90 }.toString() + " of " + cells.size +
                        " -- C-0109 reads 64 of 64 at 10 x 6; the " +
                        cells.count { it.beatsUncoupledAtP90 } + " here that are not beat their " +
                        "own uncoupled tile by at most " +
                        (cells.filter { it.beatsUncoupledAtP90 }
                            .maxOfOrNull { (it.uncoupledCorrectedOverStroke -
                                    it.p90CorrectedOverStroke) /
                                    it.uncoupledCorrectedOverStroke * 100.0 } ?: 0.0).emitted(3) +
                        " per cent, which is inside a " + t294Realisations +
                        "-draw p90's own sampling scatter and is not a refutation, and none of " +
                        "them is flat"),
        "orderingRowsWhereTheTwoReadingsAGREE" to
                (ordering.count { it.orderingAgreesWithTheFractionalReading }.toString() + " of " +
                        ordering.size),
        "orderingRowsWhere15x4IsWorse" to
                (ordering.count {
                    it.fifteenByFourP90OverStroke > it.tenBySixP90OverStroke
                }.toString() + " of " + ordering.size + " on the fractional reading, " +
                        ordering.count { it.fifteenByFourP90Nm > it.tenBySixP90Nm } + " of " +
                        ordering.size + " on the absolute one"),
        "orderingRowsWhereEITHERSideIsFlat" to
                (ordering.count { it.tenBySixFlat || it.fifteenByFourFlat }.toString() + " of " +
                        ordering.size + ", of which the two readings DISAGREE at " +
                        ordering.count {
                            (it.tenBySixFlat || it.fifteenByFourFlat) &&
                                    !it.orderingAgreesWithTheFractionalReading
                        } + " -- so the convention reversal F10 reports is confined to rows " +
                        "where BOTH cross-sections are far outside T-5b"),
        "worstResolvedRungMovement" to (worstRungMove.emitted(3) +
                " relative, over the two rungs graded, against C-0208's own " +
                (rungSpread / publishedResolvedTightest.min()).emitted(3) + " at 10 x 6"),
        "C0154sOwnThreeFifteenByFourReadings" to
                (upstream.filter { it.crossSection == "15 x 4" }
                    .joinToString("; ") {
                        it.published.emitted(9) + " -> " + it.corrected.emitted(9)
                    } + " -- reproduced at a worst departure of " +
                        upstream.maxOf { it.reproductionDeparture }.emitted(3) +
                        " and NO verdict moves (" +
                        upstream.count { it.verdictMoves } + " of " + upstream.size + ")"),
        "theStandingDishingConventionOverstates15x4By" to
                (cells.filter { it.crossSection == "15 x 4" }
                    .minOf { it.conventionRatio }.emitted(9) + " to " +
                        cells.filter { it.crossSection == "15 x 4" }
                            .maxOf { it.conventionRatio }.emitted(9) +
                        " (corrected over standing, so below one is an OVERSTATEMENT)")
    )
    verdict.forEach { (k, v) -> println("  " + k + ": " + v) }

    // ============================================ convergence, at the deciding cell
    println("T-294 - convergence at the tightest 15 x 4 cell")
    val convergence = ArrayList<T294Convergence>()
    if (bestTied != null) {
        val grid = t294Placements(tall, rootingHelixY, bestTied.columns)
            .first { it.first == bestTied.placement }.second
        val stiffnesses = t294Distributions(grid, tall.edgeX, tall.edgeY)
            .first { it.first == bestTied.distribution }.second
        val ensemble = dropoutEnsemble(
            grid.map { (x, y) -> incorporation.at(x, y) }, t294Realisations, T294_SEED
        )
        val label = "f = " + bestTied.compositeFraction.emitted(3) + ", " + bestTied.placement +
                ", " + bestTied.columns + " x 15 = " + grid.size + " paths, " +
                bestTied.distribution
        fun p90At(tile: T294Tile, samples: Int): Double {
            val surrogate = tile.surrogates(grid, samples).corrected
            val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
            sample.indices.forEach { sample[it] = sample[it] / tile.freeStroke }
            return summariseDropoutDishing(
                sample, surrogate.solve(stiffnesses).peakDishing / tile.freeStroke,
                ensemble.meanSurvivors, T294_TOLERANCE
            ).p90
        }
        val coarseTile = tiles.getValue(Triple(penaltyLabel, bestTied.compositeFraction, true))
        val fineTile = T294Tile(
            tall, penaltyLabel, coarseTile.enhancement, tied = true, radial = null,
            subdivisions = 2
        )
        val base = bestTied.p90CorrectedOverStroke
        val fine = p90At(fineTile, T294_SAMPLES)
        convergence += T294Convergence(
            axis = "beam subdivisions",
            cell = label,
            quantity = "the corrected p90 of the dropout ensemble, over the stroke",
            coarse = "1", fine = "2",
            coarseValue = base, fineValue = fine,
            departure = abs(fine - base) / abs(base),
            verdictAtCoarse = base < T294_TOLERANCE,
            verdictAtFine = fine < T294_TOLERANCE,
            verdictSurvives = (base < T294_TOLERANCE) == (fine < T294_TOLERANCE)
        )
        listOf(41 to 81, 81 to 161).forEach { (coarse, samples) ->
            val a = p90At(coarseTile, coarse)
            val b = p90At(coarseTile, samples)
            convergence += T294Convergence(
                axis = "the dishing sample grid",
                cell = label,
                quantity = "the corrected p90 of the dropout ensemble, over the stroke",
                coarse = coarse.toString(), fine = samples.toString(),
                coarseValue = a, fineValue = b,
                departure = if (a == 0.0) abs(b) else abs(b - a) / abs(a),
                verdictAtCoarse = a < T294_TOLERANCE,
                verdictAtFine = b < T294_TOLERANCE,
                verdictSurvives = (a < T294_TOLERANCE) == (b < T294_TOLERANCE)
            )
        }
    }
    convergence.forEach {
        println("  " + it.axis + " " + it.coarse + " -> " + it.fine + ": departure " +
                it.departure.emitted(3) + ", verdict survives " + it.verdictSurvives)
    }

    // ============================================ the reproductions -- the 10 x 6 CONTROL
    println("T-294 - the 10 x 6 control")
    val reproductions = ArrayList<T294Reproduction>()
    fun reproduce(source: String, quantity: String, published: Double, here: Double) {
        val departure =
            if (published == 0.0) abs(here) else abs(here - published) / abs(published)
        reproductions += T294Reproduction(
            source = source, quantity = quantity, published = published, here = here,
            departure = departure, closes = departure < 1e-8
        )
    }
    val publishedGeometry = Json.parseToJsonElement(t279.readText())
        .jsonObject.getValue("geometries").jsonArray.map { it.jsonObject }
    listOf(0.30 to "0.3", 0.26 to "0.26").forEach { (fraction, _) ->
        val tile = T294Tile(flat, penaltyLabel, flat.enhancementAt(fraction), true, null)
        val published = publishedGeometry.first {
            it["compositeFraction"]?.jsonPrimitive?.content?.toDoubleOrNull() == fraction &&
                    it.getValue("tieState").jsonPrimitive.content.contains("59 raster")
        }.getValue("uncoupledDishingOverStroke").jsonPrimitive.content.toDouble()
        reproduce(
            "C-0180 (T-279), the tied 10 x 6 free tile at f = " + fraction.emitted(3),
            "uncoupledDishingOverStroke", published, tile.uncoupledStanding
        )
        reproduce(
            "C-0180 (T-279), the tied 10 x 6 free stroke at f = " + fraction.emitted(3),
            "freeStroke", 5.27921926, tile.freeStroke
        )
    }
    // the two cells C-0180 recovered, re-graded here on ITS convention and ITS link state
    val flatProbe = T294Tile(flat, penaltyLabel, flat.enhancementAt(0.30), tied = false, radial = null)
    val flatRootingY = flatProbe.lattice.faceBeams.map { flatProbe.lattice.beamY[it] }
    val flatIncorporation = measuredDepthIncorporation(flat.edgeX, flat.edgeY)
    listOf(
        Triple("abstract grid", 3, "rim-graded 5:1"),
        Triple("abstract grid on the rooting helices", 5, "rim-graded 5:1")
    ).forEach { (placement, columns, dist) ->
        val tile = T294Tile(flat, penaltyLabel, flat.enhancementAt(0.30), tied = true, radial = null)
        val grid = t294Placements(flat, flatRootingY, columns).first { it.first == placement }.second
        val stiffnesses = t294Distributions(grid, flat.edgeX, flat.edgeY)
            .first { it.first == dist }.second
        val ensemble = dropoutEnsemble(
            grid.map { (x, y) -> flatIncorporation.at(x, y) }, t294Realisations, T294_SEED
        )
        val pair = tile.surrogates(grid)
        val sample = dropoutDishingSample(pair.standing, stiffnesses, ensemble)
        sample.indices.forEach { sample[it] = sample[it] / tile.freeStroke }
        val p90 = summariseDropoutDishing(
            sample, pair.standing.solve(stiffnesses).peakDishing / tile.freeStroke,
            ensemble.meanSurvivors, T294_TOLERANCE
        ).p90
        val published = t294PublishedCell(t279, { record ->
            record.getValue("tieState").jsonPrimitive.content == "tied" &&
                    record["compositeFraction"]?.jsonPrimitive?.content?.toDoubleOrNull() == 0.30 &&
                    record.getValue("placement").jsonPrimitive.content == placement &&
                    record.getValue("columns").jsonPrimitive.content.toInt() == columns &&
                    record.getValue("distribution").jsonPrimitive.content == dist
        }, "p90OverStroke")
        reproduce(
            "C-0180 (T-279), the recovered cell " + placement + ", " + columns + " columns",
            "p90OverStroke", published, p90
        )
    }
    // and C-0208's tightest cell at its measured radial rung
    run {
        val placement = "abstract grid on the rooting helices"
        val columns = 5
        val dist = "rim-graded 5:1"
        val tile = T294Tile(
            flat, links[2].first, flat.enhancementAt(0.30), tied = true, radial = T294_RADIAL_MEASURED
        )
        val grid = t294Placements(flat, flatRootingY, columns).first { it.first == placement }.second
        val stiffnesses = t294Distributions(grid, flat.edgeX, flat.edgeY)
            .first { it.first == dist }.second
        val ensemble = dropoutEnsemble(
            grid.map { (x, y) -> flatIncorporation.at(x, y) }, t294Realisations, T294_SEED
        )
        val pair = tile.surrogates(grid)
        val sample = dropoutDishingSample(pair.standing, stiffnesses, ensemble)
        sample.indices.forEach { sample[it] = sample[it] / tile.freeStroke }
        val p90 = summariseDropoutDishing(
            sample, pair.standing.solve(stiffnesses).peakDishing / tile.freeStroke,
            ensemble.meanSurvivors, T294_TOLERANCE
        ).p90
        reproduce(
            "C-0208 (T-310), the tightest cell at the measured radial rung",
            "p90OverStroke", 0.100198485, p90
        )
    }
    reproduce(
        "C-0146 (T-235), the 15 x 4 free stroke at the 116 bp block extent",
        "freeStroke", 3.5194795, tall.closedFormStroke
    )
    reproduce(
        "C-0146 (T-235), the 15 x 4 interior pressure at the 116 bp block extent",
        "interiorPressure", 0.0444356284, tall.interiorPressure
    )
    reproduce(
        "T-253 (C-0154), the realised 15 x 4 enhancement at f = 0.30",
        "realisedEnhancement15x4", 9.65079217, tall.enhancementAt(0.30)
    )
    reproductions.forEach {
        println("  " + it.source + ": departure " + it.departure.emitted(3) +
                (if (it.closes) "  closes" else "  DOES NOT CLOSE"))
    }

    // ============================================ the falsifiers
    val uniformTied = tiles.getValue(Triple(penaltyLabel, 0.30, true))
    val uniformField = uniformTied.lattice.solve(uniformPressure(tall.interiorPressure))
    val uniformStanding = uniformField.peakDishing(T294_SAMPLES) / uniformField.meanDeflection
    val uniformCorrected = uniformTied.basis.dishingOf(uniformField)
        .peakDishing(T294_SAMPLES) / uniformField.meanDeflection
    val tieCensusTall = honeycombTieCensus(tall.block)
    val tieCensusFlat = honeycombTieCensus(flat.block)
    val bondCensusTall = honeycombBondCensus(uniformTied.lattice)
    val worstConvergence =
        if (convergence.isEmpty()) 0.0 else convergence.maxOf { it.departure }
    val fractionalOrdering = ordering.count {
        it.fifteenByFourP90OverStroke > it.tenBySixP90OverStroke
    }
    val absoluteOrdering = ordering.count { it.fifteenByFourP90Nm > it.tenBySixP90Nm }
    val falsifiers = listOf(
        T294Falsifier(
            "F1",
            "a uniform pressure on the tied 15 x 4 coupled lattice dishes exactly zero",
            uniformStanding > T294_IDENTITY,
            "FIRED on the STANDING decomposition and NOT on the corrected one: " +
                    uniformStanding.emitted(9) + " of the stroke against " +
                    uniformCorrected.emitted(9) + ". The solved field is exactly uniform -- " +
                    "every face beam reads p/k_f -- so what fails is the dishing fit, which " +
                    "HoneycombDeflection takes by three INDEPENDENT projections and which is a " +
                    "least-squares fit only where the three rigid modes are orthogonal. On a " +
                    "corrugated face <piston, tiltY> vanishes iff the raster-row count is EVEN " +
                    "(worst non-orthogonality " +
                    census.first { it.crossSection == "15 x 4" }.worstNonOrthogonality
                        .emitted(3) + " at 15 x 4 against " +
                    census.first { it.crossSection == "10 x 6" }.worstNonOrthogonality
                        .emitted(3) + " at 10 x 6). CH-0282",
            measuredIn = "this study's own run"
        ),
        T294Falsifier(
            "F2",
            "an empty tie list at 15 x 4 is bit-identical to the plain lattice, and a null " +
                    "radial constant is the penalty lattice",
            null,
            "asserted as two named tests over the crossover site set, assembleLoad, the " +
                    "point-load dual and every entry of the banded matrix",
            measuredIn = "tile/CrossSectionTiedRegradeTest.kt -- two named tests over the bond site set, assembleLoad, the point-load dual and every entry of the banded matrix"
        ),
        T294Falsifier(
            "F3",
            "the influence surrogate reproduces the assembled 15 x 4 solve",
            null,
            "asserted as a named test at 1e-9 relative, on the corrected convention, against " +
                    "the lattice re-solved under the support forces the surrogate reports",
            measuredIn = "tile/CrossSectionTiedRegradeTest.kt -- one named test at 1e-9 relative against the lattice re-solved under the support forces the surrogate reports"
        ),
        T294Falsifier(
            "F4",
            "the 10 x 6 control reproduces C-0180's and C-0208's committed values at 1e-8",
            reproductions.any { !it.closes },
            reproductions.count { it.closes }.toString() + " of " + reproductions.size +
                    " close; worst departure " +
                    reproductions.maxOf { it.departure }.emitted(3),
            measuredIn = "this study's own run"
        ),
        T294Falsifier(
            "F5",
            "the tie set TRANSFERS -- the 15 x 4 block carries 435 bonds and a 50 / 9 tie split",
            bondCensusTall.bonds == 435 && tieCensusTall.throughThickness == 50,
            "THE DECLARATION IS INTERNALLY INCONSISTENT AND BOTH READINGS ARE EMITTED " +
                    "(CLAUDE.md: a pre-registered criterion can still be wrong; retain it by " +
                    "name, emit both verdicts, strike nothing). Its STATEMENT is the transfer " +
                    "hypothesis and its expectation was 'to fire' -- but a falsifier fires when " +
                    "its statement is found TRUE, and the transfer hypothesis is FALSE, so on " +
                    "the statement as written F5 does NOT fire. What the declaration was " +
                    "reaching for is that the transfer hypothesis is REFUTED, and it is: " +
                    "15 x 4 carries " + bondCensusTall.bonds +
                    " bonds (" + bondCensusTall.inPlane + " in plane, " +
                    bondCensusTall.throughThickness + " through) and " + tieCensusTall.turns +
                    " ties split " + tieCensusTall.throughThickness + " / " +
                    tieCensusTall.inPlane + ", against 10 x 6's 435 (135 / 300) and " +
                    tieCensusFlat.throughThickness + " / " + tieCensusFlat.inPlane +
                    ". Only the COUNT H - 1 = 59 transfers",
            measuredIn = "this study's own run"
        ),
        T294Falsifier(
            "F6",
            "the tied 15 x 4 lattice recovers at least one coupled cell at the penalty link",
            tiedPenalty.any { it.flatAtP90Corrected },
            tiedPenalty.count { it.flatAtP90Corrected }.toString() + " of " + tiedPenalty.size +
                    " clear T-5b at the 90th percentile; the tightest is " +
                    (bestTied?.p90CorrectedOverStroke?.emitted(9) ?: "none") + " of the stroke, " +
                    ((bestTied?.p90CorrectedOverStroke ?: 0.0) / T294_TOLERANCE).emitted(3) +
                    "x the tolerance",
            measuredIn = "this study's own run"
        ),
        T294Falsifier(
            "F7",
            "the tie's per-cell worth at 15 x 4 lies inside C-0180's own median-ratio band " +
                    "0.902845544 to 0.988116016",
            paired.all { it.insideTenBySixMedianBand },
            paired.count { it.insideTenBySixMedianBand }.toString() + " of " + paired.size +
                    " cells fall inside it; the 15 x 4 band is " +
                    paired.minOf { it.medianRatio }.emitted(9) + " to " +
                    paired.maxOf { it.medianRatio }.emitted(9) +
                    " at a 45 / 14 tie split against 10 x 6's 50 / 9",
            measuredIn = "this study's own run"
        ),
        T294Falsifier(
            "F8",
            "the ties are ADVERSE at some 15 x 4 cell -- a median per-realisation ratio above 1",
            paired.any { it.medianRatio > 1.0 },
            paired.count { it.medianRatio > 1.0 }.toString() + " of " + paired.size +
                    " cells; the ratio's own 90th percentile is above one at " +
                    paired.count { it.p90Ratio > 1.0 } + " of them and the worst single " +
                    "realisation reads " + paired.maxOf { it.worstRatio }.emitted(9),
            measuredIn = "this study's own run"
        ),
        T294Falsifier(
            "F9",
            "the ties move an UNCOUPLED 15 x 4 reading the adverse way at some enhancement",
            geometries.filter { it.linkState == penaltyLabel }
                .groupBy { it.compositeFraction }
                .any { (_, group) ->
                    val tied = group.firstOrNull { it.turnTies > 0 }
                    val untied = group.firstOrNull { it.turnTies == 0 }
                    tied != null && untied != null &&
                            tied.uncoupledCorrectedOverStroke >
                            untied.uncoupledCorrectedOverStroke
                },
            "this row's own Notes cell asserts that the ties move every uncoupled reading the " +
                    "favourable way; measured on the corrected convention at three " +
                    "enhancements the tied readings are " +
                    geometries.filter { it.linkState == penaltyLabel && it.turnTies > 0 }
                        .joinToString(", ") { it.uncoupledCorrectedOverStroke.emitted(9) } +
                    " against the untied " +
                    geometries.filter { it.linkState == penaltyLabel && it.turnTies == 0 }
                        .joinToString(", ") { it.uncoupledCorrectedOverStroke.emitted(9) },
            measuredIn = "this study's own run"
        ),
        T294Falsifier(
            "F10",
            "the cross-section ordering REVERSES under the absolute (nm) reading",
            ordering.any { !it.orderingAgreesWithTheFractionalReading },
            fractionalOrdering.toString() + " of " + ordering.size + " matched rows put 15 x 4 " +
                    "worse on the fractional reading and " + absoluteOrdering + " of " +
                    ordering.size + " on the absolute one, so the ordering is NOT " +
                    "convention-independent. The stroke ratio is 2/3 exactly, so a 15 x 4 cell " +
                    "reads BETTER in nm than a 10 x 6 one wherever its fractional excess is " +
                    "under 1.5x -- and T-5b's own tolerance moves with it, 0.35194795 nm " +
                    "against 0.527921926. The fractional reading is the one T-5b is written " +
                    "in and it is the one the ordering is quoted on; the absolute reading is " +
                    "emitted beside it because section 3 specifies a gap in nm",
            measuredIn = "this study's own run"
        ),
        T294Falsifier(
            "F11",
            "the resolved-link rung axis moves a 15 x 4 p90 by more than 5 per cent",
            worstRungMove > 0.05,
            "worst relative movement over the two rungs graded is " + worstRungMove.emitted(3) +
                    ", against C-0208's own " +
                    (rungSpread / publishedResolvedTightest.min()).emitted(3) + " at 10 x 6",
            measuredIn = "this study's own run"
        ),
        T294Falsifier(
            "F12",
            "a 15 x 4 placement refuses its station snap",
            gradedColumns.any { columns ->
                runCatching { t294Placements(tall, rootingHelixY, columns) }.isFailure
            },
            "all four placements are realisable at all four column counts: " +
                    gradedColumns.joinToString(", ") { columns ->
                        columns.toString() + " columns -> " +
                                t294Placements(tall, rootingHelixY, columns).size + " placements"
                    },
            measuredIn = "this study's own run"
        ),
        T294Falsifier(
            "F13",
            "a cell whose T-5b verdict moves keeps it under its own convergence axes",
            convergence.any { it.verdictSurvives == false },
            convergence.count { it.verdictSurvives == true }.toString() + " of " +
                    convergence.size + " steps keep the verdict at the tightest cell, at a " +
                    "worst departure of " + worstConvergence.emitted(3) +
                    "; no verdict MOVES anywhere in this study, so the axis is taken on the " +
                    "cell nearest to moving",
            measuredIn = "this study's own run"
        ),
        T294Falsifier(
            "F14",
            "two independent runs produce a byte-identical result file",
            null,
            "discharged outside the study by diffing two runs; see the claim's Provenance row",
            measuredIn = "gpd/data/T-294-reproducibility/ -- two independent emissions, retained and diffed outside the study"
        )
    )
    falsifiers.forEach {
        println("  " + it.name + when (it.fired) {
            true -> "  FIRED"
            false -> "  did not fire"
            null -> "  measured OUTSIDE the run (CH-0281): " + it.measuredIn.take(48)
        })
    }

    // ============================================ the emission
    val tallCensus = census.first { it.crossSection == "15 x 4" }
    val flatCensus = census.first { it.crossSection == "10 x 6" }
    val result = T294Result(
        task = "T-294",
        claim = "C-0218",
        leaf = "A8.2",
        title = "the 15 x 4 block graded coupled on the TIED lattice, so the two cross-sections " +
                "compare like for like -- and the first block with an ODD raster-row count " +
                "breaks the standing dishing decomposition",
        verificationType = "in-silico (the same three-dimensional beam-and-bond lattice, the " +
                "same exact Woodbury coupling surrogate and the same C-0087-measured " +
                "incorporation as a Bernoulli dropout over " + t294Realisations +
                " realisations on one common stream restricted per cell, at the OTHER 60-helix " +
                "cross-section) + logical (a cheap bound that is integer arithmetic and two " +
                "committed result files, and a lattice census asserted against the bond graph " +
                "rather than transferred)",
        maturity = "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated.",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm",
            "pressure" to "pN/nm^2 (= 1 MPa)",
            "dishing" to "dimensionless, as a fraction of THAT tile's own free stroke, and in nm",
            "temperature" to "300 K, aqueous 2 mM MgCl2, k_BT = 4.142 pN nm"
        ),
        conventions = mapOf(
            "s" to "along the helices, origin at the face centre",
            "y" to "across the helices in the plane of the face, origin at the face centre",
            "z" to "along the block's thickness",
            "W" to "positive DOWNWARD, toward the electrode (C-0006)",
            "crossSection" to "m x n is m corrugated x-raster rows of n helices; m is the " +
                    "in-plane count at 3d/2 and n the thickness count at d sqrt(3)/2",
            "edgeY" to "the PLATE convention, m * 3d/2, which is HoneycombGrillage.lengthY -- " +
                    "not the block envelope, which is one duplex diameter larger",
            "standing dishing" to "HoneycombDeflection.dishing, three independent projections",
            "corrected dishing" to "the least-squares rigid plane in the face inner product, " +
                    "which annihilates a uniform field at every raster-row count",
            "the cross-section comparison" to "UNPAIRED: the two blocks carry different path " +
                    "counts at the same column count, so no realisation of one corresponds to " +
                    "a realisation of the other"
        ),
        parameters = mapOf(
            "crossSections" to "15 x 4 (graded) and 10 x 6 (control, and quoted from C-0180 " +
                    "and C-0208)",
            "rowBasePairs" to T294_BLOCK_EXTENT_BP.toString(),
            "raster" to "102 / 109 (C-0151), drawable at BOTH cross-sections",
            "ladderPhase" to T294_LADDER_PHASE.toString(),
            "interRowOffset" to T294_LADDER_OFFSET.toString(),
            "interhelicalDistance" to "2.536",
            "inPlaneRowPitch" to "3.804",
            "layerPitch" to "2.19624042",
            "risePerBasePair" to "0.34",
            "hingeStiffness" to "13.5294118",
            "slipStiffness" to "64.7058824",
            "linkPenalty" to "10000.0",
            "transverseCeiling" to T294_SHEAR_CEILING.toString(),
            "radialRungs" to (T294_RADIAL_CONTROL.toString() + " and " +
                    T294_RADIAL_MEASURED.toString()),
            "foundationStiffness" to "0.012625625",
            "targetForce" to "100.0",
            "compositeFractions" to "0.30 and 0.26 (C-0116)",
            "mandate" to "C-0017's 33.3333 pN/nm on the SUM, section 3's acceptable clause",
            "realisations" to t294Realisations.toString(),
            "smokeRun" to (t294Realisations != 4000).toString(),
            "seed" to T294_SEED.toString(),
            "dishingSamples" to T294_SAMPLES.toString(),
            "flatnessTolerance" to T294_TOLERANCE.toString(),
            "firstAxialSign" to "1",
            "tiePrestrain" to "0 -- CH-0240 is UPHELD by C-0190, so C-0180 section 4's " +
                    "prestrain coordinate is withdrawn and is deliberately NOT mirrored here",
            "temperature" to "300 K, aqueous 2 mM MgCl2"
        ),
        sources = listOf(
            ResultInputs.T_279.path + " (C-0180), read",
            ResultInputs.T_310.path + " (C-0208), read",
            ResultInputs.T_3B.path + " (C-0022), read",
            "C-0151 / T-245's closure sweep, cited: the three closing residue pairs are the " +
                    "same at both 60-helix cross-sections",
            "C-0146 / T-235's committed 15 x 4 geometry at the 116 bp block extent, cited",
            "C-0154 / T-253's uncoupled 15 x 4 readings at 112 bp, cited as a cheap-bound input"
        ),
        citedInputs = mapOf(
            "C-0180's tied 10 x 6 free tiles" to "0.0446459684 / 0.0467367262 / 0.12738041",
            "C-0180's two recovered cells" to "0.0995744767 and 0.0998791032",
            "C-0180's median-ratio band" to "0.902845544 to 0.988116016",
            "C-0208's tightest cell at the measured rung" to "0.100198485",
            "C-0146's 15 x 4 free stroke at 116 bp" to "3.5194795",
            "T-253's realised 15 x 4 enhancement at f = 0.30" to "9.65079217",
            "C-0154's uncoupled 15 x 4 at 112 bp" to
                    "0.312237799 / 0.227177955 / 0.220064299"
        ),
        cheapBound = cheapBound,
        census = census,
        geometries = geometries,
        cells = cells,
        paired = paired,
        ordering = ordering,
        upstream = upstream,
        verdict = verdict,
        convergence = convergence,
        reproductions = reproductions,
        falsifiers = falsifiers,
        findings = listOf(
            "The 15 x 4 block is " + tiedPenalty.count { it.flatAtP90Corrected } + " of " +
                    tiedPenalty.size + " coupled cells flat at the 90th percentile on the tied " +
                    "lattice at the link penalty, and " +
                    resolved.count { it.flatAtP90Corrected } + " of " + resolved.size +
                    " at the resolved per-bond link -- against C-0180's 2 of 64 and C-0208's " +
                    "0 of 64 at 10 x 6. The tightest 15 x 4 cell is " +
                    (bestTied?.p90CorrectedOverStroke?.emitted(9) ?: "none") + " of the stroke, " +
                    ((bestTied?.p90CorrectedOverStroke ?: 0.0) / T294_TOLERANCE).emitted(3) +
                    "x T-5b, where the tightest 10 x 6 cell in the corpus clears it by " +
                    "0.426 per cent.",
            "The tie set does NOT transfer and only its COUNT does: 15 x 4 carries " +
                    bondCensusTall.bonds + " staple bonds (" + bondCensusTall.inPlane +
                    " in plane, " + bondCensusTall.throughThickness + " through the thickness) " +
                    "against 435 (135 / 300), and its 59 raster turn ties split " +
                    tieCensusTall.throughThickness + " / " + tieCensusTall.inPlane +
                    " against 50 / 9. F5's own declaration is internally inconsistent -- its " +
                    "statement is the transfer hypothesis and its expectation was 'to fire' -- " +
                    "so both readings are emitted: the statement is FALSE and the hypothesis is " +
                    "REFUTED.",
            "The tie's WORTH does not transfer either: the per-realisation median ratio runs " +
                    paired.minOf { it.medianRatio }.emitted(9) + " to " +
                    paired.maxOf { it.medianRatio }.emitted(9) + " at 15 x 4 against C-0180's " +
                    "0.902845544 to 0.988116016 at 10 x 6, with " +
                    paired.count { it.insideTenBySixMedianBand } + " of " + paired.size +
                    " cells inside that band. The ties move " +
                    paired.count { it.verdictMoved } + " of " + paired.size +
                    " flatness verdicts here, where they moved two at 10 x 6.",
            "THIS ROW'S ACCEPTANCE CLAUSE ASKS FOR THE 'SAME NORMALISING STROKE' AND THE " +
                    "LATTICE REFUSES IT. The interior pressure is F/(edgeX * edgeY), edgeX is " +
                    "shared and edgeY = m * 3d/2, so the free stroke goes as 1/m and the ratio " +
                    "is 10/15 EXACTLY: " + tall.closedFormStroke.emitted(9) + " nm against " +
                    flat.closedFormStroke.emitted(9) + ". T-5b's 0.10 is a fraction of a " +
                    "stroke, so in absolute nm it demands " +
                    tallCensus.absoluteToleranceNm.emitted(9) + " nm at 15 x 4 against " +
                    flatCensus.absoluteToleranceNm.emitted(9) + " at 10 x 6 -- 1.5x TIGHTER. " +
                    "The ordering is emitted in both readings and agrees at " +
                    ordering.count { it.orderingAgreesWithTheFractionalReading } + " of " +
                    ordering.size + " matched rows.",
            "AND THE FIRST BLOCK WITH AN ODD RASTER-ROW COUNT BREAKS THE STANDING FALSIFIER. " +
                    "A uniform pressure on the 15 x 4 lattice gives an EXACTLY uniform field -- " +
                    "every face beam reads p/k_f to 1e-10 relative -- and " +
                    "HoneycombDeflection.dishing calls " + uniformStanding.emitted(9) +
                    " of that stroke curvature, because it removes its rigid plane by three " +
                    "INDEPENDENT projections and <piston, tiltY> = integral of y over the " +
                    "tributaries vanishes only when the corrugated gap sequence d, 2d, d, 2d, " +
                    "... is palindromic, i.e. only when m is EVEN. Every block this corpus has " +
                    "graded has m = 10. The least-squares fit reads " +
                    uniformCorrected.emitted(9) + ". CH-0282.",
            "The correction is not a rescaling either: corrected over standing runs " +
                    cells.filter { it.crossSection == "15 x 4" }
                        .minOf { it.conventionRatio }.emitted(9) + " to " +
                    cells.filter { it.crossSection == "15 x 4" }
                        .maxOf { it.conventionRatio }.emitted(9) +
                    " over the graded cells, and at 10 x 6 the two conventions differ by under " +
                    "one part in a thousand -- the residue there being a second and far smaller " +
                    "inconsistency, that the class FITS with faceFunctional's owning-beam " +
                    "reconstruction and SAMPLES with evaluate's nearest-beam one.",
            "C-0109's regularity SURVIVES at 15 x 4 and it is at the edge of what this " +
                    "ensemble resolves: " + cells.count { !it.beatsUncoupledAtP90 } + " of " +
                    cells.size + " coupled cells are worse than their own uncoupled tile, and " +
                    "the " + cells.count { it.beatsUncoupledAtP90 } + " that are not beat it by " +
                    "at most " + (cells.filter { it.beatsUncoupledAtP90 }
                        .maxOfOrNull { (it.uncoupledCorrectedOverStroke -
                                it.p90CorrectedOverStroke) /
                                it.uncoupledCorrectedOverStroke * 100.0 } ?: 0.0).emitted(3) +
                    " per cent, on a 90th percentile of " + t294Realisations + " draws. That is " +
                    "inside the percentile's own sampling scatter and is NOT reported as a " +
                    "refutation. What IS reported is the scope clause: every tile the " +
                    "regularity has been reproduced on -- C-0109's own, C-0142's, C-0180's, " +
                    "C-0212's -- has an UNCOUPLED reading inside T-5b, where a coupling can only " +
                    "spend flatness; this one is 1.5 to 2.5x outside it, so there is something " +
                    "for a coupling to improve. No exception is FLAT and nothing moves (CH-0283).",
            "AND THE ORDERING IS NOT CONVENTION-INDEPENDENT. On the fractional reading T-5b is " +
                    "written in, 15 x 4 is worse at " + fractionalOrdering + " of " +
                    ordering.size + " matched rows; in absolute nm it is worse at " +
                    absoluteOrdering + " -- because the stroke ratio is 2/3, a 15 x 4 cell reads " +
                    "better in nm wherever its fractional excess is under 1.5x. F10 was " +
                    "declared open and FIRED, and the two columns travel together everywhere " +
                    "the ordering is quoted."
        ),
        validity = listOf(
            "TRL 1-3, model-consistent and traceable, not empirically demonstrated.",
            "THE DELIVERABLE IS AN ORDERING WITH A VALIDITY RANGE, NOT A RECOMMENDATION. It " +
                    "exists so that one comparison passage of DECISIONS-FOR-NDI.md can be " +
                    "annotated in ONE state; nothing here re-opens the cross-section choice, " +
                    "the placement search, the distribution rule or the raster.",
            "The cross-section comparison is UNPAIRED and the field names say so. The two " +
                    "blocks carry different path counts at the same column count, so the " +
                    "pairing is by COLUMN COUNT -- which is C-0142's own pairing -- and no " +
                    "per-realisation ratio between them is computed or quotable.",
            "Both blocks are graded on C-0058's equal-spring and rim-graded 5:1 rules, both " +
                    "TRANSFERRED onto the lattice rather than optima of it. C-0212 shows that " +
                    "C-0208's 0 of 64 reverses to 22 of 32 the moment the distribution is " +
                    "SEARCHED, so this ordering is an ordering of two blocks UNDER TWO FIXED " +
                    "RULES. The cheap half of the searched question is stated and not run: " +
                    "C-0212 measures the search worth 1.45251772x at its own tightest cell and " +
                    "reads 0 of 32 searched cells beating the uncoupled tile, and 15 x 4's " +
                    "uncoupled tile is itself outside T-5b.",
            "k_theta is Gen1Tile's SQUARE-lattice-fitted constant and k_s is a construction. A " +
                    "scaffold turn is assembled with the same three elements a staple crossover " +
                    "has because it is the same covalent object, and the tie sits at s = +-L/2 " +
                    "exactly where a scaffold crossover sits 5 bp from a staple position.",
            "C-0180 section 4's prestrain coordinate is WITHDRAWN (CH-0240 upheld by C-0190) " +
                    "and is deliberately not mirrored; C-0190's replacement twist eigenstrain " +
                    "is not mirrored either, because it reads 0 of 64 at 10 x 6 and cannot " +
                    "separate two blocks that are both zero, and its magnitude is published as " +
                    "a threshold rather than a value.",
            "TWO of C-0208's five radial rungs are graded, on the ground that its own census " +
                    "column moves its tightest p90 by " +
                    (rungSpread / publishedResolvedTightest.min()).emitted(3) +
                    " over all five -- a figure measured at 10 x 6 and therefore re-measured " +
                    "here, where F11 reports " + worstRungMove.emitted(3) + ".",
            "The lattice carries NO across-helix parallel-axis term, so its D_perp is the " +
                    "INDEPENDENT one and a lower bound; Kirchhoff is not safe at these " +
                    "thicknesses, so every D_parallel is an upper bound.",
            "The dropout statistics are measured on a single-layer Rothemund rectangle and only " +
                    "the PROFILE transfers, in nm; the ensemble perturbs the COUPLING and never " +
                    "the block's own crossovers or its ties.",
            "The lattice carries ONE row length, the 116 bp block extent both cross-sections " +
                    "share at the 102 / 109 raster; the 102 bp interface window is not modelled.",
            "The corrected dishing is a least-squares fit in the face inner product " +
                    "areaInnerProduct builds, which evaluates both fields with evaluate's " +
                    "NEAREST-beam reconstruction -- the same reconstruction peakDishing samples. " +
                    "It is therefore self-consistent; it is not identical to what a fit in " +
                    "faceFunctional's owning-beam form would give, and at 10 x 6 the two differ " +
                    "by under one part in a thousand."
        ),
        openQuestions = listOf(
            "Whether HoneycombGrillage's own dishing decomposition should be repaired. It is a " +
                    "shared source this study did not edit, and every reading it has produced " +
                    "at an EVEN raster-row count is unaffected. C-0154's three uncoupled 15 x 4 " +
                    "readings, quoted in both deliverables, are not (CH-0282).",
            "Whether a distribution SEARCHED at 15 x 4, as C-0212 searched at 10 x 6, recovers " +
                    "any cell. The cheap bound says no and does not settle it.",
            "What the tie's true axial station is worth at a 45 / 14 split. The ties here sit " +
                    "at the rim node exactly.",
            "Whether any other odd-m honeycomb block appears anywhere in this corpus's " +
                    "committed results. The census here covers the two 60-helix cross-sections " +
                    "only.",
            "Whether the 102 bp interface window, modelled as a restricted bond set, moves any " +
                    "cell graded here at the 116 bp extent."
        ),
        proseFailure = "none"
    )

    val output = File("gpd/results/T-294-the-tied-regrade-at-the-other-cross-section.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digits = 9, floor = 1e-12
            ).withEmissionHeader(LatticeTag.HONEYCOMB, null) as JsonObject)
        ) + "\n"
    )
    println("T-294 - wrote " + output.path)
}
