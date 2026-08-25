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

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

/**
 * `T-322` — route B's own buildable widths, graded **coupled**, on stations derived at each row
 * length.
 *
 * ## Why this file exists
 *
 * `C-0211` graded route B's uniform raster **free** at `C-0208`'s resolved per-bond link and found
 * it flat at `756 of 756`. Every **coupled** number in this repository — `C-0167`'s 64 cells,
 * `C-0180`'s, `C-0205`'s, `C-0208`'s and `C-0212`'s 32 — is read on a different tile: the `116 bp`
 * block extent of the drawable `102 / 109` two-length raster, with 59 covalent **ties** rather than
 * 59 tethers, 435 staple bonds rather than route B's `358 / 385 / 410` (`CH-0270`), and a station
 * ladder that carries **six** stations per rooting helix where route B's rows carry **five**.
 *
 * Nothing here is a new mechanics. What is new is the three things a coupled census read on the
 * **wrong** tile silently inherits, each of which is a function of the row length and each of which
 * is therefore **derived** here rather than transferred:
 *
 * 1. [RouteBStationLadder] — the station set. `C-0141`'s ladder is `phase + 21k ≤ L`, and its phase
 *    is fixed by caDNAno's `±5 bp` rule only where the raster **closes**; route B's uniform rows
 *    close at no phase, so the phase is a free design variable and `T-316`'s inherited `16` is a
 *    *route-A* number.
 * 2. [TransferredRatioBand] — the cheap bound, which is a ratio transferred between two lattices
 *    and is therefore a **prediction** with its own falsifier rather than a theorem.
 * 3. [routeBUncoupledReferences] — the uncoupled reference, read out of `C-0211`'s **committed**
 *    cells rather than re-swept, so that every coupled cell is graded against the very reading the
 *    free-tile recommendation was made on.
 *
 * [RouteBAdmissibility] is the fourth and it is not about the tile at all: `CH-0272` records that a
 * verdict block reporting three thresholds separately reports no **conjunction**, so the
 * conjunction is a type here rather than a sentence.
 *
 * Lengths in nm, stiffnesses in pN/nm, dishing dimensionless as a fraction of the free-tile stroke;
 * base pairs are integers and angles appear nowhere.
 */

// ------------------------------------------------------------------------ the station ladder

/**
 * The attachment station ladder a route-B row of [rowBasePairs] offers on a face of
 * [rootingHelices] rooting helices.
 *
 * The ladder itself is [honeycombStationLattice]'s — **consumed rather than re-derived**, because
 * re-deriving a lattice is how two claims end up disagreeing about the same face. What this class
 * adds is the one question route B forces and route A never had to ask: **at which phase**, the
 * `±5 bp` rule having no purchase on a raster that does not close.
 *
 * The rule is stated before it is used: the **smallest** phase in `[0, periodBasePairs)` maximising
 * the **minimum** station count over the rooting helices, ties to the earlier phase
 * (`CLAUDE.md`: *decide coarser than the noise, earlier candidate wins ties*). The minimum over
 * rows and not the mean, because [honeycombSnappedGrid] **refuses** a placement wider than a row's
 * own ladder — that is a change of the path **count** wearing a change of position.
 *
 * @param interRowOffsetBasePairs `C-0141`'s **forced** inter-row stagger, 7 or 14 bp; there is no
 *   honeycomb face whose station rows are in register.
 */
class RouteBStationLadder(
    val rowBasePairs: Int,
    val rootingHelices: Int,
    val interRowOffsetBasePairs: Int = HoneycombLattice.ANY_AZIMUTH_STEP_BP,
    val periodBasePairs: Int = HoneycombLattice.SAME_PAIR_PERIOD_BP
) {

    init {
        // The messages NAME this class, and that is not decoration.  `honeycombStationLattice`
        // carries the same two requirements verbatim and is reached from `derivedPhase`'s own
        // initialiser, so a widened guard here still throws -- from downstream, with the
        // downstream message.  `C-0207` section 8: a guard whose only observable behaviour is
        // duplicated downstream is a guard no mutation of it can reach.  The one word that
        // differs is what makes it reachable, and the fixture asserts it.
        require(rowBasePairs > 0) {
            "a route-B station ladder needs a positive rowBasePairs, was: $rowBasePairs"
        }
        require(rootingHelices >= 1) {
            "a route-B station ladder needs at least one rooting helix, was: $rootingHelices"
        }
        require(interRowOffsetBasePairs >= 0) {
            "the inter-row stagger must not be negative, was: $interRowOffsetBasePairs"
        }
        require(periodBasePairs > 0) { "periodBasePairs must be positive, was: $periodBasePairs" }
    }

    /** How many stations each rooting helix carries at [phase], in row order. */
    fun stationsAtPhase(phase: Int): List<Int> {
        require(phase in 0 until periodBasePairs) {
            "a ladder phase lives in [0, $periodBasePairs), was: $phase"
        }
        return honeycombStationLattice(
            rootingHelices, rowBasePairs, phase, interRowOffsetBasePairs,
            periodBasePairs = periodBasePairs
        ).map { it.size }
    }

    /** The fewest any row carries at [phase] — the column count a placement is bounded by. */
    fun minimumStationsAtPhase(phase: Int): Int = stationsAtPhase(phase).min()

    /** The smallest phase maximising [minimumStationsAtPhase], ties to the earlier phase. */
    val derivedPhase: Int = (0 until periodBasePairs).maxByOrNull { phase ->
        // `maxByOrNull` keeps the FIRST maximum, which is the tie-break the rule states.
        minimumStationsAtPhase(phase)
    }!!

    /** The fewest stations any row carries at [derivedPhase]. */
    val minimumStationsPerRow: Int = minimumStationsAtPhase(derivedPhase)

    /** The most station columns this row can carry, at any phase. */
    val maximumColumns: Int get() = minimumStationsPerRow

    /** Whether a [columns]-column placement stands on every row's ladder at [phase]. */
    fun carriesColumnsAtPhase(columns: Int, phase: Int): Boolean {
        require(columns > 0) { "columns must be positive, was: $columns" }
        return columns <= minimumStationsAtPhase(phase)
    }

    /** Whether a [columns]-column placement stands on every row's ladder at [derivedPhase]. */
    fun carriesColumns(columns: Int): Boolean = carriesColumnsAtPhase(columns, derivedPhase)
}

// ------------------------------------------------------------------- the cheap bound, band form

/**
 * A ratio measured on one lattice and applied to another — `T-322`'s cheap bound 2.
 *
 * `C-0212`'s 32 cells carry `searchedP90 / uncoupledDishing` over a measured range, and applying it
 * to `C-0211`'s committed uncoupled readings predicts route B's coupled reading before a station is
 * placed. It is a **prediction and not a theorem**: the two lattices differ in bond census, station
 * ladder, tile width, interior pressure, dropout field and turn topology, which is precisely the
 * class of transfer this task exists to test. So the band carries [RouteBPrediction.contains], and
 * a miss is a *measurement* of how much of `C-0212` is a property of its own tile rather than a
 * defect of either study.
 */
class TransferredRatioBand(val low: Double, val high: Double) {

    init {
        require(low > 0.0 && low.isFinite()) { "the band's low end must be positive, was: $low" }
        require(high >= low && high.isFinite()) {
            "the band's high end must not fall below its low end, were: $low and $high"
        }
    }

    /** What this band predicts a coupled reading to be, given [uncoupled] and a [threshold]. */
    fun predict(uncoupled: Double, threshold: Double): RouteBPrediction {
        require(uncoupled > 0.0 && uncoupled.isFinite()) {
            "the uncoupled reading must be positive, was: $uncoupled"
        }
        require(threshold > 0.0 && threshold.isFinite()) {
            "the threshold must be positive, was: $threshold"
        }
        return RouteBPrediction(low * uncoupled, high * uncoupled, threshold)
    }
}

/** One predicted band against one threshold — exactly one of its three verdicts is true. */
class RouteBPrediction(val low: Double, val high: Double, val threshold: Double) {

    /** The whole band is above the threshold, so the bound EXCLUDES a flat reading. */
    val excludesFlat: Boolean = low > threshold

    /** The whole band is below it, so every reading in the band is flat. */
    val guaranteesFlat: Boolean = high < threshold

    /** The band contains the threshold, so the bound cannot decide — and says so. */
    val straddles: Boolean = !excludesFlat && !guaranteesFlat

    /** Whether a measured reading falls inside the band — `F20`'s own predicate. */
    fun contains(value: Double): Boolean = value >= low && value <= high
}

// ------------------------------------------------------- the uncoupled reference, from C-0211

/**
 * One route-B width's **uncoupled** reference, read out of `C-0211`'s committed cells.
 *
 * `CLAUDE.md`: *always run the uncoupled tile as the reference.* On route B that reference is not
 * a free parameter — `C-0211` has already graded the free tile at 756 cells, and the reading a
 * coupled cell must be judged against is the one its own recommendation was made on: the
 * recommended lattice constant `b₀`, and at that constant the **worst** of the twelve chain
 * corners, which is what the minimax the recommendation is taken over means.
 */
class RouteBUncoupledReference(
    val pairedRowBasePairs: Int,
    val classZeroResidue: Int,
    val chain: String,
    val kuhnLength: Double,
    val contourPerNucleotide: Double,
    val freeTileWithPreload: Double
)

private fun routeBBestRecords(file: File, rung: String): List<Map<String, String>> {
    require(file.exists()) { "C-0211's result file is missing: " + file.path }
    val records = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("best").jsonArray
        .map { record -> record.jsonObject.mapValues { it.value.jsonPrimitive.content } }
        .filter { it.getValue("rung") == rung }
    require(records.isNotEmpty()) {
        "C-0211's result file carries no `best` record at the rung named \"" + rung + "\""
    }
    return records.sortedBy { it.getValue("pairedRowBasePairs").toInt() }
}

/** `C-0211`'s own `bestWorstCornerDishing` at [rung], keyed on the paired row length. */
fun routeBPublishedBestWorstCorner(file: File, rung: String): Map<Int, Double> =
    routeBBestRecords(file, rung).associate {
        it.getValue("pairedRowBasePairs").toInt() to
                it.getValue("bestWorstCornerDishing").toDouble()
    }

/**
 * The uncoupled reference of every route-B width at [rung], ascending in row length.
 *
 * The phase is `C-0211`'s own `bestPhaseOnDishing` and the corner is the one **maximising**
 * `freeTileWithPreload` among that width's twelve at that phase — which is the cell whose value
 * `C-0211` publishes as `bestWorstCornerDishing`, so the two are asserted equal rather than
 * assumed (`T-322`'s gate 3).
 */
fun routeBUncoupledReferences(file: File, rung: String): List<RouteBUncoupledReference> {
    val best = routeBBestRecords(file, rung)
    val cells = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("cells").jsonArray
        .map { record -> record.jsonObject.mapValues { it.value.jsonPrimitive.content } }
        .filter { it.getValue("rung") == rung }
    return best.map { record ->
        val row = record.getValue("pairedRowBasePairs").toInt()
        val phase = record.getValue("bestPhaseOnDishing").toInt()
        val own = cells.filter {
            it.getValue("pairedRowBasePairs").toInt() == row &&
                    it.getValue("classZeroResidue").toInt() == phase
        }
        require(own.isNotEmpty()) {
            "C-0211 carries no cell at $row bp, phase $phase, rung \"" + rung + "\""
        }
        val worst = own.maxByOrNull { it.getValue("freeTileWithPreload").toDouble() }!!
        RouteBUncoupledReference(
            pairedRowBasePairs = row,
            classZeroResidue = phase,
            chain = worst.getValue("chain"),
            kuhnLength = worst.getValue("kuhnLength").toDouble(),
            contourPerNucleotide = worst.getValue("contourPerNucleotide").toDouble(),
            freeTileWithPreload = worst.getValue("freeTileWithPreload").toDouble()
        )
    }
}

// ------------------------------------------------------------------------- the conjunction

/**
 * Every threshold the moving quantity feeds, at one cell, reported as a **conjunction**.
 *
 * `CH-0272`: a verdict block that reports three thresholds separately reports no conjunction at
 * all, and `T-316`'s did not — `flatAtP90` at 22 of 32, `peakInsideUnzipCeiling` at 3, and the
 * two together at **0**, a reading its own verdict block never stated. Making the conjunction a
 * type rather than a sentence is the repair, so a downstream reader cannot take a flat count for a
 * count of admissible designs.
 *
 * `C-0060`'s `3.5 ≤ R ≤ 20` is deliberately **not** one of these three: `CH-0273` establishes it
 * is a *flatness* window swept on a square-lattice 45-station design, and this study measures
 * flatness directly. The ratio is emitted beside every cell and named for what it is.
 */
class RouteBAdmissibility(
    val flatAtP90: Boolean,
    val peakInsideUnzipCeiling: Boolean,
    val beatsUncoupledAtP90: Boolean
) {

    /** Flat **and** inside the one physical per-path threshold, `C-0023`'s unzip allowable. */
    val flatAndAdmissible: Boolean = flatAtP90 && peakInsideUnzipCeiling

    /** And better than doing nothing at all, which `C-0211` shows is already flat on route B. */
    val allThreeThresholds: Boolean = flatAndAdmissible && beatsUncoupledAtP90
}
