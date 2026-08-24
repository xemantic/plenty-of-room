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

package com.xemantic.nano.plentyofroom.tile

/**
 * `T-304` — the **azimuth** of a raster turn's two anchoring phosphates, and therefore the
 * **span** route B's tether is held at.
 *
 * ## Why a bracket is the wrong instrument here
 *
 * [`C-0147`] and [`C-0193`] carry the turn's span as an azimuth bracket, `d − 2r_P` to
 * `d + 2r_P`, because they were bounding **reach**: a turn either closes at some azimuth or it
 * closes at none, and a bracket answers that. `C-0201` then prices a mechanical **element** at
 * the same bracket, and there it straddles `T-5b` — `0.0569815008` of the stroke at one end
 * against `0.166312182` at the other, `24 of 36` corners flat.
 *
 * **The span is not unknown.** A chain leaves helix `a` at the phosphate of that helix's last
 * paired base and enters helix `b` at the phosphate of its own, and a phosphate's azimuth is
 * fixed by its base-pair index and the lattice's phase — the quantity `C-0148` reduces to a
 * residue and `C-0187` turns into a sign.
 *
 * ## The closed form, and its reference is 5.25 rather than 5
 *
 * caDNAno's scaffold rule is *"five base pairs, **or half a turn**"*, and at 10.5 bp/turn the
 * half turn is `21/4 = 5.25` bp ([EXACT_HALF_TURN_BASE_PAIRS], `C-0152` §5, `CH-0197`). So the
 * scaffold backbone faces its neighbour **exactly** at reduced residue `b₀ + 5.25`, and because
 * `10.5 bp` is exactly `360°` the two candidates `b₀ ± 5.25` are **one** azimuth. Hence
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`θ(ρ, b₀) = fold((ρ − b₀ − 21/4) · 240/7°)`
 *
 * which reproduces `C-0187`'s [scaffoldDisplacementDepartureDegrees] at both allowed residues,
 * at every `b₀`, exactly — that agreement is a named test and not a coincidence.
 *
 * ## Why one angle suffices for both phosphates
 *
 * Both helices of a honeycomb bond are parallel, same-handed and at one design twist, so
 * `∂(ψ_a − ψ_b)/∂z = 0` identically (`CLAUDE.md`; `ForcedCrossoverPrice`'s own header): a level
 * displacement rotates **both** backbones the same way. The entry azimuth is therefore the exit
 * azimuth plus `180°`, and the span is [forcedCrossoverSpan] — `C-0147`'s own
 * [com.xemantic.nano.plentyofroom.structure.turnPhosphateSpan] consumed unmodified.
 *
 * ## The anchor offset is a DESIGN, not a tolerance
 *
 * On the recommended raster the row lengths are **paired** lengths and route B's unpaired loop
 * sits **outboard** of the duplex — which is what `C-0200` reads off the built block (duplex
 * `28..125` on all 60 helices, scaffold beyond it) and what `C-0201` §7's width arithmetic
 * assumes. So the last paired base sits **at** the raster level and [anchorOffsetBasePairs] is
 * `0`. A design that carved the loop out of the paired row instead would move both anchors
 * inboard together, and the parameter prices that design rather than an uncertainty in this one.
 *
 * Angles in **degrees** at every boundary of this file; levels are integer base pairs on one
 * global `z`, `C-0140`'s convention.
 */

/** The precision an azimuth comparison is DECIDED at, in degrees — `ForcedCrossoverPrice`'s. */
private const val ANCHOR_AZIMUTH_DECISION_DEGREES: Double = 1e-6

/** The precision a span comparison is DECIDED at, in nm. */
private const val ANCHOR_SPAN_DECISION_NM: Double = 1e-9

/**
 * The azimuth in degrees of a scaffold backbone phosphate sitting at reduced residue [residue] on
 * a bond whose class-zero staple residue is [classZeroResidue], measured from the line of centres
 * pointing at the bonded neighbour and folded into `(−180°, +180°]`.
 *
 * The residue is taken as a real number so that the **exact** facing position, `b₀ + 5.25`, can
 * be evaluated — it is not a lattice position and the whole `8.57142857°` of `CH-0197` is the
 * distance from it to the nearest one.
 */
fun anchorAzimuthDegreesExact(residue: Double, classZeroResidue: Int): Double =
    foldedDegrees(
        (residue - classZeroResidue - EXACT_HALF_TURN_BASE_PAIRS) * AZIMUTH_PER_BASE_PAIR
    )

/** [anchorAzimuthDegreesExact] at an integer lattice residue. */
fun anchorAzimuthDegrees(residue: Int, classZeroResidue: Int): Double =
    anchorAzimuthDegreesExact(residue.toDouble(), classZeroResidue)

/** One raster turn's two anchoring phosphates, and the span they hold the tether at. */
data class RasterTurnAnchor(

    /** The turn's position along the raster path, `0` until `helices − 1`. */
    val index: Int,

    /** The axial level of the raster turn itself, in base pairs on one global `z`. */
    val turnLevelBasePairs: Int,

    /** The axial level of the last **paired** base, which is where the chain attaches. */
    val anchorLevelBasePairs: Int,

    /** `(level − 7·class) mod 21` at the turn — `C-0148`'s own quantity. */
    val reducedResidue: Int,

    /** The same reduction taken at the anchor level rather than at the turn level. */
    val anchorResidue: Int,

    /** Whether the turn sits at the block's high axial rim, `s = +L/2`. */
    val atHighEnd: Boolean,

    /** The azimuth of the phosphate the chain leaves by, in degrees from the line of centres. */
    val exitAzimuthDegrees: Double,

    /** The azimuth of the phosphate it enters by — the exit plus `180°`, exactly. */
    val entryAzimuthDegrees: Double,

    /** The distance in nm between the two, which is the tether's span. */
    val span: Double
)

/**
 * Every raster turn of an `m × n` honeycomb block, with the azimuth and span of its two anchors.
 *
 * @param classZeroResidue the lattice constant `b₀`. On a raster that **closes** it is derived
 *   ([derived]); on one that does not it is a free **design variable**, which is why it is an
 *   argument here rather than a property — route B does not need the residue condition at all,
 *   because an unpaired base has no azimuth (`C-0193` §4).
 * @param anchorOffsetBasePairs how far **inboard** of the turn the last paired base sits. `0` is
 *   the loop-outboard reading of the built object and of the graded lattice.
 * @param mirrored the cross-section reflected, `x → −x` — the free viewing convention.
 * @param axialReversed the axial datum negated; composed with [mirrored] it is the proper
 *   rotation about the in-plane axis (`CLAUDE.md`).
 */
@Suppress("LongParameterList")
class HoneycombRasterTurnAnchors(
    val block: HoneycombBlock,
    val senseOneBasePairs: Int,
    val senseTwoBasePairs: Int,
    val interhelicalDistance: Double,
    val phosphateRadius: Double,
    val classZeroResidue: Int,
    val anchorOffsetBasePairs: Int = 0,
    val firstAxialSign: Int = 1,
    val mirrored: Boolean = false,
    val axialReversed: Boolean = false
) {

    init {
        require(classZeroResidue in 0 until PERIOD) {
            "a class-zero residue lives in [0, $PERIOD), was: $classZeroResidue"
        }
        require(anchorOffsetBasePairs >= 0) {
            "an anchor cannot sit outboard of its own duplex end, was: $anchorOffsetBasePairs"
        }
    }

    private val residues = HoneycombRasterResidues(
        rasterRows = block.rasterRows,
        helicesPerRow = block.helicesPerRow,
        senseOneBasePairs = senseOneBasePairs,
        senseTwoBasePairs = senseTwoBasePairs,
        firstAxialSign = firstAxialSign,
        mirrored = mirrored,
        axialReversed = axialReversed
    )

    private val turns = honeycombRasterTurnList(block, firstAxialSign)

    /** Whether the raster closes on caDNAno's default scaffold rule. */
    val closes: Boolean = residues.closes

    /**
     * `+1` on the standard axial datum and `−1` on the reversed one.
     *
     * [AZIMUTH_PER_BASE_PAIR] is `+240/7°` **per base pair of increasing `z`**, so reversing `z`
     * reverses the handedness a residue is read with and the constant reverses with it —
     * *"a residue map is a handedness, so it must be reversed whenever `z` is"* (`CLAUDE.md`).
     * Carrying it is what makes the **signed** azimuth invariant under the reversal, and not only
     * its magnitude; `T-284`'s `F3` found the omission on its first real run.
     */
    private val datumSign: Int = if (axialReversed) -1 else 1

    /**
     * The derived anchor of every turn.
     *
     * The anchor sits [anchorOffsetBasePairs] base pairs **inboard** of the turn, and inboard is
     * read off the raster's own level walk rather than off the rim label. A turn is a **rim**, so
     * both of the helices it joins lie on the same side of it: helix `path[k]` runs from `L(k−1)`
     * to `L(k)` and helix `path[k+1]` from `L(k)` to `L(k+1)`, and consecutive helices are
     * traversed antiparallel, so `L(k−1) − L(k)` and `L(k+1) − L(k)` carry the **same** sign.
     * Both anchors therefore move to one level, which is what keeps a single azimuth sufficient
     * for the pair — and the two readings agreeing is a named test rather than an assumption.
     */
    val anchors: List<RasterTurnAnchor> by lazy {
        val levels = residues.crossoverLevels
        check(turns.size == residues.reducedResidues.size) {
            "the turn census carries ${turns.size} turns and the residue walk " +
                    "${residues.reducedResidues.size}; they must index the same crossovers"
        }
        turns.mapIndexed { k, turn ->
            val here = levels.getValue(k)
            val inboard = if (levels.containsKey(k + 1)) {
                if (levels.getValue(k + 1) >= here) 1 else -1
            } else {
                if (levels.getValue(k - 1) >= here) 1 else -1
            }
            val shift = inboard * anchorOffsetBasePairs
            val anchorResidue =
                Math.floorMod(residues.reducedResidues[k] + shift, PERIOD)
            val exit = datumSign * anchorAzimuthDegrees(anchorResidue, classZeroResidue)
            RasterTurnAnchor(
                index = k,
                turnLevelBasePairs = here,
                anchorLevelBasePairs = here + shift,
                reducedResidue = residues.reducedResidues[k],
                anchorResidue = anchorResidue,
                atHighEnd = turn.atHighEnd,
                exitAzimuthDegrees = foldedDegrees(exit),
                entryAzimuthDegrees = foldedDegrees(exit + STRAIGHT_ANGLE),
                span = forcedCrossoverSpan(interhelicalDistance, phosphateRadius, exit)
            )
        }
    }

    /** Every span the 59 turns take, decided coarser than the arithmetic's own noise. */
    val distinctSpans: List<Double> by lazy {
        anchors.map { it.span }
            .distinctBy { Math.round(it / ANCHOR_SPAN_DECISION_NM) }
            .sorted()
    }

    /** Every azimuth magnitude the 59 turns take, decided at the same precision. */
    val distinctAzimuthMagnitudes: List<Double> by lazy {
        anchors.map { kotlin.math.abs(it.exitAzimuthDegrees) }
            .distinctBy { Math.round(it / ANCHOR_AZIMUTH_DECISION_DEGREES) }
            .sorted()
    }

    /**
     * The one span every turn takes, or `null` where the turns take more than one.
     *
     * The `null` is a **verdict** — *"this design's tether span is a distribution and not a
     * value"* — and it is what a route-B raster that does not close returns.
     */
    val singleValuedSpan: Double?
        get() = distinctSpans.singleOrNull()

    companion object {

        private const val PERIOD: Int = HoneycombCrossoverRule.SAME_PAIR_PERIOD_BP
        private const val STRAIGHT_ANGLE: Double = 180.0

        /**
         * The same, with `b₀` **derived** from the raster's own closure condition.
         *
         * It refuses — rather than guessing — wherever the raster does not close or leaves more
         * than one candidate, which is `HoneycombRasterTurnSigns.classZeroResidue`'s own contract.
         */
        @Suppress("LongParameterList")
        fun derived(
            block: HoneycombBlock,
            senseOneBasePairs: Int,
            senseTwoBasePairs: Int,
            interhelicalDistance: Double,
            phosphateRadius: Double,
            anchorOffsetBasePairs: Int = 0,
            firstAxialSign: Int = 1,
            mirrored: Boolean = false,
            axialReversed: Boolean = false
        ): HoneycombRasterTurnAnchors {
            val signs = HoneycombRasterTurnSigns(
                block = block,
                senseOneBasePairs = senseOneBasePairs,
                senseTwoBasePairs = senseTwoBasePairs,
                firstAxialSign = firstAxialSign,
                mirrored = mirrored,
                axialReversed = axialReversed
            )
            return HoneycombRasterTurnAnchors(
                block = block,
                senseOneBasePairs = senseOneBasePairs,
                senseTwoBasePairs = senseTwoBasePairs,
                interhelicalDistance = interhelicalDistance,
                phosphateRadius = phosphateRadius,
                classZeroResidue = signs.classZeroResidue,
                anchorOffsetBasePairs = anchorOffsetBasePairs,
                firstAxialSign = firstAxialSign,
                mirrored = mirrored,
                axialReversed = axialReversed
            )
        }
    }
}
