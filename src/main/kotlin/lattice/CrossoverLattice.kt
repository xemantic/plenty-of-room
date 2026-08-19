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

package com.xemantic.nano.plentyofroom.lattice

import com.xemantic.nano.plentyofroom.tile.HoneycombLattice

/** B-DNA's own helical twist, 10.5 bp per turn — the twist a lattice's design twist departs from. */
const val B_DNA_BASES_PER_TURN: Double = 10.5

/** `360 / 10.5` = 34.2857…° per base pair. */
const val B_DNA_DEGREES_PER_BASE_PAIR: Double = 360.0 / B_DNA_BASES_PER_TURN

/**
 * One interface over both crossover lattices this programme uses.
 *
 * ## Why it exists
 *
 * This repository's **placement** machinery is lattice-generic and took the honeycomb unmodified;
 * its **site generators** are not. `CrossoverLayout`'s two parities, `upwardRootLattice` and
 * `centroSymmetricUpwardPhases` all assume four azimuths at 8 bp with a 32 bp period, and nothing
 * in the type system says so — which is how a late honeycomb correction invalidated a corpus of
 * placement results instead of re-running them.
 *
 * This interface holds the part that **is** shared, and makes each lattice declare the part that is
 * not. It deliberately holds no geometry: an interhelical distance, a row pitch and a layer pitch
 * are properties of a *cross-section* (`HoneycombCrossSectionGeometry`, `Gen1Tile`) and not of the
 * crossover lattice.
 *
 * ## The two laws that only become visible from here
 *
 * **The period is a product.** `samePairPeriod = azimuths × step`: 4 × 8 = 32 on the square lattice,
 * 3 × 7 = 21 on the honeycomb. Both numbers are in print separately and the identity is in neither.
 *
 * **The step is what it is because it lands on the neighbouring azimuth.** `8 bp × 33.75°/bp = 270°`
 * is one azimuth backwards on a four-azimuth lattice, and `7 bp × 34.2857°/bp = 240°` is one azimuth
 * backwards on a three-azimuth one. So the step is *derivable*, not a measured constant — and the
 * register departure, which is the whole of the twist-correction question, falls straight out of it:
 * exactly zero on the honeycomb (21 bp is two turns of B-DNA) and −17.14° per period on the square
 * sheet.
 */
interface CrossoverLattice {

    /** How this lattice is named in a result file. */
    val name: String

    /** Neighbours a helix has in a fully occupied lattice, hence crossover azimuths. */
    val azimuths: Int

    /** Base pairs between consecutive crossover positions on a helix, over **all** azimuths. */
    val anyAzimuthStepBasePairs: Int

    /** The helical twist the lattice is **drawn** at, which need not be B-DNA's. */
    val designBasesPerTurn: Double

    /**
     * Whether this repository has a congruence that returns a lattice's centro-symmetric phases.
     *
     * `centroSymmetricUpwardPhases` assumes row `r` and row `D−1−r` share a sublattice. That holds
     * on the square sheet and **fails** on a honeycomb face, where the 7 bp stagger between adjacent
     * station rows is forced and the existence of a symmetric family is decided by the rooting-helix
     * parity instead (`C-0141`). A caller that asks before reusing a phase result cannot silently
     * inherit the wrong lattice's.
     */
    val hasCentroSymmetricPhaseCongruence: Boolean

    /** Base pairs before the **same** adjacent pair comes round again — one azimuth's period. */
    val samePairPeriodBasePairs: Int get() = azimuths * anyAzimuthStepBasePairs

    /** `360 / azimuths`: the azimuths are equally spaced by construction of the lattice. */
    fun azimuthSeparationDegrees(): Double = 360.0 / azimuths

    /** The design twist, per base pair. */
    fun designDegreesPerBasePair(): Double = 360.0 / designBasesPerTurn

    /** How far one crossover step advances the helical phase, unfolded. */
    fun stepAdvanceDegrees(): Double = anyAzimuthStepBasePairs * designDegreesPerBasePair()

    /**
     * The same advance folded into `(−180, 180]` — one azimuth separation, in one direction or the
     * other, on any lattice whose step is the step **because** it reaches the next neighbour.
     */
    fun azimuthAdvanceDegrees(): Double {
        var folded = stepAdvanceDegrees() % 360.0
        if (folded > 180.0) folded -= 360.0
        if (folded < -180.0) folded += 360.0
        return folded
    }

    /**
     * How far the drawn lattice departs from B-DNA over [basePairs], in degrees.
     *
     * Negative is **undertwisted** — the lattice draws less twist than the duplex wants. The
     * departure is linear in the length and single-signed, which is why it accumulates along a row
     * rather than cancelling: Rothemund says so in as many words, and `C-0107`'s boundary layer is
     * what limits it.
     */
    fun registerDepartureDegrees(basePairs: Int): Double =
        basePairs * (designDegreesPerBasePair() - B_DNA_DEGREES_PER_BASE_PAIR)

    /** The departure over one azimuth's period — **exactly zero** iff the lattice is B-DNA's. */
    fun registerDepartureDegreesPerPeriod(): Double =
        registerDepartureDegrees(samePairPeriodBasePairs)

    /**
     * The offsets along a row at which **one azimuth** offers a crossover.
     *
     * The ladder steps by [samePairPeriodBasePairs] and never by [anyAzimuthStepBasePairs]: an
     * attachment roots on one azimuth, so the 7 bp step is the wrong number for it by a factor of
     * three, and the 8 bp step by a factor of four.
     */
    fun stationLadder(rowBasePairs: Int, phaseBasePairs: Int): List<Int> {
        require(rowBasePairs > 0) { "rowBasePairs must be positive, was: $rowBasePairs" }
        require(phaseBasePairs > -1 && phaseBasePairs < samePairPeriodBasePairs) {
            "phaseBasePairs must lie in [0, $samePairPeriodBasePairs), was: $phaseBasePairs — " +
                "a phase outside the period is folded by the lattice and is almost always a " +
                "caller that has confused the phase with an offset"
        }
        return generateSequence(phaseBasePairs) { it + samePairPeriodBasePairs }
            .takeWhile { it < rowBasePairs }
            .toList()
    }
}

/**
 * caDNAno's **square** lattice: four azimuths at 8 bp, drawn at `32/3` bp per turn.
 *
 * Ke et al. (*JACS* **131**:15903) state the geometry verbatim: four neighbours, crossover planes
 * every 8 bp, the same adjacent pair every 32 bp, and `8 bp × 33.75° = 270°` exactly — so the two
 * azimuths a single-layer sheet does **not** occupy point out of its plane.
 */
object SquareCrossoverLattice : CrossoverLattice {

    override val name: String = "square"
    override val azimuths: Int = 4
    override val anyAzimuthStepBasePairs: Int = 8

    /** `32/3` = 10.666…, which is what makes 8 bp exactly 270° — not the rounded 10.67. */
    override val designBasesPerTurn: Double = 32.0 / 3.0

    override val hasCentroSymmetricPhaseCongruence: Boolean = true

    /**
     * Base pairs between crossovers on **one interface** of a single-layer sheet.
     *
     * A sheet occupies two of the four azimuths, and they alternate between the helix's two
     * neighbours — so a given adjacent pair is linked every 32 bp while the helix carries a
     * crossover every 16. This is the number `CLAUDE.md` warns is not the per-helix one.
     */
    const val SHEET_DOMAIN_BASE_PAIRS: Int = 16

    /**
     * The departure a single 16 bp sheet domain carries: **−8.571°**, and every domain of a row
     * carries the same sign, which is why the error accumulates.
     */
    fun registerDepartureDegreesPerDomain(): Double =
        registerDepartureDegrees(SHEET_DOMAIN_BASE_PAIRS)
}

/**
 * caDNAno's **honeycomb** lattice: three azimuths at 7 bp, drawn at B-DNA's own 10.5 bp per turn.
 *
 * Douglas et al. (*NAR* **37**:5001) state both numbers in one sentence — *"potential
 * staple-crossover positions occur every seven base pairs"* … *"which repeat every 21 base pairs"* —
 * and they are not interchangeable: an attachment roots on one azimuth, so its ladder is the 21.
 *
 * The constants are **taken from** [HoneycombLattice] rather than restated, so the two cannot drift.
 */
object HoneycombCrossoverLattice : CrossoverLattice {

    override val name: String = "honeycomb"
    override val azimuths: Int = HoneycombLattice.AZIMUTHS
    override val anyAzimuthStepBasePairs: Int = HoneycombLattice.ANY_AZIMUTH_STEP_BP

    /** 10.5 — the honeycomb is drawn at B-DNA's own twist, which is why it has none to correct. */
    override val designBasesPerTurn: Double = B_DNA_BASES_PER_TURN

    override val hasCentroSymmetricPhaseCongruence: Boolean = false
}

/** Both lattices, for a study that wants to run a rule against every one this project knows. */
val crossoverLattices: List<CrossoverLattice> =
    listOf(SquareCrossoverLattice, HoneycombCrossoverLattice)

/** The lattice a design file's grid name refers to, or `null` if this project has none for it. */
fun crossoverLatticeOfGrid(grid: String): CrossoverLattice? = when (grid.lowercase()) {
    "square" -> SquareCrossoverLattice
    "honeycomb" -> HoneycombCrossoverLattice
    else -> null
}
