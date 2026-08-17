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

package com.xemantic.nano.plentyofroom.synthesis

import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.max

/**
 * Task `T-156` — **how many of the six 0.5 mM routes are read on WITHDRAWN objects?** Leaf `A2.2`.
 *
 * ## What this exists to settle
 *
 * `DECISIONS-FOR-NDI.md` decision 1, `ANSWERS.md` question 1 and `TASKS.md`'s `T-63` row all carry
 * one sentence: *"six independent routes recommend 0.5 mM"* — `C-0012` on the force clause,
 * `C-0016` on the bias window, `C-0017` on the stability floor, `C-0018` on the usable bias,
 * `C-0027` on the corrected margin and `C-0032` on the realised coupling law.
 *
 * `C-0084`/`CH-0098` withdrew the sixth. **This is the census `CH-0098` asked for**, and it asks
 * two questions of each of the remaining five: *is the object still in the design*, and *does the
 * conclusion survive on the element `C-0071` recommends — on the SAME ground, or a different one?*
 *
 * ## The cheap bound, which needs no physics at all
 *
 * Two of the six can be shown **not to be routes** by comparing the emitting result files field for
 * field. A number that appears in two files to their own emission precision was **transferred**, and
 * a transfer is not a second derivation:
 *
 * - `T-2`'s `biasClauses[].biasForHundredPiconewtonBlocking` against `T-3`'s
 *   `thresholds[].biasForHundredPiconewtonBlocking` — `C-0016`'s clause against `C-0012`'s;
 * - `T-25`'s `bufferComparison[].stiffnessMarginBaseline*` and `biasMargin*` against the extrema of
 *   `T-16`'s `stabilityMargin` and `T-4`'s coupled `margin` — `C-0027`'s two halves against
 *   `C-0017`'s and `C-0018`'s.
 *
 * That is a pass over four JSON files, and it decides independence before any route is evaluated.
 * **Two independent derivations of the same physics would not agree to nine digits**; only a
 * transfer does.
 *
 * ## Conventions, restated rather than inherited
 *
 * - Stiffness **pN/nm**, potential **V**, concentration **mM**; `k_BT = 4.141947 pN·nm` at **300 K**
 *   in aqueous MgCl₂ at the stated concentration.
 * - A **route** is a named claim plus the **one** quantity of it that is compared between 0.5 mM
 *   and §3's 2 mM. Two routes are independent only if neither's quantity is the other's,
 *   transferred.
 * - A **buffer advantage** is a ratio, always oriented so that a value above one favours 0.5 mM,
 *   and it is quoted **with the state it is read at** — the force clause is 4.97× at **zero stroke**
 *   and 1.48–1.57× at the **held** operating point the device occupies.
 * - The **recommended device** is the 10 nm layer at `σ` = 0.024 nm⁻², placed at 100 pN over §3's
 *   acceptable 3 nm stroke (`C-0071`, `C-0068`); states of other devices are not intersected with
 *   it (`C-0064`).
 * - **Stability is read on a coupling's TANGENT** (`C-0049`, `CH-0042`), so `C-0017`'s margin — which
 *   divides by the mandated **secant** — is rescaled by `30.028762/33.3333333` onto `C-0069`'s `Q5`.
 *   **The floor itself is element-independent and does not move.**
 */

/** `C-0017`'s mandate as a **sum**, in pN/nm — `100 pN / 3 nm`, from §3 alone. */
const val GEN1_MANDATED_SECANT: Double = 100.0 / 3.0

/**
 * The relative slack two nine-significant-digit result files leave in one comparison.
 *
 * **Derived, not chosen to pass** (`CLAUDE.md`: *"an assertion tighter than a result file's EMISSION
 * precision is not a stronger test, it is a test of the printed digits"*). Nine digits leave `5e−9`
 * of relative slack in one field; the census compares two fields and two files round independently,
 * and `T-25` prints eight where `T-16` prints nine — so the floor is `1e−8` and this sits two
 * decades above it, still six decades below any physical difference the census could mistake it for.
 */
const val EMITTED_FIELD_SLACK: Double = 1.0e-6

// ---------------------------------------------------------------- the classification vocabulary

/** What a route's compared quantity is read on, and whether the programme still builds it. */
enum class RouteObject {

    /** The unloaded force balance at `h = L₀` — tile, field and layer, and **no coupling element**. */
    UNLOADED_FIELD_BALANCE,

    /** `|k_eff(L₀ − 3 nm)|` at the held operating point — element-**independent** by construction. */
    HELD_OPERATING_POINT,

    /** `C-0018`'s affine mandate `R = 33.3333 s` — an idealisation that was never an element. */
    AFFINE_MANDATE_PATH,

    /** `C-0030`'s strain-**softening** coupled-standoff flexure — removed by `CH-0081`/`C-0069`. */
    SOFTENING_FLEXURE_PATH,

    /** Another route's own result-file numbers, corrected or re-intersected. */
    TRANSFERRED_READINGS
}

/** Whether a route's 0.5 mM conclusion survives on `C-0069`'s `Q5`, and on what ground. */
enum class RouteVerdict {

    /** It survives, and for the reason its own claim gives. */
    SURVIVES_SAME_GROUND,

    /** It survives, and the reason its own claim gives is void — a different one carries it. */
    SURVIVES_DIFFERENT_GROUND,

    /** The object it is read on has left the design; the route does not transfer. */
    WITHDRAWN
}

/** Whether a route is its own derivation or another's numbers, read again. */
enum class RouteIndependence {

    /** Nothing in the census supplies this route's compared quantity. */
    INDEPENDENT,

    /** The compared quantity agrees with another route's to the emission precision. */
    TRANSFER
}

/**
 * One of the six named 0.5 mM routes, classified.
 *
 * @property advantage the buffer advantage, oriented so that a value above one favours 0.5 mM, and
 *   `null` where the route supplies no comparable ratio (a withdrawn one).
 * @property transferOf the routes whose numbers this one carries — empty for an independent route.
 */
@Serializable
data class BufferRoute(
    val claim: String,
    val clause: String,
    val comparedQuantity: String,
    val readAt: String,
    val obj: RouteObject,
    val objectStillInTheDesign: Boolean,
    val lowSaltReading: Double?,
    val highSaltReading: Double?,
    val smallerIsBetter: Boolean,
    val advantage: Double?,
    val verdict: RouteVerdict,
    val ground: String,
    val independence: RouteIndependence,
    val transferOf: List<String>,
    val provenance: String
)

/** One field-for-field comparison behind a [RouteIndependence] verdict. */
@Serializable
data class RouteTransferCheck(
    val quantity: String,
    val state: String,
    val here: Double?,
    val there: Double?,
    val hereSource: String,
    val thereSource: String,
    val departure: Double?,
    val transfer: Boolean
)

// ---------------------------------------------------------------- the arithmetic

/**
 * The ratio by which 0.5 mM beats §3's 2 mM on one quantity, oriented so that **above one favours
 * 0.5 mM**: `high/low` where a smaller reading is better (a bias a target needs), `low/high` where a
 * larger one is (a margin).
 */
fun bufferAdvantage(
    lowSaltReading: Double,
    highSaltReading: Double,
    smallerIsBetter: Boolean
): Double {
    require(lowSaltReading > 0.0 && lowSaltReading.isFinite()) {
        "lowSaltReading must be positive and finite, was: $lowSaltReading"
    }
    require(highSaltReading > 0.0 && highSaltReading.isFinite()) {
        "highSaltReading must be positive and finite, was: $highSaltReading"
    }
    return if (smallerIsBetter) highSaltReading / lowSaltReading
    else lowSaltReading / highSaltReading
}

/**
 * `C-0017`'s stability margin, re-read on a coupling whose stability-relevant stiffness is
 * [tangentMinimum] rather than the mandated [mandatedSecant].
 *
 * The **floor** is `|k_eff|` at the held operating point and contains no coupling element at all, so
 * only the numerator moves: the rescaling is exactly `tangent/secant` and it is an identity when the
 * two coincide. `C-0049`/`CH-0042`: a stability requirement is owed on the **tangent** over the
 * strokes the device traverses, and `C-0069`'s `Q5` strain-**stiffens**, so its minimum over
 * `[0, 3 nm]` is at zero stroke.
 */
fun marginOnTangent(
    mandateMargin: Double,
    mandatedSecant: Double,
    tangentMinimum: Double
): Double {
    require(mandateMargin > 0.0 && mandateMargin.isFinite()) {
        "mandateMargin must be positive and finite, was: $mandateMargin"
    }
    require(mandatedSecant > 0.0 && mandatedSecant.isFinite()) {
        "mandatedSecant must be positive and finite, was: $mandatedSecant"
    }
    require(tangentMinimum > 0.0 && tangentMinimum.isFinite()) {
        "tangentMinimum must be positive and finite, was: $tangentMinimum"
    }
    return mandateMargin * tangentMinimum / mandatedSecant
}

/**
 * The relative departure between two readings of what may be one number, or `null` when either is
 * absent — a departure between a number and an absence is not a number (`CLAUDE.md`:
 * *"`kotlinx.serialization` refuses `NaN`"*).
 */
fun transferDeparture(here: Double?, there: Double?): Double? {
    if (here == null || there == null) return null
    val scale = max(max(abs(here), abs(there)), 1.0e-300)
    return abs(here - there) / scale
}

/**
 * Whether two readings are the **same number, printed twice** — decided at [tolerance], which must
 * be derived from the two files' emission precision and never from equality.
 *
 * Two absences agree; an absence against a reading does not.
 */
fun isTransfer(here: Double?, there: Double?, tolerance: Double): Boolean {
    require(tolerance > 0.0) { "tolerance must be positive, was: $tolerance" }
    if (here == null && there == null) return true
    val departure = transferDeparture(here, there) ?: return false
    return departure <= tolerance
}
