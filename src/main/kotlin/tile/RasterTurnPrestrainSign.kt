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

import com.xemantic.nano.plentyofroom.structure.honeycombRasterTurns
import com.xemantic.nano.plentyofroom.structure.honeycombXRasterPath

/**
 * `T-284` — the **sign** of a raster turn's `8.57142857°` departure, derived on the lattice.
 *
 * ## The premise, read as a congruence rather than as a magnitude
 *
 * `C-0152` §5 fixes the magnitude and, in the same sentence, says where the sign lives: caDNAno's
 * *"five base pairs, or half a turn"* is an integer approximation to **5.25** bp, so an allowed
 * scaffold crossover sits a quarter of a base pair off the line of centres — *"on either side"*.
 * A crossover placed `+5 bp` from its pair's staple position falls **0.25 bp short** of the exact
 * downstream half turn; one placed `−5 bp` sits **0.25 bp past** the exact upstream one. Equal
 * magnitude, opposite sign.
 *
 * Which one a turn takes is not a convention. `C-0148`'s closure condition — modelled here as
 * [HoneycombRasterResidues.reducedResidues] — requires every raster crossover's
 * `(level − 7·class) mod 21` to equal `b₀ ± 5` for **one** lattice constant `b₀`, and where two
 * distinct residues appear they are `10` apart, which pins `b₀` **uniquely** (`+10` and
 * `−10 ≡ 11` are different residues modulo 21). Each crossover's own residue then says which side
 * of its staple position it is on, and therefore the sign of its departure.
 *
 * So `C-0175` §8's *"the sign … is fixed by no source in this repository"* is true of the
 * **global** phase and false of the other `58` binaries: on a **closing** raster the `2^59`
 * assignments collapse to **two**.
 *
 * ## The second derivation, which is a cross-check and not a repetition
 *
 * `C-0136`'s per-helix rule says the same thing one level up. A helix's length is the difference
 * of its two crossover levels, so `(L − 7Δ_eff) mod 21 ∈ {0, 10, 11}`: residue **0** carries the
 * sign **through** the helix and **10** or **11** **flip** it. The two constructions must agree
 * step for step — [perHelixLengthResidues] against the differences of [reducedResidues] — and
 * that agreement is asserted rather than assumed.
 *
 * ## What this does NOT fix
 *
 * The departure is an azimuth, and displacing a crossover rotates **both** backbones in the
 * **same** sense (`ForcedCrossoverPrice`'s own header). The model's tie prestrain is a
 * **relative** roll, `Φ_upper − Φ_lower`, so mapping a derived departure onto it needs a sense
 * this lattice cannot supply — which is the one global binary [ties] takes as its `phase`
 * argument, and it is why `T-284` grades both.
 *
 * Angles in **degrees** at the API and radians only where a tie is built; base-pair levels are
 * integers on one global `z`, `C-0140`'s convention.
 */

/** The exact half turn in base pairs at caDNAno's honeycomb twist — `21 / 4`, not `5`. */
const val EXACT_HALF_TURN_BASE_PAIRS: Double = 21.0 / 4.0

/**
 * The azimuthal departure, in degrees, a scaffold crossover carries when it is placed
 * [basePairs] from its pair's staple position instead of at the exact half turn on that side.
 *
 * `+5` is `0.25 bp` **short** of `+5.25`, so its departure is **negative**; `−5` is `0.25 bp`
 * past `−5.25`, so its departure is **positive**. Both are
 * [allowedScaffoldCrossoverDepartureDegrees] in magnitude, which is what makes the sign the only
 * thing left to derive.
 */
fun scaffoldDisplacementDepartureDegrees(basePairs: Int): Double {
    require(basePairs != 0) { "a scaffold crossover is displaced from its staple position" }
    val side = if (basePairs > 0) 1.0 else -1.0
    return (basePairs - side * EXACT_HALF_TURN_BASE_PAIRS) * AZIMUTH_PER_BASE_PAIR
}

/** One raster turn's derived displacement and the departure that follows from it. */
data class RasterTurnSign(

    /** The turn's position along the raster path, `0` until `helices − 1`. */
    val index: Int,

    /** `(level − 7·class) mod 21` at this crossover. */
    val reducedResidue: Int,

    /** `+5` or `−5`: which side of its pair's staple position this crossover sits on. */
    val displacementBasePairs: Int,

    /** Whether the turn sits at the block's high axial rim, `s = +L/2`. */
    val atHighEnd: Boolean,

    /** The azimuthal departure in degrees the displacement implies. */
    val departureDegrees: Double
)

/**
 * The derived sign of every raster turn of an `m × n` honeycomb block at a two-length raster.
 *
 * @param firstAxialSign the direction the scaffold traverses the first helix in.
 * @param mirrored the cross-section reflected, `x → −x` — the free viewing convention.
 * @param axialReversed the axial datum negated. Alone it is **improper**; composed with
 *   [mirrored] it is the proper rotation about the in-plane axis (`CLAUDE.md`). The departure is
 *   invariant under it because the datum's handedness travels with the level.
 */
class HoneycombRasterTurnSigns(
    val block: HoneycombBlock,
    val senseOneBasePairs: Int,
    val senseTwoBasePairs: Int,
    val firstAxialSign: Int = 1,
    val mirrored: Boolean = false,
    val axialReversed: Boolean = false
) {

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

    /**
     * `+1` on the standard axial datum and `−1` on the reversed one.
     *
     * [scaffoldDisplacementDepartureDegrees] is written at `AZIMUTH_PER_BASE_PAIR`, which is
     * `+240/7°` **per base pair of increasing `z`**. Reversing `z` reverses the handedness a
     * residue is read with, so the constant reverses with it — *"a residue map is a handedness,
     * so it must be reversed whenever `z` is"* (`CLAUDE.md`). Both the displacement and the
     * constant flip, so the **departure** is invariant under the reversal and only the
     * displacement's own label moves. The first run of `T-284`'s `F3` test is what found this.
     */
    private val datumSign: Int = if (axialReversed) -1 else 1

    /** `(level − 7·class) mod 21` at every raster crossover — `C-0148`'s own quantity. */
    val reducedResidues: List<Int> = residues.reducedResidues

    /** Every class-zero residue `b₀` that admits the whole raster. */
    val classZeroResidueCandidates: List<Int> = residues.classZeroResidueCandidates

    /** Whether the raster closes on caDNAno's default scaffold rule. */
    val closes: Boolean = residues.closes

    /**
     * The one `b₀` the raster admits.
     *
     * Two distinct reduced residues pin it uniquely, because they are `10` apart and `+10` and
     * `−10 ≡ 11` are different residues modulo 21. A raster carrying **one** residue leaves two
     * candidates and a raster that does not close leaves none; both refuse rather than guess.
     */
    val classZeroResidue: Int
        get() {
            check(classZeroResidueCandidates.size == 1) {
                "the raster admits ${classZeroResidueCandidates.size} class-zero residues, so " +
                        "no per-turn displacement is determined: $classZeroResidueCandidates"
            }
            return classZeroResidueCandidates.single()
        }

    /** The derived displacement and departure of every turn. */
    val signs: List<RasterTurnSign> by lazy {
        val b0 = classZeroResidue
        val plus = Math.floorMod(b0 + HoneycombCrossoverRule.SCAFFOLD_OFFSET_BP, PERIOD)
        val minus = Math.floorMod(b0 - HoneycombCrossoverRule.SCAFFOLD_OFFSET_BP, PERIOD)
        check(turns.size == reducedResidues.size) {
            "the tie list carries ${turns.size} turns and the residue walk " +
                    "${reducedResidues.size}; they must index the same crossovers"
        }
        reducedResidues.mapIndexed { k, residue ->
            val displacement = when (residue) {
                plus -> HoneycombCrossoverRule.SCAFFOLD_OFFSET_BP
                minus -> -HoneycombCrossoverRule.SCAFFOLD_OFFSET_BP
                else -> error("residue $residue at turn $k is neither $plus nor $minus")
            }
            RasterTurnSign(
                index = k,
                reducedResidue = residue,
                displacementBasePairs = displacement,
                atHighEnd = turns[k].atHighEnd,
                departureDegrees = datumSign * scaffoldDisplacementDepartureDegrees(displacement)
            )
        }
    }

    /** Whether consecutive turns take opposite displacements — every helix flipping the sign. */
    val isAlternating: Boolean
        get() = signs.zipWithNext().all { (a, b) ->
            a.displacementBasePairs != b.displacementBasePairs
        }

    /**
     * The displacement every turn at the block's **high** axial rim takes.
     *
     * It refuses unless the rim decides the displacement outright — which on a raster whose every
     * helix flips the sign it does, because the rim alternates with the turn index too.
     */
    val highRimDisplacementBasePairs: Int
        get() {
            val high = signs.filter { it.atHighEnd }.map { it.displacementBasePairs }.distinct()
            val low = signs.filterNot { it.atHighEnd }.map { it.displacementBasePairs }.distinct()
            check(high.size == 1 && low.size == 1 && high.single() == -low.single()) {
                "the axial rim does not decide the displacement: high $high, low $low"
            }
            return high.single()
        }

    /**
     * `C-0136`'s per-helix residue `(L − 7Δ_eff) mod 21`, one per **interior** helix.
     *
     * `0` carries the sign through the helix; `10` and `11` flip it. It is the same statement as
     * the difference of two consecutive [reducedResidues], derived from the row lengths instead
     * of from the levels, which is why [alternationAgreesWithLengths] can compare them.
     */
    val perHelixLengthResidues: List<Int> =
        honeycombRasterTurns(
            honeycombXRasterPath(block.rasterRows, block.helicesPerRow, mirrored), firstAxialSign
        ).map {
            val length =
                if (it.effectiveSense == 1) senseOneBasePairs else senseTwoBasePairs
            Math.floorMod(
                length - HoneycombCrossoverRule.ANY_AZIMUTH_STEP_BP * it.effectiveSense, PERIOD
            )
        }

    /**
     * Whether the two derivations agree helix for helix: a per-helix residue of `0` carries the
     * crossover residue through and anything else moves it by `±10`.
     */
    val alternationAgreesWithLengths: Boolean
        get() = perHelixLengthResidues.indices.all { k ->
            val step = Math.floorMod(reducedResidues[k + 1] - reducedResidues[k], PERIOD)
            step in ADMISSIBLE_STEPS && (step == 0) == (perHelixLengthResidues[k] == 0)
        }

    /**
     * The raster's turns as ties carrying the **derived** assignment.
     *
     * @param phase the one binary the lattice does not fix — which way the derived departure maps
     *   onto the model's relative roll `Φ_upper − Φ_lower`. `+1` puts a positive prestrain on the
     *   turns whose departure is positive.
     * @param departureDegrees the magnitude, `C-0152`'s allowed one by default; `0.0` returns the
     *   pure-stiffness tie list [honeycombScaffoldTurnTies] gives.
     */
    fun ties(
        nodesPerBeam: Int,
        phase: Int,
        departureDegrees: Double = allowedScaffoldCrossoverDepartureDegrees()
    ): List<HoneycombScaffoldTurnTie> {
        require(phase == 1 || phase == -1) { "phase must be +1 or -1, was: $phase" }
        require(departureDegrees.isFinite() && departureDegrees >= 0.0) {
            "departureDegrees must be a finite magnitude, was: $departureDegrees"
        }
        val bare = honeycombScaffoldTurnTies(block, nodesPerBeam, firstAxialSign)
        val assignment = signs
        // a zero magnitude returns the pure-stiffness list ITSELF rather than a scaled copy:
        // `phase * 0.0 * (-x)` is `-0.0`, and a data class compares Doubles with `equals`.
        if (departureDegrees == 0.0) return bare
        check(bare.size == assignment.size) {
            "the tie list and the derived assignment must be the same length"
        }
        val scale = departureDegrees / allowedScaffoldCrossoverDepartureDegrees()
        return bare.mapIndexed { k, tie ->
            tie.copy(
                prestrainRadians =
                    phase * scale * Math.toRadians(assignment[k].departureDegrees)
            )
        }
    }

    private companion object {
        const val PERIOD: Int = HoneycombCrossoverRule.SAME_PAIR_PERIOD_BP
        val ADMISSIBLE_STEPS: Set<Int> = setOf(0, 10, 11)
    }
}
