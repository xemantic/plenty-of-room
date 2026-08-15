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
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * `T-134`, leaf `A8.2` — **a tolerance model for the two knife edges the output branch carries.**
 *
 * `C-0069` reports a **0.0256 nm** plan margin on the arm it recommends and `C-0066` reports a
 * **0.0256 nm** clearance for a tie duplex standing between two consecutive arms. This file's
 * first statement is that **those are one number**:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`M = p − d − L`
 *
 * the root pitch minus one interhelical distance minus the element's own plan length.
 * `C-0069` groups it as `(p − d) − L` — a budget minus an arm — and `C-0066` as `(p − L) − d` —
 * a gap minus a duplex. Neither claim noticed that the grouping is all that separates them.
 *
 * Everything else here is propagation onto that one quantity, and it is deliberately **linear
 * and exact**: the margin is a difference of two integer base-pair counts times a rise, minus a
 * measured lattice constant, so its sensitivities are integers and need no solve.
 *
 * ### The discipline this file exists to impose
 *
 * `CLAUDE.md`: *"Which way a tolerance is correlated matters more than how big it is."* The rise
 * enters the margin **twice** — once through the host duplex's 32 bp crossover pitch and once
 * through the element's own base-pair count — and the coefficient of a given amplitude therefore
 * depends entirely on the correlation between those two. [RiseCorrelation] carries the structure
 * and [riseCoefficient] the arithmetic; the spread between the best and the worst structure at
 * equal amplitude is `(N_host + N_element)/(N_host − N_element)`, which is **7** here.
 *
 * ### Units
 *
 * Lengths **nm**, forces **pN**, stiffness **pN/nm**, rotational stiffness **pN·nm/rad**,
 * energies **pN·nm**. `k_BT = 4.141947 pN·nm` at 300 K.
 */

// ---------------------------------------------------------------- lattice constants under dispute

/**
 * The **square-lattice** interhelical distance, 2.73 nm (SAXS, Fischer et al., *Nano Lett.*
 * **16**:4282, 2016), against the 2.69 nm single-layer value this programme's geometry is written
 * on ([Gen1Tile.INTERHELICAL_SHEET]).
 *
 * **CITED, MEASURED.** It is carried here as a named constant rather than as a sensitivity because
 * `T-134`'s central finding is that the **difference between these two measurements of the same
 * material** — 0.04 nm — already exceeds the margin both `C-0066` and `C-0069` quote.
 */
const val SQUARE_LATTICE_INTERHELICAL: Double = 2.73

/**
 * The number of base pairs between two crossovers **on one interface** of a single-layer sheet,
 * derived from the twist rather than asserted.
 *
 * Crossovers recur every 1.5 turns along a helix but **alternate between its two neighbours**
 * (`CLAUDE.md`), so a given adjacent pair is linked every **three** turns. The count is an
 * integer — a design cannot place half a base pair — which is why the twist enters the plan margin
 * only through a rounding, and does not move it at all between the two readings this project
 * carries (10.5 and 10.67 bp/turn both round to **32**).
 *
 * @throws IllegalArgumentException if [basePairsPerTurn] or [turnsPerInterface] is not positive.
 */
fun crossoverSpacingBasePairs(
    basePairsPerTurn: Double,
    turnsPerInterface: Double = 3.0
): Int {
    require(basePairsPerTurn > 0.0) {
        "basePairsPerTurn must be positive, was: $basePairsPerTurn"
    }
    require(turnsPerInterface > 0.0) {
        "turnsPerInterface must be positive, was: $turnsPerInterface"
    }
    return (turnsPerInterface * basePairsPerTurn).roundToInt()
}

// ---------------------------------------------------------------- the knife edge itself

/**
 * **The plan margin, and the whole of both knife edges**: `pitch − width − length`.
 *
 * Positive means a rooted element of that length fits between consecutive same-sense roots under
 * `C-0053`'s footprint convention, in which the next collinear element may start one full duplex
 * past the previous one's tip. It is `C-0069`'s *"margin to the plan budget"* and `C-0066`'s
 * *"the root pitch minus the arm, against a tie's width"* written once.
 *
 * @throws IllegalArgumentException if any argument is negative.
 */
fun planMargin(pitch: Double, width: Double, length: Double): Double {
    require(pitch >= 0.0) { "pitch must not be negative, was: $pitch" }
    require(width >= 0.0) { "width must not be negative, was: $width" }
    require(length >= 0.0) { "length must not be negative, was: $length" }
    return pitch - width - length
}

/**
 * The whole number of base pairs nearest [length] — what a **build** can specify, as against what
 * an equation returns.
 *
 * `C-0039`'s elastica answers 8.16439 nm; no design can order that. It orders 24 base pairs, and
 * the difference matters twice: the built element is 0.0044 nm shorter (which *opens* the margin),
 * and its length now **tracks the rise**, where a solved length in nm does not.
 *
 * @throws IllegalArgumentException if [length] is negative or [rise] is not positive.
 */
fun basePairsNearest(length: Double, rise: Double = Gen1Tile.RISE_PER_BASE_PAIR): Int {
    require(length >= 0.0) { "length must not be negative, was: $length" }
    require(rise > 0.0) { "rise must be positive, was: $rise" }
    return (length / rise).roundToInt()
}

/**
 * A length a build can specify: [basePairs] times the [rise].
 *
 * @throws IllegalArgumentException if [basePairs] is negative or [rise] is not positive.
 */
fun builtLength(basePairs: Int, rise: Double = Gen1Tile.RISE_PER_BASE_PAIR): Double {
    require(basePairs >= 0) { "basePairs must not be negative, was: $basePairs" }
    require(rise > 0.0) { "rise must be positive, was: $rise" }
    return basePairs * rise
}

// ---------------------------------------------------------------- correlation structure

/**
 * How a relative rise perturbation is shared between the **host** duplex, whose 32 bp crossover
 * pitch sets the root spacing, and the **element**, whose own base-pair count sets its length.
 *
 * They are different molecules, so the correlation is a physical question and not a convention —
 * and it is worth a factor of **7** in the coefficient at this design.
 */
enum class RiseCorrelation {

    /** Both duplexes see the same relative perturbation. The coefficient is the **difference** of
     * the two counts, which is why a 32 bp pitch carrying a 24 bp arm is only 8 bp sensitive. */
    COMMON,

    /** Uncorrelated; quoted as an RMS, `√(N_host² + N_element²)`. */
    INDEPENDENT,

    /** Equal and opposite — the host short where the element is long. The coefficient is the
     * **sum** of the counts, and it is the worst case at any amplitude. */
    OPPOSED,

    /** The element's length is fixed in nm and does not track the rise at all. This is the
     * **solved** reading — `C-0039`'s 8.16439 nm treated as a length rather than as a count — and
     * it is what `C-0069` and `C-0066` implicitly assume. */
    FIXED_ELEMENT
}

/**
 * The **nm the margin moves per unit relative rise perturbation**, for a host of
 * [hostBasePairs] and an element of [elementBasePairs], under [correlation].
 *
 * Signed as a magnitude: the margin falls by this much per unit relative amplitude in the
 * unfavourable direction of each structure.
 *
 * @throws IllegalArgumentException if either count is not positive, or [rise] is not positive.
 */
fun riseCoefficient(
    hostBasePairs: Int,
    elementBasePairs: Int,
    rise: Double = Gen1Tile.RISE_PER_BASE_PAIR,
    correlation: RiseCorrelation = RiseCorrelation.COMMON
): Double {
    require(hostBasePairs > 0) { "hostBasePairs must be positive, was: $hostBasePairs" }
    require(elementBasePairs >= 0) {
        "elementBasePairs must not be negative, was: $elementBasePairs"
    }
    require(rise > 0.0) { "rise must be positive, was: $rise" }
    val host = hostBasePairs.toDouble()
    val element = elementBasePairs.toDouble()
    return rise * when (correlation) {
        RiseCorrelation.COMMON -> abs(host - element)
        RiseCorrelation.INDEPENDENT -> sqrt(host * host + element * element)
        RiseCorrelation.OPPOSED -> host + element
        RiseCorrelation.FIXED_ELEMENT -> host
    }
}

/**
 * The ratio `δr_element/δr_host` at which the margin is **exactly stationary** — the null
 * direction of the rise sensitivity, `N_host/N_element`.
 *
 * It is reported because it is exact and because **no build can reach it**: an arm and its host are
 * the same molecule under the same buffer, so a shared strain arrives at ratio 1, not at 4/3.
 * The analogue of `C-0026`'s exact zero, and the honest reading is the same — a null that a
 * symmetry supplies is free, and a null that requires a differential strain is not.
 *
 * @throws IllegalArgumentException if either count is not positive.
 */
fun nullRiseRatio(hostBasePairs: Int, elementBasePairs: Int): Double {
    require(hostBasePairs > 0) { "hostBasePairs must be positive, was: $hostBasePairs" }
    require(elementBasePairs > 0) {
        "elementBasePairs must be positive, was: $elementBasePairs"
    }
    return hostBasePairs.toDouble() / elementBasePairs.toDouble()
}

/**
 * The relative amplitude at which a channel of sensitivity [coefficient] nm per unit relative
 * amplitude consumes the whole [margin] — *"the value the unknown would need for the answer to
 * change"*, which is the form this project prefers when the measurement does not exist.
 *
 * @throws IllegalArgumentException if [coefficient] is not positive.
 */
fun relativeThreshold(margin: Double, coefficient: Double): Double {
    require(coefficient > 0.0) { "coefficient must be positive, was: $coefficient" }
    return margin / coefficient
}

/**
 * The **per-base-pair-step** rise standard deviation at which the margin sits at exactly one
 * standard deviation, under an independent-step model.
 *
 * A host pitch is a sum of [hostBasePairs] steps and an element a sum of [elementBasePairs] steps
 * on a different molecule, so if the steps are independent the margin's variance is
 * `(N_host + N_element) σ_step²` and the threshold is `M/√(N_host + N_element)`. The counts enter
 * **linearly** here and as a difference in [riseCoefficient] — the same two numbers answering two
 * different questions, and the pair of them is the whole correlation story.
 *
 * @throws IllegalArgumentException if the counts are not positive.
 */
fun perStepRiseSigmaThreshold(
    margin: Double,
    hostBasePairs: Int,
    elementBasePairs: Int
): Double {
    require(hostBasePairs > 0) { "hostBasePairs must be positive, was: $hostBasePairs" }
    require(elementBasePairs > 0) {
        "elementBasePairs must be positive, was: $elementBasePairs"
    }
    return margin / sqrt((hostBasePairs + elementBasePairs).toDouble())
}

// ---------------------------------------------------------------- the thermal floors

/**
 * The RMS **axial** length fluctuation of a duplex segment of [length], `√(k_BT ℓ/S)`.
 *
 * Equipartition on the segment's own axial spring `S/ℓ`, on the **measured** stretch modulus
 * (Wang et al. 1997, via `Gen1Tile`). It needs no fabrication-tolerance measurement at all, which
 * is why it is the floor this task leans on: whatever an assembly's static scatter turns out to
 * be, the object also *breathes* by this much.
 *
 * @throws IllegalArgumentException if [length] is negative or [stretchModulus] is not positive.
 */
fun axialFluctuation(
    length: Double,
    stretchModulus: Double = Gen1Tile.DUPLEX_STRETCH_MODULUS,
    kT: Double = thermalEnergy()
): Double {
    require(length >= 0.0) { "length must not be negative, was: $length" }
    require(stretchModulus > 0.0) { "stretchModulus must be positive, was: $stretchModulus" }
    require(kT > 0.0) { "kT must be positive, was: $kT" }
    if (stretchModulus.isInfinite()) return 0.0
    return sqrt(kT * length / stretchModulus)
}

/**
 * The RMS **transverse** excursion of the tip of an [arm] rigidly rotating on a hinge of
 * [hingeStiffness]: `arm √(k_BT/k_θ)`.
 *
 * @throws IllegalArgumentException if [arm] is negative or [hingeStiffness] is not positive.
 */
fun hingeTipFluctuation(
    arm: Double,
    hingeStiffness: Double,
    kT: Double = thermalEnergy()
): Double {
    require(arm >= 0.0) { "arm must not be negative, was: $arm" }
    require(hingeStiffness > 0.0) { "hingeStiffness must be positive, was: $hingeStiffness" }
    require(kT > 0.0) { "kT must be positive, was: $kT" }
    if (hingeStiffness.isInfinite()) return 0.0
    return arm * sqrt(kT / hingeStiffness)
}

/**
 * The RMS transverse excursion of the tip of a **cantilever** of length [arm] and bending rigidity
 * [bendingRigidity], `√(k_BT L³/3EI)` — the arm's **own** compliance, which survives a perfectly
 * rigid root.
 *
 * This is the floor of the transverse channel and the reason no joint stiffening can rescue the
 * margin: making the root rigid removes [hingeTipFluctuation] and leaves this.
 *
 * @throws IllegalArgumentException if [arm] is not positive or [bendingRigidity] is not positive.
 */
fun cantileverTipFluctuation(
    arm: Double,
    bendingRigidity: Double = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
    kT: Double = thermalEnergy()
): Double {
    require(arm > 0.0) { "arm must be positive, was: $arm" }
    require(bendingRigidity > 0.0) {
        "bendingRigidity must be positive, was: $bendingRigidity"
    }
    require(kT > 0.0) { "kT must be positive, was: $kT" }
    if (bendingRigidity.isInfinite()) return 0.0
    return sqrt(kT * arm * arm * arm / (3.0 * bendingRigidity))
}

/** The quadrature sum of independent RMS contributions. */
fun quadrature(vararg terms: Double): Double = sqrt(terms.sumOf { it * it })

/**
 * The **translational stiffness a margin demands** of a fluctuating channel if the channel's own
 * excursion is to sit inside it: `k ≥ k_BT/M²`.
 *
 * The inverse-square is what makes this brutal. Quoting it as a ratio against the stiffnesses the
 * catalogue actually supplies is the cheapest possible statement of the whole task.
 *
 * @throws IllegalArgumentException if [margin] is not positive.
 */
fun stiffnessForMargin(margin: Double, kT: Double = thermalEnergy()): Double {
    require(margin > 0.0) { "margin must be positive, was: $margin" }
    require(kT > 0.0) { "kT must be positive, was: $kT" }
    return kT / (margin * margin)
}

/**
 * The **rotational** stiffness a margin demands at a lever [arm]: `k_θ ≥ k_BT arm²/M²`.
 *
 * @throws IllegalArgumentException if [margin] or [arm] is not positive.
 */
fun rotationalStiffnessForMargin(
    margin: Double,
    arm: Double,
    kT: Double = thermalEnergy()
): Double {
    require(arm > 0.0) { "arm must be positive, was: $arm" }
    return stiffnessForMargin(margin, kT) * arm * arm
}

// ---------------------------------------------------------------- the design that has margin

/**
 * [rows] with [drop] roots removed — the cheapest way this lattice buys plan margin, and the only
 * one that does not touch a measured constant.
 *
 * `C-0063`'s bound 1 (`3a + 2(15 − a) = 34`) forces **four rows of three** at 34 arms, and a row of
 * three is the *only* configuration in which two same-sense arms are separated by exactly the bare
 * root pitch. So the entire 8.19 nm ceiling — and with it both knife edges — is bought by four
 * arms. Removing them is a **path count** decision, and the price is that each remaining path must
 * be stiffer, hence shorter, which moves the margin the *favourable* way as well.
 *
 * The rule is deterministic, which matters because a result file must diff clean: take from the
 * row that currently carries the most roots (lowest row index breaks the tie), and within it the
 * root nearest the row's own mean (lowest `x` breaks the tie) — the interior root, whose removal
 * is what dissolves a row of three.
 *
 * @throws IllegalArgumentException if [drop] is negative or exceeds the roots available.
 */
fun rowsWithoutInteriorRoots(rows: List<StationRow>, drop: Int): List<StationRow> {
    require(drop >= 0) { "drop must not be negative, was: $drop" }
    val total = rows.sumOf { it.roots.size }
    require(drop <= total - rows.size) {
        "cannot drop $drop of $total roots and leave every one of ${rows.size} rows occupied"
    }
    var current = rows.map { it.roots.toMutableList() }
    repeat(drop) {
        val target = current.indices
            .filter { current[it].size > 1 }
            .maxByOrNull { current[it].size * 1000 - it }
            ?: error("no row has a root to give up")
        val roots = current[target]
        val mean = roots.average()
        val victim = roots.withIndex().minByOrNull { (_, x) ->
            abs(x - mean) * 1000.0 + (x + 1000.0) * 1e-9
        }!!.index
        roots.removeAt(victim)
    }
    return rows.mapIndexed { index, row -> StationRow(row.row, row.y, current[index].sorted()) }
}

// ---------------------------------------------------------------- the stiffness channel (T-45)

/**
 * The relative per-path **stiffness** scatter a relative **rise** scatter produces, at a built
 * element whose length is a fixed base-pair count.
 *
 * A bending element's stiffness goes as `L^−3` and a built `L` is `n·r`, so the exponent is
 * exactly 3 — before the end-condition factor `c(ρ)` moves, which it does only weakly because
 * `ρ = k_θ L/EI` is itself small here. Reported as the **leading** term with its exponent named,
 * exactly as `C-0003`'s `k ∝ K^(1/(m+1))` is.
 *
 * @throws IllegalArgumentException if [relativeRiseScatter] is negative.
 */
fun stiffnessScatterFromRise(relativeRiseScatter: Double, exponent: Double = 3.0): Double {
    require(relativeRiseScatter >= 0.0) {
        "relativeRiseScatter must not be negative, was: $relativeRiseScatter"
    }
    return exponent * relativeRiseScatter
}

/**
 * The **Bernoulli dropout rate** whose population relative standard deviation is
 * [relativeScatter]: `f = σ²/(1 + σ²)`.
 *
 * `C-0026` and `C-0060` both express `T-45` as a relative scatter amplitude, which presumes a
 * continuous distribution. The failure mode an origami array actually has is **discrete** — a
 * staple is incorporated or it is not — so translating the two published thresholds onto a dropout
 * rate is what makes them comparable against anything a builder can measure.
 *
 * It is a **translation of a threshold onto a build-controllable variable and not an equivalence**:
 * a dropout has a different spatial pattern from an alternating scatter, and `C-0060` shows the
 * pattern is worth 2.21×.
 *
 * @throws IllegalArgumentException if [relativeScatter] is negative.
 */
fun dropoutRateForRelativeScatter(relativeScatter: Double): Double {
    require(relativeScatter >= 0.0) {
        "relativeScatter must not be negative, was: $relativeScatter"
    }
    val variance = relativeScatter * relativeScatter
    return variance / (1.0 + variance)
}

/**
 * The population relative standard deviation of a two-valued population in which a fraction
 * [dropoutRate] of paths carry no stiffness at all: `σ = √(f/(1 − f))`. Inverse of
 * [dropoutRateForRelativeScatter].
 *
 * @throws IllegalArgumentException if [dropoutRate] is not in `[0, 1)`.
 */
fun relativeScatterForDropoutRate(dropoutRate: Double): Double {
    require(dropoutRate >= 0.0 && dropoutRate < 1.0) {
        "dropoutRate must be in [0, 1), was: $dropoutRate"
    }
    return sqrt(dropoutRate / (1.0 - dropoutRate))
}
