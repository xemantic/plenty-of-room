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

import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Task `T-159` — **repair the doubling force ladder, and reprice `CH-0099` at the other 96 states.**
 * Leaf `A8.2`, with `A2.2` for the fold rows the repaired domain is re-read at.
 *
 * ## The one idea in this file
 *
 * `C-0092`/`CH-0107` showed `C-0084`'s 7.9197 nm branch end is a property of `C-0039`'s **doubling**
 * force ladder and not of the elastica, and re-read only the **12** states of the recommended
 * device. The obvious next move is to re-read the other **96**. It is also the wrong one, because
 * 90 of them cannot move and the reason is an identity rather than a measurement:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`strokeCeiling = min(layer stroke ceiling, element domain ceiling)`
 *
 * and correcting the element domain can only **raise** the second argument. Where the layer owns
 * the `min`, the corrected ceiling is unchanged **identically** — not to a tolerance, not to a
 * solver's precision, but because `min(a, b) = a` whenever `a ≤ b ≤ b′`. And where the load line
 * is `C-0018`'s affine mandate there is no elastica in the row at all.
 *
 * So [classifyDomainSensitivity] is one pass over a result file that already exists, it costs
 * nothing, and it says which rows are worth a Poisson-Boltzmann solve. `CLAUDE.md`'s *"the cheap
 * bound runs before the expensive calculation"* applied to a re-read rather than to a model.
 *
 * ## What the expensive half is then for
 *
 * Two things, and neither is the 90 rows:
 *
 * 1. the **corrected domain itself** ([recommendedElementDomain]), re-derived through the repaired
 *    [com.xemantic.nano.plentyofroom.anchoring.TwoSpringElastica] on the same object `C-0084` read
 *    it on, so that the difference between them is a property of the solver and of nothing else;
 * 2. the **downstream diff**: the repair changes a shared main source that produces published
 *    results, so every study that consumes it is re-run and every moved field is classified
 *    ([DownstreamDiff]). `C-0031` is the precedent — *a defect that is invisible in the answer is
 *    invisible to every check written on the answer* — and `P-18` is the cost.
 *
 * ## And one argument that is filed rather than assumed
 *
 * `C-0092` notes that `C-0033`'s collar *"composes exactly at a fold and was not composed"*, and
 * that where there is no fold it cannot create one, *"since a positive increment cannot make a
 * tangent vanish"* — and says that argument should be filed rather than assumed.
 * [collarCannotCreateAFold] files it, and checks its premise against `C-0033`'s own measured
 * gradients rather than against its headline.
 */

/**
 * The safety margin in nm `T-149` subtracts from `min(refusal, branch validity)` before running a
 * path — its own `ELEMENT_CEILING_SAFETY`, which is private to that study and is therefore
 * **reproduced here and asserted** against `runParameters.elementCeilingSafety` in its result file.
 */
const val ELEMENT_DOMAIN_SAFETY: Double = 0.01

/**
 * The recommended element's own stroke domain, re-derived rather than transcribed.
 *
 * @property refusalStrokeCeiling the largest stroke at which **both** the reaction and the tangent
 *   close, which is what a path may be run to.
 * @property branchValidityStrokeCeiling the largest stroke at which the **reaction** closes with
 *   `max_s|φ|` below a right angle. It is read on the reaction alone, so it is not bounded above by
 *   [refusalStrokeCeiling]; which of the two binds is a **result**, not a convention.
 * @property pathStrokeCeiling `min` of the two, less [ELEMENT_DOMAIN_SAFETY] — the number `T-149`'s
 *   paths are actually run to.
 */
@Serializable
data class ElementDomain(
    val steps: Int,
    val contour: Double,
    val refusalStrokeCeiling: Double,
    val branchValidityStrokeCeiling: Double,
    val pathStrokeCeiling: Double,
    val maximumRotationAtRefusal: Double,
    val maximumRotationAtAcceptableStroke: Double,
    val refusalOverContour: Double,
    val windowBelowTheContour: Double
)

/** [ElementDomain] of `C-0069`'s `Q5` at a declared RK4 step count. */
fun recommendedElementDomain(steps: Int = 400): ElementDomain {
    require(steps >= 16) { "steps must be at least 16, was: $steps" }
    val line = recommendedArmLine("LQ5 recommended hinge-rooted arm (C-0071)", steps = steps)
    val contour = line.length
    val refusal = loadLineStrokeCeiling(line, 3.0, contour - 1.0e-9)
    val branch = rotationLimitStroke(line, 3.0, contour - 1.0e-9)
    return ElementDomain(
        steps = steps,
        contour = contour,
        refusalStrokeCeiling = refusal,
        branchValidityStrokeCeiling = branch,
        pathStrokeCeiling = min(refusal, branch) - ELEMENT_DOMAIN_SAFETY,
        maximumRotationAtRefusal = line.maximumRotation(refusal),
        maximumRotationAtAcceptableStroke = line.maximumRotation(GEN1_ACCEPTABLE_STROKE),
        refusalOverContour = refusal / contour,
        windowBelowTheContour = contour - refusal
    )
}

// ---------------------------------------------------------------- the cheap bound

/** The fields of one of `C-0084`'s 108 fold rows this task reads. */
@Serializable
data class PublishedFoldRow(
    val model: String,
    val layerHeight: Double,
    val concentration: Double,
    val loadLine: String,
    val layerStrokeCeiling: Double,
    val strokeCeiling: Double,
    val strokeCeilingOwner: String,
    val bindingCeiling: String,
    val biasMargin: Double? = null,
    val biasMarginIgnoringElementBoundary: Double? = null,
    val pullInStroke: Double? = null,
    val verdict: String = ""
) {

    /** `(model, layerHeight, concentration, loadLine)` — every dimension the sweep varied. */
    val key: String get() = "$model|$layerHeight|$concentration|$loadLine"
}

/** Why one published row can, or cannot, move when the element domain is corrected. */
enum class DomainSensitivity {

    /** The load line contains no element: the domain is not an argument of the row. */
    NO_ELEMENT,

    /** `min(layer, element)` is the layer's, and raising the element's cannot change a `min`. */
    LAYER_OWNS_THE_MINIMUM,

    /** The element owns the `min`, so the row's stroke ceiling moves with the correction. */
    ELEMENT_OWNS_THE_MINIMUM
}

/** One classified row. */
@Serializable
data class DomainSensitivityRow(
    val model: String,
    val layerHeight: Double,
    val concentration: Double,
    val loadLine: String,
    val layerStrokeCeiling: Double,
    val publishedStrokeCeiling: Double,
    val correctedStrokeCeiling: Double,
    val ceilingMovement: Double,
    val sensitivity: String,
    val canMove: Boolean,
    val reReadByC0092: Boolean,
    val publishedBindingCeiling: String,
    val publishedBiasMargin: Double? = null,
    val reason: String
)

/**
 * Every published row, classified by whether raising the element domain to
 * [correctedElementCeiling] can move it — a containment argument, not a scan.
 *
 * [reReadDevice] names the `(layerHeight, concentration)` pairs `C-0092` already re-read, so the
 * classification separates *"cannot move"* from *"can move and has been read"* from *"can move and
 * has not"*, which is the distinction the acceptance predicate turns on.
 */
fun classifyDomainSensitivity(
    rows: List<PublishedFoldRow>,
    elasticaLinePrefix: String,
    correctedElementCeiling: Double,
    reReadDevice: Set<Pair<Double, Double>>,
    tolerance: Double = 1.0e-9
): List<DomainSensitivityRow> {
    require(rows.isNotEmpty()) { "there are no rows to classify" }
    require(correctedElementCeiling > 0.0) {
        "correctedElementCeiling must be positive, was: $correctedElementCeiling"
    }
    require(tolerance > 0.0) { "tolerance must be positive, was: $tolerance" }
    return rows.map { row ->
        val corrected =
            if (row.loadLine.startsWith(elasticaLinePrefix)) {
                min(row.layerStrokeCeiling, correctedElementCeiling)
            } else row.strokeCeiling
        val movement = corrected - row.strokeCeiling
        val sensitivity = when {
            !row.loadLine.startsWith(elasticaLinePrefix) -> DomainSensitivity.NO_ELEMENT
            row.layerStrokeCeiling <= row.strokeCeiling + tolerance ->
                DomainSensitivity.LAYER_OWNS_THE_MINIMUM
            else -> DomainSensitivity.ELEMENT_OWNS_THE_MINIMUM
        }
        val moves = movement > tolerance
        DomainSensitivityRow(
            model = row.model,
            layerHeight = row.layerHeight,
            concentration = row.concentration,
            loadLine = row.loadLine,
            layerStrokeCeiling = row.layerStrokeCeiling,
            publishedStrokeCeiling = row.strokeCeiling,
            correctedStrokeCeiling = corrected,
            ceilingMovement = movement,
            sensitivity = sensitivity.name,
            canMove = moves,
            reReadByC0092 = (row.layerHeight to row.concentration) in reReadDevice &&
                    row.loadLine.startsWith(elasticaLinePrefix),
            publishedBindingCeiling = row.bindingCeiling,
            publishedBiasMargin = row.biasMargin,
            reason = when (sensitivity) {
                DomainSensitivity.NO_ELEMENT ->
                    "C-0018's affine mandate carries no elastica: the element domain is not an " +
                            "argument of this row"
                DomainSensitivity.LAYER_OWNS_THE_MINIMUM ->
                    "the layer owns the stroke ceiling at " +
                            "%.6f nm, below both the published element domain and the corrected "
                                .format(row.layerStrokeCeiling) +
                            "one, so min(layer, element) is unchanged IDENTICALLY"
                DomainSensitivity.ELEMENT_OWNS_THE_MINIMUM ->
                    "the element owns the stroke ceiling, which moves by %.6f nm".format(movement)
            }
        )
    }
}

// ---------------------------------------------------------------- CH-0099, repriced

/** One row's ceiling taxonomy before and after the domain correction. */
@Serializable
data class CeilingRepricing(
    val model: String,
    val layerHeight: Double,
    val concentration: Double,
    val publishedBindingCeiling: String,
    val correctedBindingCeiling: String,
    val publishedMargin: Double? = null,
    val correctedMargin: Double? = null,
    val marginIgnoringTheElementBoundary: Double? = null,
    val publishedInflation: Double? = null,
    val marginMovement: Double? = null,
    val elementBoundaryStillBinds: Boolean,
    val repricedBy: String
)

/**
 * `CH-0099`'s inflation table, repriced at the corrected domain.
 *
 * `CH-0099` priced the taxonomy gap as the ratio of the margin *ignoring* the element boundary to
 * the margin *with* it — i.e. against the **last** candidate in the list. `C-0092` found the price
 * is set by whichever candidate is **second**, which is the generalisable half. This function
 * carries both, per row, so the two readings can be compared rather than asserted.
 */
fun repriceCeilings(
    published: List<PublishedFoldRow>,
    corrected: Map<String, PublishedFoldRow>,
    elementBoundaryName: String
): List<CeilingRepricing> = published.map { row ->
    val after = corrected[row.key]
    CeilingRepricing(
        model = row.model,
        layerHeight = row.layerHeight,
        concentration = row.concentration,
        publishedBindingCeiling = row.bindingCeiling,
        correctedBindingCeiling = after?.bindingCeiling ?: row.bindingCeiling,
        publishedMargin = row.biasMargin,
        correctedMargin = after?.biasMargin ?: row.biasMargin,
        marginIgnoringTheElementBoundary = row.biasMarginIgnoringElementBoundary,
        publishedInflation =
            if (row.biasMargin == null || row.biasMargin == 0.0 ||
                row.biasMarginIgnoringElementBoundary == null
            ) null else row.biasMarginIgnoringElementBoundary / row.biasMargin,
        marginMovement =
            if (row.biasMargin == null || row.biasMargin == 0.0) null
            else (after?.biasMargin ?: row.biasMargin) / row.biasMargin,
        elementBoundaryStillBinds =
            (after?.bindingCeiling ?: row.bindingCeiling) == elementBoundaryName,
        repricedBy = if (after == null) "not re-read" else "re-run at the corrected domain"
    )
}

// ---------------------------------------------------------------- the collar argument

/**
 * **The argument `C-0092` said should be filed**: `C-0033`'s collar cannot create a fold where
 * there is none.
 *
 * A fold is `max_s V_eq(s)`, and `C-0018`'s search locates it where the coupled tangent
 * `k_c(s) + k_eff(s)` vanishes. *No fold on the traversed domain* therefore means that sum is
 * strictly positive at every stroke the path reaches. `C-0033`'s collar enters at a **force-pinned**
 * point — the balance fixes `|F_es| = R(s) + P(g)A` — so its whole effect on the coupled tangent is
 * `+|F_es| d ln μ/dh`, with the *level* of the force absorbed into the bias. Where `d ln μ/dh > 0`
 * the increment is strictly positive, a strictly positive increment to a strictly positive quantity
 * cannot make it vanish, and no fold can appear.
 *
 * **The premise is a measurement and it is checked here rather than quoted**: `C-0033` reports
 * `d ln μ/dh` at 80 (state, gap, scheme) records, and [gradients] is that list. The argument stands
 * only over the gap range those records cover, which [lowestMeasuredGap] and [gapAtTheCeiling]
 * compare — the corrected domain reaches a *smaller* gap than `C-0033` measured at, and saying so
 * is the whole difference between filing an argument and assuming one.
 *
 * @property holds `true` only when every measured gradient is positive **and** the corrected
 *   ceiling's gap is inside the measured range.
 */
@Serializable
data class CollarArgument(
    val gradientRecords: Int,
    val nonPositiveGradients: Int,
    val smallestGradient: Double,
    val largestGradient: Double,
    val lowestMeasuredGap: Double,
    val gapAtTheCeiling: Double,
    val gapIsInsideTheMeasuredRange: Boolean,
    val holds: Boolean,
    val statement: String
)

/** [CollarArgument] over `C-0033`'s own measured `d ln μ/dh` records. */
fun collarCannotCreateAFold(
    gradients: List<Double>,
    lowestMeasuredGap: Double,
    gapAtTheCeiling: Double
): CollarArgument {
    require(gradients.isNotEmpty()) { "there are no measured gradients to check" }
    require(lowestMeasuredGap > 0.0) {
        "lowestMeasuredGap must be positive, was: $lowestMeasuredGap"
    }
    require(gapAtTheCeiling > 0.0) { "gapAtTheCeiling must be positive, was: $gapAtTheCeiling" }
    val nonPositive = gradients.count { it <= 0.0 }
    val inside = gapAtTheCeiling >= lowestMeasuredGap
    return CollarArgument(
        gradientRecords = gradients.size,
        nonPositiveGradients = nonPositive,
        smallestGradient = gradients.min(),
        largestGradient = gradients.max(),
        lowestMeasuredGap = lowestMeasuredGap,
        gapAtTheCeiling = gapAtTheCeiling,
        gapIsInsideTheMeasuredRange = inside,
        holds = nonPositive == 0 && inside,
        statement =
            "no fold means k_c + k_eff > 0 at every traversed stroke; at a force-pinned point the " +
                    "collar's whole contribution to that sum is +|F_es| d ln mu/dh, so a positive " +
                    "gradient is a positive increment and cannot make a positive quantity vanish. " +
                    (if (nonPositive == 0) "All ${gradients.size} of C-0033's measured gradients " +
                            "are positive. " else "$nonPositive of ${gradients.size} measured " +
                            "gradients are NOT positive, and the argument fails. ") +
                    (if (inside) "The corrected ceiling's gap is inside the measured range."
                    else ("The corrected ceiling's gap is %.4f nm, %.4f nm BELOW the lowest gap " +
                            "C-0033 measured (%.4f nm), so the premise is extrapolated there and " +
                            "the argument is conditional.")
                        .format(
                            gapAtTheCeiling, lowestMeasuredGap - gapAtTheCeiling, lowestMeasuredGap
                        ))
    )
}

// ---------------------------------------------------------------- the downstream diff

/** One moved field of one re-run result file. */
@Serializable
data class DownstreamMovement(
    val file: String,
    val path: String,
    val relative: Double,
    val absolute: Double,
    val classification: String
)

/** One re-run result file's diff against the version this iteration inherited. */
@Serializable
data class DownstreamDiff(
    val file: String,
    val task: String,
    val comparedFields: Int,
    val movedFields: Int,
    val worstRelative: Double,
    val worstAbsolute: Double,
    val classifications: Map<String, Int>,
    val verdict: String
)

/**
 * Whether a downstream diff is what a **solver repair** is allowed to produce.
 *
 * A repair beneath a claim may move a number by the last digit its file emits and may move the
 * element domain it is a repair of. It may **not** move a verdict, a count or a string — that is a
 * moved claim, and a moved claim is a challenge (`CLAUDE.md`).
 */
fun diffIsBeneathTheClaims(
    diffs: List<DownstreamDiff>,
    intendedFiles: Set<String>,
    emissionPrecision: Double = 1.0e-8
): Boolean = diffs.all { diff ->
    diff.file in intendedFiles ||
            diff.movedFields == 0 ||
            (diff.worstRelative <= emissionPrecision &&
                    diff.classifications.keys.none { it == "a decision" })
}

/** The relative movement one unit in the ninth emitted significant digit can produce. */
const val EMITTED_FIELD_MOVEMENT: Double = 1.0e-8

/**
 * The absolute movement below which a field is a **residual** rather than an answer — `C-0031`'s
 * `RESULT_ABSOLUTE_FLOOR` read as a movement, in the project's locked units.
 *
 * A tangency residual, a reproduction departure and a convergence measure are all quantities that
 * vanish by construction, and a **relative** movement of one is the amplification of a last-ulp
 * root by its own near-cancellation. Judging them relatively compares their noise (`CLAUDE.md`).
 */
const val VANISHING_FIELD_MOVEMENT: Double = 1.0e-9

/** Classifies one relative movement — the vocabulary the claim reports its diff in. */
fun classifyMovement(
    published: Double,
    rerun: Double,
    absoluteFloor: Double = 1.0e-12
): String {
    val difference = abs(rerun - published)
    val scale = max(abs(published), abs(rerun))
    return when {
        difference == 0.0 -> "identical"
        scale <= absoluteFloor -> "a quantity that is identically zero"
        difference / scale <= EMITTED_FIELD_MOVEMENT ->
            "one unit in the last emitted significant digit"
        difference <= VANISHING_FIELD_MOVEMENT ->
            "a residual of a quantity that vanishes by construction"
        difference / scale <= 1.0e-4 -> "inside a declared solver tolerance"
        else -> "a real change"
    }
}

/**
 * Classifies a moved **string** field.
 *
 * A result file is rounded at the serialisation boundary and a number emitted as a string is
 * **not** (`CLAUDE.md`), so a prose field that carries a `Double.toString()` inside it moves at the
 * last ulp while saying exactly the same thing. Stripping the digits separates the two cases, and
 * the distinction is the whole of `F3`: a moved *decision* is a moved verdict and therefore a
 * challenge, while a moved *digit inside a sentence* is a rounding gap in the emitter.
 */
fun classifyTextMovement(published: String, rerun: String): String {
    if (published == rerun) return "identical"
    val strip = Regex("[0-9.eE+-]+")
    return if (strip.replace(published, "#") == strip.replace(rerun, "#"))
        "a number carried inside an unrounded string"
    else "a decision"
}
