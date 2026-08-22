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

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * The number of significant digits every floating-point number in a result file is rounded to.
 *
 * Not a display choice — a **reproducibility** one. `gpd/README.md` requires that "a re-run
 * that changes nothing produces no diff", and a bare `Double` does not satisfy that: the JIT
 * compiles a hot reduction part-way through a run, which changes the summation order, which
 * moves the last one or two units in the last place. Rounding well above that noise floor makes
 * the file a function of the model rather than of the JVM's warm-up schedule.
 *
 * Nine digits is roughly seven more than any number in this programme is worth at TRL 1–3.
 *
 * **It is a ceiling, not a tree-wide constant** (`P-18`). It is the right count only where every
 * solver on the path to the number is tighter than `1e−9` — analytic models and closed-form
 * geometry, where `C-0031` measured movements of one ulp. Anything downstream of a solved height
 * is determined to [SOLVED_HEIGHT_SIGNIFICANT_DIGITS] or fewer, and printing nine of those made a
 * re-run diff certify the code **path** rather than the **answer** (`CH-0043`).
 */
const val RESULT_SIGNIFICANT_DIGITS: Int = 9

/**
 * The magnitude in the locked units below which a result is reported as exactly zero.
 *
 * `T-5`'s central finding is that the internal shear under a uniform load is zero; the solver
 * returns it as `1e−14 pN`, whose *digits* are pure roundoff and differ run to run. Reporting
 * `0.0` for anything under a nanopiconewton is both more reproducible and more honest than
 * reporting fifteen digits of noise — the smallest force of any interest here is `1e−3 pN`.
 */
const val RESULT_ABSOLUTE_FLOOR: Double = 1e-9

/**
 * The number of significant digits a quantity downstream of a **solved SCF height** is determined to.
 *
 * `P-18`, measured rather than assumed — `brush.DeterminedPrecisionStudyKt` and
 * `gpd/results/P-18-determined-precision.json`. `SelfConsistentFieldLayer.heightAtPressure` closes
 * its bracket at a *relative* `HEIGHT_TOLERANCE = 1e-6`, and `CH-0043` measured `T-1f` relocating
 * inside exactly that when `P-15` changed the solver's path without changing its answer. Printing
 * nine digits of a six-digit number is what made a re-run diff a property of the **code path**
 * rather than of the **answer**.
 *
 * This is a ceiling, not a promise: a quantity read as a *second difference* of free energies over
 * a spacing that itself depends on the solved height is determined to fewer digits still, and those
 * are declared per key at their own emission site.
 */
const val SOLVED_HEIGHT_SIGNIFICANT_DIGITS: Int = 6

/**
 * The number of significant digits a **departure** is determined to, and it is a rule about a
 * RECORD TYPE rather than about a file.
 *
 * A departure is a difference or a ratio of two nearly equal **dimensionless** numbers, and it
 * is therefore the field a change of the solve *path* moves: `C-0093` found two runs of one
 * study agreeing on every number in a 100 kB result file and disagreeing in the eleventh decimal
 * of one convergence departure, `3.19469867e−11` against `3.19472365e−11`, because the JIT
 * recompiled a hot reduction between the two solves.
 *
 * [RESULT_ABSOLUTE_FLOOR] cannot catch it: the floor is a claim **in the locked units** (`P-18`)
 * and a ratio of two dishing fractions is not in them, so the only instrument left is the digit
 * count. Two is what `CLAUDE.md` asks for and it is the right count for the reason above — a
 * departure `d` between two quantities each determined to `RESULT_SIGNIFICANT_DIGITS` is itself
 * determined to about `RESULT_SIGNIFICANT_DIGITS + log₁₀ d` digits, which at the `1e−6` to
 * `1e−11` where these fields live is between three and *minus two*.
 *
 * **Why it is a constant here and not a `2` at an emission site.** `C-0093` cured the trap on
 * its own *convergence* axis; `C-0101` cured it in *reproduction* records of the files it was
 * re-emitting; `C-0127` then found `T-136` still carrying `reproductions[2].departure` at nine
 * digits. Each repair was correct and each was applied **per file**, which is why the class
 * survived three of them. `tools/check-result-file-hygiene.py --departures` measures what is
 * left: **222 fields in 29 files** when `C-0129` wrote this, 199 in 27 after it, and **0 in 0**
 * once `T-212` re-emitted them — which is what let the audit become a gate.
 */
const val DEPARTURE_SIGNIFICANT_DIGITS: Int = 2

/**
 * The two **record types** a departure is emitted in, and the qualifier half of the rule.
 *
 * `T-212`/`CH-0154`. `C-0129` states the rule as being about a record type and then keys it on a
 * **leaf name**, which is not the same statement: a census of `gpd/results/` finds a bare
 * `departure` under **eleven** parents, and only these two are the residual-between-two-solves
 * the rule is about. The other nine include `T-193`'s `potentialOfZeroCharge`, where the quantity
 * is a difference of two electrode potentials **in volts** — a dimensional literature comparison,
 * determined to far more than two digits, and exactly the kind of number
 * [RESULT_ABSOLUTE_FLOOR] *does* reach.
 */
val DEPARTURE_RECORDS: Set<String> = setOf("reproductions", "convergence")

/**
 * The spellings **classified as** a departure inside a [DEPARTURE_RECORDS] record — a
 * **judgement per name**, not a pattern, and `tools/T-225-census.py --check` is what keeps it
 * complete.
 *
 * `T-225`/`CH-0169`. The four names `T-212` enumerated were introduced as *"every spelling the
 * corpus uses"*, which is a **list**, and a list is a census that stopped: a shape census of
 * `gpd/results/` finds **fourteen** further leaf keys of the same kind inside those two records.
 * Eight of them are the rule's quantity under another name and are here; six are **not**, and
 * they are excluded on stated grounds rather than on taste, because a mechanical widening would
 * have been wrong on 21 fields in 3 files:
 *
 *  - `residualExponent`, `coverageErrorExponent` — a **`log₁₀`** of a residual. `T-1d` emits
 *    `residual` and `coverageError` as exactly `0.0` (the floor reaches them) and carries their
 *    information in the exponent instead; two digits on `−11.0931` is `−11`, i.e. a residual of
 *    `1e−11` where the solve produced `8.07e−12`, and the three-row node-spacing axis collapses
 *    to a constant.
 *  - `observedOrder` — a **logarithm of a ratio** of two of them, and the *answer* of the
 *    convergence axis. `CLAUDE.md` quotes it as `2.08–2.32`, `1.59` and `1.11`; at two digits the
 *    file would say `2.1`, `2.3`, `1.6` and `1.1`.
 *  - `worstResidual` — a **length in nm** (`T-117`: the binding link's distance from the measured
 *    `[0.60, 0.70]` nm step), carrying the decision `covalent`, and sitting beside the record's
 *    own dimensionless `departure`, which already obeys the rule.
 *  - `residual`, `coverageError` — **absolute** residuals in the solved quantity's own scale, not
 *    a relative comparison of two computations of it.
 *
 * `departureRatio` and `plateDeparture` remain absent for `T-212`'s reason: those are *ratios
 * between two models*, determined to the precision of the models themselves.
 */
val DEPARTURE_SPELLINGS: Set<String> = setOf(
    // `T-212`'s four
    "departure", "relativeDeparture", "departureFromFinest", "relativeDepartureInStroke",
    // `T-225`'s eight: a RELATIVE comparison of two computations of one quantity, dimensionless
    // by construction, zero in exact arithmetic, and therefore a field whose every digit is a
    // property of the solve path rather than of the physics
    "relativeError",                 // T-1d: |P − P_finest| / P_finest over a mesh refinement
    "relativeSpread",                // T-164: the spread over a nested 1/2/4 subdivision
    "relativeMovement",              // T-108: |coarse − fine| / coarse; T-182 and T-189 already
                                     //        emit the same key at two digits
    "multiplierDeparture",           // T-60: the 2-D edge mesh's own refinement residual on μ
    "gradientDeparture",             // T-60: the same on d ln μ/dh, which converges more slowly
    "firstIntegralRelativeSpread",   // T-3a: a first integral is constant in exact arithmetic
    "firstIntegralCoreSpread",       // T-3a: the same, over the core of the gap
    "centrelineRouteSpread"          // T-3b: two evaluation routes to one solved load
)

/**
 * The record types whose whole subtree is an **input**, and is therefore emitted unrounded.
 *
 * `T-268`, closing [`CH-0207`](../../../../../../gpd/challenges/CH-0207-a-parameter-block-cannot-re-run-its-own-study.md).
 * The rule is **round outputs, never inputs**, and it exists because
 * [JsonElement.roundedForResult] dispatches on the JSON **type**: a parameter emitted as a
 * `Double` was rounded to the file's own *output* precision along with every result beside it,
 * which breaks `gpd/README.md`'s `results/` contract — *"every parameter of the run is in the
 * file, so the result is reproducible from it alone"*.
 *
 * `CH-0207` measured it on `T-3a`: a wall charge committed as `−0.398665238` against the
 * `−0.3986652379247042` the study solved with, `1.9e−10` relative — and feeding the committed
 * literal back misses that file's own 2 V force **by one unit in the last emitted place**. Seven
 * call sites in `src/main/kotlin` read a parameter block back as an input rather than as
 * documentation, so the channel is **live**, not latent (`CH-0205`'s classification of the
 * *string* route, which this is the numeric twin of).
 *
 * **The set is a census, not a pattern.** These three spellings are every parameter block the 148
 * committed result files carry — `parameters` 95, `citedInputs` 41, `runParameters` 19, all of
 * them at top level. The singular `parameter` occurs 180 times and is deliberately **excluded**:
 * it is a swept axis coordinate (152 strings and 28 `Double`s), so widening the set to every key
 * whose name contains *parameter* would silently stop rounding 28 **outputs**. `CLAUDE.md`'s
 * *"every spelling the corpus uses is a census that stops"* read in the other direction — a named
 * set may be extended by census and never by pattern, and both directions are named tests in
 * `ParameterBlockRoundingTest`.
 *
 * The exemption beats every precision rule beneath it, [DEPARTURE_DIGITS_BY_KEY] included: those
 * are statements about how well a quantity this study *computed* is determined, and a number the
 * study was *handed* is not that quantity at all. It also suppresses `roundIntegralNumbers`, so
 * the exemption cannot move a rendering it is not about.
 */
val PARAMETER_RECORDS: Set<String> = setOf(
    "parameters", "runParameters", "citedInputs",
    // `T-272`. The fourth spelling, and the corpus coins it in the same commit that adds it here:
    // `structure/ResultEmission.kt`'s emission header writes an `emission` block carrying a
    // lattice tag and a regime, whose every number is a bound the study was HANDED — a buffer
    // molarity, the ends of a solved height range, an applied bias, a band. Rounding one is
    // `CH-0207` one key along. Extending the set is what this KDoc says the rule requires, and the
    // extension is BY CENSUS: `emission` occurs nowhere in the 152 committed result files at this
    // commit, at any depth, so the widening can move nothing that exists — a proof rather than a
    // re-run. The header's own sub-keys are deliberately NOT in this set: `lattice` names 101
    // numeric result leaves in the corpus and `regime` a string leaf in five files, so a set
    // keyed on either would stop rounding an output, which is the very thing the `parameter`
    // exclusion above exists to prevent.
    "emission"
)

/**
 * The departure rule, as `record/spelling` keys for [roundedForResult]'s `digitsByKey`.
 *
 * Pass it — merged with any per-study precisions, which are unqualified and therefore lose to it
 * inside a departure record and win everywhere else. That precedence is the point: `T-160` carries
 * the **same spelling** as its own answer (`departures[*].relativeDeparture`, declared at six
 * digits with a reason) and as a diagnostic (`convergence[*].relativeDeparture`), and a map keyed
 * on the leaf name alone cannot say both.
 */
val DEPARTURE_DIGITS_BY_KEY: Map<String, Int> = DEPARTURE_RECORDS.flatMap { record ->
    DEPARTURE_SPELLINGS.map { spelling -> "$record/$spelling" to DEPARTURE_SIGNIFICANT_DIGITS }
}.toMap()

/**
 * The digits of a quantity whose largest relative movement under a legitimate change of the solve
 * path is [relativeMovement] — `floor(−log₁₀ m)`, clamped to `[1, RESULT_SIGNIFICANT_DIGITS]`.
 *
 * A movement of `9.0e−7` — `CH-0043`'s measured median for `T-1f` — gives **six**. A movement of
 * `4.3e−16`, `C-0031`'s one-ulp majority in `T-1c`, saturates the clamp and asks for nothing: an
 * analytic model determines far more digits than this project has any use for.
 *
 * Zero and non-finite movements return the ceiling rather than an infinite digit count, because a
 * quantity that did not move under the probe is not thereby known exactly — it is known to at least
 * what the probe could resolve.
 */
fun determinedDigits(relativeMovement: Double): Int {
    if (!relativeMovement.isFinite() || relativeMovement <= 0.0) return RESULT_SIGNIFICANT_DIGITS
    val digits = floor(-log10(relativeMovement)).toInt()
    return digits.coerceIn(1, RESULT_SIGNIFICANT_DIGITS)
}

/**
 * Rounds [value] to [digits] significant digits, flooring magnitudes below [floor] to zero.
 *
 * @param floor the magnitude below which the result is reported as exactly zero. The default
 *          [RESULT_ABSOLUTE_FLOOR] is stated **in the locked units** — it is a statement that no
 *          force below a nanopiconewton is of interest — so a study emitting **dimensionless**
 *          quantities must lower it. `P-18` found its own determined-precision measurement
 *          flattened to `0.0` by the default, a relative movement of `3.3e−13` being exactly the
 *          kind of number the floor was written to suppress and exactly the number the study is
 *          about. Same shape as `C-0031`'s floored `layerStiffness` beside an unfloored
 *          `√(k_BT/k)`: **an absolute floor is a claim about units, and it does not travel.**
 */
fun roundForResult(
    value: Double,
    digits: Int = RESULT_SIGNIFICANT_DIGITS,
    floor: Double = RESULT_ABSOLUTE_FLOOR
): Double {
    require(digits in 1..RESULT_SIGNIFICANT_DIGITS) {
        "digits must be within 1..$RESULT_SIGNIFICANT_DIGITS, was: $digits"
    }
    require(floor >= 0.0) { "floor must not be negative, was: $floor" }
    if (!value.isFinite()) return value
    // An exact zero is exactly representable at every precision, and it must be returned before
    // the logarithm: at `floor = 0.0` the floor test below no longer catches it, `log10(0)` is
    // `-Infinity`, and `roundToLong` is handed a `NaN` (`CLAUDE.md`; found by `T-190` on a
    // convergence departure that is exactly zero because two sample grids agree to the last bit).
    // A no-op for every caller passing a positive floor, which is what caught the zero until now.
    if (value == 0.0) return 0.0
    if (abs(value) < floor) return 0.0
    val scale = 10.0.pow(digits - 1 - kotlin.math.floor(log10(abs(value))))
    return (value * scale).roundToLong() / scale
}

/**
 * This number rendered for a **sentence**, at the precision the result file declares.
 *
 * `T-249`, raised by `C-0150`. [JsonElement.roundedForResult] dispatches on the JSON **type** and
 * passes strings through — which is correct, and which is also why it **cannot** be the cure here:
 * by the time the serialisation boundary sees `"channel B at s = 0 is $x"` the number in it is no
 * longer a number, and re-parsing decimals back out of a study's own prose would rewrite cited
 * literals as readily as computed ones. **The only place this rule can be applied is the call
 * site**, which is what makes the artifact-side census
 * (`tools/check-result-file-hygiene.py --prose`) the instrument that keeps it closed rather than a
 * one-line repair in this layer.
 *
 * Two things go wrong when a `Double` is interpolated raw, and only the first is a reproducibility
 * defect:
 *
 *  - `Double.toString()` emits the **shortest round-trip** decimal, up to seventeen significant
 *    digits, so a JIT recompilation of a hot reduction moves the sentence — `C-0150`'s sweep
 *    watched `0.1686405908358076` become `…075` in three `T-164` findings on a run that changed
 *    nothing.
 *  - the file then **contradicts itself**: `T-164`'s numeric `sweep[0].bestDishingOverStroke` is
 *    `0.0651753854` and the sentence beside it says `0.06517538540278571` — one quantity at two
 *    precisions, one of which no field of the file states.
 *
 * Pass the digits and the floor the quantity is entitled to, exactly as at any other emission
 * site: a **departure** in prose takes [DEPARTURE_SIGNIFICANT_DIGITS] and `floor = 0.0`, because
 * [RESULT_ABSOLUTE_FLOOR] is a claim in the locked units (`P-18`) and would render the whole
 * sentence as `0.0`.
 */
fun Double.roundedForProse(
    digits: Int = RESULT_SIGNIFICANT_DIGITS,
    floor: Double = RESULT_ABSOLUTE_FLOOR
): Double = roundForResult(this, digits, floor)

/**
 * Returns this element with every non-integral number rounded by [roundForResult].
 *
 * Applied to the whole tree at the serialisation boundary rather than at each construction
 * site, so no result can be emitted unrounded by omission. Integers, booleans and strings pass
 * through untouched — a path count of `4` stays `4`.
 *
 * @param digits the precision the study's answers are determined to. The default is the ceiling
 *          [RESULT_SIGNIFICANT_DIGITS], which is correct only where every solver on the path to the
 *          number is tighter than `1e−9` — analytic models and closed-form geometry.
 * @param digitsByKey per-key overrides, applied to the **whole subtree** under that key. `P-18`'s
 *          measurement is that the determined precision is not one number even within one study:
 *          a stiffness at deep compression is a second difference of free energies and moves ~10⁴×
 *          further than the height it is evaluated at.
 * @param floor see [roundForResult] — lower it for a study emitting dimensionless quantities.
 * @param roundIntegralNumbers whether an integral JSON number (`45`) is coerced to a `Double`
 *          (`45.0`) rather than passed through. A **rendering** convention, not a precision one:
 *          three of the tree's independent rounding implementations coerce and three do not, and
 *          every committed result file already carries its own package's answer. Carrying it as a
 *          parameter is what lets `coupling/` and `brush/` delegate here (`T-214`) while moving
 *          departure fields and nothing else. The default is this package's own convention.
 *
 * [DEPARTURE_DIGITS_BY_KEY] is applied as a **baseline** beneath `digitsByKey`, so a study obeys
 * the departure rule by construction rather than by remembering to pass the map (`T-214`), and a
 * study that has *measured* its own departure precision can still override it with a
 * `record/spelling` entry of its own. `C-0131` refused this default on a measurement — keyed on a
 * **leaf name** it would have rounded `T-193`'s electrode potentials, in volts, to two digits —
 * and that refusal does not survive the re-keying `C-0131` performed in the same task: a
 * `record/spelling` map cannot reach `potentialOfZeroCharge/departure` at all.
 */
fun JsonElement.roundedForResult(
    digits: Int = RESULT_SIGNIFICANT_DIGITS,
    digitsByKey: Map<String, Int> = emptyMap(),
    floor: Double = RESULT_ABSOLUTE_FLOOR,
    roundIntegralNumbers: Boolean = false,
    parameterRecords: Set<String> = PARAMETER_RECORDS
): JsonElement = roundedForResult(
    digits, DEPARTURE_DIGITS_BY_KEY + digitsByKey, floor, roundIntegralNumbers,
    parameterRecords, record = null, insideParameters = false
)

/**
 * [roundedForResult], carrying the key of the nearest enclosing **object entry** as [record].
 *
 * An array contributes no key, so `convergence[*].departure` sees `record = "convergence"` — which
 * is what makes the qualifier a statement about the *record type* rather than about an index.
 */
private fun JsonElement.roundedForResult(
    digits: Int,
    digitsByKey: Map<String, Int>,
    floor: Double,
    roundIntegralNumbers: Boolean,
    parameterRecords: Set<String>,
    record: String?,
    insideParameters: Boolean
): JsonElement = when (this) {
    is JsonObject -> JsonObject(
        mapValues { (key, value) ->
            // Most specific wins: a `record/key` entry beats a bare `key` one, which beats the
            // precision inherited from the enclosing subtree.
            val qualified = record?.let { digitsByKey["$it/$key"] }
            value.roundedForResult(
                qualified ?: digitsByKey[key] ?: digits,
                digitsByKey, floor, roundIntegralNumbers, parameterRecords, record = key,
                // Sticky, and it has to be: a parameter block is a record TYPE, so everything
                // below it is input. The `record` qualifier above carries only the NEAREST
                // enclosing key, which under `parameters/buffer/debyeLength` is `buffer`.
                insideParameters = insideParameters || key in parameterRecords
            )
        }
    )
    is JsonArray -> JsonArray(
        map {
            it.roundedForResult(
                digits, digitsByKey, floor, roundIntegralNumbers, parameterRecords, record,
                insideParameters
            )
        }
    )
    is JsonPrimitive -> when {
        isString -> this
        // An input is not a result (`CH-0207`). Emitted exactly as the study was handed it, at
        // `Double.toString()`'s shortest round-trip decimal, so the file can re-run its own study.
        insideParameters -> this
        !roundIntegralNumbers && content.none { it == '.' || it == 'e' || it == 'E' } -> this
        else -> doubleOrNull?.let { JsonPrimitive(roundForResult(it, digits, floor)) } ?: this
    }
    else -> this
}
