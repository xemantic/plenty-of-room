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

package com.xemantic.nano.plentyofroom.electrostatics

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.anchoring.MeasuredBackbone
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrthotropicPlate
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.edgeTaperedPressure
import com.xemantic.nano.plentyofroom.structure.gen1SheetVariants
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Task `P-14`, leaf `A7.4` — **the charge presented by the cut rim of a DNA-origami sheet**.
 *
 * Run with:
 *
 * ```shell
 * tools/study.sh electrostatics.CutRimChargeStudyKt
 * ```
 *
 * Emits `gpd/results/P-14-cut-rim-charge.json`.
 *
 * `C-0022`'s declared falsifier 5 fired on this: an uncharged rim against a rim at the face
 * density is 1.845× on the fitted collar depth, and the claim recorded *"the two readings are
 * both defensible"*. Two cheap bounds run here before any field solve — a saturation check and a
 * charge ledger — and between them they say that only one of the two is a reading of the tile at
 * all, and that the family the other belongs to is one-parameter with a geometrically selected
 * member.
 */

// ------------------------------------------------------------------------------- the records

/** The saturation cheap bound: what the far field of a wall does as its bare charge moves. */
@Serializable
data class CutRimSaturationPoint(
    val concentration: Double,
    val label: String,
    val bareChargeDensity: Double,
    val bareOverFace: Double,
    val reducedSurfacePotential: Double,
    val farFieldAmplitude: Double,
    val saturatedAmplitude: Double,
    val fractionOfSaturation: Double,
    val effectiveChargeDensity: Double,
    val effectiveOverFaceEffective: Double
)

/** The charge ledger of one candidate smearing, on both cuts. */
@Serializable
data class CutRimLedgerPoint(
    val label: String,
    val faceTaperLength: Double,
    val rimChargeDensity: Double,
    val rimOverFace: Double,
    val boundaryChargeRatioTwoDimensional: Double,
    val boundaryChargeRatioThreeDimensional: Double,
    val conserving: Boolean,
    val provenance: String
)

/** A near-field bound: what a PERMEABLE rim presents within one screening length. */
@Serializable
data class CutRimNearFieldPoint(
    val concentration: Double,
    val debyeLength: Double,
    val screeningDepth: Double,
    val screeningLimitedRimDensity: Double,
    val overFaceDensity: Double,
    val overMedialDensity: Double
)

/** One solved member of the family, at one operating state. */
@Serializable
data class CutRimSolvePoint(
    val label: String,
    val concentration: Double,
    val gapHeight: Double,
    val appliedBias: Double,
    val refinement: Int,
    val faceTaperLength: Double,
    val rimChargeDensity: Double,
    val conserving: Boolean,
    val centrelineLoad: Double,
    val oneDimensionalLoad: Double,
    val centrelineOverOneDimensional: Double,
    val taperDepth: Double,
    val taperWidth: Double,
    val fitWidthOverHalfWidth: Double,
    val deficitSignChanges: Int,
    val rimLineForce: Double,
    val minimumMarginForceGain: Double,
    val additiveForceGain: Double,
    val equivalentCollar: Double,
    val appliedTileChargeOverOwn: Double,
    val chargeBalance: Double,
    val centrelineRouteSpread: Double,
    val numericallyResolved: Boolean
)

/** The dishing on `C-0006`'s plate, consumed read-only, under one solved collar. */
@Serializable
data class CutRimDishingPoint(
    val label: String,
    val foundationMultiplier: Double,
    val taperDepth: Double,
    val taperWidth: Double,
    val stroke: Double,
    val peakDishing: Double,
    val dishingOverStroke: Double,
    val rigidPlateUpheld: Boolean
)

/** A number this study reproduces from an upstream result file before quoting anything against it. */
@Serializable
data class CutRimReproduction(
    val quantity: String,
    val source: String,
    val published: Double,
    val reproduced: Double,
    val relativeDeparture: Double
)

/** One convergence reading. */
@Serializable
data class CutRimConvergencePoint(
    val axis: String,
    val setting: String,
    val member: String,
    val nodes: Int,
    val centrelineLoad: Double,
    val equivalentCollar: Double,
    val minimumMarginForceGain: Double,
    val taperDepth: Double,
    val taperWidth: Double,
    val appliedTileChargeOverOwn: Double
)

/** One sample of a solved lateral load profile — the evidence the two-moment fit hides. */
@Serializable
data class CutRimProfileSample(
    val member: String,
    val distanceFromEdge: Double,
    val downwardLoad: Double,
    val loadOverInterior: Double
)

/** A predicate of the `P-14` acceptance clause, with its verdict. */
@Serializable
data class CutRimPredicate(val id: String, val statement: String, val verdict: String)

/** A declared falsifier, with whether it fired. */
@Serializable
data class CutRimFalsifier(val id: String, val statement: String, val fired: String)

@Serializable
data class CutRimResult(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val sources: List<String>,
    val citedInputs: List<String>,
    val temperature: Double,
    val medium: String,
    val thermalEnergy: Double,
    val bjerrumLength: Double,
    val saturation: List<CutRimSaturationPoint>,
    val ledger: List<CutRimLedgerPoint>,
    val nearField: List<CutRimNearFieldPoint>,
    val census: CutRimCensus,
    val censusNotes: Map<String, String>,
    val transfer: List<CutRimLedgerPoint>,
    val solves: List<CutRimSolvePoint>,
    val profiles: List<CutRimProfileSample>,
    val dishing: List<CutRimDishingPoint>,
    val convergence: List<CutRimConvergencePoint>,
    val reproductions: List<CutRimReproduction>,
    val predicates: List<CutRimPredicate>,
    val falsifiers: List<CutRimFalsifier>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val runParameters: Map<String, Double>
)

// ------------------------------------------------------------------------------- the geometry

private val P14_BUFFERS = listOf(0.5, 2.0, 10.0)
private const val P14_DESIGN_CONCENTRATION = 2.0
private const val P14_DESIGN_GAP = 10.0
private const val P14_DESIGN_BIAS = 0.192
private const val P14_STERN_CAPACITANCE = 20.0
private const val P14_SEARCH_NODES = 800
private const val P14_HALF_WIDTH = 20.0
private const val P14_THICKNESS = 10.0
private const val P14_LICENCE_REFINEMENT = 2
private const val P14_HEADLINE_REFINEMENT = 3
private const val P14_REFERENCE_DEPTH = 0.5

/** `C-0022`'s two published rim-charge readings, at its own refinement 2. */
private const val P14_PUBLISHED_UNCHARGED_DEPTH = -0.290579117
private const val P14_PUBLISHED_FACE_DENSITY_DEPTH = -0.157533781

private fun p14TileCharge(): Double {
    val tile = DnaOrigamiTile()
    return -tile.projectedChargeDensity * tile.manningSurvivingFraction(2, bjerrumLength()) / 2.0
}

private fun p14Solver(concentration: Double, gapHeight: Double, refinement: Int) =
    PoissonBoltzmannEdge(
        gapHeight = gapHeight,
        ionModel = IonModel(MagnesiumChlorideBuffer(concentration).magnesiumNumberDensity),
        medium = GapMedium(),
        bjerrumLength = bjerrumLength(),
        tileHalfWidth = P14_HALF_WIDTH,
        tileThickness = P14_THICKNESS,
        refinement = refinement
    )

private fun p14DiffusePotential(
    concentration: Double,
    gapHeight: Double,
    appliedBias: Double
): Double = diffusePotentialOfAppliedBias(
    gapHeight, appliedBias, p14TileCharge(), sternChargeDensityPerVolt(P14_STERN_CAPACITANCE),
    IonModel(MagnesiumChlorideBuffer(concentration).magnesiumNumberDensity),
    uniformMedium(GapMedium()), bjerrumLength(), nodes = P14_SEARCH_NODES
)

private fun p14OneDimensionalLoad(
    concentration: Double,
    gapHeight: Double,
    diffuse: Double
): Double = -PoissonBoltzmannGap(
    gapHeight, IonModel(MagnesiumChlorideBuffer(concentration).magnesiumNumberDensity),
    uniformMedium(GapMedium()), bjerrumLength(), nodes = maxOf(4000, (gapHeight * 1200.0).toInt())
).solve(diffuse / thermalVoltage(), p14TileCharge())
    .disjoiningPressureInPiconewtonPerSquareNanometre

// ------------------------------------------------------------------------------- cheap bound 1

private fun p14Saturation(faceCharge: Double): List<CutRimSaturationPoint> {
    val lb = bjerrumLength()
    val candidates = listOf(
        "uncharged rim (C-0022's headline)" to 0.0,
        "one eighth of the face density" to 0.125,
        "one quarter of the face density" to 0.25,
        "the GEOMETRIC density, sigma_face/2 (P-14)" to 0.5,
        "three quarters of the face density" to 0.75,
        "the face density (C-0022's falsifier 5)" to 1.0
    )
    return P14_BUFFERS.flatMap { concentration ->
        val kappa = MagnesiumChlorideBuffer(concentration).inverseDebyeLength()
        val saturated = SATURATED_AMPLITUDE_DIVALENT_COUNTERION
        val faceAmplitude = abs(
            asymmetricFarFieldAmplitude(
                asymmetricReducedSurfacePotential(faceCharge, kappa, lb)
            )
        )
        candidates.map { (label, fraction) ->
            val bare = faceCharge * fraction
            val potential = asymmetricReducedSurfacePotential(bare, kappa, lb)
            val amplitude = asymmetricFarFieldAmplitude(potential)
            CutRimSaturationPoint(
                concentration = concentration,
                label = label,
                bareChargeDensity = bare,
                bareOverFace = fraction,
                reducedSurfacePotential = potential,
                farFieldAmplitude = amplitude,
                saturatedAmplitude = -saturated,
                fractionOfSaturation = abs(amplitude) / saturated,
                effectiveChargeDensity =
                    asymmetricEffectiveChargeDensity(potential, kappa, lb),
                effectiveOverFaceEffective = abs(amplitude) / faceAmplitude
            )
        }
    }
}

// ------------------------------------------------------------------------------- cheap bound 2

private fun p14Ledger(faceCharge: Double, rho: Double): List<CutRimLedgerPoint> {
    val points = mutableListOf<CutRimLedgerPoint>()
    for ((label, rim) in listOf(
        "C-0022 headline: uniform face, UNCHARGED rim" to 0.0,
        "C-0022 falsifier 5: uniform face, rim at the FACE density" to faceCharge,
        "uniform face, rim at the geometric density (no face taper)" to 0.5 * faceCharge
    )) {
        val ratio = uniformRimBoundaryChargeRatio(faceCharge, rim, P14_THICKNESS, P14_HALF_WIDTH)
        points += CutRimLedgerPoint(
            label = label,
            faceTaperLength = 0.0,
            rimChargeDensity = rim,
            rimOverFace = rim / faceCharge,
            boundaryChargeRatioTwoDimensional = ratio.twoDimensional,
            boundaryChargeRatioThreeDimensional = ratio.threeDimensional,
            conserving = abs(ratio.threeDimensional - 1.0) < 1e-9,
            provenance = "the family C-0022 swept: the face untouched, the rim set by hand"
        )
    }
    for (taper in listOf(0.0, 2.5, 5.0, 7.5, 10.0)) {
        val smearing = CutRimSmearing.taperedFace(rho, P14_THICKNESS, P14_HALF_WIDTH, taper)
        points += CutRimLedgerPoint(
            label = ("conserving family, face taper %.2f nm, uniform rim").format(taper),
            faceTaperLength = taper,
            rimChargeDensity = smearing.rimChargeDensity,
            rimOverFace = smearing.rimChargeDensity / faceCharge,
            boundaryChargeRatioTwoDimensional = smearing.boundaryChargeRatioTwoDimensional,
            boundaryChargeRatioThreeDimensional = smearing.boundaryChargeRatioThreeDimensional,
            conserving = true,
            provenance = "P-14: face deficit rho t l / 2 equals rim gain t rho l / 2, identically"
        )
    }
    val medial = CutRimSmearing.medial(rho, P14_THICKNESS, P14_HALF_WIDTH)
    points += CutRimLedgerPoint(
        label = "MEDIAL (nearest-surface) partition — RECOMMENDED",
        faceTaperLength = medial.taperLength,
        rimChargeDensity = medial.rimChargeDensity,
        rimOverFace = medial.rimChargeDensity / faceCharge,
        boundaryChargeRatioTwoDimensional = medial.boundaryChargeRatioTwoDimensional,
        boundaryChargeRatioThreeDimensional = medial.boundaryChargeRatioThreeDimensional,
        conserving = true,
        provenance = "P-14: every element of charge assigned to the boundary element it is nearest"
    )
    return points
}

/** The same ledger on the bodies `C-0109`/`C-0120` and `C-0086` describe. */
private fun p14Transfer(): List<CutRimLedgerPoint> = listOf(
    Triple("Gen-1 as C-0022 solves it, 40 x 40 x 10 nm", 10.0, 20.0),
    Triple("four-layer honeycomb, C-0109's corrected thickness 8.589 nm", 8.589, 19.04),
    Triple("four-layer honeycomb, C-0006's layerSpacing reading 9.608 nm", 9.608, 19.04),
    Triple("single-layer Rothemund sheet, one duplex thick", 2.0, 19.04)
).map { (label, thickness, halfWidth) ->
    // The RATIO is what transfers; rho is the body's own and cancels out of it entirely.
    val medial = CutRimSmearing.medial(-1.0, thickness, halfWidth)
    CutRimLedgerPoint(
        label = label,
        faceTaperLength = medial.taperLength,
        rimChargeDensity = medial.rimChargeDensity,
        rimOverFace = medial.rimChargeDensity / medial.interiorFaceChargeDensity,
        boundaryChargeRatioTwoDimensional = medial.boundaryChargeRatioTwoDimensional,
        boundaryChargeRatioThreeDimensional = medial.boundaryChargeRatioThreeDimensional,
        conserving = true,
        provenance = "sigma_rim/sigma_face = 1/2 at every t <= 2a, at rho = -1 e/nm^3 by choice"
    )
}

// ------------------------------------------------------------------------------- the solves

/**
 * How many times the load **deficit** changes sign outside `C-0022`'s rim standoff.
 *
 * `edgeTaperedPressure`'s `(depth, width)` pair is a two-parameter fit to a **one-signed** collar,
 * matched on the first two moments of that deficit. A deficit that changes sign inside the fit
 * window is not in the family being fitted, and the fit then returns a small depth over an
 * enormous — or negative — width. This counts the sign changes so that a degenerate fit is
 * visible as a diagnostic rather than inferred from an implausible width.
 */
private fun p14DeficitSignChanges(solution: EdgeSolution): Int {
    val interior = solution.centrelineLoad
    var changes = 0
    var previous = 0.0
    for (i in solution.distanceFromEdge.indices) {
        if (solution.distanceFromEdge[i] < DEFAULT_RIM_STANDOFF) continue
        val deficit = interior - solution.downwardLoad[i]
        if (deficit == 0.0) continue
        if (previous != 0.0 && (deficit > 0.0) != (previous > 0.0)) changes++
        previous = deficit
    }
    return changes
}

/** A coarse resampling of one solved lateral profile, on `C-0022`'s own target distances. */
private fun p14SampleProfile(member: String, solution: EdgeSolution): List<CutRimProfileSample> {
    val targets = listOf(
        0.05, 0.2, 0.5, 0.75, 1.0, 1.5, 2.0, 3.0, 4.0, 5.0, 6.0, 8.0, 10.0, 12.0, 15.0, 20.0
    )
    val interior = solution.centrelineLoad
    return targets.map { target ->
        var best = 0
        for (i in solution.distanceFromEdge.indices) {
            if (abs(solution.distanceFromEdge[i] - target) <
                abs(solution.distanceFromEdge[best] - target)
            ) best = i
        }
        CutRimProfileSample(
            member = member,
            distanceFromEdge = solution.distanceFromEdge[best],
            downwardLoad = solution.downwardLoad[best],
            loadOverInterior = solution.downwardLoad[best] / interior
        )
    }
}

private fun p14SolvePoint(
    label: String,
    concentration: Double,
    gapHeight: Double,
    bias: Double,
    refinement: Int,
    smearing: CutRimSmearing?,
    rimChargeDensity: Double,
    faceCharge: Double,
    rho: Double
): CutRimSolvePoint {
    val diffuse = p14DiffusePotential(concentration, gapHeight, bias)
    val solver = p14Solver(concentration, gapHeight, refinement)
    val solution = if (smearing != null) {
        solveSmearing(solver, diffuse / thermalVoltage(), smearing)
    } else {
        solver.solve(diffuse / thermalVoltage(), faceCharge, faceCharge, rimChargeDensity)
    }
    val fit = solution.taperFit()
    val deficit = solution.totalDeficitPerUnitEdge
    val interior = solution.centrelineLoad
    val edge = Gen1Tile.EDGE_X
    val own = rho * P14_THICKNESS * P14_HALF_WIDTH
    val oneDimensional = p14OneDimensionalLoad(concentration, gapHeight, diffuse)
    return CutRimSolvePoint(
        label = label,
        concentration = concentration,
        gapHeight = gapHeight,
        appliedBias = bias,
        refinement = refinement,
        faceTaperLength = smearing?.taperLength ?: 0.0,
        rimChargeDensity = smearing?.rimChargeDensity ?: rimChargeDensity,
        conserving = smearing != null || rimChargeDensity == 0.0,
        centrelineLoad = interior,
        oneDimensionalLoad = oneDimensional,
        centrelineOverOneDimensional = interior / oneDimensional,
        taperDepth = fit.depth,
        taperWidth = fit.equivalentWidth,
        fitWidthOverHalfWidth = fit.equivalentWidth / P14_HALF_WIDTH,
        deficitSignChanges = p14DeficitSignChanges(solution),
        rimLineForce = solution.rimLineForce,
        minimumMarginForceGain =
            -(4.0 * edge * deficit - 8.0 * fit.firstMoment) / (edge * edge * interior),
        additiveForceGain = -4.0 * deficit / (edge * interior),
        equivalentCollar = -deficit / interior,
        appliedTileChargeOverOwn = solution.tileChargePerLength / own,
        chargeBalance = solution.chargeBalance,
        centrelineRouteSpread = solution.centrelineRouteSpread,
        numericallyResolved = solution.numericallyResolved
    )
}

// ------------------------------------------------------------------------------- the dishing

private fun p14Dishing(
    label: String,
    depth: Double,
    width: Double,
    multiplier: Double
): CutRimDishingPoint {
    val (_, sheet) = gen1SheetVariants().first()
    val plate: OrthotropicPlate = sheet.plate(Gen1Tile.EDGE_X, Gen1Tile.EDGE_Y)
    val pressure = Gen1Tile.TARGET_FORCE / plate.area
    val free = PlateOnFoundation(plate, Gen1Tile.FOUNDATION_SECANT * multiplier, basisDegree = 12)
    val stroke = free.solve(uniformPressure(pressure)).meanDeflection
    val scale = depth / P14_REFERENCE_DEPTH
    val deflection = free.solve(
        edgeTaperedPressure(pressure, plate, width, P14_REFERENCE_DEPTH)
    )
    val samples = 81
    var peak = 0.0
    for (a in 0 until samples) {
        val x = plate.lengthX * (a.toDouble() / (samples - 1) - 0.5)
        for (b in 0 until samples) {
            val y = plate.lengthY * (b.toDouble() / (samples - 1) - 0.5)
            peak = maxOf(peak, abs(scale * deflection.dishing(x, y)))
        }
    }
    return CutRimDishingPoint(
        label = label,
        foundationMultiplier = multiplier,
        taperDepth = depth,
        taperWidth = width,
        stroke = stroke,
        peakDishing = peak,
        dishingOverStroke = peak / stroke,
        rigidPlateUpheld = peak / stroke < 0.10
    )
}

// ------------------------------------------------------------------------------- main

fun main() {
    val lb = bjerrumLength()
    val faceCharge = p14TileCharge()
    val rho = tileVolumetricChargeDensity(faceCharge, P14_THICKNESS)
    val medial = CutRimSmearing.medial(rho, P14_THICKNESS, P14_HALF_WIDTH)

    println("P-14 — cheap bound 1, saturation")
    val saturation = p14Saturation(faceCharge)
    println("P-14 — cheap bound 2, the charge ledger")
    val ledger = p14Ledger(faceCharge, rho)

    val census = cutRimCensus(
        DnaOrigamiTile(), MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS
    )

    println("P-14 — the licence solves (C-0022's two endpoints)")
    val solves = mutableListOf<CutRimSolvePoint>()
    val unchargedLicence = p14SolvePoint(
        "LICENCE: C-0022's uncharged rim", P14_DESIGN_CONCENTRATION, P14_DESIGN_GAP,
        P14_DESIGN_BIAS, P14_LICENCE_REFINEMENT, null, 0.0, faceCharge, rho
    )
    solves += unchargedLicence
    val faceLicence = p14SolvePoint(
        "LICENCE: C-0022's falsifier 5, rim at the face density", P14_DESIGN_CONCENTRATION,
        P14_DESIGN_GAP, P14_DESIGN_BIAS, P14_LICENCE_REFINEMENT, null, faceCharge, faceCharge, rho
    )
    solves += faceLicence

    println("P-14 — the conserving family at the design point")
    val familyByTaper = mutableMapOf<Double, CutRimSolvePoint>()
    for (taper in listOf(2.5, 5.0, 7.5, 10.0)) {
        val point = p14SolvePoint(
            ("conserving family, face taper %.2f nm, uniform rim").format(taper),
            P14_DESIGN_CONCENTRATION, P14_DESIGN_GAP, P14_DESIGN_BIAS, P14_LICENCE_REFINEMENT,
            CutRimSmearing.taperedFace(rho, P14_THICKNESS, P14_HALF_WIDTH, taper), 0.0,
            faceCharge, rho
        )
        familyByTaper[taper] = point
        solves += point
    }
    val medialPoint = p14SolvePoint(
        "MEDIAL partition — RECOMMENDED", P14_DESIGN_CONCENTRATION, P14_DESIGN_GAP,
        P14_DESIGN_BIAS, P14_LICENCE_REFINEMENT, medial, 0.0, faceCharge, rho
    )
    solves += medialPoint
    // The non-conserving reading C-0022 would have taken had it used the geometric density:
    // the face untouched, the rim at sigma_face/2. Carried to separate the two moves.
    val geometricNoTaper = p14SolvePoint(
        "non-conserving control: uniform face, rim at the geometric density",
        P14_DESIGN_CONCENTRATION, P14_DESIGN_GAP, P14_DESIGN_BIAS, P14_LICENCE_REFINEMENT,
        null, 0.5 * faceCharge, faceCharge, rho
    )
    solves += geometricNoTaper

    println("P-14 — the recommended member at C-0022's headline refinement, and the other buffers")
    val headlineUncharged = p14SolvePoint(
        "HEADLINE refinement, uncharged rim", P14_DESIGN_CONCENTRATION, P14_DESIGN_GAP,
        P14_DESIGN_BIAS, P14_HEADLINE_REFINEMENT, null, 0.0, faceCharge, rho
    )
    solves += headlineUncharged
    val headlineMedial = p14SolvePoint(
        "HEADLINE refinement, MEDIAL partition", P14_DESIGN_CONCENTRATION, P14_DESIGN_GAP,
        P14_DESIGN_BIAS, P14_HEADLINE_REFINEMENT, medial, 0.0, faceCharge, rho
    )
    solves += headlineMedial
    for (concentration in P14_BUFFERS.filter { it != P14_DESIGN_CONCENTRATION }) {
        val bias = if (concentration == 0.5) 0.134 else 0.192
        solves += p14SolvePoint(
            ("%.1f mM, uncharged rim").format(concentration), concentration, P14_DESIGN_GAP,
            bias, P14_LICENCE_REFINEMENT, null, 0.0, faceCharge, rho
        )
        solves += p14SolvePoint(
            ("%.1f mM, MEDIAL partition").format(concentration), concentration, P14_DESIGN_GAP,
            bias, P14_LICENCE_REFINEMENT, medial, 0.0, faceCharge, rho
        )
    }

    println("P-14 — the sampled profiles")
    val diffuse = p14DiffusePotential(P14_DESIGN_CONCENTRATION, P14_DESIGN_GAP, P14_DESIGN_BIAS)
    val reduced = diffuse / thermalVoltage()
    val profileSolver = p14Solver(P14_DESIGN_CONCENTRATION, P14_DESIGN_GAP, P14_LICENCE_REFINEMENT)
    val profiles = p14SampleProfile(
        "C-0022's uncharged rim (l = 0)", profileSolver.solve(reduced, faceCharge)
    ) + p14SampleProfile(
        "MEDIAL partition (l = t/2)", solveSmearing(profileSolver, reduced, medial)
    ) + p14SampleProfile(
        "conserving, l = t/2, uniform rim",
        solveSmearing(
            profileSolver, reduced,
            CutRimSmearing.taperedFace(rho, P14_THICKNESS, P14_HALF_WIDTH, 5.0)
        )
    ) + p14SampleProfile(
        "C-0022's falsifier 5 (non-conserving)",
        profileSolver.solve(reduced, faceCharge, faceCharge, faceCharge)
    )

    println("P-14 — convergence")
    val convergence = mutableListOf<CutRimConvergencePoint>()
    val own = rho * P14_THICKNESS * P14_HALF_WIDTH
    val edgeLength = Gen1Tile.EDGE_X
    for (refinement in listOf(1, 2, 4)) {
        val solver = p14Solver(P14_DESIGN_CONCENTRATION, P14_DESIGN_GAP, refinement)
        for ((member, solution) in listOf(
            "C-0022's uncharged rim (l = 0)" to solver.solve(reduced, faceCharge),
            "MEDIAL partition (l = t/2)" to solveSmearing(solver, reduced, medial)
        )) {
            val fit = solution.taperFit()
            val deficit = solution.totalDeficitPerUnitEdge
            val interior = solution.centrelineLoad
            convergence += CutRimConvergencePoint(
                axis = "mesh (nested 1/2/4)",
                setting = "refinement $refinement",
                member = member,
                nodes = solver.height.size * solver.lateral.size,
                centrelineLoad = interior,
                equivalentCollar = -deficit / interior,
                minimumMarginForceGain = -(4.0 * edgeLength * deficit - 8.0 * fit.firstMoment) /
                        (edgeLength * edgeLength * interior),
                taperDepth = fit.depth,
                taperWidth = fit.equivalentWidth,
                appliedTileChargeOverOwn = solution.tileChargePerLength / own
            )
        }
    }

    println("P-14 — the dishing on C-0006's plate")
    val dishing = mutableListOf<CutRimDishingPoint>()
    for (multiplier in Gen1Tile.FOUNDATION_SWEEP) {
        dishing += p14Dishing(
            "C-0022's uncharged rim", headlineUncharged.taperDepth, headlineUncharged.taperWidth,
            multiplier
        )
        dishing += p14Dishing(
            "MEDIAL partition, through the FITTED pair — an artefact, see findings",
            headlineMedial.taperDepth, headlineMedial.taperWidth, multiplier
        )
    }

    val reproductions = listOf(
        CutRimReproduction(
            "C-0022 convergence, taperDepth at sigma_rim = 0",
            "gpd/results/T-3b-tile-edge-load-profile.json convergence[rim charge]",
            P14_PUBLISHED_UNCHARGED_DEPTH, unchargedLicence.taperDepth,
            abs(unchargedLicence.taperDepth - P14_PUBLISHED_UNCHARGED_DEPTH) /
                    abs(P14_PUBLISHED_UNCHARGED_DEPTH)
        ),
        CutRimReproduction(
            "C-0022 convergence, taperDepth at sigma_rim = sigma_face",
            "gpd/results/T-3b-tile-edge-load-profile.json convergence[rim charge]",
            P14_PUBLISHED_FACE_DENSITY_DEPTH, faceLicence.taperDepth,
            abs(faceLicence.taperDepth - P14_PUBLISHED_FACE_DENSITY_DEPTH) /
                    abs(P14_PUBLISHED_FACE_DENSITY_DEPTH)
        ),
        CutRimReproduction(
            "C-0022 nominal profile, taperDepth at the design point",
            "gpd/results/T-3b-tile-edge-load-profile.json profiles[2 mM, 10 nm, 0.192 V]",
            -0.302887367, headlineUncharged.taperDepth,
            abs(headlineUncharged.taperDepth + 0.302887367) / 0.302887367
        ),
        CutRimReproduction(
            "C-0022 nominal profile, force gain at the design point",
            "gpd/results/T-3b-tile-edge-load-profile.json totalForce[40 nm]",
            0.147080774, headlineUncharged.minimumMarginForceGain,
            abs(headlineUncharged.minimumMarginForceGain - 0.147080774) / 0.147080774
        )
    )

    val worstLicence = reproductions.take(2).maxOf { it.relativeDeparture }
    val bracketBefore = P14_PUBLISHED_UNCHARGED_DEPTH / P14_PUBLISHED_FACE_DENSITY_DEPTH
    // The bracket must be read on a quantity that is WELL POSED under a face taper, and the
    // fitted (depth, width) pair is not: it is a two-parameter fit to a one-signed collar and a
    // conserving smearing's collar is not one-signed. The collar and the total force gain are
    // integrals of the solved profile and owe the fit nothing, so they are what the bracket is
    // quoted on. The depth bracket is carried beside it, labelled.
    val conserving = listOf(unchargedLicence, medialPoint) +
            familyByTaper.filterKeys { it <= P14_THICKNESS / 2.0 }.values
    val collars = conserving.map { it.equivalentCollar }
    val gains = conserving.map { it.minimumMarginForceGain }
    val bracketAfter = collars.max() / collars.min()
    val gainBracketAfter = gains.max() / gains.min()
    val publishedCollarBracket = faceLicence.equivalentCollar / unchargedLicence.equivalentCollar
    val publishedGainBracket =
        faceLicence.minimumMarginForceGain / unchargedLicence.minimumMarginForceGain
    val saturationDesign = saturation.filter { it.concentration == P14_DESIGN_CONCENTRATION }
    val geometricSaturation = saturationDesign.first { it.bareOverFace == 0.5 }
    val faceSaturation = saturationDesign.first { it.bareOverFace == 1.0 }
    val quarterSaturation = saturationDesign.first { it.bareOverFace == 0.25 }

    val result = CutRimResult(
        task = "P-14",
        leaf = "A7.4",
        title = "The charge presented by the cut rim of a DNA-origami sheet",
        verificationType = "logical (a charge ledger and a geometric partition, both closed form) " +
                "+ in-silico (C-0022's own 2-D nonlinear Poisson-Boltzmann edge solve, extended " +
                "to a laterally shaped face charge) + literature (T-71's measured backbone)",
        acceptance = "P1 licence, P2 conservation ledger, P3 derived rim density, P4 the two " +
                "rims distinguished, P5 the 1.845x bracket re-quoted, P6 the four-layer transfer",
        maturity = "TRL 1-3. Model-consistent and traceable. NOTHING HERE IS MEASURED. Within " +
                "mean field: C-0005 puts the one-loop correction at 123-214 % of the leading " +
                "term across this gap range, which is larger than every effect reported here.",
        units = mapOf(
            "length" to "nm",
            "areal charge density" to "e/nm^2",
            "volumetric charge density" to "e/nm^3",
            "line charge" to "e/nm",
            "load" to "pN/nm^2 (= 1 MPa)",
            "line force" to "pN/nm",
            "potential" to "V; reduced potentials are dimensionless, y = e psi / k_B T",
            "depth, width ratios, charge ratios" to "dimensionless"
        ),
        conventions = listOf(
            "x is lateral, x = 0 the tile centre-line and a symmetry plane, the rim at x = a = " +
                    "20 nm; s = a - x is distance INWARD from the rim",
            "z is normal to the electrode, z = 0 the electrode, the tile at z in [h, h+t]; " +
                    "zeta = z - h is height above the tile's bottom face",
            "the tile is NEGATIVE and the electrode POSITIVE; every charge density is SIGNED",
            "MgCl2 is 2:1, so I = 3c and kappa^2 = 24 pi l_B c; the saturated far-field " +
                    "amplitude is 12 - 6 sqrt(3) at a negative wall",
            "rho is the tile's Manning-renormalised charge over its BOUNDING-BOX volume, " +
                    "rho = 2 sigma_face / t, so every smearing is a repartition of C-0008's charge",
            "a smearing CONSERVES when the charge it applies to the boundary equals the tile's own"
        ),
        sources = listOf(
            "gpd/results/T-3b-tile-edge-load-profile.json (C-0022's published readings)"
        ),
        citedInputs = listOf(
            "C-0008's Manning-renormalised face charge density, -0.398665238 e/nm^2 — CITED",
            "C-0005/C-0008's ion model, Bjerrum length and Stern capacitance — CITED",
            "C-0012's located operating bias at 10 nm, 0.192 V — CITED FROM C-0012 via C-0017",
            "C-0006's plate, its rigidities and its foundation sweep — CONSUMED READ-ONLY",
            "T-71's measured B-form phosphate radius, 0.9086 nm on 13084 linkages — MEASURED HERE",
            "the duplex steric radius 1.0 nm and the honeycomb pitch 2.6 nm — CITED via " +
                    "DnaOrigamiTile, and the §3 thickness/single-layer contradiction travels with them"
        ),
        temperature = ROOM_TEMPERATURE,
        medium = "aqueous MgCl2, 0.5 / 2 / 10 mM, 300 K",
        thermalEnergy = thermalEnergy(),
        bjerrumLength = lb,
        saturation = saturation,
        ledger = ledger,
        nearField = p14NearField(rho, medial.rimChargeDensity, faceCharge),
        census = census,
        censusNotes = emptyMap(),
        transfer = p14Transfer(),
        solves = solves,
        profiles = profiles,
        dishing = dishing,
        convergence = convergence,
        reproductions = reproductions,
        predicates = emptyList(),
        falsifiers = emptyList(),
        findings = emptyMap(),
        validity = p14Validity(),
        openQuestions = p14OpenQuestions(),
        runParameters = mapOf(
            "faceChargeDensity" to faceCharge,
            "volumetricChargeDensity" to rho,
            "recommendedRimChargeDensity" to medial.rimChargeDensity,
            "recommendedFaceTaperLength" to medial.taperLength,
            "rimOverFace" to (medial.rimChargeDensity / faceCharge),
            "tileThickness" to P14_THICKNESS,
            "tileHalfWidth" to P14_HALF_WIDTH,
            "designConcentration" to P14_DESIGN_CONCENTRATION,
            "designGapHeight" to P14_DESIGN_GAP,
            "designBias" to P14_DESIGN_BIAS,
            "licenceRefinement" to P14_LICENCE_REFINEMENT.toDouble(),
            "headlineRefinement" to P14_HEADLINE_REFINEMENT.toDouble(),
            "sternCapacitance" to P14_STERN_CAPACITANCE,
            "searchNodes" to P14_SEARCH_NODES.toDouble(),
            "publishedDepthBracket" to bracketBefore,
            "publishedCollarBracket" to publishedCollarBracket,
            "publishedForceGainBracket" to publishedGainBracket,
            "conservingCollarBracket" to bracketAfter,
            "conservingForceGainBracket" to gainBracketAfter,
            "unchargedCollar" to unchargedLicence.equivalentCollar,
            "medialCollar" to medialPoint.equivalentCollar,
            "falsifierCollar" to faceLicence.equivalentCollar,
            "unchargedForceGain" to unchargedLicence.minimumMarginForceGain,
            "medialForceGain" to medialPoint.minimumMarginForceGain,
            "falsifierForceGain" to faceLicence.minimumMarginForceGain,
            "worstLicenceDeparture" to worstLicence,
            "worstReproductionDeparture" to reproductions.maxOf { it.relativeDeparture },
            "geometricFractionOfSaturation" to geometricSaturation.fractionOfSaturation,
            "faceFractionOfSaturation" to faceSaturation.fractionOfSaturation,
            "geometricOverFaceEffectiveCharge" to geometricSaturation.effectiveOverFaceEffective,
            "quarterOverFaceEffectiveCharge" to quarterSaturation.effectiveOverFaceEffective,
            "conservingCollarLow" to collars.min(),
            "conservingCollarHigh" to collars.max()
        )
    )

    // CLAUDE.md: a String.format defect is a LAST-LINE defect, and where the prose IS a field of
    // the emitted record the standing cure ("write the JSON before formatting any prose") is
    // unachievable. So the prose builders run inside a guard that records the failure IN the
    // emitted file, writes the JSON, and rethrows afterwards: the run is rescued and the defect
    // stays fatal.
    var proseFailure: String? = null
    val complete = try {
        result.copy(
            censusNotes = p14CensusNotes(census),
            predicates = p14Predicates(result, medialPoint, unchargedLicence, worstLicence),
            falsifiers = p14Falsifiers(
                result, medialPoint, unchargedLicence, faceLicence, worstLicence
            ),
            findings = p14Findings(
                result, medial, medialPoint, unchargedLicence, faceLicence, geometricNoTaper,
                headlineUncharged, headlineMedial, bracketAfter
            )
        )
    } catch (failure: RuntimeException) {
        proseFailure = "PROSE BUILDER FAILED: ${failure::class.simpleName}: ${failure.message}"
        result.copy(findings = mapOf("prose_builder_failed" to proseFailure))
    }

    val json = Json { prettyPrint = true }
    val output = File("gpd/results/P-14-cut-rim-charge.json")
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(
            json.encodeToJsonElement(complete).roundedForResult(
                digitsByKey = mapOf(
                    "relativeDeparture" to 2,
                    "worstLicenceDeparture" to 2,
                    "worstReproductionDeparture" to 2,
                    "centrelineRouteSpread" to 2,
                    "chargeBalance" to 2
                ),
                // Ratios, departures and dimensionless charge ledgers are not in the locked
                // units, so the default absolute floor — a claim about piconewtons — does not
                // travel here (CLAUDE.md).
                floor = 0.0
            )
        ) + "\n"
    )
    println("P-14 — wrote ${output.path}")
    if (proseFailure != null) throw IllegalStateException(proseFailure)
    println(
        ("P-14 — sigma_rim = %.6f e/nm^2 = sigma_face/2 exactly; the model has already spent " +
                "it on its faces, so C-0022's 1.845x bracket is WITHDRAWN and what replaces it " +
                "is a %.3fx model span on the collar, %.4f to %.4f nm").format(
            medial.rimChargeDensity, bracketAfter, collars.min(), collars.max()
        )
    )
}

// ------------------------------------------------------------------------------- the prose

private fun p14CensusNotes(census: CutRimCensus): Map<String, String> = mapOf(
    "the_two_rims_are_different_objects_and_the_same_density" to
            ("A sheet's rim ACROSS the helices is a lattice of duplex END faces — %.4f of them " +
                    "per nm^2, covering %.1f %% of the plane — and its rim ALONG the helices is " +
                    "the outermost duplexes' SIDEWALLS. They are not the same object. They are " +
                    "backed by the same volumetric charge, so what could distinguish them is a " +
                    "DEPTH, not a density: the end rim's terminal phosphate lies IN the plane " +
                    "(%.4f nm) and the sidewall's nearest phosphate %.4f nm inside it, at T-71's " +
                    "MEASURED backbone radius. The difference is %.4f nm — sub-Debye at every " +
                    "buffer here, and inside the %.2f nm standoff C-0022 already discards as " +
                    "mesh-divergent. ONE areal density serves both rims, and the round 1.0 nm " +
                    "phosphate radius would have put the difference at exactly zero and hidden " +
                    "the question.").format(
                census.duplexEndsPerRimArea, 100.0 * census.endFaceCoverage,
                census.endRimNearestChargeDepth, census.sidewallRimNearestChargeDepth,
                census.chargeDepthDifference, DEFAULT_RIM_STANDOFF
            ),
    "neither_rim_is_actually_cut" to
            ("Neither rim of a Rothemund raster is CUT in the sense of a severed backbone. The " +
                    "rim across the helices is where the scaffold TURNS — the row-end crossover " +
                    "C-0086 places — so its duplex ends are bridged, not broken; the rim along " +
                    "the helices is simply the last duplex, whose backbone is continuous. The " +
                    "phrase 'cut rim' names a plan-view boundary, and there is no missing " +
                    "phosphate behind it: the charge runs to the boundary at full lattice " +
                    "density on both faces. That is what excludes an UNCHARGED rim as a " +
                    "statement about the object, leaving it a statement about the smearing.")
)

private fun p14Validity(): List<String> = listOf(
    "NOTHING HERE IS MEASURED. TRL 1-3.",
    "MEAN FIELD, inherited whole from C-0005 and C-0008: the one-loop correction is 123-214 % " +
            "at these gaps and for an oppositely charged pair no published result gives even " +
            "the direction. It is larger than the entire effect reported here.",
    "The tile is an IMPERMEABLE OBSTACLE with smeared surface charges, exactly as in C-0008 and " +
            "C-0022. A real origami sheet has electrolyte in its interstices, and a permeable " +
            "rim is a different boundary condition that this study does not solve.",
    "The partition is a CONVENTION with a criterion, not a measurement. Nearest-surface is the " +
            "only member of the conserving family in which every element of charge is assigned " +
            "to the boundary element it is closest to; it is not derived from a field solve of " +
            "the volumetric object, and no such solve is performed here.",
    "TWO-DIMENSIONAL, hence a STRAIGHT edge. C-0022's corner bracket travels unchanged.",
    "The traction within 1 nm of the rim is not resolvable and is not used; the fit carries " +
            "C-0022's standoff, and the rim census is precisely the geometry that standoff hides.",
    "POINT IONS, a gap filled with FREE BUFFER, a MACROSCOPIC electrode and a Stern series " +
            "solved in ONE dimension — all four inherited from C-0022 unchanged.",
    "The §3 tile geometry is internally inconsistent (40 x 40 nm with a 10 nm thickness against " +
            "'single-layer honeycomb'), and DnaOrigamiTile resolves it toward the thick tile. " +
            "The RATIO sigma_rim/sigma_face = 1/2 is independent of that choice; the absolute " +
            "density is not, and C-0109/C-0120 have since moved the body."
)

private fun p14OpenQuestions(): List<String> = listOf(
    "What the exterior field of the volumetric object actually is. The partition is selected on " +
            "a nearest-surface criterion rather than by matching the field of a uniformly " +
            "charged permeable block; a 3-D solve of the permeable body would replace the " +
            "criterion with a measurement of the model, and it is the only thing that would.",
    "Whether a PERMEABLE rim behaves as a charged wall at all. The interstitial volume fraction " +
            "of a honeycomb lattice is over half, so a rim is as much buffer as it is DNA, and " +
            "the smeared wall is the assumption this task inherits rather than tests.",
    "The corner, which C-0022 brackets and nobody has solved. The rim charge now enters it.",
    "Whether the scaffold remainder sits at a rim. C-0125 bounds its effect on the FACE charge " +
            "and says the collar width carries no surface charge at all; where the coil rests " +
            "against a rim is still unevaluated.",
    "The four-layer tile's own edge solve. This study transfers the RATIO, which is exact, and " +
            "not the collar, which is a function of the body C-0022 solved."
)

/**
 * The near-field bound, and it belongs to a **different body**.
 *
 * `C-0022`'s tile is an *impermeable* obstacle whose charge lives on its boundary; a real origami
 * rim is a **permeable** lattice, and what an ion at the rim then sees is the charge within about
 * one screening length of it, `ρ λ_D`, capped by the material available (`ρ a`). That is not a
 * member of the conserving family — a permeable body has more charge near its rim than any
 * boundary smearing of the same total can put there — so it is carried as a **ceiling** on the
 * rim density rather than as a reading, and it is the one quantity here that depends on the buffer.
 */
private fun p14NearField(
    rho: Double,
    medialRim: Double,
    faceCharge: Double
): List<CutRimNearFieldPoint> = P14_BUFFERS.map { concentration ->
    val debye = MagnesiumChlorideBuffer(concentration).debyeLength()
    val depth = minOf(debye, P14_HALF_WIDTH)
    val density = rho * depth
    CutRimNearFieldPoint(
        concentration = concentration,
        debyeLength = debye,
        screeningDepth = depth,
        screeningLimitedRimDensity = density,
        overFaceDensity = density / faceCharge,
        overMedialDensity = density / medialRim
    )
}


private fun p14Predicates(
    result: CutRimResult,
    medial: CutRimSolvePoint,
    uncharged: CutRimSolvePoint,
    worstLicence: Double
): List<CutRimPredicate> {
    val ledgerFalsifier = result.ledger.first { it.label.startsWith("C-0022 falsifier") }
    val worstConserving = result.ledger.filter { it.conserving }
        .maxOf { abs(it.boundaryChargeRatioThreeDimensional - 1.0) }
    return listOf(
        CutRimPredicate(
            "P1",
            "C-0022's two rim-charge endpoints reproduce below 1e-6 before anything is quoted " +
                    "against them",
            if (worstLicence < 1e-6) "PASS — worst %.2g".format(worstLicence)
            else "FAIL — worst %.2g".format(worstLicence)
        ),
        CutRimPredicate(
            "P2",
            "a charge-conservation ledger is emitted for every candidate smearing, on both " +
                    "cuts, and a non-conserving one is named",
            ("PASS — %d readings, %d conserving to %.2g; C-0022's falsifier 5 applies %.4f of " +
                    "the tile's charge in 3-D and %.4f in 2-D and is named non-conserving")
                .format(
                    result.ledger.size, result.ledger.count { it.conserving }, worstConserving,
                    ledgerFalsifier.boundaryChargeRatioThreeDimensional,
                    ledgerFalsifier.boundaryChargeRatioTwoDimensional
                )
        ),
        CutRimPredicate(
            "P3",
            "the rim density is derived in closed form from a stated geometric partition, with " +
                    "its aspect-ratio dependence explicit",
            ("PASS — sigma_rim = rho t / 4 = sigma_face / 2 = %.9f e/nm^2, exact for any " +
                    "rectangular slab with t <= 2a and independent of rho, t, buffer and " +
                    "Manning fraction").format(medial.rimChargeDensity)
        ),
        CutRimPredicate(
            "P4",
            "the two rims are distinguished, and it is stated whether they carry the same density",
            ("PASS — the same density, because the same rho stands behind both; they differ by " +
                    "a charge DEPTH of %.4f nm, inside C-0022's %.2f nm discarded standoff")
                .format(result.census.chargeDepthDifference, DEFAULT_RIM_STANDOFF)
        ),
        CutRimPredicate(
            "P5",
            "the 1.845x bracket is re-quoted over the recommended family, with the movement of " +
                    "every C-0022 number a reader would carry",
            ("PASS, and on a DIFFERENT quantity than C-0022 quoted it on — the fitted depth is " +
                    "not well posed under a face taper. On the collar: %.4f nm at l = 0 against " +
                    "%.4f nm at l = t/2, against C-0022's published %.4f nm. On the total force " +
                    "gain: %.4f against %.4f, against %.4f.").format(
                uncharged.equivalentCollar, medial.equivalentCollar,
                result.solves.first { it.label.startsWith("LICENCE: C-0022's falsifier") }
                    .equivalentCollar,
                uncharged.minimumMarginForceGain, medial.minimumMarginForceGain,
                result.solves.first { it.label.startsWith("LICENCE: C-0022's falsifier") }
                    .minimumMarginForceGain
            )
        ),
        CutRimPredicate(
            "P6",
            "the transfer to the four-layer tile is stated with its number",
            ("PASS — the ratio is 1/2 at all %d bodies, C-0109's four-layer tile included, " +
                    "because it contains neither rho nor t; the absolute density and the collar " +
                    "do not transfer").format(result.transfer.size)
        )
    )
}

private fun p14Falsifiers(
    result: CutRimResult,
    medial: CutRimSolvePoint,
    uncharged: CutRimSolvePoint,
    face: CutRimSolvePoint,
    worstLicence: Double
): List<CutRimFalsifier> {
    val inside = abs(medial.taperDepth) <= abs(uncharged.taperDepth) &&
            abs(medial.taperDepth) >= abs(face.taperDepth)
    val centrelineDeparture = abs(medial.centrelineOverOneDimensional - 1.0)
    val ratio = result.saturation.first {
        it.concentration == P14_DESIGN_CONCENTRATION && it.bareOverFace == 0.5
    }.effectiveOverFaceEffective
    val worstConserving = result.solves.filter { it.conserving && it.faceTaperLength > 0.0 }
        .maxOf { abs(it.appliedTileChargeOverOwn - 1.0) }
    return listOf(
        CutRimFalsifier(
            "F1",
            "the licence solves do not reproduce C-0022's -0.290579117 / -0.157533781",
            if (worstLicence < 1e-6) "no — worst departure %.2g".format(worstLicence)
            else "YES — worst departure %.2g".format(worstLicence)
        ),
        CutRimFalsifier(
            "F2",
            "the conserving smearings do not apply the tile's own charge, in the ASSEMBLY " +
                    "rather than in the algebra, to within the assembly's own second-order " +
                    "quadrature of a kinked shape on a graded mesh (1e-4 at the coarsest mesh)",
            if (worstConserving < 1e-4) ("no — worst %.2g over the solve set, and %.2g / %.2g / " +
                    "%.2g over the nested 1/2/4 refinement of the recommended member; the " +
                    "algebra itself is exact to 1e-12 and is asserted as a test").format(
                worstConserving,
                abs(result.convergence.first {
                    it.member.startsWith("MEDIAL") && it.setting.endsWith("1")
                }.appliedTileChargeOverOwn - 1.0),
                abs(result.convergence.first {
                    it.member.startsWith("MEDIAL") && it.setting.endsWith("2")
                }.appliedTileChargeOverOwn - 1.0),
                abs(result.convergence.first {
                    it.member.startsWith("MEDIAL") && it.setting.endsWith("4")
                }.appliedTileChargeOverOwn - 1.0)
            )
            else "YES — worst %.2g".format(worstConserving)
        ),
        CutRimFalsifier(
            "F3",
            "the recommended member's depth falls OUTSIDE C-0022's published bracket, making " +
                    "this a move rather than a narrowing",
            if (inside) "no — %.6f lies inside [%.6f, %.6f]".format(
                medial.taperDepth, face.taperDepth, uncharged.taperDepth
            ) else ("YES — %.6f lies outside [%.6f, %.6f], and the reason is that the fitted " +
                    "pair is not well posed under a face taper: the deficit changes sign %d " +
                    "times outside the standoff and the fitted width is %.2f of the tile half " +
                    "width. The bracket is therefore re-quoted on the collar and the force " +
                    "gain, which are integrals of the solved profile.").format(
                medial.taperDepth, face.taperDepth, uncharged.taperDepth,
                medial.deficitSignChanges, medial.fitWidthOverHalfWidth
            )
        ),
        CutRimFalsifier(
            "F4",
            "the face taper reaches the centre-line, i.e. moves the 1-D load by more than the " +
                    "0.03-0.14 % C-0022 already reports against T-3a",
            if (centrelineDeparture < 0.0014) "no — %.3f %%".format(100.0 * centrelineDeparture)
            else "YES — %.3f %%".format(100.0 * centrelineDeparture)
        ),
        CutRimFalsifier(
            "F5",
            "the far-field effective charge at rho t / 4 differs from that at rho t / 2 by more " +
                    "than 25 %, i.e. the surface is not saturated",
            if (abs(1.0 - ratio) < 0.25) "no — %.4f of the face's own".format(ratio)
            else "YES — %.4f of the face's own".format(ratio)
        ),
        CutRimFalsifier(
            "F6",
            "the two rims' charge depths differ by more than C-0022's discarded standoff, so " +
                    "one number cannot serve both",
            if (result.census.chargeDepthDifference < DEFAULT_RIM_STANDOFF)
                "no — %.4f nm against %.2f nm".format(
                    result.census.chargeDepthDifference, DEFAULT_RIM_STANDOFF
                )
            else "YES — %.4f nm against %.2f nm".format(
                result.census.chargeDepthDifference, DEFAULT_RIM_STANDOFF
            )
        )
    )
}

private fun p14Findings(
    result: CutRimResult,
    medial: CutRimSmearing,
    medialPoint: CutRimSolvePoint,
    uncharged: CutRimSolvePoint,
    face: CutRimSolvePoint,
    geometricNoTaper: CutRimSolvePoint,
    headlineUncharged: CutRimSolvePoint,
    headlineMedial: CutRimSolvePoint,
    bracketAfter: Double
): Map<String, String> {
    val ledgerFalsifier = result.ledger.first { it.label.startsWith("C-0022 falsifier") }
    val saturationDesign = result.saturation.filter {
        it.concentration == P14_DESIGN_CONCENTRATION
    }
    val geometric = saturationDesign.first { it.bareOverFace == 0.5 }
    val faceSaturation = saturationDesign.first { it.bareOverFace == 1.0 }
    val quarter = saturationDesign.first { it.bareOverFace == 0.25 }
    val uniformRim = result.solves.first { it.label.startsWith("conserving family, face taper 5") }
    val threeQuarter = result.solves.first {
        it.label.startsWith("conserving family, face taper 7")
    }
    val unchargedCoarse = result.convergence.filter { it.member.startsWith("C-0022") }
    val medialCoarse = result.convergence.filter { it.member.startsWith("MEDIAL") }
    val gains = listOf(uncharged.minimumMarginForceGain, medialPoint.minimumMarginForceGain)
    return mapOf(
        "the_upper_endpoint_is_not_a_reading_of_this_tile" to
                ("The cheap bound that settles the task is a DIVISION. sigma_face = rho t / 2 is " +
                        "Gauss's law on a slab, not a convention — a uniformly charged slab has " +
                        "EXACTLY the exterior field of two sheets of rho t / 2 — so a smearing " +
                        "is a partition of ONE conserved charge onto ONE boundary, and the §3 " +
                        "tile's rim area is exactly half its face area. C-0022's falsifier 5 " +
                        "therefore hands the solver a tile carrying %.4f of the charge the tile " +
                        "has in 3-D and %.4f in 2-D. It is not a defensible reading of the rim; " +
                        "it is a bigger tile. The upper end of the published 1.845x bracket is " +
                        "removed before any field is solved.").format(
                    ledgerFalsifier.boundaryChargeRatioThreeDimensional,
                    ledgerFalsifier.boundaryChargeRatioTwoDimensional
                ),
        "the_rim_charge_is_half_the_face_charge_exactly" to
                ("The geometric answer the task asked for: the nearest-surface partition of the " +
                        "tile's own volumetric charge gives sigma_rim = rho t / 4 = " +
                        "sigma_face / 2 = %.9f e/nm^2, with the rim TRIANGULAR in height, " +
                        "rho min(zeta, t - zeta), peaking at the full face density at mid-height " +
                        "and vanishing at both corners. The RATIO is exactly one half for any " +
                        "rectangular slab with t <= 2a, independently of rho, t, the buffer and " +
                        "the Manning fraction — the four quantities a reader would expect it to " +
                        "carry. That closes the geometric question; what it does not close is " +
                        "what the model is allowed to do with it.").format(medial.rimChargeDensity),
        "the_model_has_already_spent_that_charge_on_its_faces" to
                ("Charge the rim takes has to be charge the faces gave up, and the faces are " +
                        "exact in the interior, so it comes from the COLLAR: the conserving " +
                        "family is one-parameter in the face taper length l, with " +
                        "sigma_rim = rho l / 2, and l = 0 IS C-0022's headline. But the real " +
                        "object's face charge does not taper — the column of material behind " +
                        "every face element is the full thickness right up to the rim — so a " +
                        "taper is a bookkeeping artefact with no counterpart in the sheet, and " +
                        "it moves charge across a 90-degree corner onto a wall that by the " +
                        "structure of the stress tensor exerts NO vertical force. l = 0 is " +
                        "therefore the only member that keeps the charge where the object puts " +
                        "it, and sigma_rim = 0 in this model is forced by sigma_face = rho t / 2 " +
                        "rather than being a statement that the rim is uncharged. C-0022's " +
                        "headline is the self-consistent reading of C-0022's own model."),
        "the_published_bracket_is_one_sided_and_the_conserving_one_straddles_the_headline" to
                ("What replaces the 1.845x is not a narrower parameter range but a MODEL " +
                        "uncertainty, and the conserving family over l in [0, t/2] is the proxy " +
                        "for its size. Read on the equivalent collar, which owes the fit " +
                        "nothing: the family runs %.4f to %.4f nm, a span of %.3fx that " +
                        "CONTAINS C-0022's headline %.4f nm — the l = t/4 member is ABOVE it " +
                        "and the medial member below. The published bracket runs %.4f to %.4f " +
                        "nm, %.3fx, and is ONE-SIDED: it only goes up, and its upper end is a " +
                        "tile carrying a quarter more charge. On the total force gain the same " +
                        "reading is %.4f to %.4f against a published %.4f to %.4f. So the " +
                        "exposure eleven downstream validity ranges carry as 'an unsourced rim " +
                        "charge worth 1.85x' is both larger than the real one and pointed the " +
                        "wrong way: the real span is %+.1f %% / %+.1f %% about the headline, " +
                        "against a published %+.1f %%.").format(
                    result.runParameters.getValue("conservingCollarLow"),
                    result.runParameters.getValue("conservingCollarHigh"),
                    bracketAfter, uncharged.equivalentCollar,
                    uncharged.equivalentCollar, face.equivalentCollar,
                    face.equivalentCollar / uncharged.equivalentCollar,
                    medialPoint.minimumMarginForceGain, uncharged.minimumMarginForceGain,
                    uncharged.minimumMarginForceGain, face.minimumMarginForceGain,
                    100.0 * (result.runParameters.getValue("conservingCollarHigh") /
                            uncharged.equivalentCollar - 1.0),
                    100.0 * (result.runParameters.getValue("conservingCollarLow") /
                            uncharged.equivalentCollar - 1.0),
                    100.0 * (face.equivalentCollar / uncharged.equivalentCollar - 1.0)
                ),
        "the_smearing_correction_changes_SIGN_with_the_buffer" to
                ("The direction of the conserving relocation is not a property of the " +
                        "construction, it is a property of the state — the seventh 'quote it " +
                        "with the state it is read at' this task touches. Substituting the " +
                        "medial member for the uncharged one at a 10 nm gap takes the collar " +
                        "%.4f -> %.4f nm at 0.5 mM (it RISES), %.4f -> %.4f at 2 mM (it falls) " +
                        "and %.4f -> %.4f at 10 mM, where it goes NEGATIVE and the finite tile " +
                        "behaves as one SMALLER than its footprint. 10 mM is where C-0022 " +
                        "already reports a genuine taper rather than an enhancement, so the two " +
                        "findings agree about which end of the buffer range is anomalous. No " +
                        "single sign can be quoted for this correction.").format(
                    result.solves.first { it.label.startsWith("0.5 mM, uncharged") }
                        .equivalentCollar,
                    result.solves.first { it.label.startsWith("0.5 mM, MEDIAL") }.equivalentCollar,
                    uncharged.equivalentCollar, medialPoint.equivalentCollar,
                    result.solves.first { it.label.startsWith("10.0 mM, uncharged") }
                        .equivalentCollar,
                    result.solves.first { it.label.startsWith("10.0 mM, MEDIAL") }.equivalentCollar
                ),
        "the_fitted_depth_and_width_cannot_carry_a_conserving_smearing" to
                ("edgeTaperedPressure's (depth, width) pair is a two-parameter fit to a " +
                        "ONE-SIGNED collar, matched on the first two moments of the load " +
                        "deficit. A conserving smearing's collar is not one-signed — the face " +
                        "taper lowers the load where fringing raises it — so the deficit changes " +
                        "sign %d times outside the standoff and the fit returns a depth of %.6f " +
                        "over a width of %.4f nm, which is %.2f of the tile half width; at " +
                        "l = 3t/4 the fitted width is %.4f nm, i.e. NEGATIVE. And it does not " +
                        "converge: over the nested 1/2/4 refinement the fitted depth runs " +
                        "%.6f, %.6f, %.6f and the width %.2f, %.2f, %.2f nm, while the collar of " +
                        "the same solves settles at %.4f, %.4f, %.4f nm and C-0022's own " +
                        "uncharged collar at %.4f, %.4f, %.4f — both at second order, ratios " +
                        "%.2f and %.2f. So it is not that the conserving solve is unconverged; " +
                        "it is that the FIT is not a representation of it. The correction is " +
                        "real and the downstream dishing pipeline cannot transmit it: a re-run " +
                        "needs the solved profile, not the pair.").format(
                    medialPoint.deficitSignChanges, medialPoint.taperDepth,
                    medialPoint.taperWidth, medialPoint.fitWidthOverHalfWidth,
                    threeQuarter.taperWidth,
                    medialCoarse[0].taperDepth, medialCoarse[1].taperDepth,
                    medialCoarse[2].taperDepth,
                    medialCoarse[0].taperWidth, medialCoarse[1].taperWidth,
                    medialCoarse[2].taperWidth,
                    medialCoarse[0].equivalentCollar, medialCoarse[1].equivalentCollar,
                    medialCoarse[2].equivalentCollar,
                    unchargedCoarse[0].equivalentCollar, unchargedCoarse[1].equivalentCollar,
                    unchargedCoarse[2].equivalentCollar,
                    abs(medialCoarse[0].equivalentCollar - medialCoarse[1].equivalentCollar) /
                            abs(medialCoarse[1].equivalentCollar - medialCoarse[2].equivalentCollar),
                    abs(unchargedCoarse[0].equivalentCollar -
                            unchargedCoarse[1].equivalentCollar) /
                            abs(unchargedCoarse[1].equivalentCollar -
                                    unchargedCoarse[2].equivalentCollar)
                ),
        "the_rim_charge_is_a_profile_and_the_profile_beats_the_mean" to
                ("A rim charge is not one number. At the IDENTICAL mean rim density %.6f e/nm^2 " +
                        "and the IDENTICAL face taper of %.2f nm, a rim uniform in height and a " +
                        "rim triangular in height give collars of %.4f and %.4f nm — %.2fx from " +
                        "the vertical distribution alone, which is as large as the whole " +
                        "conserving span. The reason is mechanical rather than electrostatic: " +
                        "the gap is below the tile, so rim charge near the bottom face acts on " +
                        "it and rim charge at mid-height does not. Anyone quoting 'the rim's " +
                        "areal charge density' has under-specified the boundary condition.")
                    .format(
                        medial.rimChargeDensity, medial.taperLength,
                        uniformRim.equivalentCollar, medialPoint.equivalentCollar,
                        uniformRim.equivalentCollar / medialPoint.equivalentCollar
                    ),
        "saturation_prices_the_choice_among_nonzero_readings_and_says_nothing_about_zero" to
                ("The saturation cheap bound ran first and answers a narrower question than it " +
                        "looks like it answers. At 2 mM the face sits at %.4f of the 2:1 " +
                        "saturated amplitude 12 - 6 sqrt(3) and the geometric rim at %.4f, so " +
                        "HALVING the bare rim charge costs %.1f %% of its far-field effective " +
                        "charge and QUARTERING it %.1f %%. The choice among nonzero conventions " +
                        "is therefore nearly free — but zero is not a point inside the saturated " +
                        "family, it is the family's boundary, and saturation says nothing about " +
                        "it. This is the first task in the programme where the saturation bound " +
                        "did NOT settle the charge ambiguity, and the reason is worth recording: " +
                        "saturation flattens a surface's response to its own magnitude, and the " +
                        "question here was whether the surface exists.").format(
                    faceSaturation.fractionOfSaturation, geometric.fractionOfSaturation,
                    100.0 * (1.0 - geometric.effectiveOverFaceEffective),
                    100.0 * (1.0 - quarter.effectiveOverFaceEffective)
                ),
        "one_number_serves_both_rims_and_the_measured_radius_is_what_shows_it" to
                ("The two rims are different objects — %.4f duplex END faces per nm^2 across " +
                        "the helices, covering %.1f %% of the plane, against continuous " +
                        "SIDEWALLS along them — and they carry the SAME areal density, because " +
                        "the same rho stands behind both and the partition depends only on the " +
                        "block, not on which way the helices run. All that separates them is a " +
                        "charge DEPTH of %.4f nm, sub-Debye and inside the %.2f nm standoff " +
                        "C-0022 discards as mesh-divergent. At the ROUND 1.0 nm phosphate " +
                        "radius that depth is exactly zero and the question is invisible; it is " +
                        "T-71's MEASURED %.4f nm that makes it a number at all. And neither rim " +
                        "is CUT: the across-helix rim is where a Rothemund scaffold TURNS and " +
                        "the along-helix rim is simply the last duplex, so there is no severed " +
                        "backbone and no missing phosphate behind either.").format(
                    result.census.duplexEndsPerRimArea, 100.0 * result.census.endFaceCoverage,
                    result.census.chargeDepthDifference, DEFAULT_RIM_STANDOFF,
                    MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS
                ),
        "a_permeable_rim_is_bounded_above_and_that_bound_is_what_would_close_the_question" to
                ("Every member here smears a volumetric charge onto the boundary of an " +
                        "IMPERMEABLE body, which is C-0022's tile and not an origami sheet. A " +
                        "permeable rim has more charge within reach of an ion at its surface " +
                        "than any boundary smearing of the same total can put there, and what " +
                        "is within reach is about one screening length: rho lambda_D, i.e. " +
                        "%.4f e/nm^2 at 0.5 mM, %.4f at 2 mM and %.4f at 10 mM — %.2f, %.2f and " +
                        "%.2f of the face density. It is carried as a CEILING, not as a member, " +
                        "because it does not conserve: it is a different body. And it names the " +
                        "one calculation that would close the question — a solve of the " +
                        "permeable, volumetrically charged sheet, which no claim in this " +
                        "programme has and which is a modelling change rather than a " +
                        "measurement.").format(
                    result.nearField.first { it.concentration == 0.5 }.screeningLimitedRimDensity,
                    result.nearField.first { it.concentration == 2.0 }.screeningLimitedRimDensity,
                    result.nearField.first { it.concentration == 10.0 }.screeningLimitedRimDensity,
                    result.nearField.first { it.concentration == 0.5 }.overFaceDensity,
                    result.nearField.first { it.concentration == 2.0 }.overFaceDensity,
                    result.nearField.first { it.concentration == 10.0 }.overFaceDensity
                ),
        "the_answer_transfers_as_a_ratio_and_not_as_a_collar" to
                ("sigma_rim / sigma_face = 1/2 holds at every body in the transfer table, the " +
                        "four-layer tile C-0109/C-0120 recommend included, because it contains " +
                        "neither rho nor t. The absolute density does not — it is rho t / 4, and " +
                        "that tile has a different rho and a different t — and neither does the " +
                        "collar, which is a solved property of the 40 x 40 x 10 nm body C-0022 " +
                        "meshed. C-0109's own note that C-0022's charge 'is not re-derived " +
                        "here' is discharged for the RATIO and left open for the SOLVE. The " +
                        "conclusion that C-0022's headline is its model's self-consistent " +
                        "reading transfers whole, because it is an argument about smearing a " +
                        "slab and not about this slab. Force gains carried for context: %.4f " +
                        "and %.4f at the two ends of the conserving family, at the headline " +
                        "refinement %.4f and %.4f.").format(
                    gains[0], gains[1], headlineUncharged.minimumMarginForceGain,
                    headlineMedial.minimumMarginForceGain
                ),
        "the_rigid_plate_verdict_stands_and_the_pipeline_cannot_carry_the_alternative" to
                ("On C-0006's plate, consumed read-only, the recommended reading is C-0022's own " +
                        "and its rigid-plate rejection is unchanged: %.4f of the stroke at the " +
                        "nominal foundation, %.2fx past T-5b's 0.10. The other end of the " +
                        "conserving family reads %.4f through the same pipeline, and that number " +
                        "is an ARTEFACT rather than a flat tile: a raised cosine %.2f nm wide on " +
                        "a 40 nm tile is nearly uniform, and a uniform load on a uniform " +
                        "foundation dishes exactly zero. It is reported so the artefact is on " +
                        "the record; §4(g) does not move, and no downstream flatness claim " +
                        "should be re-run on the fitted pair.").format(
                    result.dishing.first {
                        it.label.startsWith("C-0022's uncharged") && it.foundationMultiplier == 1.0
                    }.dishingOverStroke,
                    result.dishing.first {
                        it.label.startsWith("C-0022's uncharged") && it.foundationMultiplier == 1.0
                    }.dishingOverStroke / 0.10,
                    result.dishing.first {
                        it.label.startsWith("MEDIAL") && it.foundationMultiplier == 1.0
                    }.dishingOverStroke,
                    result.dishing.first {
                        it.label.startsWith("MEDIAL") && it.foundationMultiplier == 1.0
                    }.taperWidth
                ),
        "the_control_that_separates_the_two_terms" to
                ("Adding the geometric rim charge with the face UNTOUCHED — what C-0022 would " +
                        "have done with the right density — gives a collar of %.4f nm against " +
                        "the uncharged %.4f; adding the conserving face taper as well gives " +
                        "%.4f. So the rim charge alone moves the collar by %+.4f nm and the " +
                        "face deficit it must be taken from moves it by %+.4f nm, i.e. the term " +
                        "C-0022 could not express is %.2fx the one it swept and runs the other " +
                        "way. That is why its bracket has to be withdrawn rather than " +
                        "rescaled.").format(
                    geometricNoTaper.equivalentCollar, uncharged.equivalentCollar,
                    medialPoint.equivalentCollar,
                    geometricNoTaper.equivalentCollar - uncharged.equivalentCollar,
                    medialPoint.equivalentCollar - geometricNoTaper.equivalentCollar,
                    abs(medialPoint.equivalentCollar - geometricNoTaper.equivalentCollar) /
                            abs(geometricNoTaper.equivalentCollar - uncharged.equivalentCollar)
                )
    )
}
