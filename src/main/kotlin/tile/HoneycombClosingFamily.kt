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
import kotlin.math.abs

/**
 * `T-245` — the **closing** two-length honeycomb x-rasters, and everything a selection inside
 * them needs.
 *
 * ## Why this file exists
 *
 * `C-0140` recommends **112 / 108 bp** on a stated rule whose filter — *"a stagger of at most
 * 4 bp"* — `CH-0187` shows is unstated, and `C-0148`/[HoneycombRasterResidues] then shows that
 * the recommended pair **cannot be drawn**: no lattice constant `b₀` serves its 59 raster
 * crossovers, and 10 of them would have to be forced. A selection has to be re-run inside the
 * family that *does* close, and this file supplies that family and its axes.
 *
 * ## The cheap bound, and it is exhaustive
 *
 * A raster crossover's reduced residue is `(level − 7·class) mod 21`, every level is an integer
 * combination of the two row lengths on a **fixed** class sequence, and the closure condition is
 * a statement about that residue set. So **closure depends on the two lengths only through their
 * residues modulo 21** — 441 cases, enumerable exactly — and [closingResiduePairs] is therefore a
 * complete answer rather than a search. That is asserted in the tests, not assumed here.
 *
 * ## Conventions
 *
 * `C-0140`'s throughout: axial positions are **integer base pairs on one global `z`**, residues
 * are `mod 21` and non-negative, lengths are in nm at [Gen1Tile.RISE_PER_BASE_PAIR], and the
 * counted face is the `+x` one.
 */

/** The scaffold M13mp18 affords, in nucleotides — `C-0109`'s figure, as `C-0140` uses it. */
const val HONEYCOMB_M13_NUCLEOTIDES: Int = 7249

/**
 * Every residue pair `(L₁ mod 21, L₂ mod 21)` whose `rasterRows × helicesPerRow` two-length
 * x-raster closes on caDNAno's default `±5 bp` scaffold-crossover rule.
 *
 * Exhaustive over the 441 pairs, and complete because closure carries no other dependence on the
 * lengths.
 */
fun closingResiduePairs(rasterRows: Int, helicesPerRow: Int): List<Pair<Int, Int>> {
    val period = HoneycombCrossoverRule.SAME_PAIR_PERIOD_BP
    val base = 5 * period
    return (0 until period).flatMap { one ->
        (0 until period).mapNotNull { two ->
            if (HoneycombRasterResidues(
                    rasterRows, helicesPerRow, base + one, base + two
                ).closes
            ) one to two else null
        }
    }
}

/**
 * The smallest `|L₁ − L₂|` any closing pair can carry.
 *
 * Where every closing pair shares one value of `L₁ − L₂ (mod 21)` — which is what the honeycomb
 * does — this is a **proof**, not a search: the difference is congruent to one residue, so its
 * least absolute representative bounds every realisation of it.
 */
fun minimumClosingStaggerBasePairs(rasterRows: Int, helicesPerRow: Int): Int {
    val period = HoneycombCrossoverRule.SAME_PAIR_PERIOD_BP
    val pairs = closingResiduePairs(rasterRows, helicesPerRow)
    require(pairs.isNotEmpty()) { "no residue pair closes on this cross-section" }
    return pairs.minOf { (one, two) ->
        val difference = Math.floorMod(one - two, period)
        minOf(difference, period - difference)
    }
}

/**
 * Every closing **length** pair with both lengths in `[minLength, maxLength]` and a stagger of at
 * most [maxStagger] base pairs.
 */
fun closingLengthPairs(
    rasterRows: Int,
    helicesPerRow: Int,
    minLength: Int,
    maxLength: Int,
    maxStagger: Int
): List<Pair<Int, Int>> {
    require(minLength > 0) { "minLength must be positive, was: $minLength" }
    require(maxLength >= minLength) { "maxLength must not be below minLength" }
    require(maxStagger >= 0) { "maxStagger must not be negative, was: $maxStagger" }
    val period = HoneycombCrossoverRule.SAME_PAIR_PERIOD_BP
    val closing = closingResiduePairs(rasterRows, helicesPerRow).toSet()
    return (minLength..maxLength).flatMap { one ->
        (minLength..maxLength).mapNotNull { two ->
            if (abs(one - two) <= maxStagger &&
                (Math.floorMod(one, period) to Math.floorMod(two, period)) in closing
            ) one to two else null
        }
    }
}

/**
 * One two-length raster, scored on every axis a selection needs.
 *
 * The **closure-dependent** fields ([classZeroResidue], [ladderPhaseBasePairs],
 * [stationsPerRow], [stationsOnFace], [sparsestRowStations]) are `null` where the raster does not
 * close — which is `CH-0189`'s whole point: where no `b₀` serves the design, no phase is
 * determined and a 21-phase sweep is a sweep over designs that need forced crossovers.
 */
class HoneycombRasterProfile(
    val rasterRows: Int,
    val helicesPerRow: Int,
    val senseOneBasePairs: Int,
    val senseTwoBasePairs: Int,
    val faceNormalX: Int = 1,
    val risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR
) {

    init {
        require(senseOneBasePairs > 0) {
            "senseOneBasePairs must be positive, was: $senseOneBasePairs"
        }
        require(senseTwoBasePairs > 0) {
            "senseTwoBasePairs must be positive, was: $senseTwoBasePairs"
        }
        require(risePerBasePair > 0.0) {
            "risePerBasePair must be positive, was: $risePerBasePair"
        }
    }

    private val residues = HoneycombRasterResidues(
        rasterRows, helicesPerRow, senseOneBasePairs, senseTwoBasePairs
    )

    /** How many raster crossovers the scaffold path carries. */
    val rasterCrossovers: Int = residues.rasterCrossovers

    /** Whether the raster closes on caDNAno's default scaffold rule. */
    val closes: Boolean = residues.closes

    /** The fewest raster crossovers a design at these lengths would have to FORCE. */
    val offRuleCrossovers: Int = residues.offRuleCrossovers

    /** `|L₁ − L₂|`, the inter-row axial stagger. */
    val staggerBasePairs: Int = abs(senseOneBasePairs - senseTwoBasePairs)

    /** The span every x-raster row carries — the **larger** of the two lengths (`C-0146`). */
    val rowSpanBasePairs: Int = residues.rowWindows.map { it.basePairs }.distinct().single()

    /** The block's own axial extent, which is the dimension §3 is owed for an object. */
    val blockExtentBasePairs: Int = residues.blockWindow.basePairs

    /** That extent in nm. */
    val blockExtentNm: Double = blockExtentBasePairs * risePerBasePair

    /** The row span in nm. */
    val rowSpanNm: Double = rowSpanBasePairs * risePerBasePair

    /** The window every **interface** offers a crossover column — `T-243`'s reading. */
    val interfaceWindowBasePairs: Int =
        residues.interfaceWindows.map { it.basePairs }.distinct().single()

    /** That window in nm. */
    val interfaceWindowNm: Double = interfaceWindowBasePairs * risePerBasePair

    /**
     * The nucleotides the scaffold spends on route A — an antiparallel crossover at every turn.
     *
     * Every interior helix spends its own span and the two path **ends**, whose turn sense is
     * undefined, are charged one of each length. That is `C-0140`'s own accounting, reproduced.
     */
    val scaffoldNucleotides: Int =
        residues.helixWindows.values.sumOf { it.basePairs } +
                senseOneBasePairs + senseTwoBasePairs

    /** What M13 has left over at these lengths, in nucleotides — negative where it does not fit. */
    val scaffoldSpareOnM13: Int = HONEYCOMB_M13_NUCLEOTIDES - scaffoldNucleotides

    /** Whether M13 affords route A here. */
    val fitsM13: Boolean get() = scaffoldNucleotides <= HONEYCOMB_M13_NUCLEOTIDES

    /**
     * The unpaired nucleotides **per helix** M13 still affords at these lengths — `C-0147`'s own
     * quantity, the slack a turn could be given if it turned on a loop instead of on a crossover.
     *
     * `C-0147` measures it at **8 nt** for a uniform 112 bp row (`60 × (112 + L) ≤ 7 249`), and
     * it is per **helix** rather than per turn because the built blocks divide their allowance
     * into *"front and rear unpaired loop fragments at the ends of each helix"*.
     */
    val unpairedNucleotidesPerHelixOnM13: Int = scaffoldSpareOnM13 / (rasterRows * helicesPerRow)

    private val levels: Map<Int, Int> = residues.crossoverLevels

    /** The front face's axial raggedness — the spread of the **even** crossover levels. */
    val frontFaceRaggednessBasePairs: Int = levels.filterKeys { Math.floorMod(it, 2) == 0 }
        .values.let { it.max() - it.min() }

    /** The rear face's axial raggedness — the spread of the **odd** crossover levels. */
    val rearFaceRaggednessBasePairs: Int = levels.filterKeys { Math.floorMod(it, 2) == 1 }
        .values.let { it.max() - it.min() }

    /** The front-face relief in nm — `CH-0187`'s third and fourth axes read this. */
    val frontFaceRaggednessNm: Double = frontFaceRaggednessBasePairs * risePerBasePair

    /** The rear-face relief in nm. */
    val rearFaceRaggednessNm: Double = rearFaceRaggednessBasePairs * risePerBasePair

    /** The lattice constant `b₀` the rule determines, or `null` where the raster does not close. */
    val classZeroResidue: Int? = residues.classZeroResidueCandidates.firstOrNull()

    /** The inter-row ladder offset — `C-0148`'s 14 bp, and it needs no `b₀`. */
    val interRowOffsetBasePairs: Int = residues.interRowOffsetBasePairs(faceNormalX)

    /** The determined ladder phase from the block's low plane, or `null` where none is. */
    val ladderPhaseBasePairs: Int? =
        if (closes) residues.determinedLadderPhaseBasePairs(faceNormalX) else null

    /** The stations each face row carries at the determined phase, or `null`. */
    val stationsPerRow: List<Int>? =
        if (closes) residues.stationsPerRow(faceNormalX) else null

    /** The face's station count at the determined phase, or `null`. */
    val stationsOnFace: Int? = stationsPerRow?.sum()

    /** The sparsest row's station count — the ceiling on a placement's column count. */
    val sparsestRowStations: Int? = stationsPerRow?.min()

    /** The stations the face would carry if every row were full. */
    val stationsAtSaturation: Int = rasterRows * (rowSpanBasePairs / 21 + 1)
}

/** [HoneycombRasterProfile] of an `m × n` block at [senseOne] / [senseTwo] base pairs. */
fun honeycombRasterProfile(
    rasterRows: Int,
    helicesPerRow: Int,
    senseOne: Int,
    senseTwo: Int,
    faceNormalX: Int = 1
): HoneycombRasterProfile = HoneycombRasterProfile(
    rasterRows, helicesPerRow, senseOne, senseTwo, faceNormalX
)
