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

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import org.jetbrains.bio.viktor.F64Array
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.sqrt

/**
 * Task `T-10` / leaves `A8.2` and `A1.2` — the discrete beam-and-hinge check of the tile,
 * replacing `C-0006`'s continuum Kirchhoff plate by the grillage the sheet physically is.
 *
 * ```shell
 * ./gradlew study -Pstudy=structure.DiscreteLatticeTileStudyKt
 * ```
 *
 * Emits `gpd/results/T-10-discrete-lattice-tile.json`, deterministically.
 */

// --------------------------------------------------------------------------- records

@Serializable
data class LatticeGeometry(
    val beamCount: Int,
    val crossoverColumns: Int,
    val crossovers: Int,
    val subdivisions: Int,
    val nodesPerBeam: Int,
    val degreesOfFreedom: Int,
    val lengthX: Double,
    val lengthY: Double,
    val area: Double,
    val interhelicalDistance: Double,
    val crossoverSpacing: Double,
    val crossoversPerInterface: Double,
    val continuumCrossoverCount: Double,
    val integerCountRatio: Double,
    val linkStiffness: Double
)

@Serializable
data class RigidityRecovery(
    val quantity: String,
    val closedForm: Double,
    val latticeEnergy: Double,
    val plateEnergy: Double,
    val ratio: Double,
    val note: String
)

@Serializable
data class DiscretenessRatios(
    val winklerLengthAlongHelix: Double,
    val winklerLengthAcrossHelix: Double,
    /** `C-0006`'s own criterion, which pairs the across-helix length with the along-helix spacing. */
    val acrossHelixLengthOverCrossoverSpacing: Double,
    /** The direction-matched criterion: bending along `x` against the hinge spacing along `x`. */
    val alongHelixLengthOverCrossoverSpacing: Double,
    /** The direction-matched criterion: bending along `y` against the duplex spacing along `y`. */
    val acrossHelixLengthOverInterhelicalDistance: Double,
    val crossoversInAnchorPatch: Double,
    val duplexesInAnchorPatch: Double,
    val continuumValidByMatchedCriterion: Boolean
)

@Serializable
data class DishingComparison(
    val source: String,
    val mechanism: String,
    val latticePeak: Double,
    val latticeRms: Double,
    val platePeak: Double,
    val plateRms: Double,
    val stroke: Double,
    val latticePeakOverStroke: Double,
    val platePeakOverStroke: Double,
    val latticeOverPlate: Double,
    val rigidPlateUpheld: Boolean,
    val plateIsConservativeAboutFlatness: Boolean
)

@Serializable
data class ThermalComparison(
    val foundationStiffness: Double,
    val latticePistonRms: Double,
    val latticeTiltRms: Double,
    val latticeDishingRms: Double,
    val latticeCentreRms: Double,
    val platePistonRms: Double,
    val plateTiltRms: Double,
    val plateDishingRms: Double,
    val plateCentreRms: Double,
    val dishingLatticeOverPlate: Double,
    val centreLatticeOverPlate: Double,
    val latticeDishingOverStroke: Double,
    val plateIsConservativeAboutFlatness: Boolean
)

@Serializable
data class AnchoredLatticeCase(
    val anchorCount: Int,
    val anchorStiffnessFraction: Double,
    val anchorStiffnessEach: Double,
    val peakAnchorForce: Double,
    val platePeakAnchorForce: Double,
    val anchorForceLatticeOverPlate: Double,
    val peakCrossoverForce: Double,
    val peakCrossoverBand: String,
    val peakCrossoverVerdict: String,
    val peakHingeMoment: Double,
    val hingeMomentEquivalentForce: Double,
    val peakDuplexShear: Double,
    val equalSharingEstimate: Double,
    val contourPaths: Double,
    val concentrationFactor: Double,
    val peakDishing: Double,
    val strokeLossFraction: Double
)

@Serializable
data class ConcentratedLatticeCase(
    val attachmentCount: Int,
    val forcePerAttachment: Double,
    val peakCrossoverForce: Double,
    val peakCrossoverBand: String,
    val peakCrossoverVerdict: String,
    val peakDuplexShear: Double,
    val peakHingeMoment: Double,
    val peakDishing: Double,
    val platePeakDishing: Double,
    val dishingOverStroke: Double,
    val exceedsLayerHeight: Boolean
)

@Serializable
data class CrossoverRecord(
    val x: Double,
    val y: Double,
    val distanceFromAnchor: Double,
    val verticalForce: Double
)

@Serializable
data class AnchorPhaseCase(
    val placement: String,
    val x: Double,
    val y: Double,
    val peakAnchorForce: Double,
    val peakCrossoverForce: Double,
    val peakDuplexShear: Double,
    val peakDishing: Double
)

@Serializable
data class LatticeFoundationCase(
    val label: String,
    val multiplier: Double,
    val foundationStiffness: Double,
    val stroke: Double,
    val discreteness: DiscretenessRatios,
    val sources: List<DishingComparison>,
    val thermal: ThermalComparison,
    val anchored: List<AnchoredLatticeCase>,
    val concentrated: List<ConcentratedLatticeCase>
)

@Serializable
data class HingeCase(
    val label: String,
    val alpha: Double,
    val crossoverHingeStiffness: Double,
    val acrossHelixRigidity: Double,
    val anisotropy: Double,
    val winklerLengthAcrossHelix: Double,
    val alongHelixLengthOverCrossoverSpacing: Double,
    val acrossHelixLengthOverInterhelicalDistance: Double,
    val fourAnchorLatticePeakDishing: Double,
    val fourAnchorPlatePeakDishing: Double,
    val fourAnchorDishingRatio: Double,
    val thermalLatticeDishingRms: Double,
    val thermalPlateDishingRms: Double,
    val thermalDishingRatio: Double,
    val peakCrossoverForceFourAnchors: Double,
    val peakCrossoverForceOneLever: Double,
    val peakDuplexShearOneLever: Double
)

@Serializable
data class ConvergenceRecord(
    val parameter: String,
    val value: Double,
    val peakCrossoverForce: Double,
    val fourAnchorPeakDishing: Double,
    val thermalDishingRms: Double
)

@Serializable
data class PatchCount(
    val continuumPatches: Double,
    val latticeCrossovers: Int,
    val latticeUnitCells: Double,
    val independentBeams: Double,
    val flatnessAttachmentsHeuristicFromPlate: Int,
    val flatnessAttachmentsSolvedOnPlate: Int,
    val flatnessAttachmentsSolvedOnLattice: Int,
    val attachmentsPerCrossover: Double,
    val note: String
)

/** One attachment count in the flatness search, on both models. */
@Serializable
data class FlatnessScanRecord(
    val attachments: Int,
    val forcePerAttachment: Double,
    val latticePeakDishing: Double,
    val platePeakDishing: Double,
    val latticeOverStroke: Double,
    val plateOverStroke: Double,
    val latticeFlat: Boolean,
    val plateFlat: Boolean
)

/** `C-0006`'s published numbers, held here so the discrepancy is computed and not asserted. */
@Serializable
data class PublishedComparison(
    val quantity: String,
    val c0006Value: Double,
    val plateOnLatticeFootprint: Double,
    val lattice: Double,
    val latticeOverC0006: Double,
    val footprintEffect: Double
)

@Serializable
data class LatticeParameters(
    val temperature: Double,
    val medium: String,
    val thermalEnergy: Double,
    val tileFootprint: String,
    val tileArea: Double,
    val targetForce: Double,
    val targetPressure: Double,
    val acceptableStroke: Double,
    val desiredStroke: Double,
    val debyeLength: Double,
    val layerHeight: Double,
    val plateBasisDegree: Int,
    val edgeTaperWidth: Double,
    val edgeTaperDepth: Double,
    val hingeMomentLeverArm: Double,
    val foundationReference: String,
    val foundationSweep: List<Double>,
    val crossoverAlphaSweep: List<Double>,
    val duplexShearAllowable: Double,
    val duplexUnzipAllowable: Double,
    val overstretchingCeiling: Double,
    val rigidPlateCriterion: String,
    val provenance: Map<String, String>
)

@Serializable
data class LatticeResult(
    val task: String,
    val leaves: List<String>,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val validity: List<String>,
    val parameters: LatticeParameters,
    val geometry: LatticeGeometry,
    val rigidityRecovery: List<RigidityRecovery>,
    val cases: List<LatticeFoundationCase>,
    val hingeCases: List<HingeCase>,
    val convergence: List<ConvergenceRecord>,
    val anchorPhase: List<AnchorPhaseCase>,
    val worstCrossoversAtOneAnchor: List<CrossoverRecord>,
    val patchCount: PatchCount,
    val flatnessScan: List<FlatnessScanRecord>,
    val againstC0006: List<PublishedComparison>,
    val verdict: Map<String, String>
)

// --------------------------------------------------------------------------- run parameters

/** Duplexes across the §3 tile: `40 / 2.69` rounded to the integer a lattice must have. */
private const val BEAM_COUNT = 15

/** Crossover columns across the §3 tile, spaced `p/2 = 5.44 nm`, all fitting inside 40 nm. */
private const val CROSSOVER_COLUMNS = 8

private const val SUBDIVISIONS = 2

private const val PLATE_BASIS_DEGREE = 12

private const val EDGE_TAPER_DEPTH = 0.5
private const val SHALLOW_TAPER_DEPTH = 0.1

/** A dishing amplitude below this fraction of the stroke leaves the rigid-plate picture standing. */
private const val RIGID_PLATE_TOLERANCE = 0.10

/**
 * The lever arm in nm over which a crossover's hinge moment is turned into a force pair.
 *
 * An antiparallel crossover carries its moment on **two** phosphate backbone bonds, and their
 * separation is not measured anywhere. One duplex radius is the largest arm the geometry
 * admits, so the equivalent force it produces is a **lower bound** on the bond force.
 */
private const val HINGE_MOMENT_LEVER_ARM = 1.0

/** The largest square attachment array the flatness search is allowed to reach. */
private const val MAXIMUM_ATTACHMENT_SIDE = 14

// C-0006's published numbers at its own 40 x 40 nm footprint and nominal k_f — CITED from
// gpd/results/T-5-load-distribution.json and gpd/results/T-5b-tile-flatness.json, so that
// the discrepancy this task reports is computed against them rather than asserted.
private const val C0006_D_PARALLEL = 85.5018587
private const val C0006_D_PERPENDICULAR = 3.34504758
private const val C0006_DISHING_EDGE_TAPER = 1.32559043
private const val C0006_DISHING_FOUR_ANCHORS = 2.48012818
private const val C0006_DISHING_ONE_LEVER = 18.277931
private const val C0006_THERMAL_DISHING = 1.27194934
private const val C0006_THERMAL_CENTRE = 1.36543356

private val ANCHOR_COUNTS = listOf(1, 4, 9, 25)
private val ANCHOR_STIFFNESS_FRACTIONS = listOf(0.1, 1.0, 10.0)
private val LEVER_ATTACHMENTS = listOf(1, 4, 9, 16, 25, 49)
private val ALPHA_SWEEP = listOf(0.6, 1.0, 1.2)

/**
 * A deliberately **out-of-range** hinge probe: the `alpha` at which `D_⊥` reaches `D_∥` and the
 * sheet becomes isotropic.
 *
 * `CH-0005` names this as the way `C-0006` could be wrong — "if the true crossover is ~30x
 * stiffer than [Chen et al.'s fit]". `D_⊥ = D_∥` needs `k_θ = D_∥ p/d`, i.e. this alpha.
 * It is reported so that the verdict is stated across the whole physically conceivable range
 * of the largest open premise and not only across the fitted one. `T-9` is what settles it.
 */
private val ISOTROPIC_PROBE_ALPHA: Double =
    Gen1Tile.DUPLEX_BENDING_RIGIDITY / Gen1Tile.INTERHELICAL_SHEET *
            (Gen1Tile.CROSSOVER_SPACING_SHEET_BP * Gen1Tile.RISE_PER_BASE_PAIR) /
            Gen1Tile.INTERHELICAL_SHEET / Gen1Tile.crossoverHingeStiffness(1.0)

private fun sheetOf(alpha: Double): OrigamiSheet = origamiSheet(
    Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP, crossoverAlpha = alpha
)

private fun lattice(
    sheet: OrigamiSheet,
    foundationStiffness: Double,
    supports: List<PointSupport> = emptyList(),
    subdivisions: Int = SUBDIVISIONS,
    columns: Int = CROSSOVER_COLUMNS,
    linkStiffness: Double = OrigamiGrillage.RIGID_LINK_STIFFNESS
): OrigamiGrillage = OrigamiGrillage(
    sheet = sheet,
    lengthX = Gen1Tile.EDGE_X,
    beamCount = BEAM_COUNT,
    foundationStiffness = foundationStiffness,
    crossoverColumns = columns,
    subdivisions = subdivisions,
    linkStiffness = linkStiffness,
    supports = supports
)

/** The continuum plate on the **same** footprint as the lattice, so only the form differs. */
private fun plateOf(sheet: OrigamiSheet): OrthotropicPlate = OrthotropicPlate(
    lengthX = Gen1Tile.EDGE_X,
    lengthY = BEAM_COUNT * sheet.interhelicalDistance,
    rigidityX = sheet.alongHelixRigidity,
    rigidityY = sheet.acrossHelixRigidity,
    twistingRigidity = sheet.twistingRigidity
)

private fun perPathVerdict(force: Double): String = when {
    force >= Gen1Tile.OVERSTRETCHING_CEILING ->
        "ABOVE the 65 pN nicked-duplex overstretching ceiling"
    force >= Gen1Tile.DUPLEX_SHEAR_ALLOWABLE ->
        "above the 48 pN quasi-static single-duplex shear allowable"
    force >= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE ->
        "above the 10-15 pN unzip allowable, below duplex shear"
    else -> "below every per-path allowable"
}

// --------------------------------------------------------------------------- the study

fun main() {
    val nominalSheet = sheetOf(1.0)
    val nominalPlate = plateOf(nominalSheet)
    val reference = lattice(nominalSheet, Gen1Tile.FOUNDATION_SECANT)
    val pressure = Gen1Tile.TARGET_FORCE / reference.area

    val cases = Gen1Tile.FOUNDATION_SWEEP.map { multiplier ->
        foundationCase(multiplier, nominalSheet, nominalPlate, pressure)
    }
    val nominal = cases.first { it.multiplier == 1.0 }

    val result = LatticeResult(
        task = "T-10",
        leaves = listOf("A8.2", "A1.2"),
        title = "Discrete beam-and-hinge check of the Gen-1 tile, against C-0006's continuum plate",
        verificationType = "in-silico (a beam-and-hinge grillage finite-element model written " +
                "for this task, calibrated against C-0006's orthotropic Kirchhoff plate on a " +
                "shared set of physical ingredients)",
        acceptance = "The continuum plate reduction upheld or rejected against C-0006's own " +
                "reported quantities, with the discrepancy as a number and the direction of " +
                "the plate's error stated; the peak per-load-path force at and around a " +
                "discrete anchor reported against the per-path allowables; and the 'no " +
                "discrete attachment scheme is flat' conclusion confirmed, refuted or sharpened",
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "moment" to "pN*nm",
            "pressure" to "pN/nm^2 (= MPa)",
            "foundationStiffness" to "pN/nm^3",
            "flexuralRigidity" to "pN*nm",
            "hingeStiffness" to "pN*nm/rad",
            "temperature" to "K"
        ),
        conventions = listOf(
            "x along the helices, y across them, origin at the centre of the footprint",
            "w positive DOWNWARD, compressing the polymer layer — the T-5 convention",
            "a load path carries TENSION as positive; a crossover force is signed so that " +
                    "positive is transmitted from the far side of its interface toward the near one",
            "each duplex is an Euler-Bernoulli beam with a roll degree of freedom; the " +
                    "deflection field is reconstructed on its tributary strip as " +
                    "w(x, y) = w_i(x) + phi_i(x) (y - y_i)",
            "a crossover is a torsional hinge of constant k_theta on the RELATIVE ROLL of the " +
                    "two duplexes it joins, plus a stiff vertical link holding their surfaces " +
                    "together; the link is a constraint and its stiffness is a penalty",
            "the footprint across the helices is beamCount x d = 40.35 nm, not 40 nm, because " +
                    "a lattice can only be an integer number of duplexes wide; the plate it is " +
                    "compared against uses the SAME footprint, so the comparison isolates the form"
        ),
        validity = listOf(
            "TRL 1-3. Model-consistent and traceable. NOT measured.",
            "k_theta is the single largest open premise, inherited from C-0006 and swept over " +
                    "Chen et al.'s admissible alpha in [0.6, 1.2]. T-9 is queued to settle it " +
                    "by oxDNA and has NOT run. Every conclusion here is stated as a function of it.",
            "the vertical crossover link is a penalty: the transmitted force is a constraint " +
                    "force and is verified to have stopped moving with the penalty stiffness",
            "linear Winkler foundation, DRAINED at the >= 1 kHz operating point per C-0004; " +
                    "the real layer is strongly nonlinear (C-0001 gate 2) and its three " +
                    "stiffnesses at rest / secant / at the working point are carried separately",
            "k_f from C-0001 are LOWER BOUNDS per CH-0001 and are being re-derived under T-1c",
            "no electrostatics is solved: the load enters as a 100 pN total and a bounded edge " +
                    "taper (T-3 owns the load model), and the response is linear in both",
            "in-plane membrane stiffening is neglected, which is conservative",
            "the duplex transverse shear is a BEAM internal force; the per-path rupture " +
                    "allowables were measured on hybridised staple domains, so the crossover " +
                    "force is the number they apply to and the duplex shear is reported against " +
                    "the nicked-duplex ceiling only",
            "a discrete anchor is modelled as attaching at one point of one duplex, which is " +
                    "what a tether physically is; the anchor-phase sweep shows how much that " +
                    "placement matters"
        ),
        parameters = LatticeParameters(
            temperature = ROOM_TEMPERATURE,
            medium = "aqueous buffer, 2/5/10 mM MgCl2",
            thermalEnergy = thermalEnergy(),
            tileFootprint = "${Gen1Tile.EDGE_X} x ${reference.lengthY} nm",
            tileArea = reference.area,
            targetForce = Gen1Tile.TARGET_FORCE,
            targetPressure = pressure,
            acceptableStroke = Gen1Tile.ACCEPTABLE_STROKE,
            desiredStroke = Gen1Tile.DESIRED_STROKE,
            debyeLength = Gen1Tile.DEBYE_LENGTH,
            layerHeight = Gen1Tile.LAYER_HEIGHT,
            plateBasisDegree = PLATE_BASIS_DEGREE,
            edgeTaperWidth = Gen1Tile.DEBYE_LENGTH,
            edgeTaperDepth = EDGE_TAPER_DEPTH,
            hingeMomentLeverArm = HINGE_MOMENT_LEVER_ARM,
            foundationReference = "C-0001, 10 nm layer at sigma = 0.024 nm^-2: k(L0) = 7.402, " +
                    "k_secant = 20.201, k(h) = 53.337 pN/nm over 1600 nm^2. Loaded cases use " +
                    "the secant; thermal cases use the tangent at first contact, which is the " +
                    "stiffness an unbiased tile fluctuates against.",
            foundationSweep = Gen1Tile.FOUNDATION_SWEEP,
            crossoverAlphaSweep = ALPHA_SWEEP + ISOTROPIC_PROBE_ALPHA,
            duplexShearAllowable = Gen1Tile.DUPLEX_SHEAR_ALLOWABLE,
            duplexUnzipAllowable = Gen1Tile.DUPLEX_UNZIP_ALLOWABLE,
            overstretchingCeiling = Gen1Tile.OVERSTRETCHING_CEILING,
            rigidPlateCriterion = "dishing amplitude below $RIGID_PLATE_TOLERANCE of the stroke",
            provenance = mapOf(
                "duplexBendingRigidity" to
                        "CITED — CanDo, Kim et al. NAR 40:2862 (2012), 230 pN*nm^2",
                "duplexTorsionalRigidity" to "CITED — CanDo, 460 pN*nm^2",
                "interhelicalDistance" to "CITED, MEASURED — Fischer et al. Nano Lett 16:4282 " +
                        "(2016), SAXS: 26.9 +/- 0.2 A for a one-layer sheet",
                "crossoverSpacing" to "CITED — Rothemund Nature 440:297 (2006): 1.5 turns " +
                        "alternating between two neighbours = 32 bp per interface",
                "crossoverHingeStiffness" to "CITED, fitted — Chen et al. JACS 136:6995 (2014) " +
                        "SI: k2 = alpha*B/(100a) per phosphate bond, 2 bonds per crossover",
                "perPathAllowables" to "CITED, MEASURED — Strunz et al. PNAS 96:11277 (1999) " +
                        "48 +/- 2 pN shear; Essevaz-Roulet et al. PNAS 94:11935 (1997) 10-15 pN " +
                        "unzip; van Mameren et al. PNAS 106:18231 (2009) 65 pN nicked ceiling. " +
                        "The 35-60 pN band of §4(f) is a WHOLE-CROSS-SECTION number and is NOT " +
                        "used as a per-path allowable here (C-0006 traced it to Shrestha et al.).",
                "foundationStiffness" to "DERIVED from C-0001, itself under challenge (CH-0001)",
                "plateComparison" to "DERIVED — C-0006, re-run on the lattice's own footprint"
            )
        ),
        geometry = geometryOf(reference, nominalSheet),
        rigidityRecovery = rigidityRecovery(reference, nominalSheet),
        cases = cases,
        hingeCases = (ALPHA_SWEEP + ISOTROPIC_PROBE_ALPHA).map { hingeCase(it, pressure) },
        convergence = convergence(nominalSheet, pressure),
        anchorPhase = anchorPhase(nominalSheet, pressure),
        worstCrossoversAtOneAnchor = worstCrossovers(nominalSheet, pressure),
        patchCount = patchCount(reference, nominalSheet, nominalPlate, nominal),
        flatnessScan = flatnessScan(nominalSheet, nominalPlate, nominal.stroke),
        againstC0006 = publishedComparison(nominal, reference, nominalSheet),
        verdict = verdicts(cases, reference)
    )

    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-10-discrete-lattice-tile.json")
    output.parentFile.mkdirs()
    output.writeText(json.encodeToString(json.encodeToJsonElement(result).roundedForResult()) + "\n")
    report(result, output)
}

private fun geometryOf(grillage: OrigamiGrillage, sheet: OrigamiSheet): LatticeGeometry {
    val continuumCount = grillage.area / (sheet.interhelicalDistance * sheet.crossoverSpacing)
    return LatticeGeometry(
        beamCount = BEAM_COUNT,
        crossoverColumns = CROSSOVER_COLUMNS,
        crossovers = grillage.crossovers.size,
        subdivisions = SUBDIVISIONS,
        nodesPerBeam = grillage.nodesPerBeam,
        degreesOfFreedom = grillage.degreesOfFreedom,
        lengthX = grillage.lengthX,
        lengthY = grillage.lengthY,
        area = grillage.area,
        interhelicalDistance = sheet.interhelicalDistance,
        crossoverSpacing = sheet.crossoverSpacing,
        crossoversPerInterface = CROSSOVER_COLUMNS / 2.0,
        continuumCrossoverCount = continuumCount,
        integerCountRatio = grillage.crossovers.size / continuumCount,
        linkStiffness = OrigamiGrillage.RIGID_LINK_STIFFNESS
    )
}

/**
 * Gate 2 in the result file rather than only in the tests: the lattice's long-wavelength
 * energy against the plate's, for each of the three rigidities separately.
 */
private fun rigidityRecovery(
    grillage: OrigamiGrillage,
    sheet: OrigamiSheet
): List<RigidityRecovery> {
    val curvature = 1e-3
    val quadratic = 0.5 * curvature * curvature * grillage.area
    fun record(
        quantity: String,
        closedForm: Double,
        field: F64Array,
        plateEnergy: Double,
        note: String
    ): RigidityRecovery {
        val latticeEnergy = grillage.structuralEnergy(field)
        return RigidityRecovery(
            quantity = quantity,
            closedForm = closedForm,
            latticeEnergy = latticeEnergy,
            plateEnergy = plateEnergy,
            ratio = latticeEnergy / plateEnergy,
            note = note
        )
    }
    return listOf(
        record(
            "D_parallel = EI/d", sheet.alongHelixRigidity,
            grillage.curvatureFieldAlongHelices(curvature),
            sheet.alongHelixRigidity * quadratic,
            "exact: the duplexes are continuous along x, so there is no discreteness in this " +
                    "direction at all and the lattice reproduces the plate identically"
        ),
        record(
            "D_perpendicular = k_theta d/p", sheet.acrossHelixRigidity,
            grillage.curvatureFieldAcrossHelices(curvature),
            sheet.acrossHelixRigidity * quadratic,
            "the lattice costs 1/2 k_theta (kappa d)^2 per crossover EXACTLY; the only " +
                    "difference from the plate is that a finite lattice holds an integer " +
                    "number of crossovers where the plate assumes the areal density 1/(dp)"
        ),
        record(
            "D_k = GJ/(4d)", sheet.twistingRigidity,
            grillage.twistField(curvature),
            2.0 * sheet.twistingRigidity * curvature * curvature * grillage.area,
            "exact: a uniform twist costs 2 D_k tau^2 per unit area in the Huber form, and " +
                    "the lattice's beam torsion reproduces it identically"
        )
    )
}

private fun discreteness(
    sheet: OrigamiSheet,
    plate: OrthotropicPlate,
    foundation: Double
): DiscretenessRatios {
    val along = winklerLength(plate.rigidityX, foundation)
    val across = winklerLength(plate.rigidityY, foundation)
    return DiscretenessRatios(
        winklerLengthAlongHelix = along,
        winklerLengthAcrossHelix = across,
        acrossHelixLengthOverCrossoverSpacing = across / sheet.crossoverSpacing,
        alongHelixLengthOverCrossoverSpacing = along / sheet.crossoverSpacing,
        acrossHelixLengthOverInterhelicalDistance = across / sheet.interhelicalDistance,
        crossoversInAnchorPatch =
            PI * along * across / (sheet.interhelicalDistance * sheet.crossoverSpacing),
        duplexesInAnchorPatch = 2.0 * across / sheet.interhelicalDistance,
        continuumValidByMatchedCriterion = along > sheet.crossoverSpacing &&
                across > sheet.interhelicalDistance
    )
}

private fun foundationCase(
    multiplier: Double,
    sheet: OrigamiSheet,
    plate: OrthotropicPlate,
    pressure: Double
): LatticeFoundationCase {
    val foundation = Gen1Tile.FOUNDATION_SECANT * multiplier
    val stroke = pressure / foundation
    val free = lattice(sheet, foundation)
    val freePlate = PlateOnFoundation(plate, foundation, basisDegree = PLATE_BASIS_DEGREE)

    val uniform = free.solve(uniformPressure(pressure)) to
            freePlate.solve(uniformPressure(pressure))
    val taper = edgeTaperedPressure(pressure, plate, Gen1Tile.DEBYE_LENGTH, EDGE_TAPER_DEPTH)
    val tapered = free.solve(taper) to freePlate.solve(taper)
    val shallow = edgeTaperedPressure(pressure, plate, Gen1Tile.DEBYE_LENGTH, SHALLOW_TAPER_DEPTH)
    val shallowTapered = free.solve(shallow) to freePlate.solve(shallow)

    val anchorPositions = insetGrid(2, plate.lengthX, plate.lengthY)
    val anchorEach = foundation * plate.area / anchorPositions.size
    val anchoredLattice = lattice(
        sheet, foundation, anchorPositions.map { (x, y) -> PointSupport(x, y, anchorEach) }
    ).solve(uniformPressure(pressure))
    val anchoredPlate = PlateOnFoundation(
        plate, foundation,
        anchorPositions.map { (x, y) -> PointSupport(x, y, anchorEach) }, PLATE_BASIS_DEGREE
    ).solve(uniformPressure(pressure))

    val leverLoad = insetGrid(1, plate.lengthX, plate.lengthY).map { (x, y) ->
        PointLoad(x, y, Gen1Tile.TARGET_FORCE)
    }
    val lever = free.solve(pointLoads = leverLoad) to freePlate.solve(pointLoads = leverLoad)

    val sources = listOf(
        comparison(
            "uniform-load",
            "the leading-order load case: uniform pressure, uniform foundation, free edges",
            uniform.first, uniform.second, stroke
        ),
        comparison(
            "electrostatic-edge-taper",
            "pressure falling by ${(100 * EDGE_TAPER_DEPTH).toInt()}% over one Debye length " +
                    "at the rim — the finite-tile field effect as a bounded perturbation",
            tapered.first, tapered.second, stroke
        ),
        comparison(
            "electrostatic-edge-taper-shallow",
            "the same taper at ${(100 * SHALLOW_TAPER_DEPTH).toInt()}% depth, carried to show " +
                    "the lattice response is linear in the depth exactly as the plate's is",
            shallowTapered.first, shallowTapered.second, stroke
        ),
        comparison(
            "discrete-anchors",
            "4 tethers of total stiffness equal to the layer's own, reacting a uniform load " +
                    "at points — the §4(g) geometry",
            anchoredLattice, anchoredPlate, stroke
        ),
        comparison(
            "concentrated-lever-attachment",
            "the whole 100 pN leaving the tile through one attachment while entering distributed",
            lever.first, lever.second, stroke
        )
    )

    val thermalFoundation = Gen1Tile.FOUNDATION_AT_REST * multiplier
    val latticeThermal = lattice(sheet, thermalFoundation).thermalFluctuation()
    val plateThermal = PlateOnFoundation(plate, thermalFoundation, basisDegree = PLATE_BASIS_DEGREE)
        .thermalFluctuation()

    return LatticeFoundationCase(
        label = if (multiplier == 1.0) "C-0001 secant, 10 nm layer at sigma = 0.024 nm^-2"
        else "C-0001 secant x $multiplier",
        multiplier = multiplier,
        foundationStiffness = foundation,
        stroke = stroke,
        discreteness = discreteness(sheet, plate, foundation),
        sources = sources,
        thermal = ThermalComparison(
            foundationStiffness = thermalFoundation,
            latticePistonRms = latticeThermal.pistonRms,
            latticeTiltRms = latticeThermal.tiltRms,
            latticeDishingRms = latticeThermal.dishingRms,
            latticeCentreRms = latticeThermal.centreRms,
            platePistonRms = plateThermal.pistonRms,
            plateTiltRms = plateThermal.tiltRms,
            plateDishingRms = plateThermal.dishingRms,
            plateCentreRms = plateThermal.centreRms,
            dishingLatticeOverPlate = latticeThermal.dishingRms / plateThermal.dishingRms,
            centreLatticeOverPlate = latticeThermal.centreRms / plateThermal.centreRms,
            latticeDishingOverStroke = latticeThermal.dishingRms / stroke,
            plateIsConservativeAboutFlatness =
                latticeThermal.dishingRms > plateThermal.dishingRms
        ),
        anchored = ANCHOR_COUNTS.flatMap { count ->
            ANCHOR_STIFFNESS_FRACTIONS.map { fraction ->
                anchoredCase(count, fraction, sheet, plate, foundation, pressure, stroke)
            }
        },
        concentrated = LEVER_ATTACHMENTS.map { count ->
            concentratedCase(count, free, freePlate, plate, stroke)
        }
    )
}

private fun comparison(
    source: String,
    mechanism: String,
    latticeSolution: GrillageDeflection,
    plateSolution: PlateDeflection,
    stroke: Double
): DishingComparison {
    val latticePeak = latticeSolution.peakDishing()
    val platePeak = plateSolution.peakDishing()
    return DishingComparison(
        source = source,
        mechanism = mechanism,
        latticePeak = latticePeak,
        latticeRms = latticeSolution.dishingRms,
        platePeak = platePeak,
        plateRms = plateSolution.dishingRms,
        stroke = stroke,
        latticePeakOverStroke = latticePeak / stroke,
        platePeakOverStroke = platePeak / stroke,
        latticeOverPlate = if (platePeak > 1e-9) latticePeak / platePeak else 1.0,
        rigidPlateUpheld = latticePeak / stroke < RIGID_PLATE_TOLERANCE,
        plateIsConservativeAboutFlatness = latticePeak >= platePeak - 1e-9
    )
}

private fun anchoredCase(
    count: Int,
    fraction: Double,
    sheet: OrigamiSheet,
    plate: OrthotropicPlate,
    foundation: Double,
    pressure: Double,
    stroke: Double
): AnchoredLatticeCase {
    val side = sqrt(count.toDouble()).toInt()
    val positions = insetGrid(side, plate.lengthX, plate.lengthY)
    val each = fraction * foundation * plate.area / positions.size
    val supports = positions.map { (x, y) -> PointSupport(x, y, each) }
    val solution = lattice(sheet, foundation, supports).solve(uniformPressure(pressure))
    val plateSolution = PlateOnFoundation(plate, foundation, supports, PLATE_BASIS_DEGREE)
        .solve(uniformPressure(pressure))
    val peakAnchor = solution.supportForces.maxOf { abs(it) }
    val platePeakAnchor = plateSolution.supportForces.maxOf { abs(it) }
    val along = winklerLength(plate.rigidityX, foundation)
    val across = winklerLength(plate.rigidityY, foundation)
    // C-0006's equal-sharing contour: the anchor force spread over the load paths on an
    // ell-sized contour around it. The number C-0006 declined to turn into a peak.
    val contourPaths = 4.0 * along / sheet.crossoverSpacing +
            4.0 * across / sheet.interhelicalDistance
    val equalSharing = peakAnchor / contourPaths
    val peakCrossover = solution.peakCrossoverForce
    return AnchoredLatticeCase(
        anchorCount = positions.size,
        anchorStiffnessFraction = fraction,
        anchorStiffnessEach = each,
        peakAnchorForce = peakAnchor,
        platePeakAnchorForce = platePeakAnchor,
        anchorForceLatticeOverPlate = peakAnchor / platePeakAnchor,
        peakCrossoverForce = peakCrossover,
        peakCrossoverBand = structuralBand(peakCrossover).name,
        peakCrossoverVerdict = perPathVerdict(peakCrossover),
        peakHingeMoment = solution.peakHingeMoment,
        hingeMomentEquivalentForce = solution.peakHingeMoment / HINGE_MOMENT_LEVER_ARM,
        peakDuplexShear = solution.peakDuplexShear,
        equalSharingEstimate = equalSharing,
        contourPaths = contourPaths,
        concentrationFactor = peakCrossover / equalSharing,
        peakDishing = solution.peakDishing(),
        strokeLossFraction = 1.0 - solution.meanDeflection / stroke
    )
}

private fun concentratedCase(
    count: Int,
    free: OrigamiGrillage,
    freePlate: PlateOnFoundation,
    plate: OrthotropicPlate,
    stroke: Double
): ConcentratedLatticeCase {
    val side = sqrt(count.toDouble()).toInt()
    val loads = insetGrid(side, plate.lengthX, plate.lengthY).map { (x, y) ->
        PointLoad(x, y, Gen1Tile.TARGET_FORCE / count)
    }
    val solution = free.solve(pointLoads = loads)
    val plateSolution = freePlate.solve(pointLoads = loads)
    val peakCrossover = solution.peakCrossoverForce
    val peak = solution.peakDishing()
    return ConcentratedLatticeCase(
        attachmentCount = count,
        forcePerAttachment = Gen1Tile.TARGET_FORCE / count,
        peakCrossoverForce = peakCrossover,
        peakCrossoverBand = structuralBand(peakCrossover).name,
        peakCrossoverVerdict = perPathVerdict(peakCrossover),
        peakDuplexShear = solution.peakDuplexShear,
        peakHingeMoment = solution.peakHingeMoment,
        peakDishing = peak,
        platePeakDishing = plateSolution.peakDishing(),
        dishingOverStroke = peak / stroke,
        exceedsLayerHeight = solution.peakDeflection() > Gen1Tile.LAYER_HEIGHT
    )
}

private fun hingeCase(alpha: Double, pressure: Double): HingeCase {
    val sheet = sheetOf(alpha)
    val plate = plateOf(sheet)
    val foundation = Gen1Tile.FOUNDATION_SECANT
    val thermalFoundation = Gen1Tile.FOUNDATION_AT_REST
    val positions = insetGrid(2, plate.lengthX, plate.lengthY)
    val each = foundation * plate.area / positions.size
    val supports = positions.map { (x, y) -> PointSupport(x, y, each) }
    val anchored = lattice(sheet, foundation, supports).solve(uniformPressure(pressure))
    val anchoredPlate = PlateOnFoundation(plate, foundation, supports, PLATE_BASIS_DEGREE)
        .solve(uniformPressure(pressure))
    val latticeThermal = lattice(sheet, thermalFoundation).thermalFluctuation()
    val plateThermal = PlateOnFoundation(plate, thermalFoundation, basisDegree = PLATE_BASIS_DEGREE)
        .thermalFluctuation()
    val lever = lattice(sheet, foundation).solve(
        pointLoads = listOf(PointLoad(0.0, 0.0, Gen1Tile.TARGET_FORCE))
    )
    val across = winklerLength(plate.rigidityY, foundation)
    val along = winklerLength(plate.rigidityX, foundation)
    return HingeCase(
        label = if (alpha in ALPHA_SWEEP) "Chen et al. alpha = $alpha"
        else "OUT OF RANGE probe: alpha = %.2f, at which D_perp reaches D_par and the sheet is "
            .format(alpha) + "isotropic — the CH-0005 scenario in which C-0006 would be wrong",
        alpha = alpha,
        crossoverHingeStiffness = sheet.crossoverHingeStiffness,
        acrossHelixRigidity = sheet.acrossHelixRigidity,
        anisotropy = sheet.alongHelixRigidity / sheet.acrossHelixRigidity,
        winklerLengthAcrossHelix = across,
        alongHelixLengthOverCrossoverSpacing = along / sheet.crossoverSpacing,
        acrossHelixLengthOverInterhelicalDistance = across / sheet.interhelicalDistance,
        fourAnchorLatticePeakDishing = anchored.peakDishing(),
        fourAnchorPlatePeakDishing = anchoredPlate.peakDishing(),
        fourAnchorDishingRatio = anchored.peakDishing() / anchoredPlate.peakDishing(),
        thermalLatticeDishingRms = latticeThermal.dishingRms,
        thermalPlateDishingRms = plateThermal.dishingRms,
        thermalDishingRatio = latticeThermal.dishingRms / plateThermal.dishingRms,
        peakCrossoverForceFourAnchors = anchored.peakCrossoverForce,
        peakCrossoverForceOneLever = lever.peakCrossoverForce,
        peakDuplexShearOneLever = lever.peakDuplexShear
    )
}

private fun convergence(sheet: OrigamiSheet, pressure: Double): List<ConvergenceRecord> {
    val foundation = Gen1Tile.FOUNDATION_SECANT
    val plate = plateOf(sheet)
    val positions = insetGrid(2, plate.lengthX, plate.lengthY)
    val each = foundation * plate.area / positions.size
    val supports = positions.map { (x, y) -> PointSupport(x, y, each) }
    fun record(
        parameter: String,
        value: Double,
        subdivisions: Int = SUBDIVISIONS,
        columns: Int = CROSSOVER_COLUMNS,
        link: Double = OrigamiGrillage.RIGID_LINK_STIFFNESS
    ): ConvergenceRecord {
        val anchored = lattice(sheet, foundation, supports, subdivisions, columns, link)
            .solve(uniformPressure(pressure))
        val lever = lattice(sheet, foundation, emptyList(), subdivisions, columns, link)
            .solve(pointLoads = listOf(PointLoad(0.0, 0.0, Gen1Tile.TARGET_FORCE)))
        return ConvergenceRecord(
            parameter = parameter,
            value = value,
            peakCrossoverForce = lever.peakCrossoverForce,
            fourAnchorPeakDishing = anchored.peakDishing(),
            thermalDishingRms = lattice(
                sheet, Gen1Tile.FOUNDATION_AT_REST, emptyList(), subdivisions, columns, link
            ).thermalFluctuation().dishingRms
        )
    }
    return buildList {
        listOf(1, 2, 4).forEach { add(record("subdivisions", it.toDouble(), subdivisions = it)) }
        listOf(7, 8).forEach { add(record("crossoverColumns", it.toDouble(), columns = it)) }
        listOf(1e2, 1e3, 1e4, 1e5, 1e6).forEach { add(record("linkStiffness", it, link = it)) }
    }
}

/**
 * Where the anchor sits **within the unit cell** is a question the continuum plate cannot
 * even pose, and the answer is the size of the discreteness effect on the peak force.
 */
private fun anchorPhase(sheet: OrigamiSheet, pressure: Double): List<AnchorPhaseCase> {
    val foundation = Gen1Tile.FOUNDATION_SECANT
    val reference = lattice(sheet, foundation)
    val each = foundation * reference.area
    val d = sheet.interhelicalDistance
    val halfSpacing = sheet.crossoverSpacing / 2.0
    val placements = listOf(
        Triple("on a crossover", reference.columnX[CROSSOVER_COLUMNS / 2], d / 2.0),
        Triple("on a duplex axis, midway between crossover columns",
            reference.columnX[CROSSOVER_COLUMNS / 2] + halfSpacing / 2.0, 0.0),
        Triple("on a duplex axis, on a crossover column",
            reference.columnX[CROSSOVER_COLUMNS / 2], 0.0),
        Triple("midway between duplexes and between crossover columns",
            reference.columnX[CROSSOVER_COLUMNS / 2] + halfSpacing / 2.0, d / 2.0)
    )
    return placements.map { (name, x, y) ->
        val solution = lattice(sheet, foundation, listOf(PointSupport(x, y, each)))
            .solve(uniformPressure(pressure))
        AnchorPhaseCase(
            placement = name,
            x = x,
            y = y,
            peakAnchorForce = solution.supportForces.single(),
            peakCrossoverForce = solution.peakCrossoverForce,
            peakDuplexShear = solution.peakDuplexShear,
            peakDishing = solution.peakDishing()
        )
    }
}

/** The ten most loaded crossovers under a single central anchor, with their distances. */
private fun worstCrossovers(sheet: OrigamiSheet, pressure: Double): List<CrossoverRecord> {
    val foundation = Gen1Tile.FOUNDATION_SECANT
    val reference = lattice(sheet, foundation)
    val solution = lattice(
        sheet, foundation, listOf(PointSupport(0.0, 0.0, foundation * reference.area))
    ).solve(uniformPressure(pressure))
    return solution.crossoverForces
        .sortedByDescending { abs(it.verticalForce) }
        .take(10)
        .map {
            CrossoverRecord(
                x = it.x,
                y = it.y,
                distanceFromAnchor = hypot(it.x, it.y),
                verticalForce = it.verticalForce
            )
        }
}

/**
 * The smallest square array of lever attachments that keeps the dishing below
 * [RIGID_PLATE_TOLERANCE] of the stroke — **solved**, rather than taken from the continuum
 * patch heuristic `1.25 A/ℓ_eff²` that produced `C-0006`'s 55.
 */
private fun attachmentsForFlatness(
    peakDishing: (List<PointLoad>) -> Double,
    plate: OrthotropicPlate,
    stroke: Double
): Int = (1..MAXIMUM_ATTACHMENT_SIDE).map { it * it }.firstOrNull { count ->
    val loads = insetGrid(sqrt(count.toDouble()).toInt(), plate.lengthX, plate.lengthY)
        .map { (x, y) -> PointLoad(x, y, Gen1Tile.TARGET_FORCE / count) }
    peakDishing(loads) / stroke < RIGID_PLATE_TOLERANCE
} ?: (MAXIMUM_ATTACHMENT_SIDE * MAXIMUM_ATTACHMENT_SIDE)

/**
 * The dishing of both models against the number of lever attachments, so the "how many
 * attachments does flatness need" answer is a **curve** and not one threshold that a single
 * grid phase could have decided.
 */
private fun flatnessScan(
    sheet: OrigamiSheet,
    plate: OrthotropicPlate,
    stroke: Double
): List<FlatnessScanRecord> {
    val foundation = Gen1Tile.FOUNDATION_SECANT
    val free = lattice(sheet, foundation)
    val freePlate = PlateOnFoundation(plate, foundation, basisDegree = PLATE_BASIS_DEGREE)
    return (1..MAXIMUM_ATTACHMENT_SIDE).map { side ->
        val count = side * side
        val loads = insetGrid(side, plate.lengthX, plate.lengthY).map { (x, y) ->
            PointLoad(x, y, Gen1Tile.TARGET_FORCE / count)
        }
        val latticePeak = free.solve(pointLoads = loads).peakDishing()
        val platePeak = freePlate.solve(pointLoads = loads).peakDishing()
        FlatnessScanRecord(
            attachments = count,
            forcePerAttachment = Gen1Tile.TARGET_FORCE / count,
            latticePeakDishing = latticePeak,
            platePeakDishing = platePeak,
            latticeOverStroke = latticePeak / stroke,
            plateOverStroke = platePeak / stroke,
            latticeFlat = latticePeak / stroke < RIGID_PLATE_TOLERANCE,
            plateFlat = platePeak / stroke < RIGID_PLATE_TOLERANCE
        )
    }
}

private fun patchCount(
    grillage: OrigamiGrillage,
    sheet: OrigamiSheet,
    plate: OrthotropicPlate,
    nominal: LatticeFoundationCase
): PatchCount {
    val along = nominal.discreteness.winklerLengthAlongHelix
    val across = nominal.discreteness.winklerLengthAcrossHelix
    val foundation = Gen1Tile.FOUNDATION_SECANT
    val free = lattice(sheet, foundation)
    val freePlate = PlateOnFoundation(plate, foundation, basisDegree = PLATE_BASIS_DEGREE)
    val onLattice = attachmentsForFlatness(
        { loads -> free.solve(pointLoads = loads).peakDishing() }, plate, nominal.stroke
    )
    return PatchCount(
        continuumPatches = grillage.area / (along * across),
        latticeCrossovers = grillage.crossovers.size,
        latticeUnitCells = (grillage.lengthY / sheet.interhelicalDistance) *
                (grillage.lengthX / sheet.crossoverSpacing),
        independentBeams = grillage.lengthY / sheet.interhelicalDistance,
        flatnessAttachmentsHeuristicFromPlate = 55,
        flatnessAttachmentsSolvedOnPlate = attachmentsForFlatness(
            { loads -> freePlate.solve(pointLoads = loads).peakDishing() }, plate, nominal.stroke
        ),
        flatnessAttachmentsSolvedOnLattice = onLattice,
        attachmentsPerCrossover = onLattice.toDouble() / grillage.crossovers.size,
        note = "C-0006's 55 is the continuum heuristic ceil(1.25 A/ell_eff^2), not a solve. " +
                "Both models are solved here for the smallest square attachment array that " +
                "keeps the peak dishing below 10% of the stroke. The lattice's own natural " +
                "count is the number of crossovers it contains, because a crossover is the " +
                "only across-helix load path AND the only across-helix compliance: below one " +
                "attachment per crossover the load has to travel through the lattice to reach " +
                "an attachment, and the travel is what dishes it."
    )
}

private fun publishedComparison(
    nominal: LatticeFoundationCase,
    grillage: OrigamiGrillage,
    sheet: OrigamiSheet
): List<PublishedComparison> {
    fun record(
        quantity: String,
        c0006: Double,
        plateHere: Double,
        latticeHere: Double
    ) = PublishedComparison(
        quantity = quantity,
        c0006Value = c0006,
        plateOnLatticeFootprint = plateHere,
        lattice = latticeHere,
        latticeOverC0006 = latticeHere / c0006,
        footprintEffect = plateHere / c0006
    )
    fun source(name: String) = nominal.sources.first { it.source == name }
    val curvature = 1e-3
    val quadratic = 0.5 * curvature * curvature * grillage.area
    return listOf(
        record(
            "D_parallel [pN*nm]", C0006_D_PARALLEL, sheet.alongHelixRigidity,
            grillage.structuralEnergy(grillage.curvatureFieldAlongHelices(curvature)) / quadratic
        ),
        record(
            "D_perpendicular [pN*nm]", C0006_D_PERPENDICULAR, sheet.acrossHelixRigidity,
            grillage.structuralEnergy(grillage.curvatureFieldAcrossHelices(curvature)) / quadratic
        ),
        record("dishing, uniform load [nm]", 0.0, source("uniform-load").platePeak,
            source("uniform-load").latticePeak).copy(latticeOverC0006 = 1.0, footprintEffect = 1.0),
        record(
            "dishing, 50% edge taper [nm]", C0006_DISHING_EDGE_TAPER,
            source("electrostatic-edge-taper").platePeak,
            source("electrostatic-edge-taper").latticePeak
        ),
        record(
            "dishing, 4 anchors [nm]", C0006_DISHING_FOUR_ANCHORS,
            source("discrete-anchors").platePeak, source("discrete-anchors").latticePeak
        ),
        record(
            "dishing, 1 lever [nm]", C0006_DISHING_ONE_LEVER,
            source("concentrated-lever-attachment").platePeak,
            source("concentrated-lever-attachment").latticePeak
        ),
        record(
            "thermal dishing RMS [nm]", C0006_THERMAL_DISHING,
            nominal.thermal.plateDishingRms, nominal.thermal.latticeDishingRms
        ),
        record(
            "thermal point RMS at centre [nm]", C0006_THERMAL_CENTRE,
            nominal.thermal.plateCentreRms, nominal.thermal.latticeCentreRms
        )
    )
}

private fun verdicts(
    cases: List<LatticeFoundationCase>,
    grillage: OrigamiGrillage
): Map<String, String> {
    val nominal = cases.first { it.multiplier == 1.0 }
    val anchors = nominal.sources.first { it.source == "discrete-anchors" }
    val thermal = nominal.thermal
    val worstAnchored = cases.flatMap { it.anchored }.maxByOrNull { it.peakCrossoverForce }!!
    val worstLever = cases.flatMap { it.concentrated }.maxByOrNull { it.peakCrossoverForce }!!
    val leverRatios = cases.map {
        it.sources.first { s -> s.source == "concentrated-lever-attachment" }.latticeOverPlate
    }
    return mapOf(
        "continuum-plate-reduction" to (
                "The lattice dishes %.3f nm against the plate's %.3f nm under 4 discrete " +
                        "anchors, a ratio of %.2f, and fluctuates %.3f nm RMS against the " +
                        "plate's %.3f nm at 300 K, a ratio of %.2f. C-0006 predicted the plate " +
                        "would be conservative about flatness; the prediction is %s. Under a " +
                        "load delivered INTO the sheet at a point the lattice is softer than " +
                        "the plate by %.0f-%.0f%% across the sweep, so there the plate is " +
                        "conservative as C-0006 said."
                ).format(
                anchors.latticePeak, anchors.platePeak, anchors.latticeOverPlate,
                thermal.latticeDishingRms, thermal.plateDishingRms,
                thermal.dishingLatticeOverPlate,
                if (anchors.plateIsConservativeAboutFlatness &&
                    thermal.plateIsConservativeAboutFlatness
                ) "CONFIRMED on both" else "CONFIRMED for the thermal case and REFUTED for the " +
                        "anchored one",
                100.0 * (leverRatios.min() - 1.0), 100.0 * (leverRatios.max() - 1.0)
            ),
        "matched-discreteness-criterion" to (
                "C-0006 compared ell_perp against p, which pairs the ACROSS-helix bending " +
                        "length with the ALONG-helix hinge spacing. Direction-matched, the two " +
                        "criteria are ell_par/p = %.2f and ell_perp/d = %.2f at the nominal " +
                        "foundation. An anchor's influence patch contains %.1f crossovers."
                ).format(
                nominal.discreteness.alongHelixLengthOverCrossoverSpacing,
                nominal.discreteness.acrossHelixLengthOverInterhelicalDistance,
                nominal.discreteness.crossoversInAnchorPatch
            ),
        "peak-per-load-path-force" to (
                "The worst anchored case in the sweep puts %.2f pN on a single crossover " +
                        "against C-0006's %.2f pN equal-sharing estimate, a concentration " +
                        "factor of %.2f; %s. A single concentrated lever attachment puts " +
                        "%.1f pN on one crossover and %.1f pN of transverse shear in one " +
                        "duplex; %s."
                ).format(
                worstAnchored.peakCrossoverForce, worstAnchored.equalSharingEstimate,
                worstAnchored.concentrationFactor,
                perPathVerdict(worstAnchored.peakCrossoverForce),
                worstLever.peakCrossoverForce, worstLever.peakDuplexShear,
                perPathVerdict(worstLever.peakCrossoverForce)
            ),
        "crossover-count" to (
                "The 40 x %.2f nm tile holds %d crossovers as a lattice, against the %.1f the " +
                        "continuum areal density 1/(dp) implies and the 55 attachment points " +
                        "C-0006 found flatness needs. The flatness requirement is therefore " +
                        "roughly one attachment per crossover."
                ).format(
                grillage.lengthY, grillage.crossovers.size,
                grillage.area / (grillage.interhelicalDistance * grillage.crossoverSpacing)
            )
    )
}

// --------------------------------------------------------------------------- console report

private fun report(result: LatticeResult, output: File) {
    println("T-10 / A8.2 + A1.2 — ${result.title}")
    println("300 K, aqueous buffer, k_BT = ${"%.3f".format(thermalEnergy())} pN*nm")
    val g = result.geometry
    println(
        "lattice: %d duplexes x %d crossover columns = %d crossovers, %d DOF, %.1f x %.2f nm"
            .format(g.beamCount, g.crossoverColumns, g.crossovers, g.degreesOfFreedom,
                g.lengthX, g.lengthY)
    )
    println()
    println("--- gate 2: long-wavelength rigidity recovery ".padEnd(110, '-'))
    println("%28s %14s %14s %14s".format("quantity", "lattice", "plate", "ratio"))
    result.rigidityRecovery.forEach {
        println("%28s %14.6e %14.6e %14.6f".format(
            it.quantity, it.latticeEnergy, it.plateEnergy, it.ratio
        ))
    }
    println()
    println("--- dishing, lattice against plate, by source ".padEnd(110, '-'))
    println("%7s %34s %11s %11s %9s %9s".format(
        "k_f x", "source", "lattice[nm]", "plate[nm]", "ratio", "/stroke"
    ))
    result.cases.forEach { case ->
        case.sources.forEach {
            println("%7.2f %34s %11.4f %11.4f %9.3f %9.3f".format(
                case.multiplier, it.source, it.latticePeak, it.platePeak,
                it.latticeOverPlate, it.latticePeakOverStroke
            ))
        }
    }
    println()
    println("--- thermal fluctuation at 300 K, unloaded ".padEnd(110, '-'))
    println("%7s %10s %10s %10s %10s %10s %9s".format(
        "k_f x", "piston", "L dish", "P dish", "L centre", "P centre", "dishL/P"
    ))
    result.cases.forEach {
        val t = it.thermal
        println("%7.2f %10.3f %10.3f %10.3f %10.3f %10.3f %9.3f".format(
            it.multiplier, t.latticePistonRms, t.latticeDishingRms, t.plateDishingRms,
            t.latticeCentreRms, t.plateCentreRms, t.dishingLatticeOverPlate
        ))
    }
    println()
    println("--- peak per-load-path force at a discrete anchor, nominal k_f ".padEnd(110, '-'))
    val nominal = result.cases.first { it.multiplier == 1.0 }
    println("%8s %8s %10s %12s %12s %12s %10s".format(
        "anchors", "k_a/k_f", "anchor[pN]", "crossover[pN]", "duplexV[pN]", "equalShare", "factor"
    ))
    nominal.anchored.forEach {
        println("%8d %8.1f %10.2f %12.3f %12.3f %12.3f %10.2f".format(
            it.anchorCount, it.anchorStiffnessFraction, it.peakAnchorForce,
            it.peakCrossoverForce, it.peakDuplexShear, it.equalSharingEstimate,
            it.concentrationFactor
        ))
    }
    println()
    println("--- concentrated lever attachment, nominal k_f ".padEnd(110, '-'))
    println("%12s %12s %14s %12s %14s".format(
        "attachments", "each[pN]", "crossover[pN]", "duplexV[pN]", "verdict"
    ))
    nominal.concentrated.forEach {
        println("%12d %12.2f %14.3f %12.3f   %s".format(
            it.attachmentCount, it.forcePerAttachment, it.peakCrossoverForce,
            it.peakDuplexShear, it.peakCrossoverVerdict
        ))
    }
    println()
    println("--- k_theta sweep, the largest open premise ".padEnd(110, '-'))
    println("%7s %10s %12s %12s %10s %12s".format(
        "alpha", "k_theta", "D_perp", "dish L/P", "therm L/P", "crossover[pN]"
    ))
    result.hingeCases.forEach {
        println("%7.2f %10.3f %12.4f %12.3f %10.3f %12.3f".format(
            it.alpha, it.crossoverHingeStiffness, it.acrossHelixRigidity,
            it.fourAnchorDishingRatio, it.thermalDishingRatio, it.peakCrossoverForceFourAnchors
        ))
    }
    println()
    println("--- convergence ".padEnd(110, '-'))
    println("%18s %10s %14s %14s %14s".format(
        "parameter", "value", "crossover[pN]", "dishing[nm]", "thermal[nm]"
    ))
    result.convergence.forEach {
        println("%18s %10.0f %14.4f %14.4f %14.4f".format(
            it.parameter, it.value, it.peakCrossoverForce, it.fourAnchorPeakDishing,
            it.thermalDishingRms
        ))
    }
    println()
    println("--- anchor placement within the unit cell ".padEnd(110, '-'))
    result.anchorPhase.forEach {
        println("%54s  anchor %6.2f pN  crossover %6.3f pN  duplexV %6.3f pN".format(
            it.placement, it.peakAnchorForce, it.peakCrossoverForce, it.peakDuplexShear
        ))
    }
    println()
    result.verdict.forEach { (key, value) -> println("$key: $value"); println() }
    println("written: ${output.path}")
    val uniform = nominal.sources.first { it.source == "uniform-load" }
    check(uniform.latticePeak < 1e-9) {
        "the uniform-load case must dish the lattice by nothing at all; " +
                "the assembly is wrong if it does not"
    }
}
