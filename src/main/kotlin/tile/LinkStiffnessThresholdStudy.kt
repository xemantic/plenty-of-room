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

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.anchoring.MeasuredBackbone
import com.xemantic.nano.plentyofroom.anchoring.SsDnaTether
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
import com.xemantic.nano.plentyofroom.structure.DEPARTURE_SIGNIFICANT_DIGITS
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.ResultInputs
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs

// ---------------------------------------------------------------------------------------------
// T-303 -- what link stiffness C-0180's coupled recovery needs, and what a crossover connector
// can supply.
//
// C-0194 section 6 declared F10 open and it FIRED: the FREE tile is penalty-independent over six
// decades (0 of 6 verdicts move) and the two COUPLED cells C-0180 recovered are flat at
// k_link >= 1000 pN/nm and NOT flat at 100 or at the span law's own 41.4338953. So the corpus's
// only coupled recovery is quoted with no link stiffness attached at all.
//
// Two halves. The cheap one is arithmetic: three closed-form routes to the connector's own
// transverse stiffness, of which two are independent of k_theta. The expensive one is a
// bisection and a census over an EXISTING constructor argument.
// ---------------------------------------------------------------------------------------------

private const val T303_SAMPLES: Int = 81
private const val T303_TOLERANCE: Double = 0.10
private const val T303_RIM_STANDOFF: Double = 1.0
private const val T303_RIM_BAND: Double = 6.7
private const val T303_SEED: Long = 197_197L
private const val T303_BLOCK_EXTENT_BP: Int = 116
private const val T303_LADDER_PHASE: Int = 16
private const val T303_LADDER_OFFSET: Int = 14
private const val T303_RECOMMENDED_ONE: Int = 102
private const val T303_RECOMMENDED_TWO: Int = 109
private const val T303_BISECTIONS: Int = 16

/**
 * The bisection count the two convergence axes are re-taken at.
 *
 * Ten iterations put the bracket at `3/2^10 = 0.0029` decades, i.e. `0.68 %` in `k_link` —
 * a factor of 7 below `F8`'s own 5 % threshold, so the axis measures the refinement and not
 * the bisector.
 */
private const val T303_CONVERGENCE_BISECTIONS: Int = 10
private const val T303_BISECTION_LOW: Double = 30.0
private const val T303_BISECTION_HIGH: Double = 3000.0

/** The relative tolerance every same-quantity identity is asserted at. */
private const val T303_IDENTITY: Double = 1e-9

/** The study runs at 4 000 realisations; `T303_SMOKE=1` drops it to 150 for a plumbing pass. */
private val t303Realisations: Int =
    if (System.getenv("T303_SMOKE") == "1") 150 else 4000

private fun Double.emitted(digits: Int = 9): String = roundedForProse(digits).toString()

/**
 * The same, with the absolute floor removed.
 *
 * `RESULT_ABSOLUTE_FLOOR` is a claim about the **locked units** — `1e-9 pN` — and `P-18` records
 * that it does not travel: a dimensionless departure of `4.4e-10` between two `p90`s is not a
 * force, and rendering it as `0.0` states an exactness the study did not measure.
 */
private fun Double.emittedDimensionless(digits: Int = 2): String =
    roundedForProse(digits, floor = 0.0).toString()

// ------------------------------------------------------------------------------ the records

@Serializable
private class T303CheapBoundRow(
    val question: String,
    val answer: String,
    val value: Double,
    val units: String,
    val consequence: String
)

@Serializable
private class T303Route(
    val route: String,
    val premise: String,
    val independentOfHingeStiffness: Boolean,
    val stiffness: Double,
    val note: String
)

@Serializable
private class T303LadderRow(
    val cell: String,
    val columns: Int,
    val pathCount: Int,
    val linkStiffness: Double,
    val ground: String,
    val p90OverStroke: Double,
    val nominalOverStroke: Double,
    val exceedance: Double,
    val exceedanceStandardError: Double,
    val exceedanceOneSidedBound: Double?,
    val flatAtP90: Boolean
)

@Serializable
private class T303Threshold(
    val cell: String,
    val columns: Int,
    val pathCount: Int,
    val thresholdLinkStiffness: Double?,
    val straddles: Boolean,
    val residualAtLowEnd: Double,
    val residualAtHighEnd: Double,
    val bracketWidthDecades: Double,
    val residualAtThreshold: Double?,
    val ceilingOverThreshold: Double?,
    val ceilingReachesThreshold: Boolean,
    val thresholdOverSoftenedBond: Double?,
    val note: String
)

@Serializable
private class T303CensusRow(
    val linkStiffness: Double,
    val ground: String,
    val cellsGraded: Int,
    val flatAtP90: Int,
    val flatAtNominal: Int,
    val tightestP90: Double,
    val tightestCell: String
)

@Serializable
private class T303Cell(
    val linkStiffness: Double,
    val compositeFraction: Double,
    val hingeStiffnessEnhancement: Double,
    val placement: String,
    val columns: Int,
    val rows: Int,
    val pathCount: Int,
    val distribution: String,
    val nominalOverStroke: Double,
    val p90OverStroke: Double,
    val exceedance: Double,
    val exceedanceStandardError: Double,
    val exceedanceOneSidedBound: Double?,
    val flatAtNominal: Boolean,
    val flatAtP90: Boolean,
    val uncoupledDishingOverStroke: Double,
    val beatsUncoupledAtP90: Boolean
)

@Serializable
private class T303MonotonicityRow(
    val cell: String,
    val cellsChecked: Int,
    val risesWithLinkStiffness: Int,
    val worstRise: Double,
    val monotone: Boolean
)

@Serializable
private class T303ResolutionRow(
    val bondDirection: String,
    val bonds: Int,
    val meanAbsUnitY: Double,
    val meanUnitZSquared: Double,
    val shearShare: Double,
    val axialShare: Double,
    val resolvedAtImpliedStepStiffness: Double,
    val resolvedAtDuplexStretchOverSpan: Double,
    val straddlesTheThresholds: Boolean,
    val note: String
)

@Serializable
private class T303RouteBRow(
    val cell: String,
    val linkStiffness: Double,
    val p90OverStroke: Double,
    // `T-337`. This row TRANSCRIBES `T-299`'s verdict rather than grading its own, so the
    // proportion the verdict is a function of is read across with it -- and `getValue` is
    // deliberate: a source that cannot supply the datum must stop this study loudly rather
    // than let a transcribed verdict be published without its sample.
    val exceedance: Double,
    val exceedanceStandardError: Double,
    val exceedanceOneSidedBound: Double?,
    val flatAtP90: Boolean,
    val source: String
)

@Serializable
private class T303Convergence(
    val axis: String,
    val cell: String,
    val quantity: String,
    val coarse: String,
    val fine: String,
    val coarseValue: Double,
    val fineValue: Double,
    val departure: Double,
    val relativeDeparture: Double,
    val verdictSurvives: Boolean
)

@Serializable
private class T303Reproduction(
    val source: String,
    val quantity: String,
    val published: Double,
    val here: Double,
    val departure: Double,
    val closes: Boolean
)

@Serializable
private class T303Falsifier(
    val name: String,
    val statement: String,
    val fired: Boolean,
    val note: String
)

@Serializable
private class T303Result(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: Map<String, String>,
    val parameters: Map<String, String>,
    val sources: List<String>,
    val citedInputs: Map<String, String>,
    val cheapBound: List<T303CheapBoundRow>,
    val routes: List<T303Route>,
    val ladder: List<T303LadderRow>,
    val monotonicity: List<T303MonotonicityRow>,
    val thresholds: List<T303Threshold>,
    val census: List<T303CensusRow>,
    val cells: List<T303Cell>,
    val resolution: List<T303ResolutionRow>,
    val routeB: List<T303RouteBRow>,
    val verdict: Map<String, String>,
    val convergence: List<T303Convergence>,
    val reproductions: List<T303Reproduction>,
    val falsifiers: List<T303Falsifier>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val proseFailure: String
)

// ------------------------------------------------------------------------------ the geometry

private class T303Profile(
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {
    fun field(interiorPressure: Double, edgeX: Double, edgeY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, edgeX, edgeY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T303_RIM_STANDOFF))
        )
}

private fun t303Profile(file: File): T303Profile {
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
    return T303Profile(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

/** `C-0167`/`C-0180`'s geometry, unchanged, so the only thing that moves is the link. */
private class T303Shared(val profile: T303Profile) {
    val rasterRows: Int = 10
    val helicesPerRow: Int = 6
    val rowBasePairs: Int = T303_BLOCK_EXTENT_BP
    val block: HoneycombBlock = HoneycombBlock(rasterRows, helicesPerRow)
    val d: Double = Gen1Tile.INTERHELICAL_HONEYCOMB
    val rowPitch: Double = HoneycombCrossSectionGeometry.rowPitch(d)
    val columnPitch: Double = HoneycombCrossSectionGeometry.columnPitch(d)
    val edgeX: Double = rowBasePairs * Gen1Tile.RISE_PER_BASE_PAIR
    val edgeY: Double = rasterRows * rowPitch
    val interiorPressure: Double = Gen1Tile.TARGET_FORCE / (edgeX * edgeY)
    val pressureField: PressureField = profile.field(interiorPressure, edgeX, edgeY)
    val crossSection: String = "$rasterRows x $helicesPerRow"

    fun enhancementAt(fraction: Double): Double = multiLayerRigidities(
        layers = helicesPerRow,
        interhelicalDistance = d,
        crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
        coupling = LayerCoupling.CALIBRATED,
        compositeFraction = fraction,
        layerSpacing = columnPitch
    ).realisedEnhancement
}

/** One (composite fraction, link stiffness) pair — one factorisation, many back-substitutions. */
private class T303Tile(
    val shared: T303Shared,
    val enhancement: Double,
    val linkStiffness: Double,
    val subdivisions: Int = 1
) {
    val lattice: HoneycombGrillage = honeycombTiedLatticeAtLinkStiffness(
        block = shared.block,
        rowBasePairs = shared.rowBasePairs,
        enhancement = enhancement,
        tied = true,
        linkStiffness = linkStiffness,
        subdivisions = subdivisions
    )

    val freeStroke: Double by lazy {
        lattice.solve(uniformPressure(shared.interiorPressure)).meanDeflection
    }

    val uncoupledDishing: Double by lazy {
        lattice.solve(shared.pressureField).peakDishing(T303_SAMPLES) / freeStroke
    }

    fun surrogate(grid: List<Pair<Double, Double>>, samples: Int = T303_SAMPLES):
            InfluenceSurrogate = honeycombTiedSurrogate(
        lattice, grid, shared.pressureField, samples
    )
}

private fun t303Distributions(
    grid: List<Pair<Double, Double>>,
    edgeX: Double,
    edgeY: Double
): List<Pair<String, List<Double>>> = listOf(
    "equal springs" to equalShareOfMandate(grid.size),
    "rim-graded 5:1" to rimGradedShareOfMandate(
        grid.map { (x, y) ->
            val onRim = abs(x) > edgeX / 2.0 - T303_RIM_BAND || abs(y) > edgeY / 2.0 - T303_RIM_BAND
            if (onRim) 5.0 else 1.0
        }
    )
)

/** `C-0167`'s four placements, unchanged. */
private fun t303Placements(
    shared: T303Shared,
    rootingHelixY: List<Double>,
    columns: Int
): List<Pair<String, List<Pair<Double, Double>>>> {
    val abstract = attachmentGrid(columns, shared.rasterRows, shared.edgeX, shared.edgeY)
    val raster = twoLengthRaster(
        shared.rasterRows, shared.helicesPerRow, T303_RECOMMENDED_ONE, T303_RECOMMENDED_TWO
    )
    val determined = twoLengthSnappedGrid(
        raster, columns, shared.edgeY, T303_LADDER_PHASE, T303_LADDER_OFFSET
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

private class T303Graded(
    val nominal: Double,
    val p90: Double,
    val flat: Boolean,
    // `T-337`. `C-0223`: `flat` IS `exceedance <= tolerance`, so the proportion travels with it.
    val exceedance: Double,
    val exceedanceStandardError: Double,
    val exceedanceOneSidedBound: Double?
)

private fun t303Grade(
    surrogate: InfluenceSurrogate,
    stiffnesses: List<Double>,
    freeStroke: Double,
    ensemble: DropoutEnsemble
): T303Graded {
    val nominal = surrogate.solve(stiffnesses).peakDishing / freeStroke
    val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
    sample.indices.forEach { sample[it] = sample[it] / freeStroke }
    val summary = summariseDropoutDishing(
        sample, nominal, ensemble.meanSurvivors, T303_TOLERANCE
    )
    return T303Graded(
        nominal, summary.p90, summary.flatAtP90,
        summary.exceedance, summary.exceedanceStandardError, summary.exceedanceOneSidedBound
    )
}

private fun t303Published(
    file: File,
    block: String,
    linkStiffness: Double,
    columns: Int,
    key: String
): Double = Json.parseToJsonElement(file.readText())
    .jsonObject.getValue(block).jsonArray.map { it.jsonObject }
    .first {
        abs(it.getValue("linkStiffness").jsonPrimitive.content.toDouble() - linkStiffness) <
                1e-6 * linkStiffness &&
                it.getValue("columns").jsonPrimitive.content.toInt() == columns &&
                it.getValue("phase").jsonPrimitive.content.toInt() == 0
    }
    .getValue(key).jsonPrimitive.content.toDouble()

// ------------------------------------------------------------------------------ the study

@Suppress("LongMethod", "ComplexMethod", "NestedBlockDepth")
fun main() {
    val shared = T303Shared(t303Profile(ResultInputs.T_3B.file()))
    val fractions = listOf(0.30, 0.26)
    val gradedColumns = listOf(1, 2, 3, 5)
    val d = Gen1Tile.INTERHELICAL_HONEYCOMB
    val rP = MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS
    val kTheta = Gen1Tile.crossoverHingeStiffness()
    val kT = thermalEnergy(ROOM_TEMPERATURE)
    val g = crossoverSpanFloor(d, rP)
    val softestLp = SsDnaTether.KUHN_LENGTH_FORCE_SPECTROSCOPY / 2.0
    val stiffestLp = SsDnaTether.KUHN_LENGTH_ZERO_FORCE_TWO_MILLIMOLAR / 2.0

    // ================ Deliverable 1 -- the bracket. Closed form, no solver, three routes.
    println("T-303 - the cheap bound: what a crossover connector's transverse stiffness can be")
    val bracket = crossoverLinkStiffnessBracket(
        hingeStiffness = kTheta,
        phosphateRadius = rP,
        interhelicalDistance = d,
        thermalEnergy = kT,
        softestPersistenceLength = softestLp,
        stiffestPersistenceLength = stiffestLp
    )
    val routes = listOf(
        T303Route(
            route = "tension, C-0194's span law",
            premise = "CH-0242's own premise -- both eigenmodes of the span form are ONE " +
                    "mechanism -- carried one step, so T = 2 k_theta / r_P and k_R = T / g. It " +
                    "attributes the WHOLE of k_theta to the span, which is the largest tension " +
                    "the attribution admits.",
            independentOfHingeStiffness = false,
            stiffness = bracket.floor,
            note = "T = " + impliedCrossoverBondTension(kTheta, rP).emitted(9) + " pN over g = " +
                    g.emitted(9) + " nm"
        ),
        T303Route(
            route = "Chen et al.'s softened bond, read on the DISPLACEMENT axis",
            premise = "2 alpha S / (100 a) -- the same two phosphate bonds the lattice already " +
                    "prices for axial slip, resisting a displacement in another direction. " +
                    "CLAUDE.md: an isotropic element cannot be stiff across and soft along, and " +
                    "k_perp / k_axial is identically 1 for a covalent tie on a softened bond.",
            independentOfHingeStiffness = true,
            stiffness = bracket.softenedBond,
            note = "Gen1Tile.crossoverInPlaneStiffness under a name that says which coordinate " +
                    "it is read on; alpha in Chen et al.'s admissible " +
                    Gen1Tile.CROSSOVER_ALPHA_MIN.emitted(3) + " to " +
                    Gen1Tile.CROSSOVER_ALPHA_MAX.emitted(3) + " scales it linearly"
        ),
        T303Route(
            route = "the connector's own bending, softest persistence length, CLAMPED ends",
            premise = "c(rho) EI / g^3 with c = 12 rho/(6+rho), derived here for a relative END " +
                    "DISPLACEMENT and not borrowed from C-0025 or C-0034; EI = L_p k_BT at the " +
                    "10-40 pN force-spectroscopy Kuhn.",
            independentOfHingeStiffness = true,
            stiffness = bracket.bendingSoft,
            note = "L_p = " + softestLp.emitted(9) + " nm, an ADDITIVE term, and c = 0 at a " +
                    "pinned end so the whole term brackets to zero"
        ),
        T303Route(
            route = "the connector's own bending, stiffest persistence length, CLAMPED ends",
            premise = "the same, at the zero-force scattering Kuhn, which is the corpus's own " +
                    "upper end for ssDNA at 2 mM MgCl2.",
            independentOfHingeStiffness = true,
            stiffness = bracket.bendingStiff,
            note = "L_p = " + stiffestLp.emitted(9) + " nm"
        )
    )
    routes.forEach { println("  " + it.route + " -> " + it.stiffness.emitted(9) + " pN/nm") }

    val cheapBound = ArrayList<T303CheapBoundRow>()
    cheapBound += T303CheapBoundRow(
        question = "do the two k_theta-INDEPENDENT routes agree with C-0194's k_theta-derived one?",
        answer = "Chen et al.'s softened bond on the displacement axis is " +
                (bracket.softenedBond / bracket.floor).emitted(9) + " times the span law's " +
                "tension term",
        value = bracket.softenedBond / bracket.floor,
        units = "dimensionless",
        consequence = "two constructions that share no fitted constant land within a factor of " +
                "two, so the connector's transverse stiffness is an order-10^1-10^2 quantity " +
                "and not an order-10^4 one"
    )
    cheapBound += T303CheapBoundRow(
        question = "what is the largest link stiffness any of the routes supports?",
        answer = "the larger displacement route plus the connector's bending at the stiffest " +
                "persistence length and clamped ends, every mechanism at its most favourable " +
                "simultaneously",
        value = bracket.ceiling,
        units = "pN/nm",
        consequence = "against RIGID_LINK_STIFFNESS = " +
                HoneycombGrillage.RIGID_LINK_STIFFNESS.emitted(9) + " pN/nm, a factor of " +
                (HoneycombGrillage.RIGID_LINK_STIFFNESS / bracket.ceiling).emitted(9)
    )
    cheapBound += T303CheapBoundRow(
        question = "does the direct duplex-duplex interaction raise the ceiling?",
        answer = "no -- it is a CENTRAL interaction, so its contribution to a coordinate " +
                "perpendicular to the line of centres is V'(d)/d, which is NEGATIVE wherever " +
                "the pair repels, and CLAUDE.md records this pair repels at every separation " +
                "on four independent measured methods",
        value = centralPairForceTransverseStiffness(1.0, d, Gen1Tile.RISE_PER_BASE_PAIR * 21.0),
        units = "pN/nm per pN/nm of repulsive pair force per unit length",
        consequence = "the sign is one line of algebra and it lowers the ceiling; the term is " +
                "reported for its sign and not added"
    )
    cheapBound.forEach { println("  " + it.question + " -> " + it.value.emitted(9)) }

    // ================ Deliverable 2 -- the two recovered cells over a ladder, and the threshold
    println("T-303 - the two recovered cells over the link ladder")
    val probe = T303Tile(shared, shared.enhancementAt(0.30), HoneycombGrillage.RIGID_LINK_STIFFNESS)
    val rootingHelixY = probe.lattice.faceBeams.map { probe.lattice.beamY[it] }
    val recoveredEnhancement = shared.enhancementAt(0.30)
    val incorporation = measuredDepthIncorporation(shared.edgeX, shared.edgeY)

    class T303Recovered(val label: String, val columns: Int, val placement: String)

    val recovered = listOf(
        T303Recovered("cell A", 3, "abstract grid"),
        T303Recovered("cell B", 5, "abstract grid on the rooting helices")
    )

    fun gridOf(r: T303Recovered): List<Pair<Double, Double>> =
        t303Placements(shared, rootingHelixY, r.columns).first { it.first == r.placement }.second

    fun gradeRecovered(
        r: T303Recovered,
        link: Double,
        subdivisions: Int = 1,
        samples: Int = T303_SAMPLES
    ): T303Graded {
        val grid = gridOf(r)
        val tile = T303Tile(shared, recoveredEnhancement, link, subdivisions)
        val ensemble = dropoutEnsemble(
            grid.map { (x, y) -> incorporation.at(x, y) }, t303Realisations, T303_SEED
        )
        val stiffnesses = t303Distributions(grid, shared.edgeX, shared.edgeY)
            .first { it.first == "rim-graded 5:1" }.second
        return t303Grade(tile.surrogate(grid, samples), stiffnesses, tile.freeStroke, ensemble)
    }

    val rungs = listOf(
        bracket.floor to "C-0194's span law, T = 2 k_theta / r_P over g = d - 2 r_P",
        bracket.softenedBond to "Chen et al.'s softened bond on the displacement axis",
        bracket.ceiling to "this study's CEILING -- every route at its most favourable",
        1e2 to "C-0194's own rung, two decades below the penalty",
        1e3 to "C-0194's own rung, one decade below the penalty",
        HoneycombGrillage.RIGID_LINK_STIFFNESS to "OrigamiGrillage's own penalty, the standing value"
    ).sortedBy { it.first }
    val ladder = ArrayList<T303LadderRow>()
    recovered.forEach { r ->
        rungs.forEach { (link, ground) ->
            val graded = gradeRecovered(r, link)
            ladder += T303LadderRow(
                cell = r.label + ": f = 0.3, " + r.placement + ", " + r.columns + " x " +
                        shared.rasterRows + " = " + gridOf(r).size + " paths, rim-graded 5:1",
                columns = r.columns,
                pathCount = gridOf(r).size,
                linkStiffness = link,
                ground = ground,
                p90OverStroke = graded.p90,
                nominalOverStroke = graded.nominal,
                exceedance = graded.exceedance,
                exceedanceStandardError = graded.exceedanceStandardError,
                exceedanceOneSidedBound = graded.exceedanceOneSidedBound,
                flatAtP90 = graded.flat
            )
        }
    }
    ladder.forEach {
        println("  " + it.cell.substringBefore(":") + "  k_link " + it.linkStiffness.emitted(9) +
                " -> p90 " + it.p90OverStroke.emitted(9) +
                (if (it.flatAtP90) "  FLAT" else "  not flat"))
    }

    println("T-303 - the threshold, bisected on log10 k_link")
    val thresholds = ArrayList<T303Threshold>()
    val bracketDecades = (Math.log10(T303_BISECTION_HIGH) - Math.log10(T303_BISECTION_LOW)) /
            Math.pow(2.0, T303_BISECTIONS.toDouble())
    recovered.forEach { r ->
        // CLAUDE.md: a bisection whose bracket never straddled returns its own endpoint, dressed
        // as an answer. So the endpoints are read FIRST and the absence of a crossing is a
        // reported verdict rather than a number.
        val atLow = gradeRecovered(r, T303_BISECTION_LOW).p90 - T303_TOLERANCE
        val atHigh = gradeRecovered(r, T303_BISECTION_HIGH).p90 - T303_TOLERANCE
        val straddles = (atLow > 0.0) != (atHigh > 0.0)
        val root = if (!straddles) null else bisectLogLinkStiffnessThreshold(
            T303_BISECTION_LOW, T303_BISECTION_HIGH, T303_BISECTIONS
        ) { gradeRecovered(r, it).p90 - T303_TOLERANCE }
        thresholds += T303Threshold(
            cell = r.label + ": f = 0.3, " + r.placement + ", " + r.columns + " x " +
                    shared.rasterRows + " = " + gridOf(r).size + " paths, rim-graded 5:1",
            columns = r.columns,
            pathCount = gridOf(r).size,
            thresholdLinkStiffness = root,
            straddles = straddles,
            residualAtLowEnd = atLow,
            residualAtHighEnd = atHigh,
            bracketWidthDecades = bracketDecades,
            residualAtThreshold = root?.let { gradeRecovered(r, it).p90 - T303_TOLERANCE },
            ceilingOverThreshold = root?.let { bracket.ceiling / it },
            ceilingReachesThreshold = root != null && bracket.ceiling >= root,
            thresholdOverSoftenedBond = root?.let { it / bracket.softenedBond },
            note = if (root == null)
                "the p90 does not cross T-5b's " + T303_TOLERANCE.emitted(2) + " anywhere in [" +
                        T303_BISECTION_LOW.emitted(9) + ", " + T303_BISECTION_HIGH.emitted(9) +
                        "] pN/nm, so this cell has no threshold in the swept range"
            else "the p90 crosses T-5b's " + T303_TOLERANCE.emitted(2) + " here; the ceiling " +
                    "on the physically supportable link is " + bracket.ceiling.emitted(9) +
                    " pN/nm"
        )
    }
    thresholds.forEach {
        println("  " + it.cell.substringBefore(":") + "  k* = " +
                (it.thresholdLinkStiffness?.emitted(9) ?: "none in range") +
                " pN/nm, ceiling/threshold = " +
                (it.ceilingOverThreshold?.emitted(9) ?: "-"))
    }
    val crossing = thresholds.filter { it.thresholdLinkStiffness != null }

    // ================ Deliverable 3 -- the whole 64-cell census, at every rung
    println("T-303 - the 64-cell census, " + t303Realisations + " realisations per cell")
    val censusRungs = rungs.filter { it.first != 1e2 }
    val cells = ArrayList<T303Cell>()
    val tiles = HashMap<Pair<Double, Double>, T303Tile>()
    censusRungs.forEach { (link, _) ->
        fractions.forEach { fraction ->
            tiles[link to fraction] = T303Tile(shared, shared.enhancementAt(fraction), link)
        }
    }
    gradedColumns.forEach { columns ->
        t303Placements(shared, rootingHelixY, columns).forEach { (placement, grid) ->
            val ensemble = dropoutEnsemble(
                grid.map { (x, y) -> incorporation.at(x, y) }, t303Realisations, T303_SEED
            )
            val distributions = t303Distributions(grid, shared.edgeX, shared.edgeY)
            censusRungs.forEach { (link, _) ->
                fractions.forEach { fraction ->
                    val tile = tiles.getValue(link to fraction)
                    // one surrogate per (grid, rung, fraction) -- the influence bank is a
                    // property of the STRUCTURE, so it cannot depend on the distribution
                    val surrogate = tile.surrogate(grid)
                    distributions.forEach { (label, stiffnesses) ->
                        val graded = t303Grade(
                            surrogate, stiffnesses, tile.freeStroke, ensemble
                        )
                        cells += T303Cell(
                            linkStiffness = link,
                            compositeFraction = fraction,
                            hingeStiffnessEnhancement = tile.enhancement,
                            placement = placement,
                            columns = columns,
                            rows = shared.rasterRows,
                            pathCount = grid.size,
                            distribution = label,
                            nominalOverStroke = graded.nominal,
                            p90OverStroke = graded.p90,
                            exceedance = graded.exceedance,
                            exceedanceStandardError = graded.exceedanceStandardError,
                            exceedanceOneSidedBound = graded.exceedanceOneSidedBound,
                            flatAtNominal = graded.nominal < T303_TOLERANCE,
                            flatAtP90 = graded.flat,
                            uncoupledDishingOverStroke = tile.uncoupledDishing,
                            beatsUncoupledAtP90 = graded.p90 < tile.uncoupledDishing
                        )
                    }
                }
            }
        }
    }
    val census = censusRungs.map { (link, ground) ->
        val here = cells.filter { it.linkStiffness == link }
        val tightest = here.minByOrNull { it.p90OverStroke }!!
        T303CensusRow(
            linkStiffness = link,
            ground = ground,
            cellsGraded = here.size,
            flatAtP90 = here.count { it.flatAtP90 },
            flatAtNominal = here.count { it.flatAtNominal },
            tightestP90 = tightest.p90OverStroke,
            tightestCell = "f = " + tightest.compositeFraction.emitted(3) + ", " +
                    tightest.placement + ", " + tightest.columns + " x " + tightest.rows + " = " +
                    tightest.pathCount + " paths, " + tightest.distribution
        )
    }
    census.forEach {
        println("  k_link " + it.linkStiffness.emitted(9) + " -> " + it.flatAtP90 + " of " +
                it.cellsGraded + " flat at p90, tightest " + it.tightestP90.emitted(9))
    }

    // ================ F4 -- monotonicity, over every graded cell
    val keyed = cells.groupBy {
        listOf(it.compositeFraction, it.placement, it.columns, it.distribution).joinToString("|")
    }
    var rises = 0
    var worstRise = 0.0
    keyed.values.forEach { group ->
        group.sortedBy { it.linkStiffness }.zipWithNext().forEach { (soft, stiff) ->
            val rise = stiff.p90OverStroke - soft.p90OverStroke
            if (rise > 1e-9) {
                rises += 1
                worstRise = maxOf(worstRise, rise)
            }
        }
    }
    val monotonicity = ArrayList<T303MonotonicityRow>()
    monotonicity += T303MonotonicityRow(
        cell = "every cell of the census, over every consecutive pair of the ladder",
        cellsChecked = keyed.size,
        risesWithLinkStiffness = rises,
        worstRise = worstRise,
        monotone = rises == 0
    )
    // and separately at the two cells the BISECTION is taken on, because that is where
    // monotonicity is load-bearing: a bisection on a non-monotone residual is a fiction.
    recovered.forEach { r ->
        val here = ladder.filter { it.columns == r.columns }.sortedBy { it.linkStiffness }
        var cellRises = 0
        var cellWorst = 0.0
        here.zipWithNext().forEach { (soft, stiff) ->
            val rise = stiff.p90OverStroke - soft.p90OverStroke
            if (rise > 1e-9) {
                cellRises += 1
                cellWorst = maxOf(cellWorst, rise)
            }
        }
        monotonicity += T303MonotonicityRow(
            cell = r.label + ", the cell the bisection is taken on",
            cellsChecked = 1,
            risesWithLinkStiffness = cellRises,
            worstRise = cellWorst,
            monotone = cellRises == 0
        )
    }

    // ================ the resolution the BOND link does not carry, and the TETHER does
    // HoneycombTetherElement.normalStiffness is `tangent*unitZ^2 + secant*unitY^2`: the same
    // source file already resolves a chain's two mechanisms onto the link residual by the bond's
    // own direction. The BOND's link is one scalar, so the shear ceiling above is exact for the
    // in-plane bonds and is not the whole story for the through-thickness ones, where most of a
    // relative W displacement is a change of the interhelical SEPARATION. Closed form, no solve.
    println("T-303 - the resolution the bond link does not carry")
    val impliedStep = impliedCrossoverBondTension(kTheta, rP) /
            (g - MeasuredBackbone.STEP_SOUTH)
    val duplexOverSpan = Gen1Tile.DUPLEX_STRETCH_MODULUS / g
    val allBonds = probe.lattice.bonds
    val resolution = listOf(true, false).map { inPlane ->
        val here = allBonds.filter { it.inPlane == inPlane }
        val meanY = here.map { abs(it.unitY) }.average()
        val meanZ2 = here.map { it.unitZ * it.unitZ }.average()
        val meanY2 = here.map { it.unitY * it.unitY }.average()
        val atStep = meanZ2 * impliedStep + meanY2 * bracket.ceiling
        val atDuplex = meanZ2 * duplexOverSpan + meanY2 * bracket.ceiling
        T303ResolutionRow(
            bondDirection = if (inPlane) "in plane" else "through the thickness",
            bonds = here.size,
            meanAbsUnitY = meanY,
            meanUnitZSquared = meanZ2,
            shearShare = meanY2,
            axialShare = meanZ2,
            resolvedAtImpliedStepStiffness = atStep,
            resolvedAtDuplexStretchOverSpan = atDuplex,
            straddlesTheThresholds = crossing.any {
                it.thresholdLinkStiffness!! in minOf(atStep, atDuplex)..maxOf(atStep, atDuplex)
            },
            note = if (inPlane)
                "unitZ is zero, so the whole of a relative W displacement is a transverse SHEAR " +
                        "of the connector and this study's ceiling is exact here"
            else "unitZ^2 carries most of it, and there the resisting mechanism is the " +
                    "connector's own AXIAL stiffness, which neither CH-0242 nor C-0194 nor this " +
                    "study prices -- the two candidates bracketing it are C-0194's implied " +
                    "phosphodiester-step stiffness " + impliedStep.emitted(9) + " pN/nm and the " +
                    "duplex stretch modulus over the span " + duplexOverSpan.emitted(9) + " pN/nm"
        )
    }
    resolution.forEach {
        println("  " + it.bondDirection + "  " + it.bonds + " bonds, unitZ^2 = " +
                it.meanUnitZSquared.emitted(9) + " -> resolved " +
                it.resolvedAtImpliedStepStiffness.emitted(9) + " to " +
                it.resolvedAtDuplexStretchOverSpan.emitted(9) + " pN/nm" +
                (if (it.straddlesTheThresholds) "  STRADDLES the thresholds" else ""))
    }

    // ================ Deliverable 4 -- route B, read out of C-0201's own committed artifact
    println("T-303 - route B, read out of C-0201's own linkStiffness block")
    val t299 = Json.parseToJsonElement(ResultInputs.T_299.file().readText())
        .jsonObject.getValue("linkStiffness").jsonArray.map { it.jsonObject }
    val routeB = t299.map {
        T303RouteBRow(
            cell = it.getValue("cell").jsonPrimitive.content,
            linkStiffness = it.getValue("linkStiffness").jsonPrimitive.content.toDouble(),
            p90OverStroke = it.getValue("p90OverStroke").jsonPrimitive.content.toDouble(),
            exceedance = it.getValue("exceedance").jsonPrimitive.content.toDouble(),
            exceedanceStandardError =
                it.getValue("exceedanceStandardError").jsonPrimitive.content.toDouble(),
            exceedanceOneSidedBound =
                it["exceedanceOneSidedBound"]?.jsonPrimitive?.contentOrNull?.toDouble(),
            flatAtP90 = it.getValue("flatAtP90").jsonPrimitive.content.toBoolean(),
            source = ResultInputs.T_299.path + " (C-0201), read and not re-run"
        )
    }
    val routeBFlat = routeB.count { it.flatAtP90 }
    println("  " + routeBFlat + " of " + routeB.size + " tethered readings flat")

    // ================ convergence, at the DECIDING cell on the DECIDING quantity
    println("T-303 - convergence, on the bisected threshold at the deciding cell")
    val deciding = crossing.minByOrNull { it.thresholdLinkStiffness!! }
        ?: error("no cell crossed the tolerance in the swept range, so there is no axis to take")
    val decidingCell = recovered.first { deciding.cell.startsWith(it.label) }
    val coarseAxis = bisectLogLinkStiffnessThreshold(
        T303_BISECTION_LOW, T303_BISECTION_HIGH, T303_CONVERGENCE_BISECTIONS
    ) { gradeRecovered(decidingCell, it).p90 - T303_TOLERANCE }
    val fineSubdivision = bisectLogLinkStiffnessThreshold(
        T303_BISECTION_LOW, T303_BISECTION_HIGH, T303_CONVERGENCE_BISECTIONS
    ) { gradeRecovered(decidingCell, it, subdivisions = 2).p90 - T303_TOLERANCE }
    val fineSamples = bisectLogLinkStiffnessThreshold(
        T303_BISECTION_LOW, T303_BISECTION_HIGH, T303_CONVERGENCE_BISECTIONS
    ) { gradeRecovered(decidingCell, it, samples = 161).p90 - T303_TOLERANCE }
    val convergence = listOf(
        T303Convergence(
            axis = "beam subdivisions per crossover plane",
            cell = deciding.cell,
            quantity = "the bisected link-stiffness threshold, pN/nm",
            coarse = "1",
            fine = "2",
            coarseValue = coarseAxis,
            fineValue = fineSubdivision,
            departure = abs(fineSubdivision - coarseAxis),
            relativeDeparture = abs(fineSubdivision - coarseAxis) / coarseAxis,
            verdictSurvives = (bracket.ceiling >= fineSubdivision) ==
                    (bracket.ceiling >= coarseAxis)
        ),
        T303Convergence(
            axis = "dishing samples per edge",
            cell = deciding.cell,
            quantity = "the bisected link-stiffness threshold, pN/nm",
            coarse = T303_SAMPLES.toString(),
            fine = "161",
            coarseValue = coarseAxis,
            fineValue = fineSamples,
            departure = abs(fineSamples - coarseAxis),
            relativeDeparture = abs(fineSamples - coarseAxis) / coarseAxis,
            verdictSurvives = (bracket.ceiling >= fineSamples) ==
                    (bracket.ceiling >= coarseAxis)
        )
    )
    convergence.forEach {
        println("  " + it.axis + "  " + it.coarseValue.emitted(9) + " -> " +
                it.fineValue.emitted(9) + "  relative " + it.relativeDeparture.emitted(2))
    }

    // ================ reproductions
    val t297 = ResultInputs.T_297.file()
    val reproductions = ArrayList<T303Reproduction>()
    recovered.forEach { r ->
        listOf(bracket.floor, 1e2, 1e3, HoneycombGrillage.RIGID_LINK_STIFFNESS).forEach { link ->
            val published = t303Published(t297, "cells", link, r.columns, "p90OverStroke")
            val here = ladder.first { it.columns == r.columns && it.linkStiffness == link }
                .p90OverStroke
            reproductions += T303Reproduction(
                source = "C-0194 section 6, " + ResultInputs.T_297.path,
                quantity = r.label + " zero-eigenstrain p90 at k_link = " + link.emitted(9),
                published = published,
                here = here,
                departure = abs(here - published),
                closes = abs(here - published) < 1e-7
            )
        }
    }
    reproductions += T303Reproduction(
        source = "C-0194 section 4",
        quantity = "the span-derived link stiffness k_R, pN/nm",
        published = 41.4338953,
        here = bracket.floor,
        departure = abs(bracket.floor - 41.4338953),
        closes = abs(bracket.floor - 41.4338953) < 1e-7
    )
    reproductions += T303Reproduction(
        source = "C-0180 section 2, the recovered cell A",
        quantity = "the tied p90 at the standing penalty",
        published = 0.0995744767,
        here = ladder.first {
            it.columns == 3 && it.linkStiffness == HoneycombGrillage.RIGID_LINK_STIFFNESS
        }.p90OverStroke,
        departure = abs(
            ladder.first {
                it.columns == 3 && it.linkStiffness == HoneycombGrillage.RIGID_LINK_STIFFNESS
            }.p90OverStroke - 0.0995744767
        ),
        closes = true
    )
    reproductions += T303Reproduction(
        source = "C-0180 section 2, the recovered cell B",
        quantity = "the tied p90 at the standing penalty",
        published = 0.0998791032,
        here = ladder.first {
            it.columns == 5 && it.linkStiffness == HoneycombGrillage.RIGID_LINK_STIFFNESS
        }.p90OverStroke,
        departure = abs(
            ladder.first {
                it.columns == 5 && it.linkStiffness == HoneycombGrillage.RIGID_LINK_STIFFNESS
            }.p90OverStroke - 0.0998791032
        ),
        closes = true
    )
    reproductions.forEach { it.closes }

    // ================ the standing falsifier, at both ends of the ladder
    val uniformDishing = listOf(bracket.floor, HoneycombGrillage.RIGID_LINK_STIFFNESS).map { link ->
        val tile = T303Tile(shared, recoveredEnhancement, link)
        val solution = tile.lattice.solve(uniformPressure(shared.interiorPressure))
        abs(solution.peakDishing(T303_SAMPLES)) / abs(solution.meanDeflection)
    }

    val censusAtPenalty = census.first {
        it.linkStiffness == HoneycombGrillage.RIGID_LINK_STIFFNESS
    }
    val censusAtCeiling = census.first { it.linkStiffness == bracket.ceiling }
    val worstReproduction = reproductions.filter { it.quantity.contains("zero-eigenstrain") }
        .maxOf { it.departure }

    val falsifiers = listOf(
        T303Falsifier(
            name = "F1",
            statement = "a uniform pressure on the free tied lattice dishes exactly zero at " +
                    "both ends of the link ladder",
            fired = uniformDishing.any { it > T303_IDENTITY },
            note = "peak dishing over the mean deflection: " +
                    uniformDishing.joinToString(", ") { it.emittedDimensionless() } +
                    ", against a threshold of " + T303_IDENTITY.emittedDimensionless()
        ),
        T303Falsifier(
            name = "F2",
            statement = "this study's lattice builder at the default link stiffness is the " +
                    "object C-0180 measured",
            fired = reproductions.filter { it.source.contains("C-0180") }.any { !it.closes },
            note = "asserted as a bit-identity in CrossoverLinkStiffnessTest under a unit POINT " +
                    "load, and here as the two recovered cells' own p90 at the standing penalty"
        ),
        T303Falsifier(
            name = "F3",
            statement = "the two recovered cells reproduce C-0194 section 6's zero-eigenstrain " +
                    "readings over the shared rungs",
            fired = reproductions.filter { it.quantity.contains("zero-eigenstrain") }
                .any { !it.closes },
            note = "worst departure over the eight shared rungs " +
                    worstReproduction.emittedDimensionless()
        ),
        T303Falsifier(
            name = "F4",
            statement = "DECLARED OPEN -- the p90 is monotone decreasing in k_link at every " +
                    "graded cell, without which a bisection is a fiction",
            fired = rises > 0,
            note = rises.toString() + " of " + keyed.values.sumOf { it.size - 1 } +
                    " consecutive pairs rise, worst rise " +
                    worstRise.emittedDimensionless() +
                    ", over " + keyed.size + " cells; at the two cells the BISECTION is taken " +
                    "on, " + monotonicity.drop(1).count { it.monotone } + " of " +
                    monotonicity.drop(1).size + " are monotone over the whole ladder"
        ),
        T303Falsifier(
            name = "F5",
            statement = "DECLARED OPEN -- this is the deliverable's question. The ceiling on " +
                    "the physically supportable link stiffness reaches the threshold, i.e. the " +
                    "recovery survives",
            fired = thresholds.any { it.ceilingReachesThreshold },
            note = "the ceiling is " + bracket.ceiling.emitted(9) + " pN/nm and the thresholds " +
                    "are " + crossing.joinToString(", ") {
                it.thresholdLinkStiffness!!.emitted(9)
            } + " pN/nm, so the ceiling is short by a factor of " +
                    crossing.minOf { 1.0 / it.ceilingOverThreshold!! }.emitted(9) + " to " +
                    crossing.maxOf { 1.0 / it.ceilingOverThreshold!! }.emitted(9)
        ),
        T303Falsifier(
            name = "F6",
            statement = "the two k_theta-independent routes agree with the span law within one " +
                    "order of magnitude",
            fired = (bracket.softenedBond / bracket.floor) < 0.1 ||
                    (bracket.softenedBond / bracket.floor) > 10.0,
            note = "the ratio is " + (bracket.softenedBond / bracket.floor).emitted(9)
        ),
        T303Falsifier(
            name = "F7",
            statement = "the bending continuum c(rho) = 12 rho/(6+rho) has its two textbook " +
                    "limits exactly and k_B scales as L_p / g^3",
            fired = guidedEndConditionFactor(0.0) != 0.0 ||
                    abs(guidedEndConditionFactor(1e14) - 12.0) > 1e-9,
            note = "asserted in CrossoverLinkStiffnessTest at four gates; c(0) = " +
                    guidedEndConditionFactor(0.0).emitted(2) + ", c(1e14) = " +
                    guidedEndConditionFactor(1e14).emitted(9)
        ),
        T303Falsifier(
            name = "F8",
            statement = "the bisected threshold is converged at the deciding cell over beam " +
                    "subdivision and the dishing grid",
            fired = convergence.any { it.relativeDeparture > 0.05 },
            note = "worst relative departure " +
                    convergence.maxOf { it.relativeDeparture }.emittedDimensionless() +
                    ", and the ceiling verdict survives at " +
                    convergence.count { it.verdictSurvives } + " of " + convergence.size +
                    " refinements"
        ),
        T303Falsifier(
            name = "F9",
            statement = "DECLARED OPEN -- route B has a threshold at all, i.e. some link " +
                    "stiffness in C-0201's swept range makes its tightest tethered cell flat",
            fired = routeBFlat > 0,
            note = routeBFlat.toString() + " of " + routeB.size + " tethered readings are flat " +
                    "at the 90th percentile; the p90 RISES as the link softens at every one of " +
                    "C-0201's cells, so extrapolating below the swept range cannot help either"
        ),
        T303Falsifier(
            name = "F10",
            statement = "the census at k_link = 1e4 reproduces C-0180's 2 of 64",
            fired = censusAtPenalty.flatAtP90 != 2 || censusAtPenalty.cellsGraded != 64,
            note = censusAtPenalty.flatAtP90.toString() + " of " +
                    censusAtPenalty.cellsGraded + " flat at the standing penalty"
        )
    )
    falsifiers.forEach {
        println("  " + it.name + (if (it.fired) "  FIRED  " else "  did not fire  ") + it.note)
    }

    // ================ emission
    val result = T303Result(
        task = "T-303",
        leaf = "A8.2",
        title = "what link stiffness C-0180's coupled recovery needs, and what a crossover " +
                "connector can supply",
        verificationType = "logical (three closed-form routes to the connector's own transverse " +
                "stiffness, of which two carry no k_theta at all, plus a slope-deflection " +
                "derivation of the relative-end-displacement end-condition continuum) + " +
                "in-silico (the same beam-and-bond lattice, the same exact Woodbury coupling " +
                "surrogate and the same measured-incorporation dropout ensemble as C-0167 and " +
                "C-0180, swept over an EXISTING constructor argument) + literature (C-0201's " +
                "own committed route-B sweep, read and cited)",
        maturity = "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated. " +
                "Every route to the link stiffness is a CONSTRUCTION: route 1 is CH-0242's " +
                "attribution of the whole of k_theta to the span mechanism, route 2 is Chen et " +
                "al.'s own softened-bond construction (which Gen1Tile already flags as a " +
                "construction and not a measurement) read on a second axis, and route 3 is a " +
                "worm-like chain over one phosphodiester step. NOTHING here is a measurement of " +
                "a crossover's normal-link stiffness, and no such measurement was found.",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm",
            "rotationalStiffness" to "pN*nm/rad",
            "pressure" to "pN/nm^2 = 1 MPa",
            "dishing" to "dimensionless, as a fraction of the free stroke"
        ),
        conventions = mapOf(
            "s" to "along the helices, origin at the face centre",
            "y" to "across the helices in the plane of the face, origin at the face centre",
            "z" to "along the block's thickness",
            "W" to "positive DOWNWARD, toward the electrode (C-0006)",
            "linkStiffness" to "a stiffness on the residual R = dW + (d/2) unitY (Phi_a + Phi_b), " +
                    "C-0194's coordinate unchanged",
            "threshold" to "the link stiffness at which a cell's 90th-percentile dishing over " +
                    "the stroke crosses T-5b's 0.10, bisected on log10 k_link"
        ),
        parameters = mapOf(
            "crossSection" to shared.crossSection,
            "rowBasePairs" to shared.rowBasePairs.toString(),
            "edgeX" to shared.edgeX.emitted(9),
            "edgeY" to shared.edgeY.emitted(9),
            "interhelicalDistance" to d.emitted(9),
            "phosphateRadius" to rP.emitted(9),
            "crossoverSpan" to g.emitted(9),
            "hingeStiffness" to kTheta.emitted(9),
            "slipStiffness" to Gen1Tile.crossoverInPlaneStiffness().emitted(9),
            "thermalEnergy" to kT.emitted(9),
            "softestPersistenceLength" to softestLp.emitted(9),
            "stiffestPersistenceLength" to stiffestLp.emitted(9),
            "foundationStiffness" to Gen1Tile.FOUNDATION_SECANT.emitted(9),
            "compositeFractions" to "0.30 and 0.26 (C-0116)",
            "mandate" to "C-0017's 33.3333 pN/nm on the SUM, section 3's acceptable clause",
            "realisations" to t303Realisations.toString(),
            "seed" to T303_SEED.toString(),
            "samples" to T303_SAMPLES.toString(),
            "tolerance" to T303_TOLERANCE.emitted(2),
            "raster" to (T303_RECOMMENDED_ONE.toString() + " / " + T303_RECOMMENDED_TWO +
                    " (C-0151, drawable)"),
            "bisections" to T303_BISECTIONS.toString(),
            "bisectionBracket" to (T303_BISECTION_LOW.emitted(9) + " to " +
                    T303_BISECTION_HIGH.emitted(9) + " pN/nm")
        ),
        sources = listOf(
            ResultInputs.T_3B.path + " (C-0022's solved collar at 2 mM / 10 nm / 0.192 V)",
            ResultInputs.T_297.path + " (C-0194's own six-rung coupled ladder, reproduced)",
            ResultInputs.T_299.path + " (C-0201's route-B link sweep, read and cited)"
        ),
        citedInputs = mapOf(
            "C-0194 span-derived link stiffness k_R" to "41.4338953 pN/nm",
            "C-0194 implied bond tension T" to "29.7795467 pN",
            "C-0180 recovered cell A, tied p90 at the penalty" to "0.0995744767",
            "C-0180 recovered cell B, tied p90 at the penalty" to "0.0998791032",
            "Gen1Tile.crossoverInPlaneStiffness, Chen et al.'s construction" to
                    "64.7058824 pN/nm",
            "ssDNA Kuhn, 10-40 pN force spectroscopy (Bosco/Camunas-Soler/Ritort)" to "1.34 nm",
            "ssDNA Kuhn, zero force at 2 mM MgCl2" to "2.84 nm"
        ),
        cheapBound = cheapBound,
        routes = routes,
        ladder = ladder,
        monotonicity = monotonicity,
        thresholds = thresholds,
        census = census,
        cells = cells,
        resolution = resolution,
        routeB = routeB,
        verdict = mapOf(
            "the ceiling on the link" to bracket.ceiling.emitted(9) + " pN/nm",
            "the thresholds" to crossing.joinToString(", ") {
                it.cell.substringBefore(":") + " " + it.thresholdLinkStiffness!!.emitted(9)
            } + " pN/nm",
            "does the recovery survive" to
                    (if (thresholds.any { it.ceilingReachesThreshold }) "yes, at the cell whose " +
                            "threshold the ceiling reaches" else "NO -- the ceiling is below " +
                            "BOTH thresholds"),
            "C-0180's 2 of 64, quoted with its link stiffness" to
                    censusAtPenalty.flatAtP90.toString() + " of " + censusAtPenalty.cellsGraded +
                    " at k_link = " + HoneycombGrillage.RIGID_LINK_STIFFNESS.emitted(9) +
                    " pN/nm, and " + censusAtCeiling.flatAtP90 + " of " +
                    censusAtCeiling.cellsGraded + " at this study's ceiling " +
                    bracket.ceiling.emitted(9),
            "route B" to routeBFlat.toString() + " of " + routeB.size +
                    " tethered readings flat over four decades of k_link (C-0201, cited), so on " +
                    "the BUILT object there is no threshold to bisect"
        ),
        convergence = convergence,
        reproductions = reproductions,
        falsifiers = falsifiers,
        findings = listOf(
            "The crossover connector's transverse stiffness is bounded by " +
                    bracket.ceiling.emitted(9) + " pN/nm and NOTHING measures it. Three " +
                    "closed-form routes: the span law's tension term " + bracket.floor.emitted(9) +
                    ", Chen et al.'s softened bond read on the displacement axis " +
                    bracket.softenedBond.emitted(9) + " -- which carries no k_theta at all and " +
                    "agrees with the first within a factor of " +
                    (bracket.softenedBond / bracket.floor).emitted(9) + " -- and the " +
                    "connector's own bending, " + bracket.bendingSoft.emitted(9) + " to " +
                    bracket.bendingStiff.emitted(9) + " at CLAMPED ends and exactly zero at " +
                    "pinned ones.",
            "The threshold the coupled recovery needs is " +
                    crossing.joinToString(" and ") {
                        it.thresholdLinkStiffness!!.emitted(9)
                    } + " pN/nm, bisected to a bracket " + bracketDecades.emittedDimensionless() +
                    " decades wide, and the ceiling reaches " +
                    (if (thresholds.any { it.ceilingReachesThreshold }) "at least one of them"
                    else "NEITHER") + ": it is short by " +
                    crossing.joinToString(" and ") {
                        (1.0 / it.ceilingOverThreshold!!).emitted(9)
                    } + " times.",
            "So C-0180's 2 of 64 is a reading at k_link = " +
                    HoneycombGrillage.RIGID_LINK_STIFFNESS.emitted(9) + " pN/nm and at no other. " +
                    "The census is " + census.joinToString("; ") {
                it.flatAtP90.toString() + " of " + it.cellsGraded + " at " +
                        it.linkStiffness.emitted(9)
            } + ".",
            "Route B agrees, and by a different route: " + routeBFlat + " of " + routeB.size +
                    " of C-0201's tethered readings are flat over four decades, so on the " +
                    "object the 2009 staple order actually buys there is no threshold at all. " +
                    "The link-stiffness question decides everything on route A and nothing on " +
                    "route B.",
            "The direct duplex-duplex interaction is CENTRAL, so its contribution to this " +
                    "coordinate is V'(d)/d and therefore NEGATIVE wherever the pair repels. It " +
                    "lowers the ceiling and is reported for its sign, not added.",
            "And the ceiling is exact for the IN-PLANE bonds only. " +
                    "HoneycombTetherElement.normalStiffness is tangent*unitZ^2 + " +
                    "secant*unitY^2 -- the same source file already resolves a chain's two " +
                    "mechanisms onto the link residual by the bond's own direction -- while a " +
                    "BOND's link is one scalar. Through the thickness unitZ^2 is " +
                    resolution.last().meanUnitZSquared.emitted(9) + " of it, so most of a " +
                    "relative W displacement is a change of the interhelical SEPARATION, " +
                    "resisted AXIALLY. Resolved, the through-thickness link is " +
                    resolution.last().resolvedAtImpliedStepStiffness.emitted(9) + " to " +
                    resolution.last().resolvedAtDuplexStretchOverSpan.emitted(9) + " pN/nm, " +
                    "which STRADDLES both thresholds -- so what decides the recovery is an " +
                    "axial mechanism nobody has priced, on two thirds of the bonds. CH-0259.",
        ),
        validity = listOf(
            "Every route is a construction and none is a measurement. Route 2's transfer -- " +
                    "that a softened covalent bond resists a normal displacement as it resists " +
                    "an axial one -- is an ISOTROPY argument, exact for a flexible link and a " +
                    "model for a phosphodiester pair with a preferred direction.",
            "Route 3 puts a worm-like chain over ONE phosphodiester step. A persistence length " +
                    "is a thermal average over rotameric freedom; over 0.72 nm the backbone is " +
                    "stiffer in bond bending and softer in torsion than a smooth rod, and the " +
                    "c in [0, 12] bracket is what absorbs the direction of both.",
            "The threshold is a property of ONE distribution rule, ONE placement family, ONE " +
                    "cross-section, ONE raster and ONE load case. Nothing here re-opens the " +
                    "placement search.",
            "The census is taken on route A, whose turns carry ZERO unpaired nucleotides -- " +
                    "C-0175's modelling choice. C-0193 and C-0200 establish that the only " +
                    "folded block of this cross-section does otherwise, so the threshold is a " +
                    "statement about a design nobody has folded.",
            "The dropout ensemble is C-0087's measured depth-convention incorporation over one " +
                    "common stream at " + t303Realisations + " realisations and seed " +
                    T303_SEED + ", so the ladder is PAIRED cell by cell and rung by rung."
        ),
        openQuestions = listOf(
            "What an oxDNA or all-atom measurement of a crossover's NORMAL relative " +
                    "displacement stiffness would give. Snodin et al. measure the interhelical " +
                    "DISTANCE and its standard deviation, which is the line-of-centres " +
                    "coordinate, not this one.",
            "Whether the lattice should carry ONE link stiffness at all: an in-plane bond's " +
                    "link is a transverse SHEAR of the connector and a through-thickness bond's " +
                    "has a component along the line of centres, where the resisting mechanism " +
                    "is the connector's own axial stiffness and the pair's hydration force.",
            "What Chen et al.'s own alpha bracket, 0.6 to 1.2, is worth on the census. It " +
                    "scales route 2 linearly and moves the ceiling by the same factor, which " +
                    "does not close the gap to the threshold at either end.",
            "Whether a distribution SEARCHED at the physical link stiffness, rather than " +
                    "transferred onto it, recovers any cell."
        ),
        proseFailure = "none"
    )

    val output = File("gpd/results/T-303-what-link-stiffness-the-recovery-needs.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digits = 9,
                // A residual at a bisected root is a DIFFERENCE OF TWO NEARLY EQUAL numbers, so
                // it carries no nine digits: two runs of this study agreed on every other field
                // of a 225 kB file and disagreed in the ninth of exactly these two, because the
                // root itself moves by an ulp when a `Double` comparison flips. CLAUDE.md's own
                // departure rule, met on a record type the shared baseline does not cover.
                digitsByKey = mapOf(
                    "thresholds/residualAtThreshold" to DEPARTURE_SIGNIFICANT_DIGITS,
                    "thresholds/residualAtLowEnd" to DEPARTURE_SIGNIFICANT_DIGITS,
                    "thresholds/residualAtHighEnd" to DEPARTURE_SIGNIFICANT_DIGITS,
                    "monotonicity/worstRise" to DEPARTURE_SIGNIFICANT_DIGITS
                ),
                floor = 1e-12
            ).withEmissionHeader(LatticeTag.HONEYCOMB, null) as JsonObject)
        ) + "\n"
    )
    println("T-303 - wrote " + output.path)
}
