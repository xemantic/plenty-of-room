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
import com.xemantic.nano.plentyofroom.coupling.CollarTerm
import com.xemantic.nano.plentyofroom.coupling.DropoutEnsemble
import com.xemantic.nano.plentyofroom.coupling.InfluenceSurrogate
import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.coupling.dropoutDishingSample
import com.xemantic.nano.plentyofroom.coupling.dropoutEnsemble
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.coupling.measuredDepthIncorporation
import com.xemantic.nano.plentyofroom.coupling.summariseDropoutDishing
import com.xemantic.nano.plentyofroom.electrostatics.MengMagnesium
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.DEPARTURE_SIGNIFICANT_DIGITS
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.PointLoad
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
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow

// ---------------------------------------------------------------------------------------------
// T-310 -- a bond's normal link is TWO mechanisms, and HoneycombGrillage carried one scalar.
//
// W is the deflection normal to the face. At an in-plane bond unitZ = 0 and a relative W
// displacement is a pure transverse SHEAR of the connector, where C-0205's ceiling of
// 254.808095 pN/nm is exact. At the 300 of 435 bonds that run through the thickness
// unitZ^2 = 0.75 and three quarters of it is a change of the interhelical SEPARATION -- the
// RADIAL coordinate, which neither CH-0242 nor C-0194 nor C-0205 prices.
//
// The cheap half is arithmetic plus one derivative of a function already in the tree: the pair's
// own V''(d), from MengMagnesium's MEASURED osmotic-stress law, which is the first term on
// either axis of this problem that is not a construction.
// ---------------------------------------------------------------------------------------------

private const val T310_SAMPLES: Int = 81
private const val T310_TOLERANCE: Double = 0.10
private const val T310_RIM_STANDOFF: Double = 1.0
private const val T310_RIM_BAND: Double = 6.7
private const val T310_SEED: Long = 197_197L
private const val T310_BLOCK_EXTENT_BP: Int = 116
private const val T310_LADDER_PHASE: Int = 16
private const val T310_LADDER_OFFSET: Int = 14
private const val T310_RECOMMENDED_ONE: Int = 102
private const val T310_RECOMMENDED_TWO: Int = 109
private const val T310_BISECTIONS: Int = 16
private const val T310_CONVERGENCE_BISECTIONS: Int = 10

/** `C-0205`'s own ceiling, in pN/nm — the TRANSVERSE constant every resolution is read at. */
private const val T310_SHEAR_CEILING: Double = 254.80809548301096

/** The interface one honeycomb crossover owns, in base pairs — the lattice's own period. */
private const val T310_CONTACT_BP: Double = 21.0

/** The radial bisection bracket, in pN/nm. */
private const val T310_BISECTION_LOW: Double = 10.0
private const val T310_BISECTION_HIGH: Double = 1_000_000.0

/** The study runs at 4 000 realisations; `T310_SMOKE=1` drops it to 150 for a plumbing pass. */
private val t310Realisations: Int =
    if (System.getenv("T310_SMOKE") == "1") 150 else 4000

private fun Double.emitted(digits: Int = 9): String = roundedForProse(digits).toString()

// ------------------------------------------------------------------------------ the records

@Serializable
private class T310CheapBoundRow(
    val question: String,
    val answer: String,
    val value: Double,
    val units: String,
    val consequence: String
)

@Serializable
private class T310RadialRoute(
    val route: String,
    val premise: String,
    val measuredOrConstructed: String,
    val stiffness: Double,
    val note: String
)

@Serializable
private class T310ResolutionRow(
    val bondDirection: String,
    val bonds: Int,
    val meanUnitZSquared: Double,
    val meanUnitYSquared: Double,
    val transverseConstant: Double,
    val radialConstant: Double,
    val resolvedLinkStiffness: Double,
    val note: String
)

@Serializable
private class T310LadderRow(
    val cell: String,
    val columns: Int,
    val pathCount: Int,
    val radialLinkStiffness: Double,
    val ground: String,
    val throughThicknessLink: Double,
    val inPlaneLink: Double,
    val p90OverStroke: Double,
    val nominalOverStroke: Double,
    val flatAtP90: Boolean
)

@Serializable
private class T310MonotonicityRow(
    val cell: String,
    val cellsChecked: Int,
    val risesWithRadialStiffness: Int,
    val worstRise: Double,
    val monotone: Boolean
)

@Serializable
private class T310Threshold(
    val cell: String,
    val columns: Int,
    val pathCount: Int,
    val subdivisions: Int,
    val bisections: Int,
    val thresholdRadialStiffness: Double?,
    val straddles: Boolean,
    val residualAtLowEnd: Double,
    val residualAtHighEnd: Double,
    val bracketWidthDecades: Double,
    val residualAtThreshold: Double?,
    val insideTheBracket: Boolean,
    val bracketFloorOverThreshold: Double?,
    val bracketCeilingOverThreshold: Double?,
    val note: String
)

@Serializable
private class T310Cell(
    val radialLinkStiffness: Double,
    val throughThicknessLink: Double,
    val compositeFraction: Double,
    val hingeStiffnessEnhancement: Double,
    val placement: String,
    val columns: Int,
    val rows: Int,
    val pathCount: Int,
    val distribution: String,
    val nominalOverStroke: Double,
    val p90OverStroke: Double,
    val flatAtNominal: Boolean,
    val flatAtP90: Boolean,
    val uncoupledDishingOverStroke: Double,
    val beatsUncoupledAtP90: Boolean
)

@Serializable
private class T310CensusRow(
    val radialLinkStiffness: Double,
    val throughThicknessLink: Double,
    val ground: String,
    val cellsGraded: Int,
    val flatAtP90: Int,
    val flatAtNominal: Int,
    val tightestP90: Double,
    val tightestCell: String
)

@Serializable
private class T310RouteBRow(
    val cell: String,
    val linkStiffness: Double,
    val p90OverStroke: Double,
    val flatAtP90: Boolean,
    val source: String
)

@Serializable
private class T310Convergence(
    val axis: String,
    val quantity: String,
    val cell: String,
    val coarse: Double,
    val fine: Double,
    val departure: Double,
    val verdictMoves: Boolean,
    val note: String
)

@Serializable
private class T310Reproduction(
    val statement: String,
    val published: Double,
    val here: Double,
    val relativeDeparture: Double,
    val source: String
)

@Serializable
private class T310Falsifier(
    val id: String,
    val statement: String,
    val declaredOpen: Boolean,
    val fired: Boolean,
    val note: String
)

@Serializable
private class T310Result(
    val task: String,
    val claim: String,
    val leaf: String,
    val question: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: Map<String, String>,
    val parameters: Map<String, String>,
    val sources: List<String>,
    val citedInputs: Map<String, String>,
    val cheapBound: List<T310CheapBoundRow>,
    val radialRoutes: List<T310RadialRoute>,
    val resolution: List<T310ResolutionRow>,
    val ladder: List<T310LadderRow>,
    val monotonicity: List<T310MonotonicityRow>,
    val thresholds: List<T310Threshold>,
    val census: List<T310CensusRow>,
    val cells: List<T310Cell>,
    val routeB: List<T310RouteBRow>,
    val verdict: Map<String, String>,
    val convergence: List<T310Convergence>,
    val reproductions: List<T310Reproduction>,
    val falsifiers: List<T310Falsifier>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val proseFailure: String
)

// ------------------------------------------------------------------------------ the geometry

private class T310Profile(
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {
    fun field(interiorPressure: Double, edgeX: Double, edgeY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, edgeX, edgeY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T310_RIM_STANDOFF))
        )
}

private fun t310Profile(file: File): T310Profile {
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
    return T310Profile(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

/** `C-0167`/`C-0180`/`C-0205`'s geometry, unchanged, so the only thing that moves is the link. */
private class T310Shared(val profile: T310Profile) {
    val rasterRows: Int = 10
    val helicesPerRow: Int = 6
    val rowBasePairs: Int = T310_BLOCK_EXTENT_BP
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

/** One `(composite fraction, radial constant)` pair — one factorisation, many back-substitutions. */
private class T310Tile(
    val shared: T310Shared,
    val enhancement: Double,
    val radial: Double?,
    val subdivisions: Int = 1
) {

    val lattice: HoneycombGrillage = honeycombTiedLatticeAtResolvedLink(
        block = shared.block,
        rowBasePairs = shared.rowBasePairs,
        enhancement = enhancement,
        tied = true,
        transverseLinkStiffness =
            if (radial == null) HoneycombGrillage.RIGID_LINK_STIFFNESS else T310_SHEAR_CEILING,
        radialLinkStiffness = radial,
        subdivisions = subdivisions
    )

    val freeStroke: Double by lazy {
        lattice.solve(uniformPressure(shared.interiorPressure)).meanDeflection
    }

    val uncoupledDishing: Double by lazy {
        lattice.solve(shared.pressureField).peakDishing(T310_SAMPLES) / freeStroke
    }

    fun surrogate(grid: List<Pair<Double, Double>>, samples: Int = T310_SAMPLES):
            InfluenceSurrogate = honeycombTiedSurrogate(
        lattice, grid, shared.pressureField, samples
    )
}

private fun t310Distributions(
    grid: List<Pair<Double, Double>>,
    edgeX: Double,
    edgeY: Double
): List<Pair<String, List<Double>>> = listOf(
    "equal springs" to equalShareOfMandate(grid.size),
    "rim-graded 5:1" to rimGradedShareOfMandate(
        grid.map { (x, y) ->
            val onRim = abs(x) > edgeX / 2.0 - T310_RIM_BAND || abs(y) > edgeY / 2.0 - T310_RIM_BAND
            if (onRim) 5.0 else 1.0
        }
    )
)

/** `C-0167`'s four placements, unchanged. */
private fun t310Placements(
    shared: T310Shared,
    rootingHelixY: List<Double>,
    columns: Int
): List<Pair<String, List<Pair<Double, Double>>>> {
    val abstract = attachmentGrid(columns, shared.rasterRows, shared.edgeX, shared.edgeY)
    val raster = twoLengthRaster(
        shared.rasterRows, shared.helicesPerRow, T310_RECOMMENDED_ONE, T310_RECOMMENDED_TWO
    )
    val determined = twoLengthSnappedGrid(
        raster, columns, shared.edgeY, T310_LADDER_PHASE, T310_LADDER_OFFSET
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

private class T310Graded(val nominal: Double, val p90: Double, val flat: Boolean)

private fun t310Grade(
    surrogate: InfluenceSurrogate,
    stiffnesses: List<Double>,
    freeStroke: Double,
    ensemble: DropoutEnsemble
): T310Graded {
    val nominal = surrogate.solve(stiffnesses).peakDishing / freeStroke
    val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
    sample.indices.forEach { sample[it] = sample[it] / freeStroke }
    val summary = summariseDropoutDishing(
        sample, nominal, ensemble.meanSurvivors, T310_TOLERANCE
    )
    return T310Graded(nominal, summary.p90, summary.flatAtP90)
}

private fun t310Published(file: File, block: String, key: String, columns: Int): Double =
    Json.parseToJsonElement(file.readText())
        .jsonObject.getValue(block).jsonArray.map { it.jsonObject }
        .first { it.getValue("columns").jsonPrimitive.content.toInt() == columns }
        .getValue(key).jsonPrimitive.content.toDouble()

// ------------------------------------------------------------------------------ the study

@Suppress("LongMethod", "ComplexMethod", "NestedBlockDepth")
fun main() {
    val shared = T310Shared(t310Profile(ResultInputs.T_3B.file()))
    val fractions = listOf(0.30, 0.26)
    val gradedColumns = listOf(1, 2, 3, 5)
    val d = Gen1Tile.INTERHELICAL_HONEYCOMB
    val rP = MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS
    val kTheta = Gen1Tile.crossoverHingeStiffness()
    val kT = thermalEnergy(ROOM_TEMPERATURE)
    val g = crossoverSpanFloor(d, rP)
    val eos = MengMagnesium.equationOfState
    val contact = T310_CONTACT_BP * Gen1Tile.RISE_PER_BASE_PAIR

    // ================ Deliverable 1 -- the cheap bound. Closed form, no solver.
    println("T-310 - the cheap bound: what the RADIAL constant can be")
    val radial = crossoverRadialLinkBracket(
        hingeStiffness = kTheta,
        phosphateRadius = rP,
        interhelicalDistance = d,
        relaxedStep = MeasuredBackbone.STEP_SOUTH,
        stretchModulus = Gen1Tile.DUPLEX_STRETCH_MODULUS,
        equationOfState = eos,
        contactLength = contact
    )
    val pairForce = centralPairForcePerCrossover(eos, d, contact)
    val impliedTension = impliedCrossoverBondTension(kTheta, rP)
    val pairTransverse = centralPairTransverseStiffness(eos, d, contact)

    val radialRoutes = listOf(
        T310RadialRoute(
            route = "the connector alone, at C-0194's implied phosphodiester-step stiffness",
            premise = "C-0194 section 4 derives T = 2 k_theta / r_P and cross-checks it against " +
                    "the amount the built span g stands above T-71's MEASURED C2'-endo step, " +
                    "implying a step stiffness. CH-0259 is what reads that number on the RADIAL " +
                    "axis, where it is the connector's own resistance to being stretched.",
            measuredOrConstructed = "constructed -- the tension is CH-0242's full attribution",
            stiffness = radial.connectorAtImpliedStep,
            note = "T = " + impliedTension.emitted(9) + " pN over an extension of " +
                    (g - MeasuredBackbone.STEP_SOUTH).emitted(9) + " nm"
        ),
        T310RadialRoute(
            route = "the connector alone, at the duplex stretch modulus over the span",
            premise = "RIGID_LINK_STIFFNESS's own KDoc prices 1e4 pN/nm against the duplex " +
                    "stretch modulus, so S/g is the corpus's own generous end: a phosphodiester " +
                    "connector is not a duplex and cannot be stiffer than one over the same span.",
            measuredOrConstructed = "constructed -- an upper bound rather than a candidate",
            stiffness = radial.connectorAtDuplexStretch,
            note = "S = " + Gen1Tile.DUPLEX_STRETCH_MODULUS.emitted(9) + " pN over g = " +
                    g.emitted(9) + " nm"
        ),
        T310RadialRoute(
            route = "the duplex PAIR's own repulsion, V''(d), over one crossover's interface",
            premise = "The pair interaction is CENTRAL, so the same tensor whose transverse " +
                    "eigenvalue C-0205 section 1b carried for its NEGATIVE sign has a radial " +
                    "eigenvalue V''(d), and for a repulsive decaying law that one is POSITIVE. " +
                    "MengMagnesium is the corpus's MEASURED Mg2+ DNA-DNA equation of state and " +
                    "f_par(d) = Pi(d) d / sqrt(3) is exact for a hexagonal array.",
            measuredOrConstructed = "MEASURED -- the only term on either axis that is",
            stiffness = radial.pairRadial,
            note = "Pi_R = " + MengMagnesium.REPULSION_AMPLITUDE.emitted(9) + " pN/nm^2, " +
                    "lambda = " + MengMagnesium.DECAY_LENGTH.emitted(9) + " nm, evaluated at d = " +
                    d.emitted(9) + " nm, ABOVE the fit's own " +
                    MengMagnesium.DATA_FLOOR.emitted(9) + " nm data floor, over " +
                    contact.emitted(9) + " nm of interface"
        )
    )
    radialRoutes.forEach { println("  " + it.route + " -> " + it.stiffness.emitted(9) + " pN/nm") }
    println("  radial bracket " + radial.floor.emitted(9) + " to " + radial.ceiling.emitted(9))

    val cheapBound = ArrayList<T310CheapBoundRow>()
    cheapBound += T310CheapBoundRow(
        question = "is the pair's radial term positive, where C-0205's transverse one is negative?",
        answer = "yes -- one central-force decomposition, two eigenvalues, and which one applies " +
                "is decided by the BOND'S OWN DIRECTION, which is exactly what a single scalar " +
                "cannot express",
        value = radial.pairRadial,
        units = "pN/nm",
        consequence = "the transverse eigenvalue evaluated at the same measured law is " +
                pairTransverse.emitted(9) + " pN/nm, so the two differ by " +
                (radial.pairRadial / abs(pairTransverse)).emitted(9) + " times AND in sign"
    )
    cheapBound += T310CheapBoundRow(
        question = "C-0205 quoted the transverse pair term per unit of force. What is it?",
        answer = "C-0205's -2.81545741 is -L/d and carries no force at all; evaluated at the " +
                "measured law the transverse term is " + pairTransverse.emitted(9) + " pN/nm",
        value = pairTransverse,
        units = "pN/nm",
        consequence = "added, it would LOWER C-0205's shear ceiling from " +
                T310_SHEAR_CEILING.emitted(9) + " to " +
                (T310_SHEAR_CEILING + pairTransverse).emitted(9) + " pN/nm, " +
                (T310_SHEAR_CEILING / (T310_SHEAR_CEILING + pairTransverse)).emitted(9) +
                " times; the ceiling stands as the generous reading and is not moved here"
    )
    cheapBound += T310CheapBoundRow(
        question = "do the pair's outward force and C-0194's implied inward tension agree?",
        answer = "the pair pushes " + pairForce.emitted(9) + " pN apart per crossover and the " +
                "connector's implied tension pulls " + impliedTension.emitted(9) + " pN together",
        value = pairForce / impliedTension,
        units = "dimensionless",
        consequence = "two constructions sharing no fitted constant -- a 2020 osmotic-stress " +
                "measurement and a 2014 fitted dihedral spring read through CH-0242's " +
                "attribution -- land within a factor of two of the force that holds a honeycomb " +
                "crossover at its built separation. Neither was made to"
    )
    cheapBound += T310CheapBoundRow(
        question = "how much of the link does the radial constant own?",
        answer = "unitZ^2 = 0.75 at the 300 bonds that run through the thickness and 0 at the " +
                "135 that lie in plane, so the radial constant owns three quarters of two " +
                "thirds of the lattice's links",
        value = 0.75 * 300.0 / 435.0,
        units = "dimensionless, the share of the block's links the radial constant carries",
        consequence = "C-0205's ceiling is exact on the other " +
                ((135.0 + 0.25 * 300.0) / 435.0).emitted(9) + " of it"
    )
    cheapBound.forEach { println("  " + it.question + " -> " + it.value.emitted(9)) }

    // ================ Deliverable 1b -- the resolution, on the lattice's own bonds
    println("T-310 - the resolution, on the lattice's own bonds")
    val probe = T310Tile(shared, shared.enhancementAt(0.30), null)
    val allBonds = probe.lattice.bonds
    val resolution = ArrayList<T310ResolutionRow>()
    listOf(
        "C-0194's implied phosphodiester-step stiffness, connector alone (CH-0259's own)" to
                radial.connectorAtImpliedStep,
        "the duplex stretch modulus over the span, connector alone (CH-0259's own)" to
                radial.connectorAtDuplexStretch,
        "the connector at the implied step PLUS the measured pair term" to radial.floor,
        "the connector at the duplex stretch PLUS the measured pair term" to radial.ceiling
    ).forEach { (label, constant) ->
        listOf(true, false).forEach { inPlane ->
            val here = allBonds.filter { it.inPlane == inPlane }
            val meanZ2 = here.map { it.unitZ * it.unitZ }.average()
            val meanY2 = here.map { it.unitY * it.unitY }.average()
            resolution += T310ResolutionRow(
                bondDirection = (if (inPlane) "in plane" else "through the thickness") +
                        ", at " + label,
                bonds = here.size,
                meanUnitZSquared = meanZ2,
                meanUnitYSquared = meanY2,
                transverseConstant = T310_SHEAR_CEILING,
                radialConstant = constant,
                resolvedLinkStiffness =
                    meanZ2 * constant + meanY2 * T310_SHEAR_CEILING,
                note = if (inPlane)
                    "unitZ is zero, so the resolved link is C-0205's shear ceiling EXACTLY, " +
                            "whatever the radial constant is"
                else "unitZ^2 = 0.75, so three quarters of a relative W displacement is a " +
                        "change of the interhelical separation"
            )
        }
    }
    resolution.forEach {
        println("  " + it.bondDirection + " -> " + it.resolvedLinkStiffness.emitted(9))
    }

    // ================ Deliverable 2 -- the two recovered cells over the RADIAL ladder
    println("T-310 - the two recovered cells over the radial ladder")
    val rootingHelixY = probe.lattice.faceBeams.map { probe.lattice.beamY[it] }
    val recoveredEnhancement = shared.enhancementAt(0.30)
    val incorporation = measuredDepthIncorporation(shared.edgeX, shared.edgeY)

    class T310Recovered(val label: String, val columns: Int, val placement: String)

    val recovered = listOf(
        T310Recovered("cell A", 3, "abstract grid"),
        T310Recovered("cell B", 5, "abstract grid on the rooting helices")
    )

    fun gridOf(r: T310Recovered): List<Pair<Double, Double>> =
        t310Placements(shared, rootingHelixY, r.columns).first { it.first == r.placement }.second

    fun gradeRecovered(
        r: T310Recovered,
        radialConstant: Double?,
        subdivisions: Int = 1,
        samples: Int = T310_SAMPLES
    ): T310Graded {
        val grid = gridOf(r)
        val tile = T310Tile(shared, recoveredEnhancement, radialConstant, subdivisions)
        val ensemble = dropoutEnsemble(
            grid.map { (x, y) -> incorporation.at(x, y) }, t310Realisations, T310_SEED
        )
        val stiffnesses = t310Distributions(grid, shared.edgeX, shared.edgeY)
            .first { it.first == "rim-graded 5:1" }.second
        return t310Grade(tile.surrogate(grid, samples), stiffnesses, tile.freeStroke, ensemble)
    }

    fun throughThickness(constant: Double) = 0.75 * constant + 0.25 * T310_SHEAR_CEILING

    val rungs = listOf(
        T310_SHEAR_CEILING to
                "the CONTROL -- radial = transverse = C-0205's own ceiling, which is C-0205's " +
                "own uniform rung and must reproduce its census row",
        radial.connectorAtImpliedStep to
                "CH-0259's own low candidate -- the connector at C-0194's implied step stiffness",
        radial.floor to "that connector candidate PLUS the measured pair term",
        radial.connectorAtDuplexStretch to
                "CH-0259's own high candidate -- the duplex stretch modulus over the span",
        radial.ceiling to "that connector candidate PLUS the measured pair term"
    ).sortedBy { it.first }

    val ladder = ArrayList<T310LadderRow>()
    recovered.forEach { r ->
        rungs.forEach { (constant, ground) ->
            val graded = gradeRecovered(r, constant)
            ladder += T310LadderRow(
                cell = r.label + ": f = 0.3, " + r.placement + ", " + r.columns + " x " +
                        shared.rasterRows + " = " + gridOf(r).size + " paths, rim-graded 5:1",
                columns = r.columns,
                pathCount = gridOf(r).size,
                radialLinkStiffness = constant,
                ground = ground,
                throughThicknessLink = throughThickness(constant),
                inPlaneLink = T310_SHEAR_CEILING,
                p90OverStroke = graded.p90,
                nominalOverStroke = graded.nominal,
                flatAtP90 = graded.flat
            )
        }
    }
    ladder.forEach {
        println("  " + it.cell.substringBefore(":") + "  k_radial " +
                it.radialLinkStiffness.emitted(9) + " -> p90 " + it.p90OverStroke.emitted(9) +
                (if (it.flatAtP90) "  FLAT" else "  not flat"))
    }

    // ================ Deliverable 3 -- the RADIAL threshold, bisected
    println("T-310 - the radial threshold, bisected on log10 k_radial")

    fun bisectRadial(r: T310Recovered, subdivisions: Int, iterations: Int): T310Threshold {
        val atLow = gradeRecovered(r, T310_BISECTION_LOW, subdivisions).p90 - T310_TOLERANCE
        val atHigh = gradeRecovered(r, T310_BISECTION_HIGH, subdivisions).p90 - T310_TOLERANCE
        val straddles = (atLow > 0.0) != (atHigh > 0.0)
        val root = if (!straddles) null else bisectLogLinkStiffnessThreshold(
            T310_BISECTION_LOW, T310_BISECTION_HIGH, iterations
        ) { gradeRecovered(r, it, subdivisions).p90 - T310_TOLERANCE }
        val width = (log10(T310_BISECTION_HIGH) - log10(T310_BISECTION_LOW)) /
                2.0.pow(iterations.toDouble())
        return T310Threshold(
            cell = r.label + ": f = 0.3, " + r.placement + ", " + r.columns + " x " +
                    shared.rasterRows + " = " + gridOf(r).size + " paths, rim-graded 5:1",
            columns = r.columns,
            pathCount = gridOf(r).size,
            subdivisions = subdivisions,
            bisections = iterations,
            thresholdRadialStiffness = root,
            straddles = straddles,
            residualAtLowEnd = atLow,
            residualAtHighEnd = atHigh,
            bracketWidthDecades = width,
            residualAtThreshold = root?.let {
                gradeRecovered(r, it, subdivisions).p90 - T310_TOLERANCE
            },
            insideTheBracket = root != null && root > radial.floor && root < radial.ceiling,
            bracketFloorOverThreshold = root?.let { radial.floor / it },
            bracketCeilingOverThreshold = root?.let { radial.ceiling / it },
            note = if (root == null)
                "the p90 does not cross T-5b's " + T310_TOLERANCE.emitted(2) + " anywhere in [" +
                        T310_BISECTION_LOW.emitted(9) + ", " + T310_BISECTION_HIGH.emitted(9) +
                        "] pN/nm of RADIAL stiffness, with the transverse constant pinned at " +
                        T310_SHEAR_CEILING.emitted(9)
            else "the p90 crosses T-5b's " + T310_TOLERANCE.emitted(2) + " at a radial " +
                    "constant of " + root.emitted(9) + " pN/nm, i.e. a through-thickness link " +
                    "of " + throughThickness(root).emitted(9) + " pN/nm"
        )
    }

    val thresholds = recovered.map { bisectRadial(it, 1, T310_BISECTIONS) }
    thresholds.forEach {
        println("  " + it.cell.substringBefore(":") + "  k*_radial = " +
                (it.thresholdRadialStiffness?.emitted(9) ?: "none in range") +
                (if (it.insideTheBracket) "  INSIDE the bracket" else ""))
    }
    val crossing = thresholds.filter { it.thresholdRadialStiffness != null }

    // ================ Deliverable 4 -- the whole 64-cell census, at every radial rung
    println("T-310 - the 64-cell census, " + t310Realisations + " realisations per cell")
    val cells = ArrayList<T310Cell>()
    val tiles = HashMap<Pair<Double, Double>, T310Tile>()
    rungs.forEach { (constant, _) ->
        fractions.forEach { fraction ->
            tiles[constant to fraction] = T310Tile(shared, shared.enhancementAt(fraction), constant)
        }
    }
    gradedColumns.forEach { columns ->
        t310Placements(shared, rootingHelixY, columns).forEach { (placement, grid) ->
            val ensemble = dropoutEnsemble(
                grid.map { (x, y) -> incorporation.at(x, y) }, t310Realisations, T310_SEED
            )
            val distributions = t310Distributions(grid, shared.edgeX, shared.edgeY)
            rungs.forEach { (constant, _) ->
                fractions.forEach { fraction ->
                    val tile = tiles.getValue(constant to fraction)
                    val surrogate = tile.surrogate(grid)
                    distributions.forEach { (label, stiffnesses) ->
                        val graded = t310Grade(
                            surrogate, stiffnesses, tile.freeStroke, ensemble
                        )
                        cells += T310Cell(
                            radialLinkStiffness = constant,
                            throughThicknessLink = throughThickness(constant),
                            compositeFraction = fraction,
                            hingeStiffnessEnhancement = tile.enhancement,
                            placement = placement,
                            columns = columns,
                            rows = shared.rasterRows,
                            pathCount = grid.size,
                            distribution = label,
                            nominalOverStroke = graded.nominal,
                            p90OverStroke = graded.p90,
                            flatAtNominal = graded.nominal < T310_TOLERANCE,
                            flatAtP90 = graded.flat,
                            uncoupledDishingOverStroke = tile.uncoupledDishing,
                            beatsUncoupledAtP90 = graded.p90 < tile.uncoupledDishing
                        )
                    }
                }
            }
        }
    }
    val census = rungs.map { (constant, ground) ->
        val here = cells.filter { it.radialLinkStiffness == constant }
        val tightest = here.minByOrNull { it.p90OverStroke }!!
        T310CensusRow(
            radialLinkStiffness = constant,
            throughThicknessLink = throughThickness(constant),
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
        println("  k_radial " + it.radialLinkStiffness.emitted(9) + " -> " + it.flatAtP90 +
                " of " + it.cellsGraded + " flat at p90, tightest " + it.tightestP90.emitted(9))
    }

    // ================ monotonicity, before anything is believed of the bisection
    val keyed = cells.groupBy {
        listOf(it.compositeFraction, it.placement, it.columns, it.distribution).joinToString("|")
    }
    var rises = 0
    var worstRise = 0.0
    keyed.values.forEach { group ->
        group.sortedBy { it.radialLinkStiffness }.zipWithNext().forEach { (soft, stiff) ->
            val rise = stiff.p90OverStroke - soft.p90OverStroke
            if (rise > 1e-9) {
                rises += 1
                worstRise = maxOf(worstRise, rise)
            }
        }
    }
    val monotonicity = ArrayList<T310MonotonicityRow>()
    monotonicity += T310MonotonicityRow(
        cell = "every cell of the census, over every consecutive pair of the radial ladder",
        cellsChecked = keyed.size,
        risesWithRadialStiffness = rises,
        worstRise = worstRise,
        monotone = rises == 0
    )
    recovered.forEach { r ->
        val here = ladder.filter { it.columns == r.columns }.sortedBy { it.radialLinkStiffness }
        var cellRises = 0
        var cellWorst = 0.0
        here.zipWithNext().forEach { (soft, stiff) ->
            val rise = stiff.p90OverStroke - soft.p90OverStroke
            if (rise > 1e-9) {
                cellRises += 1
                cellWorst = maxOf(cellWorst, rise)
            }
        }
        monotonicity += T310MonotonicityRow(
            cell = r.label + ", the cell the bisection is taken on",
            cellsChecked = 1,
            risesWithRadialStiffness = cellRises,
            worstRise = cellWorst,
            monotone = cellRises == 0
        )
    }

    // ================ Deliverable 5 -- route B, read out of C-0201's own committed artifact
    println("T-310 - route B, read and not re-run")
    val t299 = Json.parseToJsonElement(ResultInputs.T_299.file().readText())
        .jsonObject.getValue("linkStiffness").jsonArray.map { it.jsonObject }
    val routeB = t299.map {
        T310RouteBRow(
            cell = it.getValue("cell").jsonPrimitive.content,
            linkStiffness = it.getValue("linkStiffness").jsonPrimitive.content.toDouble(),
            p90OverStroke = it.getValue("p90OverStroke").jsonPrimitive.content.toDouble(),
            flatAtP90 = it.getValue("flatAtP90").jsonPrimitive.content.toBoolean(),
            source = ResultInputs.T_299.path + " (C-0201), read and not re-run"
        )
    }
    val routeBFlat = routeB.count { it.flatAtP90 }

    // ================ convergence -- the deciding quantity at the deciding cell
    println("T-310 - convergence, on the bisected radial threshold at the tighter cell")
    val decidingCell = recovered.minByOrNull { r ->
        thresholds.first { it.columns == r.columns }.thresholdRadialStiffness ?: Double.MAX_VALUE
    }!!
    // CLAUDE.md: a convergence axis cannot be read at a state where the quantity does not
    // exist, and the axis's state is picked AFTER the sweep. Where a threshold exists the
    // subdivision axis is taken on it; where none does, it is taken on the p90 at the radial
    // floor, which always exists, and the threshold rows are emitted with their own nulls.
    val convergence = ArrayList<T310Convergence>()
    val thresholdExists = thresholds.any { it.thresholdRadialStiffness != null }
    val coarseSubdivision = if (!thresholdExists) null
        else bisectRadial(decidingCell, 1, T310_CONVERGENCE_BISECTIONS)
    val fineSubdivision = if (!thresholdExists) null
        else bisectRadial(decidingCell, 2, T310_CONVERGENCE_BISECTIONS)
    val coarseRoot = coarseSubdivision?.thresholdRadialStiffness
    val fineRoot = fineSubdivision?.thresholdRadialStiffness
    if (coarseRoot != null && fineRoot != null) {
        convergence += T310Convergence(
            axis = "beam subdivisions per row, 1 -> 2",
            quantity = "the bisected RADIAL threshold, pN/nm",
            cell = decidingCell.label,
            coarse = coarseRoot,
            fine = fineRoot,
            departure = abs(fineRoot - coarseRoot) / coarseRoot,
            verdictMoves = (coarseRoot > radial.ceiling) != (fineRoot > radial.ceiling),
            note = "T-303's own F8 fired on the VALUE of the uniform-link threshold by 21 % " +
                    "over the same axis and not on the verdict"
        )
    }
    val subdivisionCoarse = gradeRecovered(decidingCell, radial.floor, 1, T310_SAMPLES)
    val subdivisionFine = gradeRecovered(decidingCell, radial.floor, 2, T310_SAMPLES)
    convergence += T310Convergence(
        axis = "beam subdivisions per row, 1 -> 2",
        quantity = "the 90th-percentile dishing over the stroke at the radial floor",
        cell = decidingCell.label,
        coarse = subdivisionCoarse.p90,
        fine = subdivisionFine.p90,
        departure = abs(subdivisionFine.p90 - subdivisionCoarse.p90) / subdivisionCoarse.p90,
        verdictMoves = subdivisionCoarse.flat != subdivisionFine.flat,
        note = "the deciding quantity where the threshold does not exist; T-303's own F8 " +
                "fired on the same axis by 21 % on the threshold and by nothing on the verdict"
    )
    val gridCoarse = gradeRecovered(decidingCell, radial.floor, 1, T310_SAMPLES)
    val gridFine = gradeRecovered(decidingCell, radial.floor, 1, 161)
    convergence += T310Convergence(
        axis = "dishing grid, 81 -> 161 samples per side",
        quantity = "the 90th-percentile dishing over the stroke at the radial floor",
        cell = decidingCell.label,
        coarse = gridCoarse.p90,
        fine = gridFine.p90,
        departure = abs(gridFine.p90 - gridCoarse.p90) / gridCoarse.p90,
        verdictMoves = gridCoarse.flat != gridFine.flat,
        note = "the same axis C-0205 took at 0.0"
    )
    convergence.forEach {
        println("  " + it.axis + "  " + it.coarse.emitted(9) + " -> " + it.fine.emitted(9))
    }

    // ================ reproductions
    val reproductions = ArrayList<T310Reproduction>()
    fun reproduce(statement: String, published: Double, here: Double, source: String) {
        reproductions += T310Reproduction(
            statement = statement,
            published = published,
            here = here,
            relativeDeparture = abs(here - published) / abs(published),
            source = source
        )
    }
    reproduce(
        "CH-0259's through-thickness link at C-0194's implied step stiffness",
        475.448622,
        0.75 * radial.connectorAtImpliedStep + 0.25 * T310_SHEAR_CEILING,
        "CH-0259, the number the challenge is written on"
    )
    reproduce(
        "CH-0259's through-thickness link at the duplex stretch modulus over the span",
        1211.56918,
        0.75 * radial.connectorAtDuplexStretch + 0.25 * T310_SHEAR_CEILING,
        "CH-0259"
    )
    reproduce(
        "C-0194's implied phosphodiester-step stiffness",
        548.995464,
        radial.connectorAtImpliedStep,
        "C-0194 section 4"
    )
    reproduce(
        "C-0194's implied crossover bond tension T = 2 k_theta / r_P",
        29.7795467,
        impliedTension,
        "C-0194 section 4"
    )
    reproduce(
        "C-0194's span-derived link stiffness k_R = T/g",
        41.4338953,
        spanDerivedLinkStiffness(kTheta, rP, d),
        "C-0194 section 4"
    )
    reproduce(
        "C-0205's shear ceiling on the transverse constant",
        254.808095,
        crossoverLinkStiffnessBracket(
            hingeStiffness = kTheta,
            phosphateRadius = rP,
            interhelicalDistance = d,
            thermalEnergy = kT,
            softestPersistenceLength = 1.34 / 2.0,
            stiffestPersistenceLength = 2.84 / 2.0
        ).ceiling,
        "C-0205 section 1"
    )
    reproduce(
        "C-0205's transverse pair term per unit of repulsive force per unit length",
        -2.81545741,
        centralPairForceTransverseStiffness(1.0, d, contact),
        "C-0205 section 1b"
    )
    reproduce(
        "the transverse constant this study pins, against C-0205's own bracket recomputed here",
        T310_SHEAR_CEILING,
        crossoverLinkStiffnessBracket(
            hingeStiffness = kTheta,
            phosphateRadius = rP,
            interhelicalDistance = d,
            thermalEnergy = kT,
            softestPersistenceLength = 1.34 / 2.0,
            stiffestPersistenceLength = 2.84 / 2.0
        ).ceiling,
        "this study's own constant against C-0205's own function"
    )
    val controlRow = census.first { it.radialLinkStiffness == T310_SHEAR_CEILING }
    reproduce(
        "C-0205's census row at its own ceiling -- the tightest p90 over all 64 cells, " +
                "reproduced through the per-bond code path at radial = transverse",
        0.100581834,
        controlRow.tightestP90,
        ResultInputs.T_303.path + " (C-0205) section 3, re-run"
    )
    val publishedCellA = t310Published(
        ResultInputs.T_303.file(), "thresholds", "thresholdLinkStiffness", 3
    )
    val publishedCellB = t310Published(
        ResultInputs.T_303.file(), "thresholds", "thresholdLinkStiffness", 5
    )
    reproduce(
        "C-0205's own UNIFORM-link threshold at cell A, read back out of its committed file",
        834.060958, publishedCellA, ResultInputs.T_303.path + " (C-0205), read"
    )
    reproduce(
        "C-0205's own UNIFORM-link threshold at cell B, read back out of its committed file",
        607.396049, publishedCellB, ResultInputs.T_303.path + " (C-0205), read"
    )

    // ================ falsifiers
    val defaultLattice = honeycombTiedLatticeAtLinkStiffness(
        shared.block, shared.rowBasePairs, recoveredEnhancement, tied = true
    )
    val resolvedDefault = honeycombTiedLatticeAtResolvedLink(
        shared.block, shared.rowBasePairs, recoveredEnhancement, tied = true
    )
    var bitIdentical = true
    for (i in 0 until defaultLattice.degreesOfFreedom) {
        for (j in maxOf(0, i - defaultLattice.bandwidth)..i) {
            if (defaultLattice.stiffnessEntry(i, j) != resolvedDefault.stiffnessEntry(i, j)) {
                bitIdentical = false
            }
        }
    }
    val sameSites = defaultLattice.bonds.map { it.site } == resolvedDefault.bonds.map { it.site }
    val uniformFree = honeycombTiedLatticeAtResolvedLink(
        shared.block, shared.rowBasePairs, recoveredEnhancement, tied = false,
        transverseLinkStiffness = T310_SHEAR_CEILING, radialLinkStiffness = radial.floor
    ).solve(uniformPressure(shared.interiorPressure))
    val uniformDishing = abs(uniformFree.peakDishing(T310_SAMPLES) / uniformFree.meanDeflection)
    val inPlaneBonds = allBonds.count { it.inPlane }
    val throughBonds = allBonds.count { !it.inPlane }
    val censusAtFloor = census.first { it.radialLinkStiffness == radial.floor }
    val censusAtCeiling = census.first { it.radialLinkStiffness == radial.ceiling }
    val anyFlat = census.sumOf { it.flatAtP90 }

    val falsifiers = listOf(
        T310Falsifier(
            id = "F1",
            statement = "the default per-bond lattice is not bit-identical to the standing " +
                    "object, or its crossover site set differs",
            declaredOpen = false,
            fired = !bitIdentical || !sameSites,
            note = "every band entry of a " + defaultLattice.degreesOfFreedom +
                    "-degree-of-freedom lattice compared with ==, and " +
                    defaultLattice.bonds.size + " sites compared"
        ),
        T310Falsifier(
            id = "F2",
            statement = "a uniform pressure on the free per-bond lattice does not dish exactly " +
                    "zero",
            declaredOpen = false,
            fired = uniformDishing > 1e-9,
            note = "peak dishing over the mean deflection is " + uniformDishing.emitted(2)
        ),
        T310Falsifier(
            id = "F3",
            statement = "the resolved per-bond link does not reproduce CH-0259's own two " +
                    "published readings, or C-0205's ceiling",
            declaredOpen = false,
            fired = reproductions.take(6).any { it.relativeDeparture > 1e-6 },
            note = "worst of the six construction reproductions is " +
                    reproductions.take(6).maxOf { it.relativeDeparture }.emitted(2)
        ),
        T310Falsifier(
            id = "F4",
            statement = "the bond census is not 135 in plane and 300 through the thickness, or " +
                    "the mean unitZ^2 is not 0 and 0.75",
            declaredOpen = false,
            fired = inPlaneBonds != 135 || throughBonds != 300 ||
                    allBonds.filter { it.inPlane }.any { abs(it.unitZ) > 1e-12 } ||
                    allBonds.filter { !it.inPlane }.any { abs(it.unitZ * it.unitZ - 0.75) > 1e-12 },
            note = inPlaneBonds.toString() + " in plane, " + throughBonds +
                    " through the thickness, of " + allBonds.size
        ),
        T310Falsifier(
            id = "F5",
            statement = "OPEN -- the census at the resolved per-bond link recovers cells " +
                    "C-0205's 0 of 64 refuses",
            declaredOpen = true,
            fired = anyFlat > 0,
            note = census.joinToString("; ") {
                it.flatAtP90.toString() + " of " + it.cellsGraded + " at k_radial = " +
                        it.radialLinkStiffness.emitted(9)
            }
        ),
        T310Falsifier(
            id = "F6",
            statement = "OPEN -- the bisected radial threshold falls INSIDE the radial bracket, " +
                    "so the corpus's own candidates do not decide the question",
            declaredOpen = true,
            fired = thresholds.any { it.insideTheBracket },
            note = thresholds.joinToString("; ") {
                it.cell.substringBefore(":") + " " +
                        (it.thresholdRadialStiffness?.emitted(9) ?: "none in range") +
                        " against the bracket " + radial.floor.emitted(9) + " to " +
                        radial.ceiling.emitted(9)
            }
        ),
        T310Falsifier(
            id = "F7",
            statement = "the measured pair radial term is not positive, or the equation of " +
                    "state is evaluated below its own data floor",
            declaredOpen = false,
            fired = radial.pairRadial < 0.0 || d < MengMagnesium.DATA_FLOOR,
            note = "V''(d) over one crossover's interface is " + radial.pairRadial.emitted(9) +
                    " pN/nm at d = " + d.emitted(9) + " nm, floor " +
                    MengMagnesium.DATA_FLOOR.emitted(9) + " nm"
        ),
        T310Falsifier(
            id = "F8",
            statement = "OPEN -- the pair's outward force per crossover and C-0194's implied " +
                    "inward tension disagree by more than one order of magnitude",
            declaredOpen = true,
            fired = pairForce / impliedTension > 10.0 || pairForce / impliedTension < 0.1,
            note = pairForce.emitted(9) + " pN outward against " + impliedTension.emitted(9) +
                    " pN inward, a ratio of " + (pairForce / impliedTension).emitted(9)
        ),
        T310Falsifier(
            id = "F9",
            statement = "OPEN -- the verdict at the deciding cell moves between beam " +
                    "subdivisions 1 and 2",
            declaredOpen = true,
            fired = convergence.any { it.verdictMoves },
            note = convergence.joinToString("; ") {
                it.quantity + " moves " + it.departure.emitted(2) + " relative"
            }
        ),
        T310Falsifier(
            id = "F10",
            statement = "any reproduction of C-0205, C-0194 or CH-0259 fails to close at the " +
                    "emission precision",
            declaredOpen = false,
            fired = reproductions.any { it.relativeDeparture > 1e-6 },
            note = "worst of " + reproductions.size + " reproductions is " +
                    reproductions.maxOf { it.relativeDeparture }.emitted(2)
        )
    )
    falsifiers.forEach { println("  " + it.id + (if (it.fired) "  FIRED" else "  did not fire")) }

    val verdict = mapOf(
        "the radial bracket" to radial.floor.emitted(9) + " to " + radial.ceiling.emitted(9) +
                " pN/nm, of which the measured pair term is " + radial.pairRadial.emitted(9),
        "the resolved through-thickness link" to
                throughThickness(radial.floor).emitted(9) + " to " +
                throughThickness(radial.ceiling).emitted(9) + " pN/nm, against " +
                T310_SHEAR_CEILING.emitted(9) + " in plane",
        "is there a radial stiffness that recovers each cell" to thresholds.joinToString("; ") {
            it.cell.substringBefore(":") + ": " + (
                if (it.thresholdRadialStiffness != null) "yes, at " +
                        it.thresholdRadialStiffness.emitted(9) + " pN/nm"
                else if (it.residualAtHighEnd > 0.0) "NO -- still not flat at a radial " +
                        "constant of " + T310_BISECTION_HIGH.emitted(9) + " pN/nm, so the 135 " +
                        "IN-PLANE bonds pinned at the shear ceiling forbid the recovery by " +
                        "themselves"
                else "already flat at " + T310_BISECTION_LOW.emitted(9) + " pN/nm")
        },
        "the radial threshold" to (if (crossing.isEmpty()) "no cell crosses T-5b in [" +
                T310_BISECTION_LOW.emitted(9) + ", " + T310_BISECTION_HIGH.emitted(9) + "]"
        else crossing.joinToString(", ") {
            it.cell.substringBefore(":") + " " + it.thresholdRadialStiffness!!.emitted(9)
        } + " pN/nm"),
        "does C-0205's 0 of 64 stand" to (if (anyFlat == 0)
            "YES -- 0 of " + census.first().cellsGraded + " at every radial rung of the bracket"
        else "NO -- it reverses; " + census.joinToString("; ") {
            it.flatAtP90.toString() + " of " + it.cellsGraded + " at k_radial = " +
                    it.radialLinkStiffness.emitted(9)
        }),
        "the control row, radial = transverse = C-0205's ceiling" to
                census.first { it.radialLinkStiffness == T310_SHEAR_CEILING }.flatAtP90
                    .toString() + " of " +
                census.first { it.radialLinkStiffness == T310_SHEAR_CEILING }.cellsGraded +
                " against C-0205's own 0 of 64 at the same rung",
        "the census at the radial floor" to censusAtFloor.flatAtP90.toString() + " of " +
                censusAtFloor.cellsGraded,
        "the census at the radial ceiling" to censusAtCeiling.flatAtP90.toString() + " of " +
                censusAtCeiling.cellsGraded,
        "route B" to routeBFlat.toString() + " of " + routeB.size +
                " of C-0201's own tethered readings are flat, over four decades of link " +
                "stiffness, and the p90 RISES as the link softens -- so the link decides " +
                "everything on route A and nothing on route B",
        "can the radial constant be bounded from a source" to
                "NOT as a crossover measurement -- C-0205's eight recorded queries found none " +
                "and this task adds none. What IS measured is the PAIR term, which is a term " +
                "of the radial constant and not the constant: MengMagnesium's osmotic-stress " +
                "law gives " + radial.pairRadial.emitted(9) + " pN/nm of it, " +
                (radial.pairRadial / radial.floor).emitted(9) + " of the bracket's floor"
    )

    val findings = listOf(
        "The link at a through-thickness bond is " + throughThickness(radial.floor).emitted(9) +
                " to " + throughThickness(radial.ceiling).emitted(9) + " pN/nm and at an " +
                "in-plane bond it is C-0205's shear ceiling " + T310_SHEAR_CEILING.emitted(9) +
                " EXACTLY, at every radial constant -- so a lattice carrying one scalar cannot " +
                "represent either state of the block.",
        "The RADIAL axis carries a MEASURED term and the transverse axis does not. The duplex " +
                "pair's own repulsion contributes V''(d) = " + radial.pairRadial.emitted(9) +
                " pN/nm over the 21 bp of interface one crossover owns, from MengMagnesium's " +
                "osmotic-stress equation of state at a separation ABOVE that fit's own " +
                MengMagnesium.DATA_FLOOR.emitted(9) + " nm data floor. C-0205 section 1b " +
                "carried the same tensor's TRANSVERSE eigenvalue, which is negative, and " +
                "quoted it per unit of force -- so it never evaluated it. Evaluated, that one " +
                "is " + pairTransverse.emitted(9) + " pN/nm.",
        "The pair's outward force per crossover is " + pairForce.emitted(9) + " pN against " +
                "C-0194's implied inward bond tension " + impliedTension.emitted(9) + " pN, a " +
                "ratio of " + (pairForce / impliedTension).emitted(9) + ". A 2020 " +
                "osmotic-stress measurement and a 2014 fitted dihedral spring read through " +
                "CH-0242's attribution agree, unprompted, within a factor of two about what " +
                "holds a honeycomb crossover at its built separation.",
        "The census at the resolved per-bond link reads " + census.joinToString("; ") {
            it.flatAtP90.toString() + " of " + it.cellsGraded + " at a radial constant of " +
                    it.radialLinkStiffness.emitted(9) + " pN/nm"
        } + ".",
        "The quantity the corpus needs is the RADIAL constant and not a uniform k_link: with " +
                "the transverse constant pinned at C-0205's own ceiling, the two cells C-0180 " +
                "recovered cross T-5b at " + (if (crossing.isEmpty()) "no radial constant in " +
                "the swept range" else crossing.joinToString(" and ") {
            it.thresholdRadialStiffness!!.emitted(9)
        } + " pN/nm") + ".",
        "Route B is unmoved and unresolved: " + routeBFlat + " of " + routeB.size + " of " +
                "C-0201's tethered readings are flat at any link stiffness it swept, so a link " +
                "stiffness decides everything on the design nobody has folded and nothing on " +
                "the one the 2009 staple order buys. What a resolved per-bond link does to " +
                "C-0207's 756 of 756, which is a reading at the standing penalty, is not " +
                "answered here."
    )

    val validity = listOf(
        "Every route to the RADIAL constant is a bound or a construction except one. The " +
                "connector candidates are C-0194's own attribution and an explicit upper bound " +
                "(a phosphodiester connector is not a duplex); only the pair term is measured, " +
                "and it is a TERM of the constant rather than the constant.",
        "The pair term is attributed over 21 bp of interface per crossover, which is the " +
                "honeycomb's own crossover period. The repulsion is continuous along the " +
                "interface and the lattice puts it at one node; that is the same convention " +
                "C-0205 section 1b used for the transverse half and it is a convention.",
        "MengMagnesium's fit is at 20 mM MgCl2 and this device's buffer is 2 mM. The short-range " +
                "repulsion the 0.24 nm decay length describes is hydration rather than " +
                "electrostatic, which is why the corpus uses it across buffers, but the " +
                "amplitude is not measured at 2 mM.",
        "The census is taken on ROUTE A, whose raster turns carry ZERO unpaired nucleotides -- " +
                "C-0175's modelling choice. C-0193 and C-0200 establish that the only folded " +
                "block of this cross-section does otherwise.",
        "The threshold is a property of one distribution rule, one placement family, one " +
                "cross-section, one raster and one load case, exactly as C-0205's is.",
        "The transverse constant is pinned at C-0205's ceiling throughout, which is its " +
                "GENEROUS reading: the measured pair term would lower it by " +
                (T310_SHEAR_CEILING / (T310_SHEAR_CEILING + pairTransverse)).emitted(9) + " times."
    )

    val openQuestions = listOf(
        "What an oxDNA or all-atom measurement of a crossover's stiffness against a change of " +
                "the interhelical SEPARATION would give. Snodin et al. measure that coordinate's " +
                "standard deviation along a 2D tile and report it only qualitatively, as " +
                "'significantly smaller' than the 1.5 nm weave amplitude -- so equipartition " +
                "gives a ceiling on the compliance with no number in it.",
        "What a resolved per-bond link does to C-0207's 756 of 756, which is a reading at " +
                "k_link = " + HoneycombGrillage.RIGID_LINK_STIFFNESS.emitted(9) + " pN/nm, and " +
                "whose direction C-0205 section 4 already knows: route B's p90 RISES as the " +
                "link softens.",
        "Whether the pair term belongs on the link at all or as its own element. It acts " +
                "continuously along the interface and the link acts at a node; a distributed " +
                "separation spring is a different element with a different influence function.",
        "Whether a distribution SEARCHED at the resolved per-bond link recovers any cell, which " +
                "C-0205 left open on the uniform axis and this task leaves open on the radial one."
    )

    val result = T310Result(
        task = "T-310",
        claim = "C-0208",
        leaf = "A8.2",
        question = "A crossover bond's normal link is two mechanisms -- a transverse shear of " +
                "the connector and a change of the interhelical separation -- and " +
                "HoneycombGrillage carried one scalar for both. What is the radial constant, " +
                "and what does C-0205's 0 of 64 read once the link is resolved by the bond's " +
                "own direction?",
        maturity = "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated. The " +
                "radial constant's connector candidates are constructions and its pair term is " +
                "a measurement of the DUPLEX PAIR rather than of a crossover; no published " +
                "number for a crossover's stiffness against a relative normal displacement " +
                "exists, and this task adds none.",
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
            "radial" to "along a bond's own line of centres (unitY, unitZ); CH-0259 calls this " +
                    "AXIAL, renamed here because axial already means along the duplex beam " +
                    "everywhere in HoneycombGrillage",
            "transverse" to "perpendicular to the line of centres, in the same plane -- the " +
                    "coordinate CH-0242, C-0194 section 4 and C-0205 all price",
            "resolvedLink" to "k_radial * unitZ^2 + k_transverse * unitY^2, the central-force " +
                    "decomposition projected onto the link's own gradient direction z",
            "threshold" to "the RADIAL constant at which a cell's 90th-percentile dishing over " +
                    "the stroke crosses T-5b's 0.10, with the transverse constant pinned at " +
                    "C-0205's ceiling, bisected on log10"
        ),
        parameters = mapOf(
            "crossSection" to shared.crossSection,
            "rowBasePairs" to shared.rowBasePairs.toString(),
            "edgeX" to shared.edgeX.emitted(9),
            "edgeY" to shared.edgeY.emitted(9),
            "interhelicalDistance" to d.emitted(9),
            "phosphateRadius" to rP.emitted(9),
            "crossoverSpan" to g.emitted(9),
            "relaxedPhosphodiesterStep" to MeasuredBackbone.STEP_SOUTH.emitted(9),
            "hingeStiffness" to kTheta.emitted(9),
            "slipStiffness" to Gen1Tile.crossoverInPlaneStiffness().emitted(9),
            "duplexStretchModulus" to Gen1Tile.DUPLEX_STRETCH_MODULUS.emitted(9),
            "thermalEnergy" to kT.emitted(9),
            "transverseConstant" to T310_SHEAR_CEILING.emitted(9),
            "osmoticRepulsionAmplitude" to MengMagnesium.REPULSION_AMPLITUDE.emitted(9),
            "osmoticDecayLength" to MengMagnesium.DECAY_LENGTH.emitted(9),
            "osmoticDataFloor" to MengMagnesium.DATA_FLOOR.emitted(9),
            "crossoverInterfaceBasePairs" to T310_CONTACT_BP.emitted(3),
            "foundationStiffness" to Gen1Tile.FOUNDATION_SECANT.emitted(9),
            "compositeFractions" to "0.30 and 0.26 (C-0116)",
            "mandate" to "C-0017's 33.3333 pN/nm on the SUM, section 3's acceptable clause",
            "realisations" to t310Realisations.toString(),
            "seed" to T310_SEED.toString(),
            "samples" to T310_SAMPLES.toString(),
            "tolerance" to T310_TOLERANCE.emitted(2),
            "raster" to (T310_RECOMMENDED_ONE.toString() + " / " + T310_RECOMMENDED_TWO +
                    " (C-0151, drawable)"),
            "bisections" to T310_BISECTIONS.toString(),
            "bisectionBracket" to (T310_BISECTION_LOW.emitted(9) + " to " +
                    T310_BISECTION_HIGH.emitted(9) + " pN/nm of RADIAL stiffness")
        ),
        sources = listOf(
            ResultInputs.T_3B.path + " (C-0022's solved collar at 2 mM / 10 nm / 0.192 V)",
            ResultInputs.T_303.path + " (C-0205's uniform-link thresholds, read and reproduced)",
            ResultInputs.T_299.path + " (C-0201's route-B link sweep, read and cited)"
        ),
        citedInputs = mapOf(
            "CH-0259 through-thickness link at the implied step stiffness" to "475.448622 pN/nm",
            "CH-0259 through-thickness link at the duplex stretch over the span" to
                    "1211.56918 pN/nm",
            "C-0205 shear ceiling" to "254.808095 pN/nm",
            "C-0205 uniform-link threshold, cell A" to "834.060958 pN/nm",
            "C-0205 uniform-link threshold, cell B" to "607.396049 pN/nm",
            "C-0205 transverse pair term, per unit force per unit length" to "-2.81545741",
            "C-0194 implied bond tension T" to "29.7795467 pN",
            "C-0194 implied phosphodiester-step stiffness" to "548.995464 pN/nm",
            "C-0194 span-derived link stiffness k_R" to "41.4338953 pN/nm",
            "Meng et al. 2020, Pi_R on the Mg2+-only curve" to "201.8 GPa",
            "Meng et al. 2020, the universal short-range decay length" to "2.4 Angstrom"
        ),
        cheapBound = cheapBound,
        radialRoutes = radialRoutes,
        resolution = resolution,
        ladder = ladder,
        monotonicity = monotonicity,
        thresholds = thresholds + listOfNotNull(coarseSubdivision, fineSubdivision),
        census = census,
        cells = cells,
        routeB = routeB,
        verdict = verdict,
        convergence = convergence,
        reproductions = reproductions,
        falsifiers = falsifiers,
        findings = findings,
        validity = validity,
        openQuestions = openQuestions,
        proseFailure = "none"
    )

    val output = File("gpd/results/T-310-a-bond-link-is-two-mechanisms.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digits = 9,
                // A residual at a bisected root is a difference of two nearly equal numbers, and
                // C-0205 section 8b records that two runs then disagree in its ninth digit.
                digitsByKey = mapOf(
                    "thresholds/residualAtThreshold" to DEPARTURE_SIGNIFICANT_DIGITS,
                    "thresholds/residualAtLowEnd" to DEPARTURE_SIGNIFICANT_DIGITS,
                    "thresholds/residualAtHighEnd" to DEPARTURE_SIGNIFICANT_DIGITS,
                    "monotonicity/worstRise" to DEPARTURE_SIGNIFICANT_DIGITS,
                    "convergence/departure" to DEPARTURE_SIGNIFICANT_DIGITS
                ),
                floor = 1e-12
            ).withEmissionHeader(LatticeTag.HONEYCOMB, null) as JsonObject)
        ) + "\n"
    )
    println("T-310 - wrote " + output.path)
}
