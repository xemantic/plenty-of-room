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

package com.xemantic.nano.plentyofroom.anchoring

import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.ShearJointAllowable
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.PI
import kotlin.math.abs

/**
 * Task `T-40` / leaf `A8.2` — the base of `C-0025`'s normal duplex standoff.
 *
 * ```shell
 * tools/study.sh anchoring.StandoffBaseJointStudyKt
 * ```
 *
 * Emits `gpd/results/T-40-standoff-base-joint.json`, deterministically: the file carries no
 * timestamp and the whole tree is rounded at the **serialisation boundary**.
 *
 * Infinities are a modelling idealisation (an ideal clamp, a held end, a stroke never reached)
 * and `kotlinx.serialization` refuses them, so every such field emits [INFINITE_SENTINEL].
 */

private const val INFINITE_SENTINEL = -1.0

private fun finite(value: Double): Double =
    if (value.isInfinite() || value.isNaN()) INFINITE_SENTINEL else value

@Serializable
data class BaseMotifRecord(
    val id: String,
    val name: String,
    val rotationalStiffness: Double,
    val axialStiffness: Double,
    val transverseDeadBand: Double,
    val buildable: Boolean,
    val restraintAtDesignLength: Double,
    val headRotationalFraction: Double,
    val swayFraction: Double,
    val supportFraction: Double,
    val provenance: String
)

@Serializable
data class BasedStandoffRecord(
    val baseId: String,
    val baseName: String,
    val standoffLength: Double,
    val standoffBasePairs: Double,
    val baseRestraint: Double,
    val headRotationalStiffness: Double,
    val swayStiffness: Double,
    val transverseStiffness: Double,
    val anisotropy: Double,
    val transverseDeadBand: Double,
    val span: Double,
    val spanBasePairs: Double,
    val restraint: Double,
    val midspanFactor: Double,
    val effectiveStretchFraction: Double,
    val secantStiffness: Double,
    val tangentStiffness: Double,
    val tangentToSecant: Double,
    val axialTensionAcceptable: Double,
    val axialTensionDesired: Double,
    val dutyHeldPoint: Double,
    val dutyDesiredMandate: Double,
    val dutyDesiredElement: Double,
    val dutyRatio: Double,
    val bucklingFreeHead: Double,
    val bucklingBeamSymmetric: Double,
    val bucklingBeamAntisymmetric: Double,
    val bucklingGuidedHead: Double,
    val bucklingMarginFreeHead: Double,
    val bucklingMarginBeamSymmetric: Double,
    val bucklingStrokeFreeHead: Double,
    val supportMargin: Double,
    val bondedLengthDesired: Double,
    val standoffDeflectionDesired: Double,
    val standoffDeflectionRatio: Double,
    val p1Supports: Boolean,
    val p2Placed: Boolean,
    val p3Compliant: Boolean,
    val p4Safe: Boolean,
    val p5Buildable: Boolean,
    val p6Stable: Boolean,
    val allPredicatesPass: Boolean,
    val verdict: String
)

@Serializable
data class BaseSweepRecord(
    val rotationalStiffness: Double,
    val crossoverEquivalents: Double,
    val baseRestraint: Double,
    val headRotationalStiffness: Double,
    val swayStiffness: Double,
    val span: Double,
    val midspanFactor: Double,
    val effectiveStretchFraction: Double,
    val tangentStiffness: Double,
    val axialTensionDesired: Double,
    val dutyDesiredElement: Double,
    val bucklingFreeHead: Double,
    val bucklingMarginFreeHead: Double,
    val compliantCeilingPass: Boolean,
    val stablePass: Boolean
)

@Serializable
data class BucklingCornerRecord(
    val baseCondition: String,
    val headCondition: String,
    val wavenumber: Double,
    val effectiveLengthFactor: Double,
    val criticalLoadAtDesignLength: Double,
    val note: String
)

@Serializable
data class ThresholdRecord(
    val standoffLength: Double,
    val stroke: Double,
    val headCondition: String,
    val requiredRotationalStiffness: Double,
    val crossoverEquivalents: Double,
    val requiredBaseRestraint: Double,
    val metByOneCrossover: Boolean,
    val metByTwoFavourable: Boolean
)

@Serializable
data class DutyCorrectionRecord(
    val standoffLength: Double,
    val mandateDuty: Double,
    val elementDuty: Double,
    val ratio: Double,
    val publishedMargin: Double,
    val correctedMargin: Double
)

@Serializable
data class OffDiagonalRecord(
    val baseId: String,
    val standoffLength: Double,
    val correlation: Double,
    val factor: Double,
    val note: String
)

@Serializable
data class BaseSensitivityRecord(
    val axis: String,
    val value: Double,
    val label: String,
    val baseRotationalStiffness: Double,
    val span: Double,
    val tangentStiffness: Double,
    val bucklingFreeHead: Double,
    val dutyDesiredElement: Double,
    val bucklingMarginFreeHead: Double,
    val allPredicatesPass: Boolean
)

@Serializable
data class BaseConvergenceRecord(
    val quantity: String,
    val control: String,
    val level: Double,
    val value: Double,
    val departure: Double
)

@Serializable
data class BaseReproductionRecord(
    val quantity: String,
    val source: String,
    val published: Double,
    val derived: Double,
    val relativeDeparture: Double
)

@Serializable
data class LiteratureRecord(
    val question: String,
    val finding: String,
    val flag: String,
    val source: String
)

@Serializable
data class StandoffBaseJointResult(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val parameters: Map<String, String>,
    val bases: List<BaseMotifRecord>,
    val bucklingCorners: List<BucklingCornerRecord>,
    val designs: List<BasedStandoffRecord>,
    val baseSweep: List<BaseSweepRecord>,
    val thresholds: List<ThresholdRecord>,
    val dutyCorrection: List<DutyCorrectionRecord>,
    val offDiagonal: List<OffDiagonalRecord>,
    val sensitivities: List<BaseSensitivityRecord>,
    val convergence: List<BaseConvergenceRecord>,
    val reproductions: List<BaseReproductionRecord>,
    val literature: List<LiteratureRecord>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val citedNumbers: List<String>
)

// ---------------------------------------------------------------------------------------------

private const val TARGET_FORCE = 100.0

/** §3's acceptable stroke — and `C-0019`/`C-0023`'s **held** gap, `L₀ − 3 nm`. */
private const val ACCEPTABLE_STROKE = 3.0

/** §3's **desired** stroke. */
private const val DESIRED_STROKE = 10.0

private const val MANDATE = TARGET_FORCE / ACCEPTABLE_STROKE

private const val PATH_COUNT = 45

private const val COMPLIANT_CEILING = 40.0

private const val SUPPORT_MARGIN_REQUIRED = 10.0

private const val DEAD_BAND_ALLOWED = 0.1

/** `C-0025`'s own design length, and where the base constants are tabulated. */
private const val DESIGN_LENGTH = 8.0

private val EI = Gen1Tile.DUPLEX_BENDING_RIGIDITY

private val STRETCH = Gen1Tile.DUPLEX_STRETCH_MODULUS

private val RISE = Gen1Tile.RISE_PER_BASE_PAIR

private val ALLOWABLE = ShearJointAllowable()

private val PER_PATH = MANDATE / PATH_COUNT

private val STANDOFF_LENGTHS = listOf(3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)

private val BASES: List<Pair<String, StandoffBase>> = listOf(
    "B0" to StandoffBase.idealClamp(),
    "B1" to StandoffBase.crossovers(1),
    "B2u" to StandoffBase.crossovers(2, favourableOrientation = false),
    "B2" to StandoffBase.crossovers(2, favourableOrientation = true),
    "B3" to StandoffBase.crossovers(3, favourableOrientation = true),
    "B4" to StandoffBase.nickedContinuation(),
    "B5-2" to StandoffBase.polyTJunction(2),
    "B5-10" to StandoffBase.polyTJunction(10)
)

private fun bondedLengthOrSentinel(tension: Double): Double =
    if (tension <= 0.0) 0.0
    else if (tension >= ALLOWABLE.saturationForce) INFINITE_SENTINEL
    else bondedLengthForTension(tension, ALLOWABLE)

private fun designFor(id: String, base: StandoffBase, length: Double): BasedStandoffRecord {
    val joint = basedNormalStandoff(length, base)
    val restraintBase = baseRestraintParameter(base.rotationalStiffness, EI, length)
    val span = flexureSpanForJoint(EI, joint, PATH_COUNT, MANDATE, ACCEPTABLE_STROKE, STRETCH)
    val flexure = PartiallyRestrainedFlexure(EI, span, joint, STRETCH)
    val secant = PATH_COUNT * flexure.secantStiffness(ACCEPTABLE_STROKE)
    val tangent = PATH_COUNT * flexure.tangentStiffness(ACCEPTABLE_STROKE)
    val dutyHeld = flexure.endShear(ACCEPTABLE_STROKE)
    val dutyMandate = MANDATE * DESIRED_STROKE / PATH_COUNT / 2.0
    val dutyElement = flexure.endShear(DESIRED_STROKE)
    val headSymmetric = beamHeadRestraint(length, span, 2.0)
    val headAntisymmetric = beamHeadRestraint(length, span, 6.0)
    val free = standoffBucklingLoad(EI, length, restraintBase, 0.0)
    val symmetric = standoffBucklingLoad(EI, length, restraintBase, headSymmetric)
    val antisymmetric = standoffBucklingLoad(EI, length, restraintBase, headAntisymmetric)
    val guided = standoffBucklingLoad(EI, length, restraintBase, Double.POSITIVE_INFINITY)
    val tensionDesired = flexure.axialTension(DESIRED_STROKE)
    val shareDesired = MANDATE * DESIRED_STROKE / PATH_COUNT
    // the standoff's own head deflection is half the flexure's total draw-in demand
    val headDeflection = flexure.drawInDemand(DESIRED_STROKE) / 2.0
    val p1 = base.buildable && joint.transverseDeadBand <= DEAD_BAND_ALLOWED &&
            joint.transverseStiffness >= SUPPORT_MARGIN_REQUIRED * PER_PATH
    val p2 = abs(secant - MANDATE) <= 1.0e-6 * MANDATE
    val p3 = tangent <= COMPLIANT_CEILING
    val p4 = tensionDesired <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE &&
            shareDesired <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE
    val p5 = base.buildable && length <= 10.0 && span <= 60.0
    val p6 = free >= dutyElement
    val verdict = when {
        !base.buildable -> "FAIL — the motif does not exist as a 90 degree junction; the " +
                "numbers in this row are the IDEAL CLAMP's, i.e. what it would have delivered"
        !p1 -> "FAIL P1 — the base does not support the standoff"
        !p3 -> "FAIL P3 — tangent past the 40 pN/nm compliance ceiling"
        !p4 -> "FAIL P4 — beam tension past the 10 pN unzip allowable at 10 nm"
        !p5 -> "FAIL P5 — outside C-0017's buildable envelope"
        !p6 -> "FAIL P6 — the standoff buckles before the desired stroke"
        else -> "PASS"
    }
    return BasedStandoffRecord(
        baseId = id,
        baseName = base.name,
        standoffLength = length,
        standoffBasePairs = length / RISE,
        baseRestraint = finite(restraintBase),
        headRotationalStiffness = joint.rotationalStiffness,
        swayStiffness = joint.axialStiffness,
        transverseStiffness = joint.transverseStiffness,
        anisotropy = finite(joint.anisotropy),
        transverseDeadBand = joint.transverseDeadBand,
        span = span,
        spanBasePairs = span / RISE,
        restraint = finite(flexure.restraint),
        midspanFactor = flexure.midspanFactor,
        effectiveStretchFraction = flexure.effectiveStretchModulus / STRETCH,
        secantStiffness = secant,
        tangentStiffness = tangent,
        tangentToSecant = tangent / secant,
        axialTensionAcceptable = flexure.axialTension(ACCEPTABLE_STROKE),
        axialTensionDesired = tensionDesired,
        dutyHeldPoint = dutyHeld,
        dutyDesiredMandate = dutyMandate,
        dutyDesiredElement = dutyElement,
        dutyRatio = dutyElement / dutyMandate,
        bucklingFreeHead = free,
        bucklingBeamSymmetric = symmetric,
        bucklingBeamAntisymmetric = antisymmetric,
        bucklingGuidedHead = guided,
        bucklingMarginFreeHead = free / dutyElement,
        bucklingMarginBeamSymmetric = symmetric / dutyElement,
        bucklingStrokeFreeHead = finite(bucklingStroke(flexure, free)),
        supportMargin = joint.transverseStiffness / PER_PATH,
        bondedLengthDesired = bondedLengthOrSentinel(tensionDesired),
        standoffDeflectionDesired = headDeflection,
        standoffDeflectionRatio = headDeflection / length,
        p1Supports = p1,
        p2Placed = p2,
        p3Compliant = p3,
        p4Safe = p4,
        p5Buildable = p5,
        p6Stable = p6,
        allPredicatesPass = p1 && p2 && p3 && p4 && p5 && p6,
        verdict = verdict
    )
}

private fun sweepPoint(rotational: Double, length: Double): BaseSweepRecord {
    val base = StandoffBase("sweep", rotational, Gen1Tile.crossoverInPlaneStiffness())
    val joint = basedNormalStandoff(length, base)
    val restraint = baseRestraintParameter(rotational, EI, length)
    val span = flexureSpanForJoint(EI, joint, PATH_COUNT, MANDATE, ACCEPTABLE_STROKE, STRETCH)
    val flexure = PartiallyRestrainedFlexure(EI, span, joint, STRETCH)
    val tangent = PATH_COUNT * flexure.tangentStiffness(ACCEPTABLE_STROKE)
    val duty = flexure.endShear(DESIRED_STROKE)
    val critical = standoffBucklingLoad(EI, length, restraint, 0.0)
    return BaseSweepRecord(
        rotationalStiffness = finite(rotational),
        crossoverEquivalents = finite(rotational / Gen1Tile.crossoverHingeStiffness()),
        baseRestraint = finite(restraint),
        headRotationalStiffness = joint.rotationalStiffness,
        swayStiffness = joint.axialStiffness,
        span = span,
        midspanFactor = flexure.midspanFactor,
        effectiveStretchFraction = flexure.effectiveStretchModulus / STRETCH,
        tangentStiffness = tangent,
        axialTensionDesired = flexure.axialTension(DESIRED_STROKE),
        dutyDesiredElement = duty,
        bucklingFreeHead = critical,
        bucklingMarginFreeHead = critical / duty,
        compliantCeilingPass = tangent <= COMPLIANT_CEILING,
        stablePass = critical >= duty
    )
}

/**
 * The bending rigidity implied by Fields et al.'s **measured** buckling of a naked duplex —
 * a 40.5 bp strand losing its resistance to a 9 pN compressive load in a sway-prevented vise,
 * so `EI = P_c L²/π²`. The only direct measurement of a *short* duplex in axial compression
 * this programme has found, and it is at exactly the standoff's length scale.
 */
private val FIELDS_IMPLIED_BENDING_RIGIDITY: Double =
    9.0 * (40.5 * RISE) * (40.5 * RISE) / (PI * PI)

private val FIELDS_IMPLIED_PERSISTENCE: Double =
    FIELDS_IMPLIED_BENDING_RIGIDITY / com.xemantic.nano.plentyofroom.thermalEnergy()

private val LITERATURE: List<LiteratureRecord> = listOf(
    LiteratureRecord(
        question = "Is a duplex standing NORMAL to a single-layer origami sheet an established " +
                "motif?",
        finding = "NOT ESTABLISHED. Out-of-plane duplexes on single-layer sheets appear in the " +
                "literature only as hairpin or staple-extension OVERHANGS attached at a single " +
                "point: Rothemund's own dumbbell hairpins give about 1.5 nm of extra AFM height " +
                "(\"labelled staples give greater height contrast (3 nm above the mica) than " +
                "unlabelled staples\"), and the oxDNA study of duplex overhangs on a rectangular " +
                "origami finds they \"behave more like wider stiff rods\" that lie over and " +
                "interact with the sheet strongly enough to curve the whole tile. Perpendicular " +
                "helices in origami are perpendicular WITHIN the plane (gridiron four-arm " +
                "junctions) or between stacked layers (layered crossovers); the gridiron paper " +
                "states outright that the standard crossover motif \"has been restricted by a " +
                "double-crossover motif to form parallel helices\". Six independent zero-hit " +
                "phrase searches across EuropePMC full text and arXiv returned nothing.",
        flag = "read directly (Rothemund 2006 PDF; arXiv:2302.09109v3) + abstract only " +
                "(Han et al. Science 339:1412, 2013 and Hong et al. Angew Chem 55, 2016, both " +
                "verbatim from Crossref)",
        source = "https://www.dna.caltech.edu/Papers/DNAorigami-nature.pdf ; " +
                "https://arxiv.org/pdf/2302.09109v3 ; " +
                "https://api.crossref.org/works/10.1126/science.1232252"
    ),
    LiteratureRecord(
        question = "What holds such an element's base in practice?",
        finding = "A PIN, in every published instance. Marras et al.'s origami revolute joints " +
                "are \"joined along an edge by flexible ssDNA scaffold connections\", " +
                "\"2 nt in all cases\", and the paper says plainly that \"the hinge axes are not " +
                "ideally constrained\". Lauback et al.'s rotor is mounted to its platform \"via " +
                "a single base-pairing interaction flanked by two ssDNA bases on either side for " +
                "rotational flexibility\". Kopperger et al.'s six-helix arm is \"connected to a " +
                "55 nm-by-55 nm DNA origami plate via flexible single-stranded scaffold " +
                "crossovers\". Wireframe vertices use \"3 ssnts\" or \"5 ssnts\". Where an " +
                "out-of-plane element IS held rigidly, it is triangulated rather than clamped: " +
                "Pumm et al.'s inclined obstacle plates \"were held rigidly at this angle with a " +
                "SET of double-helical spacers\". NO publication was found describing a " +
                "rotationally stiff, clamped base for a single duplex leaving a sheet.",
        flag = "read directly (Marras PNAS 112:713 2015; Lauback Nat Commun 9:1446 2018; " +
                "Pumm Nature 607:492 2022; Madhvacharyula Nat Commun 16 2025) + abstract only " +
                "(Kopperger Science 359:296 2018, verbatim from Crossref)",
        source = "https://pmc.ncbi.nlm.nih.gov/articles/PMC4311804/ ; " +
                "https://www.ebi.ac.uk/europepmc/webservices/rest/PMC5899095/fullTextXML ; " +
                "https://www.ebi.ac.uk/europepmc/webservices/rest/PMC9300469/fullTextXML"
    ),
    LiteratureRecord(
        question = "Does any measurement of such a joint's rotational stiffness exist?",
        finding = "For an IN-PLANE origami hinge, yes: Marras et al. measure \"a stiffness " +
                "(slope in Fig. 3F) of 25 pN-nm/rad that increases to 45 pN-nm/rad at angles " +
                "greater than about 100 degrees or less than about 40\", from \"918 structures " +
                "in TEM images\", for a joint of SIX 2 nt ssDNA connections between two 18-helix " +
                "bundles. Per connection that is 4.17 pN nm/rad, against C-0025's modelled 3.345 " +
                "for the same 2 nt — agreement to 25 %, and the only measurement that constant " +
                "has ever had. For an element NORMAL to a plate: NOT FOUND. Marras report only " +
                "that \"a few hinges were observed in the perpendicular orientation ... and " +
                "revealed there was little out-of-plane rotation\", with no number; Lauback " +
                "report out-of-plane angular fluctuations of \"+/-12 to +/-6 degrees\" for a " +
                "magnetically loaded lever, with no stiffness quoted.",
        flag = "read directly",
        source = "https://pmc.ncbi.nlm.nih.gov/articles/PMC4311804/ ; " +
                "https://www.ebi.ac.uk/europepmc/webservices/rest/PMC5899095/fullTextXML"
    ),
    LiteratureRecord(
        question = "Is there a measurement of a SHORT duplex under axial compression?",
        finding = "YES, and it is at exactly this length scale. Fields, Meyer & Cohen built a " +
                "\"molecular vise\" and found that \"short dsDNA strands (<41 base pairs) " +
                "resisted this force and remained straight; longer strands became bent\", the " +
                "load being the measured 9 pN A-T unzipping force, with the transition \"in good " +
                "agreement with the buckling length predicted by linear elasticity\". Inverting " +
                "Euler on their own 40.5 bp gives EI = %.1f pN nm^2, i.e. a persistence length " +
                "of %.1f nm — inside the 40-47 nm MEASURED band and %.0f %% below CanDo's " +
                "55.5 nm model input, exactly the direction CLAUDE.md records. Liedl et al.'s " +
                "tensegrity paper supplies the origami-context form, \"Fc = pi^2 P kBT/L^2\", " +
                "but for 128 nm six-helix bundles. NOT FOUND: any measurement or simulation of a " +
                "single duplex under 20 nm in axial compression inside an origami."
            .format(
                FIELDS_IMPLIED_BENDING_RIGIDITY, FIELDS_IMPLIED_PERSISTENCE,
                100.0 * (1.0 - FIELDS_IMPLIED_PERSISTENCE / 55.5)
            ),
        flag = "read directly",
        source = "https://www.ebi.ac.uk/europepmc/webservices/rest/PMC3834817/fullTextXML ; " +
                "https://www.ebi.ac.uk/europepmc/webservices/rest/PMC2898913/fullTextXML"
    ),
    LiteratureRecord(
        question = "What the literature does NOT settle",
        finding = "Buchl et al. (Biophys J 121, 2022) promise \"energy landscapes\" of a rotor " +
                "mounted on an origami plate from fluorescence particle tracking; the abstract " +
                "was read verbatim but the full text was blocked, so no number is taken from it. " +
                "EuropePMC full-text indexing covers open access only, so a closed ACS or Wiley " +
                "paper using this geometry in its results section would be invisible to the " +
                "phrase searches above. The negative is therefore a strong absence, not a proof.",
        flag = "abstract only",
        source = "https://www.ebi.ac.uk/europepmc/webservices/rest/search?query=PMCID:PMC9808541"
    )
)

private val VALIDITY: List<String> = listOf(
    "TRL 1-3. NOTHING HERE IS MEASURED. No base joint has been built and none is a sequence " +
            "design; base pairs make the statement concrete, they do not specify a staple.",
    "THE MOTIF IS NOT ESTABLISHED IN THE LITERATURE. Every published out-of-plane element on an " +
            "origami body is held by a PIN, and the base constants here are therefore a MODEL of " +
            "a joint nobody has built, composed from Chen et al.'s softened bond.",
    "The two joint springs are still treated as INDEPENDENT, exactly as in C-0025. The " +
            "off-diagonal is BOUNDED here (correlation, and a factor of exactly 4 at a clamped " +
            "base) and ARGUED to soften the joint, which makes P3 conservative and P6 NOT " +
            "conservative. It is not closed.",
    "The head restraint is a BRACKET, not a number: free (adopted, conservative), the beam's own " +
            "2EI/L and 6EI/L, and guided. The held-head reading is NOT available, because the " +
            "standoff's sway is the flexure's draw-in.",
    "The standoff's head deflection at the desired stroke is 20-46 % of its own length, past " +
            "small deflection, so every 10 nm column is a LOWER bound on the tension exactly as " +
            "C-0023 and C-0025 flag.",
    "EI = 230 pN nm^2 is a CanDo MODEL INPUT, not a measurement, and Fields et al.'s measured " +
            "buckling implies 25 % less. Every buckling load here is therefore the OPTIMISTIC " +
            "end, and the P6 verdicts would tighten on the measured rigidity.",
    "k_theta = 2 alpha B/(100 a) is C-0009's CITED, FITTED constant, swept over Chen et al.'s own " +
            "alpha in [0.6, 1.2]; k_s = 2 alpha S/(100 a) is C-0020's DERIVED construction and is " +
            "NOT measured, swept over its own four decades. No verdict moves across either.",
    "The three-crossover base B3 asks a 2 nm duplex to span a 5.38 nm footprint and is reported " +
            "as a bound rather than recommended.",
    "One flexure per load path and 45 attachments, exactly as C-0023 and C-0025 assume."
)

private val OPEN_QUESTIONS: List<String> = listOf(
    "The fully coupled 2 x 2 tip compliance of the standoff — C-0025's open question 1, bounded " +
            "here and not closed. Its sign argument says the joint is softer than modelled, " +
            "which makes the buckling verdict optimistic.",
    "Whether a 90 degree scaffold or staple routing between a sheet duplex and a normal standoff " +
            "exists at all. The literature has no instance, and a nicked continuation cannot " +
            "supply it because a nick preserves the helix axis.",
    "Whether the design should abandon the single-duplex standoff for a TRIANGULATED one — the " +
            "only rigid out-of-plane mounting the literature actually shows (Pumm et al.'s \"set " +
            "of double-helical spacers\"). It would remove the buckling problem and cost the " +
            "draw-in release the standoff exists for.",
    "k_s, C-0020's derived crossover in-plane constant, which the two-crossover base's whole " +
            "couple rests on. T-9.",
    "Whether the sheet itself can react the standoff's base moment. This task grounds the base " +
            "on the crossovers that tie it to the sheet and treats the sheet beyond them as " +
            "rigid; C-0009's grillage says it is not."
)

private val CITED: List<String> = listOf(
    "duplex EI = 230 pN nm^2 — CITED, a CanDo MODEL INPUT (Kim et al., NAR 40:2862, 2012), NOT a " +
            "measurement; Fields et al.'s measured buckling implies 25 % less",
    "duplex stretch modulus S = 1100 pN — CITED, MEASURED, Wang et al., Biophys. J. 72:1335 (1997)",
    "crossover hinge k_theta = 2 alpha B/(100 a) = 13.53 pN nm/rad, alpha in [0.6, 1.2] — CITED, " +
            "FITTED, Chen et al., JACS 136:6995 (2014) SI S2, via C-0009",
    "crossover in-plane k_s = 2 alpha S/(100 a) = 64.71 pN/nm — DERIVED (C-0020), NOT measured",
    "interhelical distance 2.69 nm — CITED, MEASURED by SAXS, Fischer et al. (2016)",
    "rise per base pair 0.34 nm — CITED, Douglas et al. (2009)",
    "ssDNA Kuhn length 2.10 nm (zero-force end) and 0.65 nm per nucleotide — CITED, MEASURED, " +
            "Chen et al., PNAS 109:799 (2012), via C-0025",
    "origami hinge stiffness 25 / 45 / 70 pN nm/rad for six 2 nt ssDNA connections — CITED, " +
            "MEASURED by TEM angular distribution over 918 structures, Marras et al., PNAS " +
            "112:713 (2015), READ DIRECTLY. Used only as a cross-check, never as an input",
    "duplex buckling at 40-41 bp under 9 pN — CITED, MEASURED, Fields, Meyer & Cohen, NAR " +
            "41:9881 (2013), READ DIRECTLY. Used only as a cross-check, never as an input",
    "10 pN unzip and 65 pN nicked ceiling — CITED via C-0006",
    "the shear allowable's constants — CITED, MEASURED, Strunz et al., PNAS 96:11277 (1999), via " +
            "C-0024/CH-0029, used only inside 16-4000 pN/s",
    "section 3 targets 100 pN, 3 nm, 10 nm, 40 x 40 nm, 2 mM — CITED",
    "C-0025's J5-8 design (span 31.64 nm, c = 95.64, tangent 37.39 pN/nm, 8.87 / 35.5 pN " +
            "buckling) — CITED, and reproduced here as gate-5 tests"
)

private fun reproduction(
    quantity: String,
    source: String,
    published: Double,
    derived: Double
): BaseReproductionRecord = BaseReproductionRecord(
    quantity = quantity,
    source = source,
    published = published,
    derived = derived,
    relativeDeparture = abs(derived - published) / abs(published)
)

fun main() {
    val bases = BASES.map { (id, base) ->
        val restraint = baseRestraintParameter(base.rotationalStiffness, EI, DESIGN_LENGTH)
        val joint = basedNormalStandoff(DESIGN_LENGTH, base)
        BaseMotifRecord(
            id = id,
            name = base.name,
            rotationalStiffness = finite(base.rotationalStiffness),
            axialStiffness = finite(base.axialStiffness),
            transverseDeadBand = base.transverseDeadBand,
            buildable = base.buildable,
            restraintAtDesignLength = finite(restraint),
            headRotationalFraction = joint.rotationalStiffness / (EI / DESIGN_LENGTH),
            swayFraction = joint.axialStiffness /
                    (3.0 * EI / (DESIGN_LENGTH * DESIGN_LENGTH * DESIGN_LENGTH)),
            supportFraction = joint.transverseStiffness / (STRETCH / DESIGN_LENGTH),
            provenance = base.provenance
        )
    }

    val inf = Double.POSITIVE_INFINITY
    val corners = listOf(
        Triple("clamped", "free", Pair(inf, 0.0)),
        Triple("clamped", "guided", Pair(inf, inf)),
        Triple("pinned", "guided", Pair(0.0, inf)),
        Triple("pinned", "free", Pair(0.0, 0.0))
    ).map { (baseCondition, headCondition, springs) ->
        val u = swayColumnWavenumber(springs.first, springs.second)
        BucklingCornerRecord(
            baseCondition = baseCondition,
            headCondition = headCondition,
            wavenumber = u,
            effectiveLengthFactor = if (u <= 1.0e-9) INFINITE_SENTINEL else PI / u,
            criticalLoadAtDesignLength =
                standoffBucklingLoad(EI, DESIGN_LENGTH, springs.first, springs.second),
            note = if (u <= 1.0e-9)
                "a MECHANISM, not a strut: P_c = 0 exactly, and it is the pinned-base limit " +
                        "C-0025's 4x bracket does not contain"
            else "K = ${"%.3f".format(PI / u)}"
        )
    }

    val designs = BASES.flatMap { (id, base) ->
        if (!base.buildable) listOf(designFor(id, StandoffBase.idealClamp().copy(
            name = base.name, buildable = false, provenance = base.provenance
        ), DESIGN_LENGTH).copy(baseId = id))
        else STANDOFF_LENGTHS.map { designFor(id, base, it) }
    }

    val crossover = Gen1Tile.crossoverHingeStiffness()
    val sweepValues = listOf(
        0.25, 0.5, 1.0, 2.0, 4.0, 8.0, 16.0, 32.0, 64.0, 128.0, 512.0, 4096.0
    ).map { it * crossover }
    val baseSweep = sweepValues.map { sweepPoint(it, DESIGN_LENGTH) } +
            sweepPoint(1.0e12, DESIGN_LENGTH)

    val thresholds = STANDOFF_LENGTHS.map { length ->
        val required = baseRotationalStiffnessThreshold(length, DESIRED_STROKE)
        ThresholdRecord(
            standoffLength = length,
            stroke = DESIRED_STROKE,
            headCondition = "free head — the conservative reading",
            requiredRotationalStiffness = finite(required),
            crossoverEquivalents = finite(required / crossover),
            requiredBaseRestraint = finite(baseRestraintParameter(required, EI, length)),
            metByOneCrossover = required <= StandoffBase.crossovers(1).rotationalStiffness,
            metByTwoFavourable = required <=
                    StandoffBase.crossovers(2, favourableOrientation = true).rotationalStiffness
        )
    }

    // CH-0037 — C-0025 read the standoff's duty at the desired stroke on the MANDATE secant,
    // while the flexure it supports strain-stiffens.
    val dutyCorrection = STANDOFF_LENGTHS.map { length ->
        val joint = basedNormalStandoff(length, StandoffBase.idealClamp())
        val span = flexureSpanForJoint(EI, joint, PATH_COUNT, MANDATE, ACCEPTABLE_STROKE, STRETCH)
        val flexure = PartiallyRestrainedFlexure(EI, span, joint, STRETCH)
        val mandate = MANDATE * DESIRED_STROKE / PATH_COUNT / 2.0
        val element = flexure.endShear(DESIRED_STROKE)
        val critical = standoffBucklingLoad(EI, length, inf, 0.0)
        DutyCorrectionRecord(
            standoffLength = length,
            mandateDuty = mandate,
            elementDuty = element,
            ratio = element / mandate,
            publishedMargin = critical / mandate,
            correctedMargin = critical / element
        )
    }

    val offDiagonal = listOf("B0" to inf, "B1" to crossover, "B2" to
            StandoffBase.crossovers(2, favourableOrientation = true).rotationalStiffness)
        .flatMap { (id, stiffness) ->
            listOf(5.0, DESIGN_LENGTH, 10.0).map { length ->
                OffDiagonalRecord(
                    baseId = id,
                    standoffLength = length,
                    correlation = offDiagonalCorrelation(EI, length, stiffness),
                    factor = offDiagonalFactor(EI, length, stiffness),
                    note = "the OTHER-DISPLACEMENT-FIXED reading over the OTHER-LOAD-ZERO one " +
                            "that C-0025 and this task both use; the sign of the real coupling " +
                            "SOFTENS the joint, so P3 is conservative and P6 is not"
                )
            }
        }

    val designBase = StandoffBase.crossovers(2, favourableOrientation = true)
    val sensitivities =
        listOf(Gen1Tile.CROSSOVER_ALPHA_MIN, 1.0, Gen1Tile.CROSSOVER_ALPHA_MAX).map { alpha ->
            val base = StandoffBase.crossovers(2, favourableOrientation = true, alpha = alpha)
            sensitivity("Chen et al. alpha", alpha, "alpha = $alpha", base)
        } + Gen1Tile.CROSSOVER_IN_PLANE_SWEEP.map { multiplier ->
            val base = StandoffBase.crossovers(
                2, favourableOrientation = true, inPlaneMultiplier = multiplier
            )
            sensitivity("k_s multiplier", multiplier, "k_s x $multiplier", base)
        }

    val convergence = listOf(64, 128, 256, 512, 2048).map { steps ->
        val reference = swayColumnWavenumber(0.4706, 0.5, scanSteps = 8192)
        val value = swayColumnWavenumber(0.4706, 0.5, scanSteps = steps)
        BaseConvergenceRecord(
            quantity = "sway column wavenumber at rho_b = 0.4706, rho_h = 0.5",
            control = "scanSteps",
            level = steps.toDouble(),
            value = value,
            departure = abs(value - reference)
        )
    } + listOf(64, 256, 1024).map { steps ->
        val joint = basedNormalStandoff(DESIGN_LENGTH, designBase)
        val reference =
            flexureSpanForJoint(EI, joint, PATH_COUNT, MANDATE, ACCEPTABLE_STROKE, STRETCH, 4096)
        val value =
            flexureSpanForJoint(EI, joint, PATH_COUNT, MANDATE, ACCEPTABLE_STROKE, STRETCH, steps)
        BaseConvergenceRecord(
            quantity = "solved span at the B2 design",
            control = "scanSteps",
            level = steps.toDouble(),
            value = value,
            departure = abs(value - reference)
        )
    }

    val clampedAt8 = designs.first { it.baseId == "B0" && it.standoffLength == DESIGN_LENGTH }
    val designAt8 = designs.first { it.baseId == "B2" && it.standoffLength == DESIGN_LENGTH }
    val reproductions = listOf(
        reproduction("C-0025 J5-8 span [nm]", "C-0025", 31.6403748, clampedAt8.span),
        reproduction("C-0025 J5-8 midspan factor c", "C-0025", 95.6390226, clampedAt8.midspanFactor),
        reproduction(
            "C-0025 J5-8 tangent [pN/nm]", "C-0025", 37.3911226, clampedAt8.tangentStiffness
        ),
        reproduction(
            "C-0025 J5-8 beam tension at 10 nm [pN]", "C-0025", 3.82799407,
            clampedAt8.axialTensionDesired
        ),
        reproduction(
            "C-0025 J5-8 transverse support [pN/nm]", "C-0025", 137.5,
            basedNormalStandoff(DESIGN_LENGTH, StandoffBase.idealClamp()).transverseStiffness
        ),
        reproduction(
            "C-0025 standoff buckling, pinned head, 8 nm [pN]", "C-0025", 8.8672227,
            standoffBucklingLoad(EI, DESIGN_LENGTH, inf, 0.0)
        ),
        reproduction(
            "C-0025 standoff buckling, guided head, 8 nm [pN]", "C-0025", 35.4688908,
            standoffBucklingLoad(EI, DESIGN_LENGTH, inf, inf)
        ),
        reproduction(
            "C-0014 eulerBucklingLoad, K = 2, 8 nm [pN]", "C-0014",
            eulerBucklingLoad(EI, DESIGN_LENGTH, BeamEndCondition.PINNED_HEAD),
            standoffBucklingLoad(EI, DESIGN_LENGTH, inf, 0.0)
        ),
        reproduction(
            "C-0014 eulerBucklingLoad, K = 1, 8 nm [pN]", "C-0014",
            eulerBucklingLoad(EI, DESIGN_LENGTH, BeamEndCondition.GUIDED_HEAD),
            standoffBucklingLoad(EI, DESIGN_LENGTH, inf, inf)
        ),
        reproduction(
            "C-0009 crossover hinge stiffness [pN nm/rad]", "C-0009", 13.5294118, crossover
        ),
        reproduction(
            "C-0020 crossover in-plane stiffness [pN/nm]", "C-0009/C-0020", 64.7058824,
            Gen1Tile.crossoverInPlaneStiffness()
        ),
        reproduction(
            "C-0025 J3-2 hinge axial stiffness [pN/nm]", "C-0025", 4.552,
            StandoffBase.polyTJunction(2).axialStiffness
        ),
        // Fields et al. (NAR 41:9881, 2013) MEASURED a naked duplex buckling at 40-41 bp under a
        // 9 pN compressive load, in a sway-prevented (pinned-pinned) vise. Inverting Euler on
        // their own measurement gives the bending rigidity their duplex actually had.
        reproduction(
            "duplex persistence length implied by Fields et al.'s measured buckling [nm]",
            "Fields, Meyer & Cohen, NAR 41:9881 (2013), READ DIRECTLY",
            41.7, FIELDS_IMPLIED_PERSISTENCE
        ),
        reproduction(
            "buckling length at 9 pN from CanDo's EI [bp]",
            "Fields et al. measured 40-41 bp; CanDo's EI is the STIFF end",
            40.5, PI * kotlin.math.sqrt(EI / 9.0) / RISE
        ),
        // Marras et al. (PNAS 112:713, 2015) MEASURED an origami hinge of six 2 nt ssDNA
        // connections at 25 pN nm/rad. Per connection that is 4.17, against C-0025's modelled
        // 3.345 for the same 2 nt of ssDNA — the only measurement this constant has ever had.
        reproduction(
            "2 nt ssDNA rotational constant per connection [pN nm/rad]",
            "Marras et al., PNAS 112:713 (2015), 25 pN nm/rad over six connections, READ DIRECTLY",
            25.0 / 6.0, StandoffBase.polyTJunction(2).rotationalStiffness
        ),
        reproduction(
            "CH-0029 shear allowable at 30 bp [pN]", "CH-0029", 47.107,
            ALLOWABLE.ruptureForce(30.0, ShearJointAllowable.REFERENCE_LOADING_RATE)
        ),
        reproduction(
            "off-diagonal correlation at a clamped base", "derived, sqrt(3)/2",
            kotlin.math.sqrt(3.0) / 2.0, offDiagonalCorrelation(EI, DESIGN_LENGTH, inf)
        ),
        reproduction(
            "off-diagonal factor at a clamped base", "derived, exactly 4",
            4.0, offDiagonalFactor(EI, DESIGN_LENGTH, inf)
        )
    )

    val passing = designs.filter { it.allPredicatesPass }
    val passingByBase = passing.groupBy { it.baseId }
        .mapValues { (_, rows) -> rows.map { it.standoffLength } }

    val findings = mapOf(
        "the base is not a clamp, and one division says so" to (
                "rho_b = k_theta_base l/EI at C-0009's crossover constant is " +
                        "%.3f at 3 nm and %.3f at 10 nm, so a single crossover delivers %.1f %% "
                            .format(
                                baseRestraintParameter(crossover, EI, 3.0),
                                baseRestraintParameter(crossover, EI, 10.0),
                                100.0 * bases.first { it.id == "B1" }.headRotationalFraction
                            ) +
                        "of the clamp C-0025 assumed, %.1f %% of its sway stiffness and %.1f %% "
                            .format(
                                100.0 * bases.first { it.id == "B1" }.swayFraction,
                                100.0 * bases.first { it.id == "B1" }.supportFraction
                            ) +
                        "of its transverse support. All three of C-0025's standoff constants are " +
                        "the rho_b -> infinity limit of a series it did not write."
                ),
        "the pinned limit is a mechanism, not a smaller number" to (
                "A column with a pin at its base and a free head has P_c = 0 EXACTLY. So the " +
                        "buckling load is not bracketed by C-0025's 8.87 and 35.5 pN at all: it " +
                        "runs to zero, and the only question is how fast."
                ),
        "the base moves the design in two opposite directions" to (
                ("A softer base releases more draw-in, so the membrane term collapses and the " +
                        "tangent FALLS: at a single crossover it is %.2f pN/nm at 3 nm and " +
                        "%.2f at 10 nm, inside the 40 pN/nm ceiling at EVERY length, where the " +
                        "clamped base fails it below 7 nm. The same softness collapses the Euler " +
                        "load faster than the duty, so P6 FAILS at every length. The window is " +
                        "not narrowed or widened by the base: it is re-cut by a different pair " +
                        "of constraints, and the binding one is buckling.").format(
                    designs.first { it.baseId == "B1" && it.standoffLength == 3.0 }.tangentStiffness,
                    designs.first { it.baseId == "B1" && it.standoffLength == 10.0 }
                        .tangentStiffness
                )
                ),
        "the base's ORIENTATION is worth 9.65x and it decides the design" to (
                ("Two crossovers to adjacent duplexes give k_theta_base = %.1f pN nm/rad when " +
                        "the pair is laid across the flexure and %.1f when it is laid along it " +
                        "— the couple k_s d^2/2 is the whole of the first and absent from the " +
                        "second. Same two staples, same %.1f pN/nm of axial support, and the " +
                        "difference between a design that passes at 7-10 nm and one that passes " +
                        "nowhere.").format(
                    StandoffBase.crossovers(2, favourableOrientation = true).rotationalStiffness,
                    StandoffBase.crossovers(2, favourableOrientation = false).rotationalStiffness,
                    StandoffBase.crossovers(2).axialStiffness
                )
                ),
        "the replacement window" to (
                ("Over all buildable base motifs and C-0025's eight standoff lengths, the " +
                        "designs passing all six predicates are: " +
                        passingByBase.entries.joinToString("; ") { (id, lengths) ->
                            "$id at ${lengths.joinToString(", ") { "%.0f".format(it) }} nm"
                        } + ". C-0025's 7-10 nm window SURVIVES, but only on a base of at least " +
                        "TWO crossovers in the FAVOURABLE orientation (B2), where it widens " +
                        "downward to 6 nm and its buckling margin runs %.2fx at 6 nm to %.2fx " +
                        "at 10 nm. The recommended design is B2 at 7-9 nm, whose margin stays " +
                        "above %.2fx; the 10 nm end is retained only nominally. B2u's single " +
                        "pass at 4 nm is a knife-edge, margin %.4fx, and is not a design.").format(
                    designs.first { it.baseId == "B2" && it.standoffLength == 6.0 }
                        .bucklingMarginFreeHead,
                    designs.first { it.baseId == "B2" && it.standoffLength == 10.0 }
                        .bucklingMarginFreeHead,
                    designs.first { it.baseId == "B2" && it.standoffLength == 9.0 }
                        .bucklingMarginFreeHead,
                    designs.first { it.baseId == "B2u" && it.standoffLength == 4.0 }
                        .bucklingMarginFreeHead
                )
                ),
        "the threshold, which is the deliverable" to (
                ("The base rotational stiffness a standoff needs so that its critical load " +
                        "reaches its own duty at the desired stroke runs " +
                        "%.1f pN nm/rad at 3 nm to %.1f at 10 nm, i.e. %.2f to %.2f crossover " +
                        "equivalents. A SINGLE crossover meets it at no length; two in the " +
                        "favourable orientation meet it at every one.").format(
                    thresholds.first().requiredRotationalStiffness,
                    thresholds.last().requiredRotationalStiffness,
                    thresholds.first().crossoverEquivalents,
                    thresholds.last().crossoverEquivalents
                )
                ),
        "CH-0037 — the duty was read on the mandate, not on the element" to (
                ("C-0025's endShearDesired is MANDATE x 10/45/2 = 3.7037 pN for every design, " +
                        "which is the secant reading. The flexure strain-stiffens, so its own " +
                        "reaction at 10 nm is larger: the element duty is %.3f to %.3f pN over " +
                        "the 7-10 nm window, a ratio of %.3f to %.3f. Every buckling margin in " +
                        "C-0025's window table is optimistic by that factor, and at a clamped " +
                        "base the 10 nm margin falls from %.2f to %.2f.").format(
                    dutyCorrection.first { it.standoffLength == 10.0 }.elementDuty,
                    dutyCorrection.first { it.standoffLength == 7.0 }.elementDuty,
                    dutyCorrection.first { it.standoffLength == 10.0 }.ratio,
                    dutyCorrection.first { it.standoffLength == 7.0 }.ratio,
                    dutyCorrection.first { it.standoffLength == 10.0 }.publishedMargin,
                    dutyCorrection.first { it.standoffLength == 10.0 }.correctedMargin
                )
                ),
        "the sway IS the draw-in, so the held-head reading is not available" to (
                ("The standoff's head translation in the flexure's plane is the column's sway " +
                        "and the flexure's draw-in under two names. Holding it against sway is " +
                        "C-0023's ends-held-axially reading, whose 91.13 pN/nm tangent is what " +
                        "the whole of T-30 was spent escaping. So the buckling bracket runs from " +
                        "a free head to a guided one and no further, and the beam's own end " +
                        "restraint 2EI/L puts the realised reading at rho_h = %.3f — nearer the " +
                        "free end, which is why the free-head reading is adopted as the " +
                        "predicate. At that realised restraint the B2 design at 8 nm has " +
                        "%.2f pN of critical load against %.3f of duty, a %.2fx margin.").format(
                    beamHeadRestraint(DESIGN_LENGTH, designAt8.span, 2.0),
                    designAt8.bucklingBeamSymmetric,
                    designAt8.dutyDesiredElement,
                    designAt8.bucklingMarginBeamSymmetric
                )
                ),
        "the literature: the motif is NOT established, and every published base is a PIN" to (
                "No publication was found in which a duplex stands normal to a single-layer " +
                        "origami sheet as a mechanical stand-off. Out-of-plane duplexes on such " +
                        "sheets exist only as hairpin or staple-extension overhangs attached at " +
                        "a single point; perpendicular helices in origami are perpendicular " +
                        "WITHIN the plane; and every body standing on an origami plate is joined " +
                        "by flexible ssDNA, by many distributed hybridisation anchors, or by " +
                        "shape complementarity. Where an out-of-plane element IS held rigidly it " +
                        "is TRIANGULATED — Pumm et al.'s inclined plates \"were held rigidly at " +
                        "this angle with a SET of double-helical spacers\". So the pinned base " +
                        "this task shows to be a mechanism is not a pessimistic corner: it is " +
                        "the only base condition the literature actually demonstrates, and the " +
                        "remedy the literature actually demonstrates is a truss, not a stiffer " +
                        "joint. See the literature block for the flag on every statement."
                ),
        "and the buckling model has a direct measurement behind it" to (
                ("Fields, Meyer & Cohen measured a naked duplex losing its resistance to a 9 pN " +
                        "compressive load at 40-41 bp in a sway-prevented vise. Inverting Euler " +
                        "on that gives EI = %.1f pN nm^2, a persistence length of %.1f nm — " +
                        "inside the 40-47 nm measured band and %.0f %% below CanDo's 55.5 nm " +
                        "model input. Every buckling load in this task uses the CanDo EI and is " +
                        "therefore the OPTIMISTIC end by that factor; scaling the critical load " +
                        "alone (a re-solve would also shorten the span) takes the B2 design's " +
                        "margin at 10 nm from %.2f to %.2f — below one. That is the second " +
                        "independent reason to read the window as 7-9 nm.").format(
                    FIELDS_IMPLIED_BENDING_RIGIDITY,
                    FIELDS_IMPLIED_PERSISTENCE,
                    100.0 * (1.0 - FIELDS_IMPLIED_PERSISTENCE / 55.5),
                    designs.first { it.baseId == "B2" && it.standoffLength == 10.0 }
                        .bucklingMarginFreeHead,
                    designs.first { it.baseId == "B2" && it.standoffLength == 10.0 }
                        .bucklingMarginFreeHead * FIELDS_IMPLIED_BENDING_RIGIDITY / EI
                )
                ),
        "a flexible base fails the same way a flexible hinge did" to (
                ("A 2 nt poly-T base gives the standoff %.2f pN/nm of transverse support against " +
                        "the %.3f that ten times the beam's own per-path stiffness demands, and " +
                        "a %.2f nm dead band. CH-0031's convexity statement one level down, and " +
                        "it fails P1 at every length.").format(
                    designs.first { it.baseId == "B5-2" && it.standoffLength == DESIGN_LENGTH }
                        .transverseStiffness,
                    SUPPORT_MARGIN_REQUIRED * PER_PATH,
                    StandoffBase.polyTJunction(2).transverseDeadBand
                )
                ),
        "a nicked continuation cannot turn a corner" to (
                "C-0025 establishes that a single nick is a clamp and a double nick is a " +
                        "crossover, and both are statements about a duplex continuing along its " +
                        "OWN axis. A nick preserves the helix axis, so there is no B-form " +
                        "geometry in which a duplex continues through a nick at 90 degrees to " +
                        "itself. The stiffest joint in C-0025's catalogue is structurally " +
                        "unavailable as a BASE, and that removes the obvious way to build one."
                ),
        "the off-diagonal, bounded rather than solved" to (
                ("A cantilever's tip translation and tip rotation are correlated at exactly " +
                        "sqrt(3)/2 = %.4f at a clamped base and %.4f at a crossover base, and " +
                        "the other-displacement-fixed reading exceeds the other-load-zero one " +
                        "that both C-0025 and this task use by exactly %.1f and %.2f. The sign " +
                        "of the real coupling SOFTENS the joint, so P3 is conservative and P6 " +
                        "is NOT. This is C-0025's open question 1 and it is bounded here, not " +
                        "closed.").format(
                    offDiagonalCorrelation(EI, DESIGN_LENGTH, inf),
                    offDiagonalCorrelation(EI, DESIGN_LENGTH, crossover),
                    offDiagonalFactor(EI, DESIGN_LENGTH, inf),
                    offDiagonalFactor(EI, DESIGN_LENGTH, crossover)
                )
                )
    )

    val result = StandoffBaseJointResult(
        task = "T-40",
        leaf = "A8.2",
        title = "The base of C-0025's normal duplex standoff, modelled rather than assumed",
        verificationType = "in-silico (C-0025's partial-restraint machinery one level down, " +
                "with a two-spring sway-column buckling eigenvalue solved rather than assumed) " +
                "+ logical (the standoff's sway and the flexure's draw-in are one coordinate) " +
                "+ literature (whether the motif is established at all)",
        acceptance = "P1 support >= 10x the beam with the base in series and no dead band; " +
                "P2 secant placed at 33.3333 pN/nm exactly; P3 tangent <= 40 pN/nm; " +
                "P4 beam tension <= 10 pN unzip at the desired stroke; P5 inside C-0017's " +
                "envelope; P6 P_c >= the standoff's own compression duty at the desired stroke, " +
                "the duty read from the element's reaction and P_c from the free-head reading",
        maturity = "TRL 1-3. Model-consistent and traceable. NOTHING HERE IS MEASURED.",
        units = mapOf(
            "length" to "nm", "force" to "pN", "stiffness" to "pN/nm",
            "rotational stiffness" to "pN nm/rad", "bending rigidity" to "pN nm^2",
            "moment" to "pN nm", "energy" to "pN nm and k_BT"
        ),
        conventions = listOf(
            "z runs from the standoff's base (on the sheet) to its head (under the beam end)",
            "x runs along the flexure's axis toward midspan; the standoff bends in the x-z plane",
            "the base is a joint in T-30's sense: (k_theta_base, k_z_base, dead band)",
            "the standoff's SWAY and the flexure's DRAW-IN are the same coordinate, so the " +
                    "held-head buckling reading is not available to this design",
            "a buckling margin is quoted against the ELEMENT's own end shear, never the mandate " +
                    "secant",
            "T = 300 K, aqueous 2 mM MgCl2, k_BT = 4.141947 pN nm"
        ),
        parameters = mapOf(
            "duplex EI" to "${EI} pN nm^2 — CITED, a CanDo MODEL INPUT (Kim et al. 2012)",
            "duplex S" to "${STRETCH} pN — CITED, MEASURED (Wang et al. 1997)",
            "crossover k_theta" to "%.4f pN nm/rad — CITED, FITTED (Chen et al. 2014, via C-0009)"
                .format(crossover),
            "crossover k_s" to "%.4f pN/nm — DERIVED (C-0020), NOT measured"
                .format(Gen1Tile.crossoverInPlaneStiffness()),
            "interhelical distance" to "${Gen1Tile.INTERHELICAL_SHEET} nm — CITED, MEASURED by " +
                    "SAXS (Fischer et al. 2016)",
            "path count" to "$PATH_COUNT — C-0015's 3 x 15 grid, via C-0023",
            "mandate" to "%.4f pN/nm — section 3's 100 pN over 3 nm, via C-0017".format(MANDATE),
            "compliance ceiling" to "$COMPLIANT_CEILING pN/nm — C-0023's own declared ceiling",
            "held operating point" to "the 3 nm stroke, i.e. C-0019/C-0023's held gap L0 - 3 nm"
        ),
        bases = bases,
        bucklingCorners = corners,
        designs = designs,
        baseSweep = baseSweep,
        thresholds = thresholds,
        dutyCorrection = dutyCorrection,
        offDiagonal = offDiagonal,
        sensitivities = sensitivities,
        convergence = convergence,
        reproductions = reproductions,
        literature = LITERATURE,
        findings = findings,
        validity = VALIDITY,
        openQuestions = OPEN_QUESTIONS,
        citedNumbers = CITED
    )

    val json = Json { prettyPrint = true; encodeDefaults = true }
    val file = File("gpd/results/T-40-standoff-base-joint.json")
    file.parentFile.mkdirs()
    file.writeText(json.encodeToString(json.encodeToJsonElement(result).roundedForResult()) + "\n")
    report(result, file)
}

private fun sensitivity(
    axis: String,
    value: Double,
    label: String,
    base: StandoffBase
): BaseSensitivityRecord {
    val record = designFor("sensitivity", base, DESIGN_LENGTH)
    return BaseSensitivityRecord(
        axis = axis,
        value = value,
        label = label,
        baseRotationalStiffness = base.rotationalStiffness,
        span = record.span,
        tangentStiffness = record.tangentStiffness,
        bucklingFreeHead = record.bucklingFreeHead,
        dutyDesiredElement = record.dutyDesiredElement,
        bucklingMarginFreeHead = record.bucklingMarginFreeHead,
        allPredicatesPass = record.allPredicatesPass
    )
}

private fun report(result: StandoffBaseJointResult, file: File) {
    println("T-40 — the base of C-0025's normal duplex standoff")
    println()
    println("bases (k_theta, k_z, dead band, rho_b at 8 nm, and the fraction of C-0025 left)")
    result.bases.forEach {
        println(
            "  %-6s %-42s %10.3f %9.3f %5.2f %8.4f %7.3f %7.3f %7.3f %s".format(
                it.id, it.name.take(42), it.rotationalStiffness, it.axialStiffness,
                it.transverseDeadBand, it.restraintAtDesignLength, it.headRotationalFraction,
                it.swayFraction, it.supportFraction, if (it.buildable) "" else "NOT BUILDABLE"
            )
        )
    }
    println()
    println("buckling corners (base, head, u, K, P_c at 8 nm)")
    result.bucklingCorners.forEach {
        println(
            "  %-8s %-8s %8.5f %8.3f %9.3f  %s".format(
                it.baseCondition, it.headCondition, it.wavenumber, it.effectiveLengthFactor,
                it.criticalLoadAtDesignLength, it.note
            )
        )
    }
    println()
    println("designs (base, l, span, c, tangent, T10, duty10, Pc_free, margin, s_buckle, verdict)")
    result.designs.forEach {
        println(
            "  %-6s %5.1f %7.2f %7.2f %8.2f %7.2f %7.3f %8.2f %7.2f %7.2f  %s".format(
                it.baseId, it.standoffLength, it.span, it.midspanFactor, it.tangentStiffness,
                it.axialTensionDesired, it.dutyDesiredElement, it.bucklingFreeHead,
                it.bucklingMarginFreeHead, it.bucklingStrokeFreeHead, it.verdict
            )
        )
    }
    println()
    println("the duty correction (CH-0037): l, mandate, element, ratio, published, corrected")
    result.dutyCorrection.forEach {
        println(
            "  %5.1f %8.4f %8.4f %7.3f %8.2f %8.2f".format(
                it.standoffLength, it.mandateDuty, it.elementDuty, it.ratio,
                it.publishedMargin, it.correctedMargin
            )
        )
    }
    println()
    println("thresholds: l, k_theta_base required, crossover equivalents, met by 1 / 2 favourable")
    result.thresholds.forEach {
        println(
            "  %5.1f %12.3f %8.2f  %-5s %-5s".format(
                it.standoffLength, it.requiredRotationalStiffness, it.crossoverEquivalents,
                it.metByOneCrossover, it.metByTwoFavourable
            )
        )
    }
    println()
    println("reproductions (published, derived, departure)")
    result.reproductions.forEach {
        println(
            "  %-52s %12.5f %12.5f %10.2e".format(
                it.quantity.take(52), it.published, it.derived, it.relativeDeparture
            )
        )
    }
    println()
    result.findings.forEach { (key, value) -> println("$key:\n  $value\n") }
    println("written to $file")
}
