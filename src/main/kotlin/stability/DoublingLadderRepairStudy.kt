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

package com.xemantic.nano.plentyofroom.stability

import com.xemantic.nano.plentyofroom.actuator.roundedForActuatorResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs

/**
 * Task `T-159` — **repair the doubling force ladder, and reprice `CH-0099` at the other 96 states.**
 *
 * Emits `gpd/results/T-159-doubling-ladder-repair.json`.
 *
 * Reads `gpd/results/T-149-recommended-element-fold.json` (the 108 published fold rows),
 * `gpd/results/T-60-collar-on-the-equilibrium-path.json` (`C-0033`'s measured `d ln μ/dh`) and
 * `gpd/data/T-159-downstream-diff.json` (the classified re-run of every study that consumes the
 * changed source, produced by `tools/T-159-result-diff.py`).
 */

private const val T159_ELASTICA_LINE = "LQ5"

private const val T159_ELEMENT_BOUNDARY = "element model branch end (C-0039's small-rotation branch)"

private val T159_RECOMMENDED_DEVICE = setOf(10.0 to 0.5, 10.0 to 2.0)

private val T159_INTENDED_FILES = setOf(
    "T-149-recommended-element-fold.json",
    "T-157-large-rotation-arm-branch.json",
    "T-79-two-spring-elastica.json"
)

@Serializable
private data class T159Convergence(
    val axis: String,
    val setting: String,
    val quantity: String,
    val value: Double,
    val departureFromFinest: Double
)

@Serializable
private data class T159Falsifier(
    val id: String,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

@Suppress("LongParameterList")
@Serializable
private data class T159Result(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val parameters: Map<String, String>,
    val citedInputs: List<String>,
    val elementDomain: ElementDomain,
    val domainConvergence: List<T159Convergence>,
    val cheapBound: Map<String, String>,
    val classification: List<DomainSensitivityRow>,
    val repricing: List<CeilingRepricing>,
    val collar: CollarArgument,
    val downstream: List<DownstreamDiff>,
    val downstreamMovements: List<DownstreamMovement>,
    val falsifiers: List<T159Falsifier>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

private val t159Reader = Json { ignoreUnknownKeys = true }

private fun t159PublishedRows(): List<PublishedFoldRow> {
    val file = File("gpd/results/T-149-recommended-element-fold.json")
    require(file.exists()) { "T-149's result file is this task's own input and is missing" }
    return t159Reader.parseToJsonElement(file.readText()).jsonObject["folds"]!!.jsonArray
        .map { t159Reader.decodeFromJsonElement<PublishedFoldRow>(it) }
}

private fun t159ElementCeilingSafety(): Double {
    val file = File("gpd/results/T-149-recommended-element-fold.json")
    return t159Reader.parseToJsonElement(file.readText())
        .jsonObject["runParameters"]!!.jsonObject["elementCeilingSafety"]!!
        .jsonPrimitive.content.toDouble()
}

/** `C-0033`'s own measured `d ln μ/dh` records and the lowest gap it measured one at. */
private fun t159CollarGradients(): Pair<List<Double>, Double> {
    val file = File("gpd/results/T-60-collar-on-the-equilibrium-path.json")
    require(file.exists()) { "C-0033's result file is this task's own input and is missing" }
    val rows = t159Reader.parseToJsonElement(file.readText()).jsonObject["gradients"]!!.jsonArray
        .map { t159Reader.decodeFromJsonElement<T159CollarGradient>(it) }
    return rows.map { it.logGradient } to rows.minOf { it.gapHeight }
}

@Serializable
private data class T159CollarGradient(
    val gapHeight: Double,
    val logGradient: Double
)

@Serializable
private data class T159DiffDocument(
    val files: List<DownstreamDiff> = emptyList(),
    val movements: List<DownstreamMovement> = emptyList(),
    val t149Rows: List<PublishedFoldRow> = emptyList()
)

private fun t159Downstream(): T159DiffDocument {
    val file = File("gpd/data/T-159-downstream-diff.json")
    require(file.exists()) {
        "the classified downstream diff is missing — produce it with " +
                "tools/T-159-result-diff.py --baseline <inherited> --rerun <snapshot> " +
                "--emit gpd/data/T-159-downstream-diff.json"
    }
    return t159Reader.decodeFromJsonElement(t159Reader.parseToJsonElement(file.readText()))
}

private fun t159DomainConvergence(design: ElementDomain): List<T159Convergence> {
    val finest = recommendedElementDomain(steps = 1600)
    return listOf(200, 400, 800, 1600).flatMap { steps ->
        val here = if (steps == 1600) finest else recommendedElementDomain(steps = steps)
        listOf(
            T159Convergence(
                axis = "RK4 steps",
                setting = "$steps",
                quantity = "the arm's contour, re-derived through C-0039's placement solve [nm]",
                value = here.contour,
                departureFromFinest = abs(here.contour / finest.contour - 1.0)
            ),
            T159Convergence(
                axis = "RK4 steps",
                setting = "$steps",
                quantity = "the corrected refusal stroke ceiling [nm]",
                value = here.refusalStrokeCeiling,
                departureFromFinest =
                    abs(here.refusalStrokeCeiling / finest.refusalStrokeCeiling - 1.0)
            ),
            T159Convergence(
                axis = "RK4 steps",
                setting = "$steps",
                quantity = "max_s|phi| at the refusal [rad]",
                value = here.maximumRotationAtRefusal,
                departureFromFinest =
                    abs(here.maximumRotationAtRefusal / finest.maximumRotationAtRefusal - 1.0)
            )
        )
    }
}

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    println("T-159 — the cheap bound, on a result file that already exists ...")
    val published = t159PublishedRows()
    val safety = t159ElementCeilingSafety()
    require(abs(safety - ELEMENT_DOMAIN_SAFETY) <= 1e-12) {
        "T-149's element ceiling safety is $safety nm and this task reproduces " +
                "$ELEMENT_DOMAIN_SAFETY nm"
    }
    val publishedCeiling = published.filter { it.strokeCeilingOwner == "element model" }
        .map { it.strokeCeiling }.distinct().single()

    println("T-159 — the corrected element domain, re-derived on C-0084's own object ...")
    val domain = recommendedElementDomain()
    println(
        ("  contour %.9f nm; refusal %.8f nm (published %.8f); branch validity %.8f nm; " +
                "path ceiling %.8f nm; max|phi| %.7f rad")
            .format(
                domain.contour, domain.refusalStrokeCeiling, 7.91968584,
                domain.branchValidityStrokeCeiling, domain.pathStrokeCeiling,
                domain.maximumRotationAtRefusal
            )
    )

    val classification = classifyDomainSensitivity(
        published, T159_ELASTICA_LINE, domain.pathStrokeCeiling, T159_RECOMMENDED_DEVICE
    )
    val noElement = classification.count { it.sensitivity == DomainSensitivity.NO_ELEMENT.name }
    val layerOwned =
        classification.count { it.sensitivity == DomainSensitivity.LAYER_OWNS_THE_MINIMUM.name }
    val movable = classification.filter { it.canMove }
    val outstanding = movable.filter { !it.reReadByC0092 }
    println(
        "  108 rows: %d carry no element, %d are layer-owned, %d can move, %d of those are new"
            .format(noElement, layerOwned, movable.size, outstanding.size)
    )

    println("T-159 — reading the classified downstream diff ...")
    val diff = t159Downstream()
    val rerunRows = diff.t149Rows.associateBy { it.key }
    val repricing = repriceCeilings(published, rerunRows, T159_ELEMENT_BOUNDARY)
    val stillBinds = repricing.count { it.elementBoundaryStillBinds }
    val movedMargins = repricing.filter {
        it.marginMovement != null && abs(it.marginMovement - 1.0) > 1e-6
    }
    println(
        "  the element boundary binds at %d of %d rows after the correction; %d margins move"
            .format(stillBinds, repricing.size, movedMargins.size)
    )

    val (gradients, lowestGap) = t159CollarGradients()
    val gapAtCeiling = 10.0 - domain.pathStrokeCeiling
    val collar = collarCannotCreateAFold(gradients, lowestGap, gapAtCeiling)

    println("T-159 — the domain's own convergence ...")
    val convergence = t159DomainConvergence(domain)

    val decisionsMoved = diff.movements.count { it.classification == "a decision" }
    val realChanges = diff.movements.count { it.classification == "a real change" }
    val unintended = diff.files.filter {
        it.file !in T159_INTENDED_FILES &&
                (it.verdict == "a decision moved" || it.verdict == "a real change")
    }
    // F1, as a comparison rather than as a claim: every row the cheap bound says CANNOT move,
    // checked field by field against the re-run of the whole 108-row study.
    val immovable = classification.filter { !it.canMove }.map {
        "${it.model}|${it.layerHeight}|${it.concentration}|${it.loadLine}"
    }.toSet()
    val containmentViolations = published.filter { it.key in immovable }.count { row ->
        val after = rerunRows[row.key]
        after != null && (
                after.strokeCeiling != row.strokeCeiling ||
                        after.strokeCeilingOwner != row.strokeCeilingOwner ||
                        after.bindingCeiling != row.bindingCeiling ||
                        after.biasMargin != row.biasMargin ||
                        after.pullInStroke != row.pullInStroke ||
                        after.verdict != row.verdict
                )
    }
    val immovableCompared = published.count { it.key in immovable && rerunRows.containsKey(it.key) }
    val outstandingKeys = outstanding.map {
        "${it.model}|${it.layerHeight}|${it.concentration}|${it.loadLine}"
    }.toSet()
    val outstandingMovedCeiling = published.filter { it.key in outstandingKeys }.count {
        rerunRows[it.key]?.strokeCeiling != it.strokeCeiling
    }
    val outstandingMovedBinding = published.filter { it.key in outstandingKeys }.count {
        rerunRows[it.key]?.bindingCeiling != it.bindingCeiling
    }
    val outstandingMovedMargin = published.filter { it.key in outstandingKeys }.count {
        rerunRows[it.key]?.biasMargin != it.biasMargin
    }
    val foldsAppeared = diff.t149Rows.count {
        it.loadLine.startsWith(T159_ELASTICA_LINE) && it.layerHeight == 10.0 &&
                it.pullInStroke != null
    }

    val falsifiers = listOf(
        T159Falsifier(
            id = "F1",
            statement = "the containment being wrong — a row the cheap bound excludes moving its " +
                    "stroke ceiling, its ceiling owner, its binding ceiling, its bias margin, " +
                    "its fold stroke or its verdict when the whole 108-row study is re-run",
            fired = containmentViolations > 0,
            outcome = ("%d of the %d excluded rows compared against the re-run moved any of " +
                    "those six fields").format(containmentViolations, immovableCompared)
        ),
        T159Falsifier(
            id = "F2",
            statement = "the repair not being one — a supremum at or below the ladder's " +
                    "7.9196867 nm, or at or above the contour",
            fired = domain.refusalStrokeCeiling <= 7.9196867 ||
                    domain.refusalStrokeCeiling >= domain.contour,
            outcome = ("the corrected refusal is %.8f nm, %.8f nm past the ladder's and " +
                    "%.8f nm below the contour").format(
                domain.refusalStrokeCeiling, domain.refusalStrokeCeiling - 7.9196867,
                domain.contour - domain.refusalStrokeCeiling
            )
        ),
        T159Falsifier(
            id = "F3",
            statement = "a headline number moving by more than its own emission precision",
            fired = unintended.isNotEmpty(),
            outcome = if (unintended.isEmpty())
                "every file outside the three this repair is ABOUT moves at most one unit in " +
                        "its last emitted significant digit, a residual of a vanishing quantity, " +
                        "or a number carried inside an unrounded prose string"
            else "a decision or a real change moved outside the three files this repair is " +
                    "ABOUT: " + unintended.joinToString { "${it.file} (${it.verdict})" }
        ),
        T159Falsifier(
            id = "F4",
            statement = "a fold in the extension at the six outstanding states",
            fired = foldsAppeared > 0,
            outcome = "%d of the re-run 10 nm rows report a pull-in stroke".format(foldsAppeared)
        ),
        T159Falsifier(
            id = "F5",
            statement = "the collar creating a fold — a non-positive d ln mu/dh anywhere " +
                    "C-0033 measured one",
            fired = collar.nonPositiveGradients > 0,
            outcome = ("%d of %d measured gradients are non-positive; the range is " +
                    "%.6f to %.6f per nm").format(
                collar.nonPositiveGradients, collar.gradientRecords,
                collar.smallestGradient, collar.largestGradient
            )
        )
    )

    val result = T159Result(
        task = "T-159",
        leaf = "A8.2",
        title = "The doubling force ladder repaired as a branch continuation, and C-0084's 108 " +
                "fold rows re-read at the corrected element domain — 90 of them by a " +
                "containment identity that costs nothing, 18 by re-running the study, and 6 of " +
                "the 96 CH-0107 left outstanding are the whole of what could move",
        verificationType = "logical (a containment on min(layer, element) that settles 90 rows " +
                "with no solve) + in-silico (the repaired continuation, the re-derived domain, " +
                "the re-run of every consumer of the changed source, and its classified diff)",
        acceptance = "A1 every row classified and the movable set re-read; A2 " +
                "forceForDisplacement continuing rather than doubling, reaching past the " +
                "ladder's refusal and below the contour; A3 the evaluation count asserted; " +
                "A4 the downstream diff classified field by field; A5 CH-0099 repriced over all " +
                "108; A6 C-0033's collar argument filed with its premise checked",
        maturity = "TRL 1-3. Model-consistent and traceable. NOTHING HERE IS MEASURED, AND THE " +
                "MOTIF IS NOT DEMONSTRATED. C-0055's 62 recorded queries stand and are upstream " +
                "of the element itself; C-0005's one-loop correction is 123-214 % of the leading " +
                "term and is larger than every margin re-read here.",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm",
            "rotationalStiffness" to "pN nm/rad",
            "rotation" to "rad",
            "potential" to "V",
            "concentration" to "mM",
            "temperature" to "K"
        ),
        conventions = listOf(
            "C-0039's geometry: s in [0, L] from the hinge, phi the tangent angle toward the " +
                    "stroke, x = integral cos phi, z = integral sin phi; EI phi'' = -F cos phi " +
                    "+ H sin phi, EI phi'(0) = k_n phi(0), EI phi'(L) = M0 - k_f phi(L).",
            "One arm's stroke is delta = z(L); the array's reaction is n times one arm's tip " +
                    "force, because C-0017's mandate is a SUM.",
            "The device's stroke s = L0 - h is positive downward; L0 is a force-onset height at " +
                    "a defining load of 1.0 pN over the tile (C-0011, CH-0010).",
            "A fold is max_s V_eq(s), and at it k_c(s) + k_eff(s) = 0 exactly. A boundary " +
                    "maximum is not a stationary point.",
            "A row's stroke ceiling is min(layer stroke ceiling, element domain ceiling), and " +
                    "correcting the element domain can only RAISE the second argument.",
            "The recommended device is the 10 nm layer at sigma = 0.024 nm^-2 in 0.5 and 2.0 mM " +
                    "(C-0071, C-0068); the 5 nm and 7 nm rows are coverage parity and are not " +
                    "intersected with it (C-0064).",
            "A result file is rounded at the serialisation boundary, so a diff that appears at " +
                    "all is already at least one unit in the last emitted significant digit."
        ),
        parameters = mapOf(
            "publishedElementStrokeCeiling" to publishedCeiling.toString(),
            "elementCeilingSafety" to ELEMENT_DOMAIN_SAFETY.toString(),
            "elasticaLinePrefix" to T159_ELASTICA_LINE,
            "recommendedDevice" to T159_RECOMMENDED_DEVICE.toString(),
            "rk4Steps" to domain.steps.toString(),
            "smallRotationBranchLimit" to "pi/2",
            "publishedRows" to published.size.toString()
        ),
        citedInputs = listOf(
            "C-0084's 108 fold rows, its element ceiling safety and its arm length — READ from " +
                    "gpd/results/T-149-recommended-element-fold.json.",
            "C-0092's contour bound 8.164390826631303 nm and its continuation supremum " +
                    "8.1610821 nm — CITED, and the contour RE-DERIVED here.",
            "C-0084's ladder refusal 7.91968584 nm and path ceiling 7.909685836937754 nm — CITED.",
            "C-0033's measured d ln mu/dh — READ from " +
                    "gpd/results/T-60-collar-on-the-equilibrium-path.json.",
            "CH-0099's 2.567-3.740x inflation at 8 states — CITED, and recomputed here from the " +
                    "paired fields C-0084 emitted.",
            "duplex EI = 230 pN nm^2 — a CanDo MODEL INPUT, not a measurement.",
            "Section 3's targets: 100 pN, 3 nm acceptable / 10 nm desired, 40 x 40 nm, 10 nm " +
                    "layer, 0.5/2/10 mM — CITED."
        ),
        elementDomain = domain,
        domainConvergence = convergence,
        cheapBound = mapOf(
            "theIdentity" to ("A path is searched over [0, min(layer, element)]. Correcting the " +
                    "element domain raises only the second argument, so a row can move ONLY " +
                    "where the element owns the min. Of 108 rows, %d carry C-0018's affine " +
                    "mandate and contain no elastica at all, %d have a layer-owned ceiling of " +
                    "4.33-6.50 nm — far below both the published element domain and the " +
                    "corrected one — and %d are element-owned.")
                .format(noElement, layerOwned, movable.size),
            "whatIsOutstanding" to ("C-0092 re-read %d of the %d movable rows. The other %d are " +
                    "the 10 nm layer in 10 mM, and they are the WHOLE of what CH-0107's 96 " +
                    "could move.").format(
                movable.size - outstanding.size, movable.size, outstanding.size
            ),
            "whatItCost" to "one pass over a result file that already existed, against 96 fold " +
                    "searches at ~42 Poisson-Boltzmann solves per path point"
        ),
        classification = classification,
        repricing = repricing,
        collar = collar,
        downstream = diff.files,
        downstreamMovements = diff.movements,
        falsifiers = falsifiers,
        findings = emptyMap(),
        validity = listOf(
            "TRL 1-3. Nothing here is measured and the motif is not demonstrated.",
            "The CONTOUR bound needs no validity range — it is C-0092's, a bound on an integral " +
                    "of a bounded function. Everything else here does.",
            "The corrected DOMAIN is a measured integrator limit, not a property of the " +
                    "elastica: it moves with the RK4 step count, and the convergence table says " +
                    "by how much. What does not move is that it is strictly inside the contour.",
            "Only the SMALL-ROTATION branch is continued. C-0092 enumerated the curled branches " +
                    "and found every one of them at a SMALLER stroke; they are refused here " +
                    "rather than returned, and that refusal is the repair.",
            "Mean field, inherited whole: C-0005's one-loop correction is 123-214 % over this " +
                    "gap range, larger than every margin re-read.",
            "The re-read rows are C-0084's, with its layer models, its field and its " +
                    "conventions unchanged. Nothing here re-opens them.",
            "The downstream diff is taken against the result files this iteration inherited. " +
                    "Nothing is copied back: a moved number belongs to the claim that owns it.",
            "C-0033's collar is still NOT composed into any path here. What is filed is the " +
                    "argument that it cannot create a fold, and its premise is checked against " +
                    "C-0033's own measured gradients.",
            "1-D, static, tile mean under a uniform load — the same choices C-0018, C-0032, " +
                    "C-0084 and C-0092 made."
        ),
        openQuestions = listOf(
            "The window between the corrected refusal and the contour is still open, and it is " +
                    "an integrator limit rather than a physical one. C-0092's separate " +
                    "continuation reaches further at 800 RK4 steps than this one does at 400.",
            "stateAtForce is UNCHANGED and still brackets its shooting parameter by doubling " +
                    "from a linear seed. Its direct callers work at forces where the residual " +
                    "has one root, but the trap is still in the file for the next caller.",
            "elasticaArmForStiffness still searches from a floor of 1.5 x the working stroke, " +
                    "CLAUDE.md's own recorded trap, and this task did not touch it.",
            "The per-path allowable at the deep end of the corrected domain: the element's LAW " +
                    "is defined there and the DEVICE is not, and nothing here says where between " +
                    "3 nm and 8.14 nm it stops being buildable — C-0092's open item 5, unmoved.",
            "The curled branches under a dynamic bias step. Static only."
        )
    )

    val json = Json { prettyPrint = true }
    val findings = mutableMapOf<String, String>()
    findings["theCheapBound"] =
        ("Of C-0084's 108 fold rows, %d carry a load line with no elastica and %d have a " +
                "layer-owned stroke ceiling, so 90 of them cannot move at all — not to a " +
                "tolerance, but because min(a, b) = a whenever a <= b <= b'. %d are " +
                "element-owned, C-0092 re-read %d of them, and the %d outstanding are the 10 nm " +
                "layer in 10 mM. The answer to 'do the other 96 matter' is therefore SIX, and " +
                "it was available from one pass over a file.")
            .format(noElement, layerOwned, movable.size, movable.size - outstanding.size,
                outstanding.size)
    findings["theCorrectedDomain"] =
        ("The repaired continuation answers to %.8f nm of stroke where C-0084's doubling ladder " +
                "refused at 7.91968584, %.8f nm further, with max_s|phi| = %.7f rad — %.6f of a " +
                "right angle and still below it — out of a contour of %.9f nm. The path ceiling " +
                "moves 7.90968584 -> %.8f nm.")
            .format(
                domain.refusalStrokeCeiling, domain.refusalStrokeCeiling - 7.91968584,
                domain.maximumRotationAtRefusal,
                domain.maximumRotationAtRefusal / (0.5 * Math.PI), domain.contour,
                domain.pathStrokeCeiling
            )
    findings["theRepricing"] =
        ("With the domain corrected the element boundary binds at %d of %d rows, %d bias margins " +
                "move at all, and CH-0099's candidate stands while its value does not. The 8 " +
                "states it priced at 2.567-3.740x are all at the 10 nm layer in 0.5 and 2.0 mM, " +
                "which C-0092 had already repriced; the 6 rows this task adds carry no operating " +
                "point at all, so they carry no margin to inflate.")
            .format(stillBinds, repricing.size, movedMargins.size)
    findings["theSixOutstandingRows"] =
        ("The %d rows CH-0107 left outstanding that can move are the 10 nm layer in 10 mM. " +
                "Re-run at the corrected domain, %d of %d move their stroke ceiling, %d change " +
                "which ceiling binds, and %d move a bias margin — because none of the six has an " +
                "operating point at all: at 10 mM the target stroke is outside the model floor, " +
                "so there is no margin there to inflate. The element boundary's own candidate " +
                "now binds at 0 of 108 rows.")
            .format(
                outstanding.size, outstandingMovedCeiling, outstanding.size,
                outstandingMovedBinding, outstandingMovedMargin
            )
    findings["theContainment"] =
        ("The cheap bound excluded %d rows and the whole 108-row study was re-run anyway. %d of " +
                "them moved a stroke ceiling, a ceiling owner, a binding ceiling, a bias margin, " +
                "a fold stroke or a verdict. What did move, at 3 of the 90, is a " +
                "coupledTangentAtFold and its tangencyResidual — two diagnostics of a quantity " +
                "that vanishes at a fold by construction, whose relative movement is the " +
                "amplification of a last-ulp root by its own near-cancellation.")
            .format(immovableCompared, containmentViolations)
    findings["theDownstreamDiff"] =
        ("Fourteen studies consume the changed source and %d of their result files moved at all. " +
                "%d fields moved by more than one unit in the last emitted significant digit, " +
                "%d are decisions, %d are a number carried inside an unrounded prose string and " +
                "%d are a residual of a quantity that vanishes by construction. %s")
            .format(
                diff.files.size, realChanges, decisionsMoved,
                diff.movements.count {
                    it.classification == "a number carried inside an unrounded string"
                },
                diff.movements.count {
                    it.classification == "a residual of a quantity that vanishes by construction"
                },
                if (unintended.isEmpty())
                    "Every decision is in a file this repair is ABOUT."
                else "Decisions moved outside the three files this repair is ABOUT, at " +
                        unintended.joinToString { it.file } + " — and they are CH-0112."
            )
    findings["theCollarArgument"] =
        ("C-0092 said the argument should be filed rather than assumed, and filing it makes it " +
                "conditional on a measurement: %d of C-0033's %d measured d ln mu/dh records are " +
                "positive, over gaps from %.2f to 11 nm, and the corrected ceiling sits at a " +
                "%.4f nm gap. %s")
            .format(
                collar.gradientRecords - collar.nonPositiveGradients, collar.gradientRecords,
                collar.lowestMeasuredGap, collar.gapAtTheCeiling,
                if (collar.gapIsInsideTheMeasuredRange) "The premise is measured where it is used."
                else ("The premise is measured down to %.2f nm and used at %.4f nm, so the " +
                        "argument is sound and its premise is extrapolated by %.4f nm.")
                    .format(
                        collar.lowestMeasuredGap, collar.gapAtTheCeiling,
                        collar.lowestMeasuredGap - collar.gapAtTheCeiling
                    )
            )
    val finished = result.copy(findings = findings)

    val output = File("gpd/results/T-159-doubling-ladder-repair.json")
    output.writeText(
        json.encodeToString(json.encodeToJsonElement(finished).roundedForActuatorResult()) + "\n"
    )
    println()
    finished.findings.forEach { (key, value) -> println("  $key:\n    $value\n") }
    finished.falsifiers.forEach { println("  ${it.id} fired=${it.fired}: ${it.outcome}") }
    println()
    println("  written to $output")
}
