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

package com.xemantic.nano.plentyofroom.design

import com.xemantic.nano.plentyofroom.anchoring.rasterColumnLayout
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiSheet
import com.xemantic.nano.plentyofroom.structure.grillageImport
import kotlin.math.abs

/**
 * `T-275` — which crossover census a measured constant is read against.
 *
 * ## The two phase data, and why they collide on the integer 8
 *
 * A crossover column lattice has to be laid down from *some* datum, and this repository uses two:
 *
 * - the **row-start** datum, which is what a design file states — `ScadnanoDesign.crossoverPhase()`
 *   is the offset of the first crossover column counted from the scaffold's own offset 0, and it
 *   is the datum `tools/oxdna/gen1_tile_design.py` writes its columns at (`x = 8 + 16k`);
 * - the **tile-centre** datum, which is what the mechanics uses — [CrossoverLayout.phased] and
 *   [rasterColumnLayout] lay the lattice down as `x = phase·rise + k·(p/2)` with `x` measured from
 *   the centre of the footprint, and that is the datum `C-0063`, `C-0090`, `C-0134` and every
 *   placement study in this corpus quote a phase in.
 *
 * They differ by [phaseDatumOffsetBasePairs], which is `(rowBp/2) mod (p/2)` — **zero** whenever a
 * row spans an even number of column pitches and **half a pitch** whenever it spans an odd number.
 * `C-0086`'s buildable seamless row is `112 bp = 7 × 16 bp`, seven being odd, so on *this* row the
 * two data differ by exactly **8 bp** and the same integer names two different lattices.
 *
 * That is not a naming nuisance. `CLAUDE.md` records that shifting the column lattice by one column
 * pitch *"hands every interface the other parity's columns, which is a physically different
 * sheet"*, and here the shift is half of that again: at the tile-centre phase 8 a column lands
 * **exactly** on each row end, so `CrossoverLayout.EDGE_MARGIN` — a numerical guard — decides
 * eight columns against six, while at tile-centre phase 0 or 16 no column touches the edge, the
 * guard is inert, and the count is seven.
 *
 * ## What is *not* here
 *
 * Nothing in this file solves anything. Every quantity is an integer count, a base-pair offset, a
 * position in nm or a ratio of integers, which is what makes the census affordable against the
 * alternative — re-running oxDNA on the other lattice, about one day of wall clock and 649 MB
 * (`C-0169` §1).
 */

/** `C-0086`'s buildable seamless row, in nm — `112 bp × 0.34 nm`. */
val BUILDABLE_EDGE_X: Double = 112 * Gen1Tile.RISE_PER_BASE_PAIR

/** The column pitch of a single-layer square-lattice sheet, in base pairs — half of `p`. */
const val COLUMN_PITCH_BASE_PAIRS: Int = 16

/**
 * How far the **tile-centre** phase datum runs ahead of the **row-start** one, in base pairs.
 *
 * `centreDatumPhase ≡ rowStartDatumPhase + phaseDatumOffsetBasePairs(rowBp, pitch)  (mod pitch)`.
 *
 * It is `(rowBp/2) mod pitch` and nothing else: the tile centre sits at `rowBp/2`, so a lattice
 * point at row-start offset `q` sits at `q − rowBp/2` from the centre, and the centre-datum phase
 * that reproduces it is that, folded back into the pitch.
 *
 * @throws IllegalArgumentException if the row has no centre base pair, or the pitch is not positive.
 */
fun phaseDatumOffsetBasePairs(rowBasePairs: Int, columnPitchBasePairs: Int): Int {
    require(columnPitchBasePairs > 0) {
        "columnPitchBasePairs must be positive, was: $columnPitchBasePairs"
    }
    require(rowBasePairs > 0) { "rowBasePairs must be positive, was: $rowBasePairs" }
    require(rowBasePairs % 2 == 0) {
        "an odd row has no centre base pair, so the two phase data are not both on the " +
                "lattice; rowBasePairs was: $rowBasePairs"
    }
    return Math.floorMod(rowBasePairs / 2, columnPitchBasePairs)
}

/**
 * The **smeared** across-helix rigidity of a lattice with these per-interface crossover counts, as
 * a fraction of the same lattice at [reference] crossovers on every interface.
 *
 * Linear in the count, because `D_⊥` smears the hinges over the interface — the optimistic reading
 * (`CLAUDE.md`: a seven-column sheet loses `49/56`).
 */
fun smearedRigidityFraction(crossoversPerInterface: List<Int>, reference: Int): Double {
    require(crossoversPerInterface.isNotEmpty()) { "there must be at least one interface" }
    require(reference > 0) { "reference must be positive, was: $reference" }
    return crossoversPerInterface.sumOf { it.toDouble() } /
            (crossoversPerInterface.size.toDouble() * reference)
}

/**
 * The **series** across-helix rigidity of the same lattice, as a fraction of the uniform one.
 *
 * A harmonic mean, because bending across the helices puts the interfaces in series — the reading
 * that annihilates on one empty interface, and the pessimistic one (`42/49` at the 4/3 split).
 */
fun seriesRigidityFraction(crossoversPerInterface: List<Int>, reference: Int): Double {
    require(crossoversPerInterface.isNotEmpty()) { "there must be at least one interface" }
    require(reference > 0) { "reference must be positive, was: $reference" }
    if (crossoversPerInterface.any { it <= 0 }) return 0.0
    val reciprocalSum = crossoversPerInterface.sumOf { 1.0 / it }
    return crossoversPerInterface.size / reciprocalSum / reference
}

/**
 * The crossovers the **continuum** `D_⊥ = k_θ · d / p` assumes on this footprint.
 *
 * `OrigamiSheet.crossoverLinearDensity` is `layers/p`, a density **per unit length along the
 * helices**, so the continuum's own census over `duplexes − 1` interfaces of length [edgeX] is
 * `(duplexes − 1) · edgeX / p`. On `C-0086`'s 112 bp row that is `14 × 112/32 = 49` — which is the
 * count the *seven-column* lattice carries, and `8/7` of it is what the phase-8 grillage carries.
 */
fun smearedCrossoverCountOfContinuum(
    sheet: OrigamiSheet,
    edgeX: Double,
    duplexes: Int
): Double {
    require(duplexes >= 2) { "a sheet needs at least two duplexes, was: $duplexes" }
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    return (duplexes - 1) * edgeX * sheet.crossoverLinearDensity
}

/** One row of the tile-centre phase census — a lattice, counted and never solved. */
data class LatticeCensusRow(
    val phaseBasePairs: Int,
    val admitRowEnd: Boolean,
    val columnCount: Int,
    val columnPositionsNm: List<Double>,
    val columnParities: List<Int>,
    val parityCounts: List<Int>,
    val crossoverCount: Int,
    val crossoversPerInterface: List<Int>,
    val columnOnRowEnd: Boolean,
    /** Of [crossoverCount], the ties the grillage builds at a row-end column. */
    val modelledRasterTurns: Int,
    /** Of [crossoverCount], the ties that are STAPLE crossovers. */
    val stapleCrossovers: Int,
    /**
     * The inter-duplex ties the folded object carries: [stapleCrossovers] plus the `D − 1` raster
     * turns a **seamless** boustrophedon always has, whether or not this lattice models them.
     */
    val physicalTies: Int,
    val physicalTiesPerInterface: List<Int>,
    val smearedFraction: Double,
    val seriesFraction: Double,
    val physicalSmearedFraction: Double,
    val physicalSeriesFraction: Double
)

/**
 * The census of one tile-centre phase on a footprint of [edgeX] nm carrying [duplexes] duplexes.
 *
 * The lattice is `rasterColumnLayout`'s — the function every placement study in this corpus calls —
 * so this censuses the corpus's own object rather than a restatement of it. The rigidity fractions
 * are taken against the densest per-interface count the sweep reaches, which on this row is four.
 */
fun latticeCensusRow(
    phaseBasePairs: Int,
    sheet: OrigamiSheet,
    edgeX: Double,
    duplexes: Int,
    admitRowEnd: Boolean,
    reference: Int = 4
): LatticeCensusRow {
    require(duplexes >= 2) { "a sheet needs at least two duplexes, was: $duplexes" }
    val layout = rasterColumnLayout(phaseBasePairs, sheet, edgeX, admitRowEnd)
    val parityCounts = listOf(layout.countOfParity(0), layout.countOfParity(1))
    val perInterface = (0 until duplexes - 1).map { parityCounts[it % 2] }
    val half = edgeX / 2.0 - CrossoverLayout.EDGE_MARGIN
    // A column lying ON the row end is, in a seamless boustrophedon, the SCAFFOLD's own raster
    // turn and not a staple crossover (`C-0090`: "in a seamless boustrophedon that column IS the
    // scaffold crossover"). So the row-end admission binary is not a column count — it is whether
    // the grillage models the turns at all.
    val endParities = layout.positions.indices
        .filter { abs(abs(layout.positions[it]) - half) <= 1.0e-9 }
        .map { layout.parities[it] }
    val turnsPerInterface = (0 until duplexes - 1).map { b -> endParities.count { it == b % 2 } }
    val staplesPerInterface = perInterface.zip(turnsPerInterface) { a, b -> a - b }
    val physicalPerInterface = staplesPerInterface.map { it + 1 }
    return LatticeCensusRow(
        phaseBasePairs = phaseBasePairs,
        admitRowEnd = admitRowEnd,
        columnCount = layout.size,
        columnPositionsNm = layout.positions,
        columnParities = layout.parities,
        parityCounts = parityCounts,
        crossoverCount = perInterface.sum(),
        crossoversPerInterface = perInterface,
        columnOnRowEnd = endParities.isNotEmpty(),
        modelledRasterTurns = turnsPerInterface.sum(),
        stapleCrossovers = staplesPerInterface.sum(),
        physicalTies = physicalPerInterface.sum(),
        physicalTiesPerInterface = physicalPerInterface,
        smearedFraction = smearedRigidityFraction(perInterface, reference),
        seriesFraction = seriesRigidityFraction(perInterface, reference),
        physicalSmearedFraction = smearedRigidityFraction(physicalPerInterface, reference),
        physicalSeriesFraction = seriesRigidityFraction(physicalPerInterface, reference)
    )
}

/** Every tile-centre phase, at both row-end settings — 32 × 2 rows, and no solve. */
fun latticeCensusSweep(
    sheet: OrigamiSheet,
    edgeX: Double,
    duplexes: Int,
    reference: Int = 4
): List<LatticeCensusRow> = (0 until CrossoverLayout.BASE_PAIRS_PER_PERIOD).flatMap { phase ->
    listOf(true, false).map { admit ->
        latticeCensusRow(phase, sheet, edgeX, duplexes, admit, reference)
    }
}

/**
 * The design's own crossover census, **filtered by strand role**.
 *
 * A bare *"crossovers"* count on a seamless raster is ambiguous by [roleAmbiguity], because the
 * scaffold's raster turns are strand crossings and are not lattice sites (`C-0157`, `CLAUDE.md`).
 */
data class DesignCrossoverCensus(
    val rowBasePairs: Int,
    val stapleCrossovers: Int,
    val scaffoldTurns: Int,
    val allStrandCrossings: Int,
    val roleAmbiguity: Double,
    val columnOffsetsBasePairs: List<Int>,
    val crossoversPerInterface: List<Int>,
    val rowStartPhaseBasePairs: Int
)

/** The census of an emitted design, read straight off the file. */
fun ScadnanoDesign.crossoverCensus(): DesignCrossoverCensus {
    val staples = crossovers()
    val turns = scaffoldTurns()
    val all = allStrandCrossings()
    return DesignCrossoverCensus(
        rowBasePairs = rowBasePairs(),
        stapleCrossovers = staples.size,
        scaffoldTurns = turns.size,
        allStrandCrossings = all.size,
        roleAmbiguity = if (staples.isEmpty()) 0.0
        else all.size.toDouble() / staples.size - 1.0,
        columnOffsetsBasePairs = crossoverColumns(),
        crossoversPerInterface = crossoversPerInterface(),
        rowStartPhaseBasePairs = crossoverPhase()
    )
}

/** One phase of the sweep, graded against the design's own columns. */
data class PhaseMatch(
    val phaseBasePairs: Int,
    val admitRowEnd: Boolean,
    val columnCount: Int,
    val positionsMatch: Boolean,
    val paritiesMatch: Boolean,
    val worstColumnDepartureNm: Double
)

/**
 * Which tile-centre phases reproduce this design's crossover columns.
 *
 * The design's columns are converted to the **tile-centre** datum with the importer's own rule —
 * the centre of the axial window of every strand in the file — so that a match here is a match of
 * the object `C-0161`'s `grillageImport` builds and not of a second reading of the file.
 *
 * A phase can match the **positions** and invert every **parity**: that is `C-0090`'s pair of
 * phases, and it is why a phase integer alone does not determine a sheet.
 */
fun matchDesignToPhases(
    design: ScadnanoDesign,
    sheet: OrigamiSheet,
    tolerance: Double = 1.0e-9
): List<PhaseMatch> {
    val window = design.axialWindowBasePairs()
    val centre = (window.first + window.last + 1) / 2.0
    val rise = design.risePerBasePairOrNull() ?: Gen1Tile.RISE_PER_BASE_PAIR
    val edgeX = (window.last + 1 - window.first) * rise
    val import = design.grillageImport("T-275 census")
    val designPositions = import.columnBasePairs.map { (it - centre) * rise }
    val designParities = import.columnParities
    return latticeCensusSweep(sheet, edgeX, design.helixCount).map { row ->
        val sameCount = row.columnCount == designPositions.size
        val departure = if (!sameCount) Double.POSITIVE_INFINITY
        else row.columnPositionsNm.zip(designPositions).maxOf { (a, b) -> abs(a - b) }
        PhaseMatch(
            phaseBasePairs = row.phaseBasePairs,
            admitRowEnd = row.admitRowEnd,
            columnCount = row.columnCount,
            positionsMatch = sameCount && departure <= tolerance,
            paritiesMatch = sameCount && departure <= tolerance &&
                    row.columnParities == designParities,
            worstColumnDepartureNm = departure
        )
    }
}
