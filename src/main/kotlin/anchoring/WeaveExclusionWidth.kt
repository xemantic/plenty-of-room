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
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * `T-137`, leaf `A8.2` — **the plan model against the measured weave.**
 *
 * `C-0072`'s literature survey found that the interhelical distance in a lattice origami is a
 * *deterministic sawtooth* rather than a constant, and read its two ends through the plan margin
 * `M = p − d − L` to get **+0.866 nm** and **−0.884 nm** — comfortable and impossible. This file
 * is what that reading needs before it can be believed: a model of the weave that carries its
 * **phase**, so that the value at a *named lattice coordinate* can be computed instead of the two
 * extremes being substituted blind.
 *
 * ## The measurement, read directly
 *
 * **Snodin, Romano, Rovigatti, Ouldridge, Louis, Doye, *NAR* **47**:1585 (2019)** — oxDNA on a
 * **2D tile**, which is this programme's own object — `PMC6379721`, verbatim:
 *
 * > *"We quantify the weave pattern of the 2D tile by measuring the distance between the helix
 * > axes (defined as the midpoint between the bases for each base pair) for adjacent double
 * > helices. … Each group exhibits a wave-like pattern with **minima at the crossovers**, where
 * > the double helices are brought closest together, and **maxima away from the crossovers,
 * > normally at a position which is both midway between the junctions and where the adjacent pair
 * > of helices have a crossover**. This pattern has a **periodicity of about 32 base-pair
 * > steps**, corresponding to the periodic junction placement in the origami."*
 *
 * and, on the shape:
 *
 * > *"It is also interesting to note the **'triangular wave' character** of the weave plots. The
 * > bending that creates the weave pattern is mostly localized at the junctions with the
 * > **intervening sections basically straight**."*
 *
 * **Bai, Martin, Scheres, Dietz, *PNAS* **109**:20012 (2012)**, `PMC3523823`, Fig. 3 E/F caption,
 * verbatim:
 *
 * > *"The pattern was computed using the coordinates of base pair midpoints in the pseudoatomic
 * > model. The midpoints of neighboring dsDNA helices move on average from a minimum distance
 * > ⟨d min⟩ = 18.5 Å at the cross-over to a maximum distance of ⟨d max⟩ = 36 Å away from each
 * > other."*
 *
 * Both define the quantity as a distance between **helix axes**, both put the **minimum at the
 * crossover**, and Snodin puts the **maximum at the adjacent interface's crossover** — which is
 * the geometric fact this file's phase model is built on and not an assumption of it.
 *
 * ## The model, and why it is exact rather than fitted
 *
 * A single-layer square-lattice sheet's duplex `b` faces its two in-plane neighbours at the
 * crossover planes `k ≡ 2b (mod 4)` (`NORTH`, to `b+1`) and `k ≡ 2b+2 (mod 4)` (`SOUTH`, to
 * `b−1`) — `C-0055`'s azimuth rule, consumed rather than restated. So duplex `b` is pulled toward
 * `b+1` at one plane and toward `b−1` two planes later, and between them it is straight. That is
 * a **zig-zag of one amplitude and one phase per duplex**, and it reproduces the measured
 * triangular wave on **every** interface simultaneously:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`y_b(k) = b·a + (Δ/2)·h((k − 2b) mod 4)`, `h(v) = |v − 2|/2 − ½`
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`D_b(k) = y_{b+1} − y_b = a − Δ/2 + Δ·g((k − 2b) mod 4)`,
 * `g(v) = 1 − |v − 2|/2`
 *
 * with `a` the lattice constant and `Δ` the measured peak-to-peak. `h` vanishes at **odd** `v` and
 * `g` is `½` there, so **every odd plane is a node of the weave**: the duplex sits at its ideal
 * lattice position and its two neighbours are at exactly `a`.
 *
 * **That is the whole of `T-137`'s cheap bound**, because `C-0055`'s upward roots are at
 * `k ≡ 2b+3 (mod 4)`, which is odd for every `b` and every phase.
 *
 * ## The three quantities the plan model calls `d`
 *
 * `C-0041`, `C-0053`, `C-0065`, `C-0066` and `C-0069` all write *"a duplex in plan is a rectangle
 * of width `d = 2.69 nm`"* and use the one symbol for three different things — [ExclusionRole].
 * The weave is a measurement of exactly one of them.
 *
 * ## Conventions
 *
 * Lengths **nm**; `x` along the helices, `y` across them, origin at the tile centre; a **plane
 * index** `k` is `C-0055`'s 8 bp crossover plane, `x = phase + k · 8 · rise`. Interface `b` is the
 * interface between duplex `b` and duplex `b+1`. T = 300 K, aqueous buffer; the sources' own ionic
 * strengths are recorded in the claim and are **not** silently transferred.
 */

// ---------------------------------------------------------------- the measured constants

/** `d_min` in nm at a crossover — Bai et al. (2012), **CITED, MEASURED**, read directly. */
const val BAI_MINIMUM: Double = 1.85

/** `d_max` in nm midway between crossovers — Bai et al. (2012), **CITED, MEASURED**. */
const val BAI_MAXIMUM: Double = 3.60

/** The midpoint of Bai's sawtooth in nm — **DERIVED**, and the quantity a Bragg fit returns. */
const val BAI_MEAN: Double = (BAI_MINIMUM + BAI_MAXIMUM) / 2.0

/** Bai's peak-to-peak in nm — **DERIVED**. */
const val BAI_PEAK_TO_PEAK: Double = BAI_MAXIMUM - BAI_MINIMUM

/**
 * The peak-to-peak of the weave on a **2D tile** in nm — Snodin et al. (2019), oxDNA at 300 K and
 * `[Na⁺] = 0.5 M`, **CITED, SIMULATED**, verbatim *"1.5 nm as a typical value of the difference in
 * the interhelix distance between the maxima and minima of the weave pattern"*.
 *
 * This is the reading on **this programme's own object**, and it is the default.
 */
const val SNODIN_TILE_PEAK_TO_PEAK: Double = 1.5

/**
 * The mean interhelix distance oxDNA gives the same 2D tile in nm, **CITED, SIMULATED** — *"Taking
 * the average between the maxima and minima of the triangular wave form gives an interhelix
 * distance of about 3.25 [nm] at [Na⁺] = 0.5 M … and 3.1 [nm] at the high ionic strength limit"*
 * (the paper prints Å for nm at that sentence). Carried because it disagrees with the SAXS lattice
 * constant by 15 %, which is a spread on the **mean** and not on the weave.
 */
const val SNODIN_TILE_MEAN: Double = 3.25

/** All-atom MD's window in nm — Yoo & Aksimentiev (2013), **CITED, SIMULATED**, *"18 and 30 Å"*. */
const val YOO_MINIMUM: Double = 1.80

/** The upper end of the same window in nm. */
const val YOO_MAXIMUM: Double = 3.00

/**
 * The SAXS lattice constant of a **square-lattice multilayer** in nm — Fischer et al. (2016),
 * **CITED, MEASURED**. Carried here only as the cross-check against [BAI_MEAN], which is measured
 * on the same lattice type by a different method.
 */
const val INTERHELICAL_SQUARE_LATTICE: Double = 2.73

/**
 * The three quantities the plan model's single `d` stands for.
 *
 * The distinction is the whole of `T-137`: a measurement of one of them is not a measurement of
 * the others, and the weave measures exactly [ROW_PITCH].
 */
enum class ExclusionRole(val alongHelices: Boolean, val betweenBondedBodies: Boolean) {

    /**
     * The **across-helix pitch of the host sheet's own duplexes**, which is what `C-0015`'s
     * attachment grid and `C-0063`'s station rows are laid on. Two crossover-bonded host duplexes;
     * the weave **is** this quantity's variation along `x`.
     */
    ROW_PITCH(alongHelices = false, betweenBondedBodies = true),

    /**
     * The **plan girth of one free body** — an arm above the sheet, a tie standing normal to it.
     * A steric half-width about an axis, and no lattice constant at all.
     */
    BODY_WIDTH(alongHelices = false, betweenBondedBodies = false),

    /**
     * The clearance `C-0053`'s footprint convention charges **along the helices** between one
     * rooted element's tip and the next element's root — the `d` in `C-0072`'s `M = p − d − L`.
     * Two **unbonded** bodies, and the axis the weave has no component on.
     */
    COLLINEAR_CLEARANCE(alongHelices = true, betweenBondedBodies = false);

    /**
     * Whether the measured weave is a measurement of this quantity. It is a statement about the
     * **slot**, not about the number: the weave is a separation across the helices between two
     * duplexes that are covalently linked at its own minimum.
     */
    val weaveApplies: Boolean get() = !alongHelices && betweenBondedBodies
}

/**
 * The duplex's own steric width, in its three readings.
 *
 * `CLAUDE.md` asserts *"the phosphate radius in B-DNA is 10 Å, which IS the duplex's steric
 * radius"*, with the 8.9–9.4 Å fibre numbers as *"the other end of a bracket"*. `T-71` **measured**
 * it, on 13 084 crystallographic linkages, and that measurement is what adjudicates Bai's 18.5 Å
 * against this project's 2.0 nm.
 */
object DuplexSteric {

    /** The round 2.0 nm this project asserts — **CITED**. */
    const val ASSERTED_DIAMETER: Double = 2.0 * BForm.DUPLEX_RADIUS

    /** The narrow fibre reading — **CITED**, the other end of `CLAUDE.md`'s bracket. */
    const val FIBRE_NARROW_DIAMETER: Double = 2.0 * BForm.PHOSPHATE_RADIUS_NARROW

    /** `T-71`'s **MEASURED** B-form C2′-endo population phosphate radius, in nm. */
    const val MEASURED_RADIUS: Double = MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS

    /** Its population standard deviation, in nm. */
    const val MEASURED_RADIUS_SD: Double = MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS_SD

    /** Twice [MEASURED_RADIUS] — the measured phosphate-backbone contact distance, in nm. */
    val MEASURED_DIAMETER: Double get() = 2.0 * MEASURED_RADIUS

    /** The standard deviation of [MEASURED_DIAMETER] treating the two radii as independent. */
    val MEASURED_DIAMETER_SD: Double get() = sqrt(2.0) * MEASURED_RADIUS_SD

    /** How many standard deviations [approach] sits above phosphate-backbone contact. */
    fun contactMargin(approach: Double): Double =
        (approach - MEASURED_DIAMETER) / MEASURED_DIAMETER_SD
}

// ---------------------------------------------------------------- the weave

/**
 * The measured weave as a function of the lattice coordinate: a **triangular** wave in the
 * interhelical distance of period `4` crossover planes (32 bp), minimum at the interface's own
 * crossovers and maximum at the adjacent interfaces' crossovers.
 *
 * @param meanDistance the lattice constant `a` in nm — what a Bragg fit returns.
 * @param peakToPeak the measured `d_max − d_min` in nm.
 * @param planeBasePairs the crossover-plane spacing in base pairs (`C-0055`'s 8).
 * @param phaseBasePairs the column-lattice phase the layout chooses.
 * @param edgeDuplexesStraight whether the two edge duplexes, which Snodin explicitly excludes
 *          because they are *"only constrained on one side"*, are held straight. It **halves** the
 *          amplitude on the two edge interfaces and leaves the node structure untouched.
 * @param duplexes the sheet's duplex count, used only when [edgeDuplexesStraight].
 */
data class WeaveProfile(
    val meanDistance: Double = Gen1Tile.INTERHELICAL_SHEET,
    val peakToPeak: Double = SNODIN_TILE_PEAK_TO_PEAK,
    val planeBasePairs: Int = CROSSOVER_PLANE_BASE_PAIRS,
    val risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR,
    val phaseBasePairs: Int = 24,
    val edgeDuplexesStraight: Boolean = false,
    val duplexes: Int = 15
) {

    init {
        require(meanDistance > 0.0) { "meanDistance must be positive, was: $meanDistance" }
        require(peakToPeak >= 0.0) { "peakToPeak must not be negative, was: $peakToPeak" }
        require(peakToPeak <= 2.0 * meanDistance) {
            "a peak-to-peak of $peakToPeak nm cannot sit on a mean of $meanDistance nm"
        }
        require(planeBasePairs > 0) { "planeBasePairs must be positive, was: $planeBasePairs" }
        require(risePerBasePair > 0.0) {
            "risePerBasePair must be positive, was: $risePerBasePair"
        }
        require(phaseBasePairs >= 0) {
            "phaseBasePairs must not be negative, was: $phaseBasePairs"
        }
        require(duplexes >= 2) { "duplexes must be at least 2, was: $duplexes" }
    }

    /** The plane spacing in nm — 2.72 nm on the Gen-1 sheet. */
    val planeSpacing: Double get() = planeBasePairs * risePerBasePair

    /** The weave period in nm — four planes, i.e. 32 bp, which is what Snodin measures. */
    val period: Double get() = PLANES_PER_PERIOD * planeSpacing

    /** `d_min`, at a crossover of the interface's own. */
    val minimumDistance: Double get() = meanDistance - peakToPeak / 2.0

    /** `d_max`, at the adjacent interfaces' crossovers. */
    val maximumDistance: Double get() = meanDistance + peakToPeak / 2.0

    /** The real-valued plane coordinate of a position [x] in nm along the helices. */
    fun planeCoordinate(x: Double): Double =
        (x - phaseBasePairs * risePerBasePair) / planeSpacing

    /**
     * The interhelical distance on interface [interfaceIndex] at plane coordinate [plane] —
     * the **primary** triangular form, `a − Δ/2 + Δ·g(v)`.
     */
    fun distanceAtPlane(interfaceIndex: Int, plane: Double): Double {
        require(interfaceIndex >= 0) {
            "interfaceIndex must not be negative, was: $interfaceIndex"
        }
        val amplitude = peakToPeak * interfaceAmplitudeFactor(interfaceIndex)
        val v = folded(plane - 2.0 * interfaceIndex)
        return meanDistance - amplitude / 2.0 + amplitude * triangle(v)
    }

    /** [distanceAtPlane] at a position [x] in nm. */
    fun distanceAt(interfaceIndex: Int, x: Double): Double =
        distanceAtPlane(interfaceIndex, planeCoordinate(x))

    /**
     * Duplex [duplex]'s own excursion in nm from its ideal lattice position `b·a`, at plane
     * coordinate [plane] — the **primary** zig-zag, `(Δ/2)·h(v)`, positive toward `+y`.
     *
     * A duplex held straight (an edge duplex under [edgeDuplexesStraight]) has none.
     */
    fun axisOffsetAtPlane(duplex: Int, plane: Double): Double {
        require(duplex >= 0) { "duplex must not be negative, was: $duplex" }
        if (edgeDuplexesStraight && (duplex == 0 || duplex == duplexes - 1)) return 0.0
        val v = folded(plane - 2.0 * duplex)
        return (peakToPeak / 2.0) * (0.5 - triangle(v))
    }

    /** [axisOffsetAtPlane] at a position [x] in nm. */
    fun axisOffset(duplex: Int, x: Double): Double =
        axisOffsetAtPlane(duplex, planeCoordinate(x))

    /** Duplex [duplex]'s axis position in nm, measured from duplex 0's ideal lattice position. */
    fun axisPosition(duplex: Int, x: Double): Double =
        duplex * meanDistance + axisOffset(duplex, x)

    /**
     * The amplitude an interface carries relative to [peakToPeak]. An interface bounded by a
     * straight edge duplex carries **half**, because only one of its two duplexes zig-zags.
     */
    private fun interfaceAmplitudeFactor(interfaceIndex: Int): Double =
        if (edgeDuplexesStraight && (interfaceIndex == 0 || interfaceIndex == duplexes - 2)) {
            0.5
        } else {
            1.0
        }

    private companion object {

        const val PLANES_PER_PERIOD: Int = CROSSOVER_PLANES_PER_PERIOD

        /** `v` folded into `[0, 4)`. */
        fun folded(value: Double): Double {
            val period = PLANES_PER_PERIOD.toDouble()
            val wrapped = value - period * Math.floor(value / period)
            return if (wrapped >= period) 0.0 else wrapped
        }

        /** `g(v) = 1 − |v − 2|/2` — `0` at `v = 0`, `1` at `v = 2`, `½` at both odd planes. */
        fun triangle(v: Double): Double = 1.0 - abs(v - 2.0) / 2.0
    }
}

/**
 * Whether [plane] is a **node** of the weave — an odd plane, where every duplex sits at its ideal
 * lattice position and every interface is at its lattice constant, whatever the amplitude.
 */
fun isWeaveNode(plane: Int): Boolean = Math.floorMod(plane, 2) == 1

/**
 * The plane index of a position [x] in nm on the lattice of [profile], rounded — the inverse of
 * `x = phase + k · planeSpacing`.
 */
fun weavePlaneIndex(profile: WeaveProfile, x: Double): Int =
    Math.round(profile.planeCoordinate(x)).toInt()

// ---------------------------------------------------------------- what the plan model asks

/** `M = p − d − L`, `C-0072`'s identity, with the exclusion width supplied rather than assumed. */
fun planMarginAtWidth(pitch: Double, width: Double, length: Double): Double {
    require(pitch > 0.0) { "pitch must be positive, was: $pitch" }
    require(width >= 0.0) { "width must not be negative, was: $width" }
    require(length > 0.0) { "length must be positive, was: $length" }
    return pitch - width - length
}

/**
 * The **across-row** clearance an arm of plan girth [bodyWidth] rooted on [duplex] at [x] has to
 * the arm on the next duplex up, under [profile].
 *
 * This is the one plan quantity the weave is a measurement of, and its value at an upward root is
 * `a − bodyWidth` **exactly**, independently of the weave's amplitude — because an upward root is
 * an odd plane and an odd plane is a node.
 */
fun acrossRowClearance(
    profile: WeaveProfile,
    duplex: Int,
    x: Double,
    bodyWidth: Double
): Double {
    require(bodyWidth >= 0.0) { "bodyWidth must not be negative, was: $bodyWidth" }
    return profile.distanceAt(duplex, x) - bodyWidth
}

/**
 * `C-0063`'s [armDirections] with the collinear clearance supplied as a **function of position**
 * rather than as a constant.
 *
 * The frontier rule, the `+x`-first search order, the edge test and the tolerance are `C-0063`'s
 * verbatim; the single change is that the clearance charged past an element's tip is
 * `clearanceAt(high)` rather than a fixed `width`. A constant function must therefore reproduce
 * [armDirections] **bit for bit**, and that is asserted as this task's free limiting case.
 */
fun armDirectionsWithClearance(
    roots: List<Double>,
    arm: Double,
    edgeX: Double,
    clearanceAt: (Double) -> Double
): List<Boolean>? {
    require(roots.isNotEmpty()) { "roots must not be empty" }
    require(arm > 0.0) { "arm must be positive, was: $arm" }
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    val half = edgeX / 2.0
    val sorted = roots.sorted()
    require(sorted == roots) { "roots must be given in ascending order, were: $roots" }

    fun search(index: Int, frontier: Double, taken: List<Boolean>): List<Boolean>? {
        if (index == roots.size) return taken
        for (toward in listOf(true, false)) {
            val low = if (toward) roots[index] else roots[index] - arm
            val high = if (toward) roots[index] + arm else roots[index]
            if (low < -half - PLAN_TANGENCY_TOLERANCE) continue
            if (high > half + PLAN_TANGENCY_TOLERANCE) continue
            if (low < frontier - PLAN_TANGENCY_TOLERANCE) continue
            val clearance = clearanceAt(high)
            require(clearance >= 0.0) { "a clearance must not be negative, was: $clearance" }
            val found = search(index + 1, high + clearance, taken + toward)
            if (found != null) return found
        }
        return null
    }
    require(clearanceAt(sorted.first()) >= 0.0) {
        "a clearance must not be negative, was: ${clearanceAt(sorted.first())}"
    }
    return search(0, Double.NEGATIVE_INFINITY, emptyList())
}
