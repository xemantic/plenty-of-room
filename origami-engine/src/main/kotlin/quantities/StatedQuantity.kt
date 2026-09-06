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

package com.xemantic.nano.plentyofroom.quantities

import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.sqrt

/**
 * A number that is **not well posed without the state it was read at**.
 *
 * This is the corpus's dominant error class given a type. `CLAUDE.md` records eleven separate
 * instances of one mistake — a layer stiffness quoted without its compression, a variance without
 * its bandwidth, a rupture force without its bonded length and loading rate, `k_es` without its
 * gap, a flatness without its load case and operating state, a plan ceiling without its occupancy,
 * a buffer advantage without the stroke it was read at. Every one was caught by a person reading
 * prose. Every one cost an iteration.
 *
 * The type does three things prose cannot.
 *
 *  1. **The state is a constructor parameter**, so omitting it is a compile error.
 *  2. [quote] renders the state beside the value, so a number cannot be *printed* bare.
 *  3. [ratioOf] and [differenceOf] **refuse** two quantities read at different states. That is the
 *     half that actually moved answers here: `C-0012`'s *"a factor of five better at 0.5 mM"* is a
 *     ratio taken at zero stroke and quoted at the operating point, an overstatement of 3.16–3.35×,
 *     and it is exactly the operation this refuses.
 *
 * The interface is deliberately thin. It does not attempt units algebra — the project's units are
 * locked (`pN`, `nm`, `pN·nm`) and a unit error has never been one of its failures. What has failed,
 * repeatedly, is the *state*.
 */
interface StatedQuantity {

    /** The number itself, in [unit]. */
    val value: Double

    /** The locked unit this quantity is carried in. */
    val unit: String

    /**
     * What kind of quantity this is.
     *
     * Two quantities of different kinds never compare, however similar their states look — a layer
     * stiffness and an electrostatic stiffness are both `pN/nm` and are the two *terms* of `k_eff`,
     * and `CLAUDE.md` records that comparing across them is how a mean-field level got read as a
     * stability margin.
     */
    val kind: String

    /**
     * The state the value was read at, keyed by the name the state is known by.
     *
     * Insertion-ordered, because [quote] renders it in the order the quantity declares.
     */
    val state: Map<String, String>

    /** The value, its unit and every state entry, in one string that cannot omit the state. */
    fun quote(): String =
        "$value $unit at " + state.entries.joinToString(", ") { "${it.key} = ${it.value}" }

    /** The same quantity, scaled — the state is unchanged, because scaling does not move it. */
    fun scaledBy(factor: Double): StatedQuantity
}

/** The shared part of every [StatedQuantity]: a value, a unit, a kind and a state. */
private data class PlainQuantity(
    override val value: Double,
    override val unit: String,
    override val kind: String,
    override val state: Map<String, String>
) : StatedQuantity {
    override fun scaledBy(factor: Double): StatedQuantity = copy(value = value * factor)
}

/**
 * The grafted layer's stiffness, which has no meaning at all without a compression.
 *
 * `C-0003`: the de Gennes scaling form has finite stiffness at first contact and the
 * Milner-Witten-Cates form has **exactly zero** there, so *"the layer stiffness"* is not a number
 * — and the [model] belongs in the state for the same reason.
 */
data class LayerStiffness(
    val pnPerNm: Double,
    val atCompressionNm: Double,
    val model: String
) : StatedQuantity {

    override val value: Double get() = pnPerNm
    override val unit: String get() = "pN/nm"
    override val kind: String get() = "layer stiffness"
    override val state: Map<String, String> = linkedMapOf(
        "compressionNm" to atCompressionNm.toString(),
        "model" to model
    )

    override fun scaledBy(factor: Double): LayerStiffness = copy(pnPerNm = pnPerNm * factor)
}

/**
 * The electrostatic stiffness, whose **sign** is a property of the gap.
 *
 * `CLAUDE.md`: *"`k_es < 0` is a property of the force decaying with the gap, not an axiom"* —
 * past the force peak the electrostatics stiffens the layer instead of softening it. And a bias
 * that is not stated is a bias somebody will assume.
 */
data class ElectrostaticStiffness(
    val pnPerNm: Double,
    val gapNm: Double,
    val biasVolts: Double
) : StatedQuantity {

    override val value: Double get() = pnPerNm
    override val unit: String get() = "pN/nm"
    override val kind: String get() = "electrostatic stiffness"
    override val state: Map<String, String> = linkedMapOf(
        "gapNm" to gapNm.toString(),
        "biasVolts" to biasVolts.toString()
    )

    override fun scaledBy(factor: Double): ElectrostaticStiffness = copy(pnPerNm = pnPerNm * factor)
}

/**
 * A fluctuation amplitude, which is a statement about a **band**.
 *
 * For an overdamped mode the fraction of the variance below `f` is `(2/π) arctan(f/f_c)`, so a
 * broadband RMS is the `f → ∞` limit and is not what any instrument measures. `C-0004`'s drainage
 * corner puts a few per cent of the Gen-1 tile's variance below 1 kHz — 13× in amplitude, more
 * than the entire model bracket.
 */
data class FluctuationAmplitude(
    val rms: Double,
    val bandwidthHz: Double,
    val cornerHz: Double
) : StatedQuantity {

    init {
        require(rms > -1.0e-30) { "rms must be non-negative, was: $rms" }
        require(cornerHz > 0.0) { "cornerHz must be positive, was: $cornerHz" }
        require(bandwidthHz > -1.0e-30) { "bandwidthHz must be non-negative, was: $bandwidthHz" }
    }

    override val value: Double get() = rms
    override val unit: String get() = "nm"
    override val kind: String get() = "fluctuation amplitude"
    override val state: Map<String, String> = linkedMapOf(
        "bandwidthHz" to bandwidthHz.toString(),
        "cornerHz" to cornerHz.toString()
    )

    /** `(2/π) arctan(f/f_c)`, the fraction of an Ornstein-Uhlenbeck variance inside the band. */
    fun fractionOfVarianceInBand(): Double = (2.0 / PI) * atan(bandwidthHz / cornerHz)

    /**
     * The same fluctuation read through a narrower instrument.
     *
     * The variance scales with the band fraction, so the amplitude scales with its square root —
     * which is why a broadband number and a measured one differ by far more than the models do.
     */
    fun within(bandwidthHz: Double): FluctuationAmplitude {
        val narrowed = copy(bandwidthHz = bandwidthHz)
        val ratio = narrowed.fractionOfVarianceInBand() / fractionOfVarianceInBand()
        return narrowed.copy(rms = rms * sqrt(ratio))
    }

    override fun scaledBy(factor: Double): FluctuationAmplitude = copy(rms = rms * factor)
}

/**
 * A DNA rupture force, which is a function of the **bonded length** and the **loading rate**.
 *
 * `CLAUDE.md`: the 48 pN in circulation is Strunz's **30 bp** number, and his own constants give
 * 18.8 pN at 8 bp and 34.8 at 16 — so quoting it for a staple domain of unstated length is
 * optimistic by up to 2.6×. The same origami class moves ~42 → ~75 pN over the measured rates.
 */
data class RuptureForce(
    val pn: Double,
    val bondedBasePairs: Int,
    val loadingRatePnPerSecond: Double
) : StatedQuantity {

    init {
        require(bondedBasePairs > 0) {
            "bondedBasePairs must be positive, was: $bondedBasePairs"
        }
        require(loadingRatePnPerSecond > 0.0) {
            "loadingRatePnPerSecond must be positive, was: $loadingRatePnPerSecond"
        }
    }

    override val value: Double get() = pn
    override val unit: String get() = "pN"
    override val kind: String get() = "rupture force"
    override val state: Map<String, String> = linkedMapOf(
        "bondedBasePairs" to bondedBasePairs.toString(),
        "loadingRatePnPerSecond" to loadingRatePnPerSecond.toString()
    )

    override fun scaledBy(factor: Double): RuptureForce = copy(pn = pn * factor)
}

/**
 * The state check both comparisons share.
 *
 * It reports the **first** differing key with both readings, because the useful half of the message
 * is which state moved — not that one did.
 */
private fun requireOneState(left: StatedQuantity, right: StatedQuantity, operation: String) {
    require(left.kind == right.kind) {
        "$operation is not defined across two kinds of quantity: " +
            "'${left.kind}' against '${right.kind}'. They are different quantities that happen " +
            "to share a unit."
    }
    require(left.unit == right.unit) {
        "$operation is not defined across two units: '${left.unit}' against '${right.unit}'."
    }
    val keys = left.state.keys + right.state.keys
    keys.forEach { key ->
        val here = left.state[key]
        val there = right.state[key]
        require(here == there) {
            "$operation is not defined across two states: $key = $here against $key = $there. " +
                "A comparison is itself a quantity and it needs ONE state; read both at the same " +
                "$key, or quote the comparison with the two states it spans."
        }
    }
}

/**
 * The ratio of two quantities read at the **same** state, as a plain number.
 *
 * A ratio across two states is refused rather than returned, because that is the operation this
 * project has got wrong: an advantage measured at zero stroke and quoted at the operating point,
 * a stiffness ratio across two compressions, a margin ratio across two gaps.
 */
fun ratioOf(numerator: StatedQuantity, denominator: StatedQuantity): Double {
    requireOneState(numerator, denominator, "a ratio")
    require(denominator.value != 0.0) { "a ratio against an exactly zero denominator" }
    return numerator.value / denominator.value
}

/** The difference of two quantities read at the **same** state, in their shared unit. */
fun differenceOf(left: StatedQuantity, right: StatedQuantity): Double {
    requireOneState(left, right, "a difference")
    return left.value - right.value
}

/**
 * A quantity of an arbitrary kind, for a study that has one this file does not name yet.
 *
 * Prefer a named type: the point of the named ones is that the state cannot be forgotten at the
 * call site, and this escape hatch gives that back.
 */
fun statedQuantity(
    value: Double,
    unit: String,
    kind: String,
    vararg state: Pair<String, String>
): StatedQuantity {
    require(state.isNotEmpty()) {
        "a stated quantity with no state is exactly what this package exists to prevent; " +
            "if the quantity genuinely has no state, it is a constant and does not belong here"
    }
    return PlainQuantity(value, unit, kind, linkedMapOf(*state))
}
