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

import com.xemantic.nano.plentyofroom.structure.Gen1Tile

/**
 * `T-307` — route B's own **uniform** raster, whose 59 turns take a span **distribution**.
 *
 * ## Why this file exists beside `T-299`'s
 *
 * [honeycombScaffoldTurnTethers] carries **one** chain state per rim, which is exactly right on
 * the drawable `102 / 109` raster: there `C-0187` pins `b₀ = 5`, every turn sits at an *allowed*
 * scaffold crossover, and `C-0204` shows the span is **one number** at all 59.
 *
 * Route B does not need that closure at all — an unpaired base has no azimuth (`C-0193` §4) — and
 * at the built `28 nt` allowance its own uniform paired rows (`92 / 98 / 106 bp`) close at **no**
 * lattice phase. There `b₀` is a free **design variable** and the turns take between two and five
 * distinct spans, so the element is **per turn** and the lattice phase is an axis of the design.
 *
 * ## What is shared and what is not
 *
 * The site census is [honeycombScaffoldTurnTies]' own and the element is
 * [HoneycombScaffoldTurnTether], both unchanged — so a per-turn tethered lattice and `T-299`'s
 * single-state one differ in **exactly** the span each of the 59 chains is held at. The span
 * itself is [HoneycombRasterTurnAnchors]' own, consumed rather than re-derived, and the two
 * censuses are asserted to index the same turns rather than assumed to.
 *
 * ## The stiffness handle, and why it is here
 *
 * A tether's tension is a **load** in `C-0104`'s exact sense and its stiffness is not, so the two
 * have to be separable to be measured apart. [UniformRasterTethers.elements] takes an optional
 * `stiffness` override: at `0.0` the element list is **inert in the stiffness matrix** — bit-
 * identical to the untethered lattice on `assembleLoad` with the preload dropped as well — which
 * is what makes an influence bank over the 59 tensions a bank on **one** factorisation, and what
 * makes the difference between the banked field and the solved one a measurement of the
 * stiffness rather than an assumption about it.
 *
 * Angles nowhere; lengths in nm, tensions in pN, stiffnesses in pN/nm.
 */

/** The honeycomb crossover lattice's own period, in base pairs. */
private const val UNIFORM_RASTER_PERIOD: Int = HoneycombCrossoverRule.SAME_PAIR_PERIOD_BP

/**
 * The 59 raster turns of an `m × n` honeycomb block on a **uniform** raster, each carried as the
 * freely-jointed chain its own anchor azimuth holds at its own span.
 *
 * @param pairedRowBasePairs the **paired** row length, which on route B is
 *   `scaffoldNucleotides / helices − loopPerTurn` and is what the built allowance fixes.
 * @param classZeroResidue the lattice constant `b₀`. On a raster that does not close it is a free
 *   **design variable**, which is why it is an argument and never derived here.
 * @param lowRimNucleotides the unpaired count of a turn at the block's **low** axial rim, and
 *   [highRimNucleotides] of one at the high — `C-0200`'s ordered `24 / 32` split read on the two
 *   rims, whose assignment is a free convention of that reading and is swept by exchanging them.
 * @param anchorOffsetBasePairs how far **inboard** of the turn the last paired base sits; `0` is
 *   the loop-outboard reading of the built block.
 */
@Suppress("LongParameterList")
class UniformRasterTethers(
    val block: HoneycombBlock,
    val pairedRowBasePairs: Int,
    val interhelicalDistance: Double,
    val phosphateRadius: Double,
    val classZeroResidue: Int,
    val lowRimNucleotides: Int,
    val highRimNucleotides: Int,
    val kuhnLength: Double,
    val contourPerNucleotide: Double,
    val thermalEnergy: Double,
    val anchorOffsetBasePairs: Int = 0,
    val firstAxialSign: Int = 1
) {

    init {
        require(pairedRowBasePairs > 0) {
            "pairedRowBasePairs must be positive, was: $pairedRowBasePairs"
        }
        require(classZeroResidue in 0 until UNIFORM_RASTER_PERIOD) {
            "a class-zero residue lives in [0, $UNIFORM_RASTER_PERIOD), was: $classZeroResidue"
        }
    }

    /** The turn census, in the raster path's own order. */
    val turns: List<HoneycombRasterTurn> = honeycombRasterTurnList(block, firstAxialSign)

    /** The anchor census, which must index the same turns. */
    val anchorCensus: HoneycombRasterTurnAnchors by lazy {
        HoneycombRasterTurnAnchors(
            block = block,
            senseOneBasePairs = pairedRowBasePairs,
            senseTwoBasePairs = pairedRowBasePairs,
            interhelicalDistance = interhelicalDistance,
            phosphateRadius = phosphateRadius,
            classZeroResidue = classZeroResidue,
            anchorOffsetBasePairs = anchorOffsetBasePairs,
            firstAxialSign = firstAxialSign
        )
    }

    /** Whether the raster closes on caDNAno's default scaffold rule — on route B it need not. */
    val closes: Boolean get() = anchorCensus.closes

    /** The span in nm each of the 59 chains is held at, in the turn census's own order. */
    val spans: List<Double> by lazy {
        val anchors = anchorCensus.anchors
        check(anchors.size == turns.size) {
            "the anchor census carries ${anchors.size} turns and the turn census ${turns.size}; " +
                    "they must index the same raster turns"
        }
        anchors.mapIndexed { k, anchor ->
            check(anchor.index == turns[k].index) {
                "anchor ${anchor.index} is indexed against turn ${turns[k].index}"
            }
            anchor.span
        }
    }

    /** The chain state of every turn, at its own rim's nucleotide count and its own span. */
    val states: List<HoneycombTetherState> by lazy {
        spans.mapIndexed { k, span ->
            freelyJointedTetherState(
                span = span,
                unpairedNucleotides =
                    if (turns[k].atHighEnd) highRimNucleotides else lowRimNucleotides,
                kuhnLength = kuhnLength,
                contourPerNucleotide = contourPerNucleotide,
                thermalEnergy = thermalEnergy
            )
        }
    }

    /** Every span the turns take, decided coarser than the arithmetic's own noise. */
    val distinctSpans: List<Double> get() = anchorCensus.distinctSpans

    /** The one span every turn takes, or `null` where they take more than one. */
    val singleValuedSpan: Double? get() = anchorCensus.singleValuedSpan

    /** The shortest span any turn takes, in nm. */
    val minimumSpan: Double get() = spans.min()

    /** The longest, in nm. */
    val maximumSpan: Double get() = spans.max()

    /** Their arithmetic mean, in nm. */
    val meanSpan: Double get() = spans.average()

    /**
     * How many turns fall inside the **aligned half** of `C-0147`'s bracket, `span < d`.
     *
     * `T-304`'s own criterion, restated here so that the two studies count the same thing.
     */
    val turnsInsideTheAlignedHalf: Int get() = spans.count { it < interhelicalDistance }

    /** The largest tension any chain carries, in pN. */
    val maximumTension: Double get() = states.maxOf { it.tension }

    /** The mean tension over the 59 chains, in pN. */
    val meanTension: Double get() = states.map { it.tension }.average()

    /**
     * The 59 tethers as lattice elements, at the rim node each turn sits on.
     *
     * @param withPreload whether the chain's own tension enters as a load. `false` is the state
     *   every influence function must be taken at (`C-0104`).
     * @param stiffness an override applied to **both** stiffnesses of every element. `null` keeps
     *   each chain's own; `0.0` makes the list inert in the stiffness matrix, which is what an
     *   influence bank over the tensions needs.
     */
    fun elements(
        nodesPerBeam: Int,
        withPreload: Boolean = true,
        stiffness: Double? = null
    ): List<HoneycombScaffoldTurnTether> {
        val ties = honeycombScaffoldTurnTies(block, nodesPerBeam, firstAxialSign)
        require(ties.size == states.size) { "the tie census and the state census must agree" }
        return ties.mapIndexed { k, tie ->
            val state = if (stiffness == null) states[k] else states[k].withStiffness(stiffness)
            HoneycombScaffoldTurnTether(
                lowerBeam = tie.lowerBeam,
                upperBeam = tie.upperBeam,
                node = tie.node,
                secantStiffness = state.secantStiffness,
                tangentStiffness = state.tangentStiffness,
                tension = if (withPreload) states[k].tension else 0.0
            )
        }
    }

    /**
     * The same list carried at **zero** stiffness with a **unit** tension at [turnIndex] alone —
     * one column of the influence bank `C-0104`'s linearity makes exact.
     */
    fun unitTensionElements(
        nodesPerBeam: Int,
        turnIndex: Int
    ): List<HoneycombScaffoldTurnTether> {
        require(turnIndex in turns.indices) {
            "turnIndex must be within ${turns.indices}, was: $turnIndex"
        }
        val ties = honeycombScaffoldTurnTies(block, nodesPerBeam, firstAxialSign)
        return ties.mapIndexed { k, tie ->
            HoneycombScaffoldTurnTether(
                lowerBeam = tie.lowerBeam,
                upperBeam = tie.upperBeam,
                node = tie.node,
                secantStiffness = 0.0,
                tangentStiffness = 0.0,
                tension = if (k == turnIndex) 1.0 else 0.0
            )
        }
    }

    /**
     * The block's grillage at this raster's own row length, carrying the 59 per-turn tethers.
     *
     * Built the way [honeycombTetheredLattice] builds its states — one bare lattice to read
     * `nodesPerBeam` off, then one carrying the elements — so nothing but the turn element can
     * differ between the states of a comparison.
     */
    @Suppress("LongParameterList")
    fun lattice(
        enhancement: Double,
        withPreload: Boolean = true,
        stiffness: Double? = null,
        subdivisions: Int = 1,
        linkStiffness: Double = HoneycombGrillage.RIGID_LINK_STIFFNESS,
        tethers: (Int) -> List<HoneycombScaffoldTurnTether> = {
            elements(it, withPreload, stiffness)
        }
    ): HoneycombGrillage {
        fun build(elements: List<HoneycombScaffoldTurnTether>) = HoneycombGrillage(
            block = block,
            rowBasePairs = pairedRowBasePairs,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            hingeStiffnessEnhancement = enhancement,
            subdivisions = subdivisions,
            linkStiffness = linkStiffness,
            scaffoldTurnTethers = elements
        )
        val bare = build(emptyList())
        return build(tethers(bare.nodesPerBeam))
    }
}
