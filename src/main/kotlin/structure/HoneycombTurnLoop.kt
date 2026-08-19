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

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tanh

/**
 * `T-230` / `T-231` — the **unpaired slack** a honeycomb raster turn needs, and what the **ragged
 * face** a two-length raster leaves costs the tile.
 *
 * `C-0140` establishes that a honeycomb x-raster carries **both** turn senses, so there is no
 * uniform row length at all, and that the two escapes are complementary:
 *
 *  * **route A** — an antiparallel scaffold **crossover** at every turn, zero unpaired slack, at
 *    the price of a **two-length** raster (112 / 108 bp) and the ragged faces it leaves;
 *  * **route B** — an unpaired **loop** at every turn, which frees the row length from the 21 bp
 *    residue condition entirely, at the price of **scaffold**. The only folded instance of the
 *    `15 × 4` cross-section spends **28 nt per helix** on it.
 *
 * Both questions are closed forms.
 *
 * ## The reach bound (`T-230`)
 *
 * `n` unpaired nucleotides between two anchoring phosphates make **`n + 1`** phosphodiester steps,
 * so the greatest span the chain can reach is `(n + 1) × step` with `step` the **measured**
 * intrastrand P···P step (`T-71`, 13 084 crystallographic linkages). Below `span/step − 1` the
 * turn closes at **no conformation whatever** — an impossibility statement of exactly the kind
 * `CLAUDE.md` records for `O3′–P–O5′–C5′`, one scale up, and it needs no polymer model.
 *
 * The `n = 0` case **is** a scaffold crossover, so the honeycomb's own interhelical distance less
 * two phosphate radii must fall inside the measured step, or the geometry here is being read
 * wrongly. It does, at `+1.50 σ`.
 *
 * ## The cost (`T-230`)
 *
 * A turn loop held at a fraction `x = R/L_c` of its contour carries a tension and stores a free
 * energy, and for a freely jointed chain both are closed forms:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`f = (k_BT/b)·L⁻¹(x)`, &nbsp;&nbsp;
 * `G = (k_BT·L_c/b)·[x·u − ln(sinh u / u)]`, &nbsp; `u = L⁻¹(x)`.
 *
 * The Kuhn length is the **zero-force** end of `CLAUDE.md`'s 2× method-systematic bracket,
 * 2.10–2.84 nm, and the contour per nucleotide that travels with it is the **inextensible**
 * 0.65–0.70 nm/nt. The 1.34–1.41 nm force-spectroscopy Kuhn and the 0.57 nm/nt extensible contour
 * are never mixed with them.
 *
 * ## The ragged face (`T-231`)
 *
 * A two-length raster's crossover **levels** drift and return; the spread of the levels at each
 * face is that face's raggedness, and it is a coordinate **in the plane of the tile** — the helix
 * ends are the tile's rim, not its gap-facing surface. [twoLengthRaster] emits the levels, the
 * per-helix row lengths and both spreads; [gapFacingRimLevels] reads the rim of one layer, whose
 * modulation is what a plate can or cannot follow.
 */

// ------------------------------------------------------------------ T-230, the reach bound

/**
 * The distance in nm between the two phosphates a raster turn must bridge: one on the backbone of
 * a helix at the origin, one on the backbone of a parallel helix at [interhelicalDistance], at
 * azimuths [exitAzimuthDegrees] and [entryAzimuthDegrees] measured from the line of centres, and
 * separated axially by [axialOffset].
 *
 * Azimuth `0°` points at the other helix, so `(0°, 180°)` is the closest approach and
 * `(180°, 0°)` the furthest.
 *
 * @throws IllegalArgumentException if the geometry is not physical.
 */
fun turnPhosphateSpan(
    interhelicalDistance: Double,
    phosphateRadius: Double,
    exitAzimuthDegrees: Double,
    entryAzimuthDegrees: Double,
    axialOffset: Double = 0.0
): Double {
    require(interhelicalDistance > 0.0) {
        "interhelicalDistance must be positive, was: $interhelicalDistance"
    }
    require(phosphateRadius >= 0.0) {
        "phosphateRadius must not be negative, was: $phosphateRadius"
    }
    val a = Math.toRadians(exitAzimuthDegrees)
    val b = Math.toRadians(entryAzimuthDegrees)
    val x = interhelicalDistance + phosphateRadius * cos(b) - phosphateRadius * cos(a)
    val y = phosphateRadius * sin(b) - phosphateRadius * sin(a)
    return sqrt(x * x + y * y + axialOffset * axialOffset)
}

/** The closest the two turn phosphates can be — both azimuths on the line of centres. */
fun minimumTurnPhosphateSpan(interhelicalDistance: Double, phosphateRadius: Double): Double =
    turnPhosphateSpan(interhelicalDistance, phosphateRadius, 0.0, 180.0)

/** The furthest the two turn phosphates can be — both azimuths pointing away. */
fun maximumTurnPhosphateSpan(interhelicalDistance: Double, phosphateRadius: Double): Double =
    turnPhosphateSpan(interhelicalDistance, phosphateRadius, 180.0, 0.0)

/**
 * The greatest span in nm that [unpairedNucleotides] unpaired nucleotides can reach: they make
 * `n + 1` phosphodiester steps of [phosphodiesterStep] between the two anchoring phosphates.
 */
fun maximumBackboneSpan(unpairedNucleotides: Int, phosphodiesterStep: Double): Double {
    require(unpairedNucleotides >= 0) {
        "unpairedNucleotides must not be negative, was: $unpairedNucleotides"
    }
    require(phosphodiesterStep > 0.0) {
        "phosphodiesterStep must be positive, was: $phosphodiesterStep"
    }
    return (unpairedNucleotides + 1) * phosphodiesterStep
}

/** The reach ceiling on the loop count: a turn is searched at most this far before refusing. */
const val TURN_LOOP_REACH_CEILING: Int = 10_000

/**
 * The fewest unpaired nucleotides that can bridge [span] nm — the smallest `n` with
 * `(n + 1) × step ≥ span`. Below it the turn closes at **no** conformation.
 *
 * Written as the exact inverse of [maximumBackboneSpan], the same expression evaluated the same
 * way, so no rounding convention can put the two out of step.
 */
fun minimumUnpairedNucleotides(span: Double, phosphodiesterStep: Double): Int {
    require(span >= 0.0) { "span must not be negative, was: $span" }
    require(phosphodiesterStep > 0.0) {
        "phosphodiesterStep must be positive, was: $phosphodiesterStep"
    }
    var n = 0
    while ((n + 1) * phosphodiesterStep < span) {
        n++
        require(n <= TURN_LOOP_REACH_CEILING) { "span $span is beyond any buildable loop" }
    }
    return n
}

// ------------------------------------------------------------- T-230, the freely jointed chain

/** Above this argument the Langevin function is evaluated on its own asymptote. */
private const val LANGEVIN_SMALL: Double = 0.5
private const val LANGEVIN_LARGE: Double = 20.0

/**
 * The Langevin function `L(u) = coth(u) − 1/u`, guarded at both ends.
 *
 * `cosh(u)/sinh(u)` returns `NaN` above `u ≈ 20` — the trap `CLAUDE.md` records three times — so
 * the quotient is never formed; and `1/tanh(u) − 1/u` loses every digit to cancellation below
 * `u ≈ 0.5`, where the series is used instead.
 */
fun langevin(u: Double): Double {
    require(u >= 0.0) { "the Langevin argument must not be negative, was: $u" }
    if (u < LANGEVIN_SMALL) {
        val u2 = u * u
        return u / 3.0 - u2 * u / 45.0 + 2.0 * u2 * u2 * u / 945.0 - u2 * u2 * u2 * u / 4725.0
    }
    return 1.0 / tanh(u) - 1.0 / u
}

/**
 * `ln(sinh(u)/u)`, guarded: `sinh` overflows above `u ≈ 710`, and the large-`u` form
 * `u − ln(2u) + ln(1 − e^(−2u))` is exact to the last ulp there.
 */
private fun logSinhOverU(u: Double): Double {
    if (u < 1e-8) return u * u / 6.0
    if (u > LANGEVIN_LARGE) return u - ln(2.0 * u) + ln(1.0 - exp(-2.0 * u))
    return ln(sinh(u) / u)
}

/**
 * The inverse Langevin, by bisection on a strictly increasing function.
 *
 * Bisection rather than a Padé approximant because the free energy below is an *integral* of this
 * and a 1 % approximant is not good enough to check against a quadrature; and because a
 * root-finder whose exit criterion is its own bracket width is exactly what `CLAUDE.md` warns
 * about only for **secant** methods — for bisection the bracket width *is* the error.
 */
fun inverseLangevin(x: Double, iterations: Int = 200): Double {
    require(x >= 0.0 && x < 1.0) { "the extension ratio must be in [0, 1), was: $x" }
    require(iterations > 0) { "iterations must be positive, was: $iterations" }
    if (x == 0.0) return 0.0
    var low = 0.0
    var high = 1.0
    while (langevin(high) < x) {
        high *= 2.0
        require(high < 1e18) { "the extension ratio $x is not reachable" }
    }
    repeat(iterations) {
        val mid = 0.5 * (low + high)
        if (langevin(mid) < x) low = mid else high = mid
    }
    return 0.5 * (low + high)
}

/** The tension in pN carried by a freely jointed chain of [contourLength] held at [extension]. */
fun turnLoopTension(
    extension: Double,
    contourLength: Double,
    kuhnLength: Double,
    thermalEnergy: Double
): Double {
    require(contourLength > 0.0) { "contourLength must be positive, was: $contourLength" }
    require(kuhnLength > 0.0) { "kuhnLength must be positive, was: $kuhnLength" }
    require(extension >= 0.0 && extension < contourLength) {
        "extension $extension must lie in [0, $contourLength)"
    }
    return thermalEnergy * inverseLangevin(extension / contourLength) / kuhnLength
}

/** A turn loop of a stated length, held at the span its two anchors impose. */
data class TurnLoopState(
    val unpairedNucleotides: Int,
    val span: Double,
    val contourLength: Double,
    val extensionRatio: Double,
    val tension: Double,
    val freeEnergy: Double
)

/**
 * The state of a turn loop of [unpairedNucleotides] nucleotides whose two anchors are [span] nm
 * apart, on a freely jointed chain of Kuhn length [kuhnLength] and contour
 * [contourPerNucleotide] per nucleotide.
 *
 * @throws IllegalArgumentException if the loop cannot reach the span at all — which is the reach
 *   bound, restated where a polymer model would otherwise silently return infinity.
 */
fun turnLoopState(
    span: Double,
    unpairedNucleotides: Int,
    kuhnLength: Double,
    contourPerNucleotide: Double,
    thermalEnergy: Double
): TurnLoopState {
    require(unpairedNucleotides > 0) {
        "unpairedNucleotides must be positive, was: $unpairedNucleotides"
    }
    require(contourPerNucleotide > 0.0) {
        "contourPerNucleotide must be positive, was: $contourPerNucleotide"
    }
    val contour = unpairedNucleotides * contourPerNucleotide
    require(span < contour) {
        "a loop of $unpairedNucleotides nt has contour $contour nm and cannot reach $span nm"
    }
    val x = span / contour
    val u = inverseLangevin(x)
    return TurnLoopState(
        unpairedNucleotides = unpairedNucleotides,
        span = span,
        contourLength = contour,
        extensionRatio = x,
        tension = thermalEnergy * u / kuhnLength,
        freeEnergy = thermalEnergy * contour / kuhnLength * (x * u - logSinhOverU(u))
    )
}

/** The fewest nucleotides whose turn loop carries no more than [allowableTension] pN. */
fun minimumNucleotidesForTension(
    span: Double,
    allowableTension: Double,
    kuhnLength: Double,
    contourPerNucleotide: Double,
    thermalEnergy: Double
): Int = smallestLoopSatisfying(span, contourPerNucleotide) { n ->
    turnLoopState(span, n, kuhnLength, contourPerNucleotide, thermalEnergy)
        .tension <= allowableTension
}

/** The fewest nucleotides whose turn loop stores no more than [allowableEnergy] pN·nm. */
fun minimumNucleotidesForFreeEnergy(
    span: Double,
    allowableEnergy: Double,
    kuhnLength: Double,
    contourPerNucleotide: Double,
    thermalEnergy: Double
): Int = smallestLoopSatisfying(span, contourPerNucleotide) { n ->
    turnLoopState(span, n, kuhnLength, contourPerNucleotide, thermalEnergy)
        .freeEnergy <= allowableEnergy
}

private fun smallestLoopSatisfying(
    span: Double,
    contourPerNucleotide: Double,
    satisfied: (Int) -> Boolean
): Int {
    require(span > 0.0) { "span must be positive, was: $span" }
    var n = 1
    while (n * contourPerNucleotide <= span || !satisfied(n)) {
        n++
        require(n <= TURN_LOOP_REACH_CEILING) { "no buildable loop satisfies the criterion" }
    }
    return n
}

/**
 * The longest uniform **paired** row length a [scaffoldNucleotides]-base scaffold affords a raster
 * of [helices] helices, each carrying [loopPerTurn] unpaired nucleotides of turn slack.
 *
 * The built design's own accounting is the check: `60 × (98 + 28) = 7 560`, exactly.
 */
fun maximumUniformRowLength(
    scaffoldNucleotides: Int,
    helices: Int,
    loopPerTurn: Int
): Int {
    require(helices > 0) { "helices must be positive, was: $helices" }
    require(loopPerTurn >= 0) { "loopPerTurn must not be negative, was: $loopPerTurn" }
    require(scaffoldNucleotides > 0) {
        "scaffoldNucleotides must be positive, was: $scaffoldNucleotides"
    }
    return scaffoldNucleotides / helices - loopPerTurn
}

// ---------------------------------------------------------------- T-231, the ragged face

/**
 * A two-length raster's axial geometry: the level at every one of the `H − 1` turns, the row
 * length every interior helix carries, and the spread each face is left with.
 *
 * A face's **raggedness** is the spread of its own crossover levels, in base pairs. The two faces
 * are the tile's in-plane **rim** — the plane in which the helices terminate — and not the
 * gap-facing surface, which is a row of duplex sidewalls at one column of the cross-section.
 */
data class RaggedTwoLengthRaster(
    val crossoverLevels: List<Int>,
    val helixRowLength: Map<Int, Int>,
    val frontSpreadBasePairs: Int,
    val rearSpreadBasePairs: Int,
    val axialExtentBasePairs: Int,
    val scaffoldNucleotides: Int
)

/**
 * The axial geometry a two-length assignment produces on [turns]: sense 1 helices take
 * [senseOneRowLength] base pairs and sense 2 helices [senseTwoRowLength].
 *
 * Reproduces `C-0140`'s own construction — the level walk, the extent and both face spreads — so
 * its 112 / 108 recommendation is re-derived here rather than transcribed.
 */
fun twoLengthRaster(
    turns: List<RasterTurn>,
    senseOneRowLength: Int,
    senseTwoRowLength: Int
): RaggedTwoLengthRaster {
    require(turns.size >= 3) { "a raster needs at least three turns, had: ${turns.size}" }
    require(senseOneRowLength > 0 && senseTwoRowLength > 0) {
        "both row lengths must be positive, were: $senseOneRowLength, $senseTwoRowLength"
    }
    val levels = HashMap<Int, Int>()
    levels[turns.first().index - 1] = 0
    val lengths = LinkedHashMap<Int, Int>()
    var current = 0
    turns.forEach { turn ->
        val length = if (turn.effectiveSense == 1) senseOneRowLength else senseTwoRowLength
        lengths[turn.index] = length
        current += turn.axialSign * length
        levels[turn.index] = current
    }
    val spans = turns.map {
        minOf(levels.getValue(it.index - 1), levels.getValue(it.index)) to
                maxOf(levels.getValue(it.index - 1), levels.getValue(it.index))
    }
    val front = levels.filterKeys { Math.floorMod(it, 2) == 0 }.values
    val rear = levels.filterKeys { Math.floorMod(it, 2) == 1 }.values
    return RaggedTwoLengthRaster(
        crossoverLevels = levels.keys.sorted().map { levels.getValue(it) },
        helixRowLength = lengths,
        frontSpreadBasePairs = front.max() - front.min(),
        rearSpreadBasePairs = rear.max() - rear.min(),
        axialExtentBasePairs = spans.maxOf { it.second } - spans.minOf { it.first },
        scaffoldNucleotides = lengths.values.sum() + senseOneRowLength + senseTwoRowLength
    )
}

/**
 * The front-face rim level of every helix of [raster] that sits in cross-section column [column],
 * paired with the raster row it belongs to and ordered by that row.
 *
 * One column is one **layer**, so column `0` (or the last) is the gap-facing layer, and this is
 * the sequence a plate would have to follow if the raggedness were to reach the flatness at all.
 */
fun gapFacingRimLevels(
    turns: List<RasterTurn>,
    raster: RaggedTwoLengthRaster,
    column: Int
): List<Pair<Int, Int>> {
    val first = turns.first().index - 1
    return turns.filter { it.cell?.x == column }.map { turn ->
        val cell = requireNotNull(turn.cell) { "a honeycomb turn must carry a cell" }
        val row = (-cell.y) / 3
        val before = turn.index - 1
        val frontIndex = if (Math.floorMod(before, 2) == 0) before else turn.index
        row to raster.crossoverLevels[frontIndex - first]
    }.sortedBy { it.first }
}

/**
 * The smallest `p` for which [values] repeats with period `p`. A constant sequence returns `1`;
 * a sequence with no repeat returns its own length.
 */
fun sequencePeriod(values: List<Int>): Int {
    require(values.isNotEmpty()) { "an empty sequence has no period" }
    (1..values.size).forEach { p ->
        if ((0 until values.size - p).all { values[it] == values[it + p] }) return p
    }
    return values.size
}

/**
 * The peak-to-peak amplitude of a square wave of half-amplitude `a` seen as its **fundamental**
 * sinusoid: `4a/π` peak-to-peak per half-amplitude, i.e. `(4/π)` of the square wave's own
 * amplitude. Used so that [loadRippleTransmission], which is written for a sinusoid, is applied
 * to the sinusoid the rim modulation actually contains rather than to a square wave.
 */
fun squareWaveFundamentalAmplitude(peakToPeak: Double): Double {
    require(peakToPeak >= 0.0) { "peakToPeak must not be negative, was: $peakToPeak" }
    return 2.0 * peakToPeak / PI
}

/** `|a − b| / |b|`, the departure convention this repository quotes at two significant digits. */
fun relativeDeparture(value: Double, reference: Double): Double =
    if (reference == 0.0) abs(value) else abs(value - reference) / abs(reference)
