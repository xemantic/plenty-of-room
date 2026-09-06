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

package com.xemantic.nano.plentyofroom.coupling

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * `T-148` — staple incorporation as a **position-dependent Bernoulli dropout**, which is
 * [`CH-0084`]'s flatness half.
 *
 * ## Why a dropout is not a scatter
 *
 * Every tolerance this programme has priced so far is *multiplicative*: `C-0026`'s named patterns
 * and `C-0060`'s threshold both perturb a path's stiffness by `1 ± ε`. **A missing staple is not
 * that.** The path is not there: its stiffness is exactly zero, the coupling has one fewer
 * attachment, and the population of per-path stiffnesses is **two-valued** rather than spread.
 * Its relative standard deviation is [bernoulliRelativeScatter], `√(f/(1 − f))`, and `CH-0084`
 * grades that against `C-0060`'s threshold — while saying, correctly, that the two have the same
 * standard deviation and **different spatial structure**.
 *
 * `CLAUDE.md` records what that difference is worth in this project: *"which way a tolerance is
 * correlated matters more than how big it is"*. So the amplitude is not the statement, and this
 * file exists to supply the structure.
 *
 * ## What is measured and what is a convention
 *
 * [StapleDropoutLiterature] carries the measurement — Strauss et al. (2018), **read directly** —
 * and nothing else here is measured. The **mapping** of that measurement onto a 40 nm tile is a
 * convention, and it is carried as three of them ([IncorporationConvention]) rather than one,
 * each **fitted** to the measured mean on a stated reference geometry so that the Gen-1 tile's own
 * mean incorporation comes out as a *result*.
 *
 * ## Conventions, restated rather than inherited
 *
 * Lengths **nm**, stiffness **pN/nm**; probabilities are dimensionless and live in `[0, 1]`.
 * `x` runs **along** the helices, `y` **across** them; the origin is the tile centre.
 * A station's **edge distance** is `d = min(edgeX/2 − |x|, edgeY/2 − |y|)`.
 * Realisations are **independent** across stations: Strauss reports no correlation length, and
 * this is stated as the convention it is rather than derived.
 */

// --------------------------------------------------------------------------------- the measurement

/**
 * Strauss, Schueder, Haas, Nickels, Jungmann, *Nat. Commun.* **9**:1600 (2018),
 * *"Quantifying absolute addressability in DNA origami with molecular resolution"*
 * (`PMC5913233`) — **READ DIRECTLY** from EuropePMC's full text, not from a summary.
 *
 * A DNA-PAINT map of **detection** efficiency at single-staple resolution over **168** staples of
 * a 2D rectangular DNA origami folded from scaffold **p7249** (= M13mp18), corroborated by
 * next-generation sequencing of relative staple abundance.
 *
 * Verbatim, Results: *"The results indicate a consistently lower efficiency of detection on the
 * outside of the structure (with a minimum of 41 %) compared to inner areas where detection
 * efficiencies reached 88 % (the average detection efficiency for all strands was 77 %). Taking
 * the detection efficiency offset of 7 % … this translates to absolute incorporation efficiencies
 * of 48–95 % with an average of 84 %"*.
 *
 * **The offset is additive in percentage points, and that is checkable rather than assumed**:
 * `41 + 7 = 48`, `88 + 7 = 95`, `77 + 7 = 84`, all three exactly. Gate 1 asserts it.
 *
 * Verbatim, on the mechanism: *"staples at the edges and corners are missing neighboring helices
 * and/or lack stacking interactions to neighboring strands"* — which is what makes a **boundary
 * layer** the right shape of model and a bulk gradient the wrong one.
 */
object StapleDropoutLiterature {

    /** The lowest **detection** efficiency, on the outside of the structure. */
    const val DETECTION_MINIMUM: Double = 0.41

    /** The highest **detection** efficiency, in the inner areas. */
    const val DETECTION_MAXIMUM: Double = 0.88

    /** The mean **detection** efficiency over all 168 probed staples. */
    const val DETECTION_MEAN: Double = 0.77

    /** The detection-to-incorporation offset, **additive in percentage points**. */
    const val DETECTION_TO_INCORPORATION_OFFSET: Double = 0.07

    /** The lowest **incorporation** efficiency, at the edges. */
    const val INCORPORATION_EDGE: Double = 0.48

    /** The highest **incorporation** efficiency, in the centre. */
    const val INCORPORATION_CENTRE: Double = 0.95

    /** The mean **incorporation** efficiency over all 168 probed staples. */
    const val INCORPORATION_MEAN: Double = 0.84

    /** The number of staples individually probed. */
    const val PROBED_STAPLES: Int = 168

    /**
     * Rothemund's own pixel yield, *Nature* **440**:297 (2006), **read directly** —
     * *"94 % of '1' pixels (of 1,080 observed) were visualized"*. A corroborating reading of the
     * same quantity on the same object class by a different method, carried as a bracket end.
     */
    const val ROTHEMUND_PIXEL_YIELD: Double = 0.94

    /** The number of structures each cell of the map is a proportion of — Methods, `n = 186`. */
    const val STRUCTURES_IMAGED: Int = 186
}

/**
 * **The map itself** — the 168 per-staple detection efficiencies of Strauss et al.'s
 * Supplementary Fig. 14, transcribed from the figure and validated three ways.
 *
 * ## Provenance and how the transcription is checked
 *
 * **READ DIRECTLY** from the paper's own Supplementary Information
 * (`41467_2018_4031_MOESM1_ESM.pdf`, Springer Nature's open-access ESM endpoint), Supplementary
 * Fig. 14, which is Fig. 4c with the numbers printed. The transcription is validated by three
 * independent checks, each asserted as a test rather than claimed:
 *
 * 1. the paper's own panel arithmetic — panel *b* is panel *a* plus the 7 percentage-point
 *    detection-to-incorporation offset, for all 168 cells;
 * 2. the paper's own printed summary — minimum **41**, maximum **88**, mean **77**, which this
 *    array reproduces at 40.9, 88.2 and 76.50;
 * 3. **every value is `k/186`**, because each is a proportion of the `n = 186` imaged structures.
 *    The mean distance to the nearest such multiple is **0.067** percentage points here, against
 *    0.23 at `n = 185` and 0.24 at `n = 187` — so the quantisation identifies the denominator and
 *    certifies that no digit was mis-read.
 *
 * ## The layout, from the paper's own caDNAno tables
 *
 * **16 columns (A–P) × 12 rows (1–12) = 192 grid positions**, of which 168 are probed; the 24
 * unprobed ones are the interior positions at columns C, G, K, O and rows 2–4 and 9–11, which
 * carry the biotinylated surface-attachment strands and their neighbours.
 *
 * **A row is two helices** (row 1 = helices 21–23, row 12 = helices 0–2), so the row index runs
 * **across** the helices at a pitch of two interhelical distances; a column is a **16 bp**
 * staple domain, so the column index runs **along** them. The cells are therefore very nearly
 * square, 5.44 nm along by 5.38 nm across on this project's measured 2.69 nm interhelical
 * distance.
 *
 * ## What the map says that the three summary numbers do not
 *
 * **The 48 % is ONE CORNER of 168, not the edge.** The lowest cell, `P1` = 40.9 % detected
 * (47.9 % incorporated), is the bottom-right corner; the **perimeter mean** is 70.51 % detected,
 * i.e. **77.5 %** incorporated, against an interior mean of 79.18 % detected (**86.2 %**
 * incorporated). Six perimeter cells are *above* the interior mean, the perimeter's standard
 * deviation is 1.8× the interior's, and the four corners run 40.9, 53.2, 62.9 and **80.1** — so
 * corner-ness is not a predictor. Any field that puts the whole rim at 48 % is far harsher than
 * the measurement.
 */
object StrausIncorporationMap {

    /** Columns of the map — 16 staple domains along the helices. */
    const val COLUMNS: Int = 16

    /** Rows of the map — 12 pairs of helices across them. */
    const val ROWS: Int = 12

    /** Unprobed cells are `NaN`: the map is 192 positions and 168 measurements. */
    private const val UNPROBED = Double.NaN

    /**
     * Detection efficiency in **per cent**, `[row][column]` with row 0 the map's row 1 and
     * column 0 its column A. Row 1 and row 12 are the outermost helices.
     */
    val DETECTION_PER_CENT: Array<DoubleArray> = arrayOf(
        doubleArrayOf(
            53.2, 70.4, 59.7, 80.1, 76.9, 67.2, 66.7, 74.7,
            66.9, 76.9, 63.4, 72.0, 71.5, 64.5, 64.5, 40.9
        ),
        doubleArrayOf(
            71.0, 76.3, UNPROBED, 73.1, 80.6, 75.3, UNPROBED, 75.3,
            75.3, 72.0, UNPROBED, 73.1, 81.2, 72.6, UNPROBED, 75.8
        ),
        doubleArrayOf(
            65.1, 72.0, UNPROBED, 75.3, 78.5, 70.4, UNPROBED, 78.0,
            70.4, 76.3, UNPROBED, 81.7, 78.5, 78.5, UNPROBED, 70.4
        ),
        doubleArrayOf(
            68.8, 69.9, UNPROBED, 80.1, 81.7, 76.3, UNPROBED, 79.3,
            80.7, 82.8, UNPROBED, 78.5, 78.5, 79.0, UNPROBED, 75.3
        ),
        doubleArrayOf(
            78.0, 76.3, 72.0, 81.7, 81.2, 76.3, 79.0, 78.5,
            85.2, 81.2, 83.3, 83.9, 87.6, 76.3, 74.2, 77.4
        ),
        doubleArrayOf(
            52.7, 79.6, 80.6, 84.9, 82.8, 82.8, 83.9, 86.6,
            80.7, 81.2, 80.6, 80.6, 85.5, 85.5, 76.9, 79.0
        ),
        doubleArrayOf(
            83.9, 82.8, 71.0, 82.3, 84.9, 84.4, 83.9, 85.0,
            82.3, 87.1, 79.0, 87.6, 77.4, 77.4, 77.4, 74.7
        ),
        doubleArrayOf(
            84.4, 78.0, 74.2, 87.1, 85.5, 82.3, 84.4, 86.0,
            79.8, 78.5, 78.5, 80.6, 88.2, 75.8, 75.3, 71.5
        ),
        doubleArrayOf(
            82.3, 77.4, UNPROBED, 80.6, 80.1, 75.3, UNPROBED, 77.5,
            79.6, 76.3, UNPROBED, 84.9, 83.3, 71.0, UNPROBED, 76.3
        ),
        doubleArrayOf(
            76.3, 70.4, UNPROBED, 73.7, 77.4, 72.0, UNPROBED, 76.1,
            75.9, 84.9, UNPROBED, 80.1, 75.3, 68.8, UNPROBED, 71.0
        ),
        doubleArrayOf(
            65.1, 76.3, UNPROBED, 81.2, 79.0, 79.0, UNPROBED, 81.5,
            81.2, 80.6, UNPROBED, 84.4, 80.6, 74.2, UNPROBED, 80.1
        ),
        doubleArrayOf(
            62.9, 66.1, 66.7, 62.9, 68.3, 68.3, 61.3, 66.7,
            73.9, 75.3, 75.3, 71.5, 76.3, 73.1, 69.4, 80.1
        )
    )

    /** Every probed cell as `(column, row, incorporation)`, the offset already applied. */
    fun probedCells(): List<Triple<Int, Int, Double>> = buildList {
        for (row in 0 until ROWS) for (column in 0 until COLUMNS) {
            val value = DETECTION_PER_CENT[row][column]
            if (!value.isNaN()) {
                add(
                    Triple(
                        column, row,
                        value / 100.0 + StapleDropoutLiterature.DETECTION_TO_INCORPORATION_OFFSET
                    )
                )
            }
        }
    }

    /** How many staple domains from the nearer **along-helix** edge a column sits, `0` at it. */
    fun alongDepthIndex(column: Int): Int = min(column, COLUMNS - 1 - column)

    /** How many helix pairs from the nearer **across-helix** edge a row sits, `0` at it. */
    fun acrossDepthIndex(row: Int): Int = min(row, ROWS - 1 - row)

    /** Whether a cell is on the perimeter of the map. */
    fun onPerimeter(column: Int, row: Int): Boolean =
        alongDepthIndex(column) == 0 || acrossDepthIndex(row) == 0
}

// --------------------------------------------------------------------------------- the random stream

/**
 * A SplitMix64 stream, so that a Monte Carlo result file is **bit-reproducible from its seed**.
 *
 * Written out rather than taken from `kotlin.random` because a result file's reproducibility must
 * not depend on a standard-library implementation detail, and because a generator whose whole
 * state is one `Long` is testable in three lines. `CLAUDE.md`'s discipline applies: the *seed* and
 * the *sample count* are emitted with the result, and nothing that counts steps is.
 */
class DropoutRandom(seed: Long) {

    private var state: Long = seed

    /** The next 64 bits of the stream. */
    fun nextLong(): Long {
        state += GAMMA
        var z = state
        z = (z xor (z ushr 30)) * MIX_A
        z = (z xor (z ushr 27)) * MIX_B
        return z xor (z ushr 31)
    }

    /** The next uniform deviate in `[0, 1)`, from the top 53 bits. */
    fun nextDouble(): Double = (nextLong() ushr 11).toDouble() * SCALE

    private companion object {
        const val GAMMA: Long = -0x61c8864680b583ebL
        const val MIX_A: Long = -0x40a7b892e31b1a47L
        const val MIX_B: Long = -0x6b2fb644ecceee15L
        const val SCALE: Double = 1.0 / 9007199254740992.0
    }
}

/** One independent Bernoulli draw per station: `true` where the staple formed. */
fun bernoulliPresence(probabilities: List<Double>, random: DropoutRandom): List<Boolean> {
    require(probabilities.isNotEmpty()) { "probabilities must not be empty" }
    require(probabilities.all { it >= 0.0 && it <= 1.0 }) {
        "every probability must lie in [0, 1], were: $probabilities"
    }
    return probabilities.map { random.nextDouble() < it }
}

// --------------------------------------------------------------------------------- the field

/** How a measured edge/centre incorporation map is transferred onto a tile. */
enum class IncorporationConvention(val label: String) {

    /** Position-independent — `CH-0084`'s own reading, and the baseline the rest is graded against. */
    UNIFORM("position-independent, the measured mean everywhere"),

    /** Two-valued: the measured edge value inside a band, the measured centre value outside it. */
    FLAT_BAND("a boundary band of the measured edge value, the centre value inside"),

    /** Smooth: the centre value relaxed toward the edge value over a decay length. */
    EXPONENTIAL("an exponential boundary layer between the two measured values"),

    /**
     * The mechanism read literally: one lattice cell, and the incorporation falls by **half** the
     * measured range for each lattice direction in which the staple has no neighbour.
     *
     * It carries **no** free parameter, so its predicted mean is a **check** on the measurement
     * rather than a fit to it — and that is the point of carrying it.
     */
    LATTICE_RING("one lattice cell, graded by the count of missing neighbour directions"),

    /**
     * **The measured map itself**, transferred by depth from the two kinds of edge in nm.
     *
     * No fit and no free parameter: a station's incorporation is the mean of the measured cells
     * at the same along-helix and across-helix depth class. It is the least pessimistic of the
     * position-dependent readings, because it uses the perimeter's measured **mean** rather than
     * its single worst cell — which is what makes a negative verdict on it the strong one.
     */
    MEASURED_DEPTH("the measured map, by along-helix and across-helix depth in nm")
}

/**
 * The measured incorporation as a function of the two depth classes, `[alongClass][acrossClass]`,
 * with the cell pitches the classes are measured in.
 */
class MeasuredDepthTable internal constructor(

    /** The along-helix cell pitch in nm — one 16 bp staple domain. */
    val alongPitch: Double,

    /** The across-helix cell pitch in nm — two interhelical distances, one map row. */
    val acrossPitch: Double,

    private val table: Array<DoubleArray>,

    private val alongMarginal: DoubleArray
) {

    /** The number of along-helix depth classes the map resolves. */
    val alongClasses: Int get() = table.size

    /** The number of across-helix depth classes the map resolves. */
    val acrossClasses: Int get() = table[0].size

    /** The measured incorporation at the depth class nearest ([along], [across]) nm. */
    fun at(along: Double, across: Double): Double {
        val j = kotlin.math.round(along / alongPitch - 0.5).toInt().coerceIn(0, alongClasses - 1)
        val k = kotlin.math.round(across / acrossPitch - 0.5).toInt().coerceIn(0, acrossClasses - 1)
        val value = table[j][k]
        return if (value.isNaN()) alongMarginal[j] else value
    }

    /** The mean over the classes weighted by how many measured cells each holds. */
    val cellWeightedMean: Double by lazy {
        StrausIncorporationMap.probedCells().map { it.third }.average()
    }
}

/**
 * The depth table of [StrausIncorporationMap], at the pitches the map's own lattice has.
 *
 * A cell class with no probed cell falls back to the along-helix marginal, which cannot happen on
 * the published map — every one of the 8 × 6 classes holds two to four cells — and is carried so
 * that the construction is total rather than lucky.
 */
fun measuredDepthTable(
    alongPitch: Double = 16.0 * 0.34,
    acrossPitch: Double = 2.0 * 2.69
): MeasuredDepthTable {
    require(alongPitch > 0.0) { "alongPitch must be positive, was: $alongPitch" }
    require(acrossPitch > 0.0) { "acrossPitch must be positive, was: $acrossPitch" }
    val alongClasses = StrausIncorporationMap.COLUMNS / 2
    val acrossClasses = StrausIncorporationMap.ROWS / 2
    val sums = Array(alongClasses) { DoubleArray(acrossClasses) }
    val counts = Array(alongClasses) { IntArray(acrossClasses) }
    val marginalSum = DoubleArray(alongClasses)
    val marginalCount = IntArray(alongClasses)
    StrausIncorporationMap.probedCells().forEach { (column, row, value) ->
        val j = StrausIncorporationMap.alongDepthIndex(column)
        val k = StrausIncorporationMap.acrossDepthIndex(row)
        sums[j][k] += value
        counts[j][k]++
        marginalSum[j] += value
        marginalCount[j]++
    }
    return MeasuredDepthTable(
        alongPitch, acrossPitch,
        Array(alongClasses) { j ->
            DoubleArray(acrossClasses) { k ->
                if (counts[j][k] == 0) Double.NaN else sums[j][k] / counts[j][k]
            }
        },
        DoubleArray(alongClasses) { marginalSum[it] / marginalCount[it] }
    )
}

/**
 * The probability that a station's staple is incorporated, as a function of position on a
 * [edgeX] × [edgeY] tile centred on the origin.
 *
 * Construct through [uniformIncorporation], [flatBandIncorporation], [exponentialIncorporation]
 * or [latticeRingIncorporation]; the parameters not used by a convention are carried as zero so
 * that the whole field is one serialisable record.
 */
class IncorporationField internal constructor(

    /** Which transfer this field is. */
    val convention: IncorporationConvention,

    /** The tile's extent along the helices, nm. */
    val edgeX: Double,

    /** The tile's extent across the helices, nm. */
    val edgeY: Double,

    /** The measured incorporation at the rim, dimensionless. */
    val edgeValue: Double,

    /** The measured incorporation in the interior, dimensionless. */
    val centreValue: Double,

    /** The fitted band width in nm — [IncorporationConvention.FLAT_BAND] only. */
    val bandWidth: Double,

    /** The fitted decay length in nm — [IncorporationConvention.EXPONENTIAL] only. */
    val decayLength: Double,

    /** The across-helix lattice cell in nm — [IncorporationConvention.LATTICE_RING] only. */
    val acrossCell: Double,

    /** The along-helix lattice cell in nm — [IncorporationConvention.LATTICE_RING] only. */
    val alongCell: Double,

    /** The measured table — [IncorporationConvention.MEASURED_DEPTH] only. */
    internal val depthTable: MeasuredDepthTable? = null
) {

    /**
     * The incorporation probability at ([x], [y]), in nm from the tile centre.
     *
     * A [IncorporationConvention.UNIFORM] field carries no geometry at all — it is the
     * position-independent reading — so it is answered before the tile bounds are consulted.
     */
    fun at(x: Double, y: Double): Double {
        if (convention == IncorporationConvention.UNIFORM) return centreValue
        val alongMargin = edgeX / 2.0 - abs(x)
        val acrossMargin = edgeY / 2.0 - abs(y)
        require(alongMargin >= -PLAN_TOLERANCE && acrossMargin >= -PLAN_TOLERANCE) {
            "the station ($x, $y) lies outside the ${edgeX} x ${edgeY} nm tile"
        }
        val along = max(0.0, alongMargin)
        val across = max(0.0, acrossMargin)
        val distance = min(along, across)
        return when (convention) {
            IncorporationConvention.UNIFORM -> centreValue
            IncorporationConvention.FLAT_BAND ->
                if (distance <= bandWidth) edgeValue else centreValue

            IncorporationConvention.EXPONENTIAL ->
                centreValue - (centreValue - edgeValue) * exp(-distance / decayLength)

            IncorporationConvention.LATTICE_RING -> {
                val missing = (if (along <= alongCell) 1 else 0) +
                        (if (across <= acrossCell) 1 else 0)
                centreValue - missing * (centreValue - edgeValue) / 2.0
            }

            IncorporationConvention.MEASURED_DEPTH -> checkNotNull(depthTable) {
                "a MEASURED_DEPTH field carries no table"
            }.at(along, across)
        }
    }

    private companion object {
        /** A station laid exactly on the rim must not be refused for a rounding of its own. */
        const val PLAN_TOLERANCE: Double = 1e-9
    }
}

private fun checkTile(edgeX: Double, edgeY: Double) {
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(edgeY > 0.0) { "edgeY must be positive, was: $edgeY" }
}

private fun checkValues(edgeValue: Double, centreValue: Double) {
    require(edgeValue in 0.0..1.0) { "edgeValue must lie in [0, 1], was: $edgeValue" }
    require(centreValue in 0.0..1.0) { "centreValue must lie in [0, 1], was: $centreValue" }
}

/** A position-independent field at [value] — `CH-0084`'s own reading of the measurement. */
fun uniformIncorporation(value: Double): IncorporationField {
    require(value in 0.0..1.0) { "value must lie in [0, 1], was: $value" }
    return IncorporationField(
        IncorporationConvention.UNIFORM, 0.0, 0.0, value, value, 0.0, 0.0, 0.0, 0.0
    )
}

/** [IncorporationConvention.FLAT_BAND] with a band of [bandWidth] nm. */
fun flatBandIncorporation(
    edgeX: Double,
    edgeY: Double,
    bandWidth: Double,
    edgeValue: Double = StapleDropoutLiterature.INCORPORATION_EDGE,
    centreValue: Double = StapleDropoutLiterature.INCORPORATION_CENTRE
): IncorporationField {
    checkTile(edgeX, edgeY)
    checkValues(edgeValue, centreValue)
    require(bandWidth >= 0.0) { "bandWidth must not be negative, was: $bandWidth" }
    return IncorporationField(
        IncorporationConvention.FLAT_BAND, edgeX, edgeY, edgeValue, centreValue,
        bandWidth, 0.0, 0.0, 0.0
    )
}

/** [IncorporationConvention.EXPONENTIAL] with a decay length of [decayLength] nm. */
fun exponentialIncorporation(
    edgeX: Double,
    edgeY: Double,
    decayLength: Double,
    edgeValue: Double = StapleDropoutLiterature.INCORPORATION_EDGE,
    centreValue: Double = StapleDropoutLiterature.INCORPORATION_CENTRE
): IncorporationField {
    checkTile(edgeX, edgeY)
    checkValues(edgeValue, centreValue)
    require(decayLength > 0.0) { "decayLength must be positive, was: $decayLength" }
    return IncorporationField(
        IncorporationConvention.EXPONENTIAL, edgeX, edgeY, edgeValue, centreValue,
        0.0, decayLength, 0.0, 0.0
    )
}

/** [IncorporationConvention.LATTICE_RING] on cells of [acrossCell] and [alongCell] nm. */
fun latticeRingIncorporation(
    edgeX: Double,
    edgeY: Double,
    acrossCell: Double,
    alongCell: Double,
    edgeValue: Double = StapleDropoutLiterature.INCORPORATION_EDGE,
    centreValue: Double = StapleDropoutLiterature.INCORPORATION_CENTRE
): IncorporationField {
    checkTile(edgeX, edgeY)
    checkValues(edgeValue, centreValue)
    require(acrossCell > 0.0) { "acrossCell must be positive, was: $acrossCell" }
    require(alongCell > 0.0) { "alongCell must be positive, was: $alongCell" }
    return IncorporationField(
        IncorporationConvention.LATTICE_RING, edgeX, edgeY, edgeValue, centreValue,
        0.0, 0.0, acrossCell, alongCell
    )
}

/**
 * [IncorporationConvention.MEASURED_DEPTH] on a [edgeX] × [edgeY] tile — the measured map itself,
 * looked up at the station's own two edge depths.
 *
 * The measured extremes are carried in [IncorporationField.edgeValue] and
 * [IncorporationField.centreValue] for reporting only; nothing here is fitted to them.
 */
fun measuredDepthIncorporation(
    edgeX: Double,
    edgeY: Double,
    table: MeasuredDepthTable = measuredDepthTable()
): IncorporationField {
    checkTile(edgeX, edgeY)
    return IncorporationField(
        IncorporationConvention.MEASURED_DEPTH, edgeX, edgeY,
        StapleDropoutLiterature.INCORPORATION_EDGE,
        StapleDropoutLiterature.INCORPORATION_CENTRE,
        0.0, 0.0, table.acrossPitch, table.alongPitch, table
    )
}

// --------------------------------------------------------------------------------- the calibration

/**
 * The fraction of a [lengthX] × [lengthY] rectangle within [bandWidth] of its own boundary —
 * `1 − (W − 2b)(L − 2b)/(WL)`, exactly, and clamped at one where the band swallows the rectangle.
 */
fun flatBandAreaFraction(lengthX: Double, lengthY: Double, bandWidth: Double): Double {
    checkTile(lengthX, lengthY)
    require(bandWidth >= 0.0) { "bandWidth must not be negative, was: $bandWidth" }
    val innerX = max(0.0, lengthX - 2.0 * bandWidth)
    val innerY = max(0.0, lengthY - 2.0 * bandWidth)
    return 1.0 - innerX * innerY / (lengthX * lengthY)
}

/** The **area** mean of a [IncorporationConvention.FLAT_BAND] field over the same rectangle. */
fun flatBandAreaMean(
    lengthX: Double,
    lengthY: Double,
    bandWidth: Double,
    edgeValue: Double,
    centreValue: Double
): Double {
    checkValues(edgeValue, centreValue)
    return centreValue -
            (centreValue - edgeValue) * flatBandAreaFraction(lengthX, lengthY, bandWidth)
}

/**
 * The **area** mean of an [IncorporationConvention.EXPONENTIAL] field over a
 * [lengthX] × [lengthY] rectangle, in closed form.
 *
 * The level set `{d ≥ t}` of the distance to the boundary is the rectangle
 * `(W − 2t) × (L − 2t)`, so `∫ f(d) dA = ∫₀^T f(t)·(2(W + L) − 8t) dt` with `T = min(W, L)/2` —
 * and both moments of `e^{−t/λ}` against that linear weight are elementary. The normalisation is
 * its own check: at `f ≡ 1` the integral is `2(W + L)T − 4T² = WL`, exactly.
 */
fun exponentialAreaMean(
    lengthX: Double,
    lengthY: Double,
    decayLength: Double,
    edgeValue: Double,
    centreValue: Double
): Double {
    checkTile(lengthX, lengthY)
    checkValues(edgeValue, centreValue)
    require(decayLength > 0.0) { "decayLength must be positive, was: $decayLength" }
    val half = min(lengthX, lengthY) / 2.0
    val ratio = half / decayLength
    val decayed = exp(-ratio)
    val first = decayLength * (1.0 - decayed)
    val second = decayLength * decayLength *
            (1.0 - if (decayed == 0.0) 0.0 else decayed * (1.0 + ratio))
    val integral = 2.0 * (lengthX + lengthY) * first - 8.0 * second
    return centreValue - (centreValue - edgeValue) * integral / (lengthX * lengthY)
}

private fun bisectForMean(
    low: Double,
    high: Double,
    targetMean: Double,
    tolerance: Double,
    meanAt: (Double) -> Double
): Double {
    var lower = low
    var upper = high
    // The mean is monotone DECREASING in the parameter: a wider band, a longer decay.
    require(meanAt(lower) >= targetMean && meanAt(upper) <= targetMean) {
        "the target mean $targetMean is not reachable between $low and $high"
    }
    repeat(400) {
        val middle = 0.5 * (lower + upper)
        if (meanAt(middle) > targetMean) lower = middle else upper = middle
        if (upper - lower <= tolerance) return 0.5 * (lower + upper)
    }
    return 0.5 * (lower + upper)
}

/**
 * The band width in nm at which a [IncorporationConvention.FLAT_BAND] field's **area** mean over
 * a [lengthX] × [lengthY] rectangle equals [targetMean].
 *
 * Bisected on the bracket width rather than on a residual — `CLAUDE.md`'s rule — over the only
 * interval on which the mean is defined, `[0, min(W, L)/2]`, where it runs monotonically from
 * [centreValue] down to [edgeValue].
 */
fun bandWidthForAreaMean(
    lengthX: Double,
    lengthY: Double,
    targetMean: Double,
    edgeValue: Double,
    centreValue: Double,
    tolerance: Double = 1e-12
): Double {
    checkTile(lengthX, lengthY)
    checkValues(edgeValue, centreValue)
    require(tolerance > 0.0) { "tolerance must be positive, was: $tolerance" }
    require(targetMean > edgeValue && targetMean < centreValue) {
        "the target mean $targetMean must lie strictly between $edgeValue and $centreValue"
    }
    return bisectForMean(0.0, min(lengthX, lengthY) / 2.0, targetMean, tolerance) {
        flatBandAreaMean(lengthX, lengthY, it, edgeValue, centreValue)
    }
}

/**
 * The decay length in nm at which an [IncorporationConvention.EXPONENTIAL] field's **area** mean
 * over a [lengthX] × [lengthY] rectangle equals [targetMean].
 *
 * The upper bracket is grown geometrically rather than assumed, because the mean approaches
 * [edgeValue] only asymptotically and a fixed ceiling would silently refuse a target close to it.
 */
fun decayLengthForAreaMean(
    lengthX: Double,
    lengthY: Double,
    targetMean: Double,
    edgeValue: Double,
    centreValue: Double,
    tolerance: Double = 1e-12
): Double {
    checkTile(lengthX, lengthY)
    checkValues(edgeValue, centreValue)
    require(tolerance > 0.0) { "tolerance must be positive, was: $tolerance" }
    require(targetMean > edgeValue && targetMean < centreValue) {
        "the target mean $targetMean must lie strictly between $edgeValue and $centreValue"
    }
    fun meanAt(decay: Double) =
        exponentialAreaMean(lengthX, lengthY, decay, edgeValue, centreValue)

    var high = min(lengthX, lengthY)
    repeat(80) {
        if (meanAt(high) <= targetMean) return bisectForMean(1e-9, high, targetMean, tolerance) {
            meanAt(it)
        }
        high *= 2.0
    }
    error("no decay length below $high nm reaches a mean of $targetMean")
}

// --------------------------------------------------------------------------------- the arithmetic

/**
 * The relative standard deviation of a **two-valued** per-path stiffness population at a dropout
 * probability [dropout] — `√(f/(1 − f))`, exactly.
 *
 * A path is present with stiffness `k` or absent with stiffness `0`, so the mean is `k(1 − f)`
 * and the variance `k² f(1 − f)`. **43.6 %** at Strauss's mean, **104.1 %** at his edge value and
 * **22.9 %** at his centre value — which is `CH-0084`'s table, re-derived rather than cited.
 */
fun bernoulliRelativeScatter(dropout: Double): Double {
    require(dropout >= 0.0 && dropout < 1.0) {
        "dropout must lie in [0, 1) — at 1 the whole coupling is absent, was: $dropout"
    }
    return sqrt(dropout / (1.0 - dropout))
}

private fun checkPaired(stiffnesses: List<Double>, probabilities: List<Double>) {
    require(stiffnesses.isNotEmpty()) { "stiffnesses must not be empty" }
    require(probabilities.size == stiffnesses.size) {
        "expected one probability per path, was: ${probabilities.size} for ${stiffnesses.size}"
    }
    require(stiffnesses.all { it > 0.0 && it.isFinite() }) {
        "every nominal stiffness must be positive and finite"
    }
    require(probabilities.all { it >= 0.0 && it <= 1.0 }) {
        "every probability must lie in [0, 1]"
    }
}

/**
 * `E[K] = Σ kᵢ pᵢ` in pN/nm — **the cheap bound**, and the whole of the mandate half of `T-148`.
 *
 * `C-0017`'s mandate is an equality on a **sum** (`C-0058`, `C-0060`), so a dropout's effect on it
 * needs no spatial model beyond the `pᵢ` and no solve at all.
 */
fun expectedTotalStiffness(stiffnesses: List<Double>, probabilities: List<Double>): Double {
    checkPaired(stiffnesses, probabilities)
    return stiffnesses.indices.sumOf { stiffnesses[it] * probabilities[it] }
}

/** `√(Σ kᵢ² pᵢ(1 − pᵢ))` in pN/nm — the realised total's own standard deviation. */
fun totalStiffnessDeviation(stiffnesses: List<Double>, probabilities: List<Double>): Double {
    checkPaired(stiffnesses, probabilities)
    return sqrt(
        stiffnesses.indices.sumOf {
            stiffnesses[it] * stiffnesses[it] * probabilities[it] * (1.0 - probabilities[it])
        }
    )
}

/**
 * [stiffnesses] divided path by path by [probabilities], so that the **expected** realised total
 * is the nominal total exactly.
 *
 * The design answer to a known dropout field: over-stiffen each path by its own inverse
 * incorporation. It costs a per-path force, and it stiffens hardest exactly where the measurement
 * says the paths are least likely to form — which is why the price is reported beside it.
 */
fun compensatedStiffnesses(
    stiffnesses: List<Double>,
    probabilities: List<Double>
): List<Double> {
    checkPaired(stiffnesses, probabilities)
    require(probabilities.all { it > 0.0 }) {
        "a path with zero incorporation cannot be compensated for"
    }
    return stiffnesses.indices.map { stiffnesses[it] / probabilities[it] }
}

/**
 * [stiffnesses] with the absent paths set to zero and the surviving ones rescaled so that they sum
 * to [total] — the **diagnostic** convention, which separates what the dropout does to the
 * *distribution* from what it does to the *level*.
 *
 * It is not a design: a builder cannot know which staples dropped. `C-0060` reports its tolerance
 * both ways for the same reason, and both are reported here.
 */
fun renormalisedSurvivors(
    stiffnesses: List<Double>,
    present: List<Boolean>,
    total: Double
): List<Double> {
    require(stiffnesses.isNotEmpty()) { "stiffnesses must not be empty" }
    require(present.size == stiffnesses.size) {
        "expected one presence flag per path, was: ${present.size} for ${stiffnesses.size}"
    }
    require(total > 0.0) { "total must be positive, was: $total" }
    val surviving = stiffnesses.indices.filter { present[it] }
    require(surviving.isNotEmpty()) { "no path survives, so no rescaling reaches the total" }
    val sum = surviving.sumOf { stiffnesses[it] }
    require(sum > 0.0) { "the surviving stiffnesses must sum to a positive value" }
    return stiffnesses.indices.map { if (present[it]) stiffnesses[it] * total / sum else 0.0 }
}

// --------------------------------------------------------------------------------- the statistics

/**
 * The nearest-rank order statistic of [sample] at [fraction] — the `⌈q n⌉`-th smallest value,
 * clamped to the sample.
 *
 * A percentile of a Monte Carlo is an **order statistic** and not an interpolation, so it is a
 * value the sample actually took and no averaging can move it off the sample. The input is copied
 * before sorting, so two readings of one sample agree whatever order they are taken in.
 */
fun orderStatistic(sample: DoubleArray, fraction: Double): Double {
    require(sample.isNotEmpty()) { "sample must not be empty" }
    require(fraction in 0.0..1.0) { "fraction must lie in [0, 1], was: $fraction" }
    val sorted = sample.copyOf()
    sorted.sort()
    val rank = kotlin.math.ceil(fraction * sorted.size).toInt().coerceIn(1, sorted.size)
    return sorted[rank - 1]
}

/**
 * The standard error `√(p(1 − p)/n)` of an exceedance probability estimated from [count] draws.
 *
 * Gate 4 here is **statistical power**, and a probability quoted without it is not a result.
 */
fun binomialStandardError(probability: Double, count: Int): Double {
    require(probability in 0.0..1.0) { "probability must lie in [0, 1], was: $probability" }
    require(count > 0) { "count must be positive, was: $count" }
    return sqrt(probability * (1.0 - probability) / count)
}

/**
 * The one-sided confidence bound on a **saturated** proportion — `T-210`, `C-0129`.
 *
 * At `p̂ = 1` (or `p̂ = 0`) the symmetric binomial error [binomialStandardError] is **identically
 * zero for every sample count**: it is `√(p̂(1 − p̂)/n)`, and the numerator is zero whatever `n`
 * is. It therefore cannot distinguish 1 250 draws from 20 000, and quoting it as *"the resolution
 * the verdict is quoted to"* — which `gpd/results/T-148-staple-dropout.json` did — states the
 * opposite of the truth. **A saturated statistic is the resolution of nothing.**
 *
 * The instrument that does carry the sample size is one-sided. Observing [count] successes out of
 * [count] is exactly as likely as `p^count`, so the 95 % lower limit is the `p` at which that
 * equals `1 − confidence`:
 *
 *     p_lower = (1 − confidence)^(1/n)
 *
 * the exact Clopper-Pearson limit at `x = n`. The **rule of three**, `p > 1 − 3/n`, is its
 * large-`n` form, because `ln(1/20) = −2.996 ≈ −3`; at `n = 10 000` the two agree to `5e−7`.
 * For `p̂ = 0` the same argument gives the upper limit `1 − (1 − confidence)^(1/n)`.
 *
 * @param probability the observed proportion. **Must be saturated** — at `0 < p̂ < 1` the
 *          symmetric error is the right instrument and a one-sided bound is the wrong question,
 *          so this refuses rather than answering it silently.
 */
fun saturatedProportionBound(
    probability: Double,
    count: Int,
    confidence: Double = 0.95
): Double {
    require(probability == 0.0 || probability == 1.0) {
        "probability must be saturated (exactly 0.0 or 1.0) for a one-sided bound, " +
                "was: $probability"
    }
    require(count > 0) { "count must be positive, was: $count" }
    require(confidence > 0.0 && confidence < 1.0) {
        "confidence must lie in (0, 1), was: $confidence"
    }
    val limit = (1.0 - confidence).pow(1.0 / count)
    return if (probability == 1.0) limit else 1.0 - limit
}
