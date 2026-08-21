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

import com.xemantic.nano.plentyofroom.lattice.CrossoverLattice
import com.xemantic.nano.plentyofroom.lattice.SquareCrossoverLattice
import com.xemantic.nano.plentyofroom.lattice.crossoverLatticeOfGrid
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

/**
 * The interchange boundary this repository did not have.
 *
 * Every other tool in this field reads and writes a design file — caDNAno's JSON, scadnano's `.sc`,
 * oxDNA's topology pair — and imports or exports between them: DNAforge, MagicDNA, ENSnano and
 * Adenita all export to oxDNA/oxView, and DNAforge's own paper describes its output as *"the full
 * 3D nucleotide model, stapling arrangement where applicable, and the primary sequences"*.
 *
 * This corpus's tile is a set of Kotlin constants in [Gen1Tile]. That has two consequences and both
 * are costly. Its buildability results — the quantised seamless width, the crossover-phase census,
 * the station ladder, the parity split — **cannot be run against anybody else's design**; and the
 * tile it recommends **cannot be handed to anybody** without a human redrawing it in caDNAno.
 *
 * This file is the smallest thing that removes both: a reader for the scadnano `.sc` format, and
 * the derivation of the lattice facts this corpus reasons about **from the file** rather than from
 * a constant. The oxDNA run of `C-0157` already produced such a file for the Gen-1 tile
 * (`tools/oxdna/gen1_tile_design.py`), so the round trip is testable against a design this
 * programme has actually simulated: read it back, and the counts must be the corpus's own.
 *
 * ## What is deliberately not here
 *
 * Sequences, colours, insertions and deletions, loopouts, and any 3-D geometry. This reader exists
 * to answer **lattice** questions, and a lattice question needs the helix index, the offset and the
 * strand topology. A design carrying insertions or deletions — the standard twist correction — is
 * read, and [BuildabilityReport] says so rather than silently treating the offsets as base pairs.
 */
@Serializable
data class ScadnanoDomain(
    val helix: Int,
    val forward: Boolean,
    val start: Int,
    val end: Int,
    val deletions: List<Int> = emptyList(),
    val insertions: List<List<Int>> = emptyList()
) {

    init {
        require(end > start) { "a domain must span at least one offset, was: [$start, $end)" }
    }

    /** The offset the strand **leaves** this domain from, in the strand's own 5'→3' direction. */
    val exitOffset: Int get() = if (forward) end - 1 else start

    /** The offset the strand **enters** this domain at. */
    val entryOffset: Int get() = if (forward) start else end - 1

    /** Base pairs spanned, before insertions and deletions are applied. */
    val length: Int get() = end - start
}

/** One strand of a scadnano design: an ordered list of domains, 5'→3'. */
@Serializable
data class ScadnanoStrand(
    val domains: List<ScadnanoDomain>,
    @SerialName("is_scaffold") val isScaffold: Boolean = false,
    val sequence: String? = null,
    val color: String? = null
)

@Serializable
private data class ScadnanoHelix(
    @SerialName("grid_position") val gridPosition: List<Int> = emptyList(),
    val roll: Double = 0.0
)

@Serializable
private data class ScadnanoFile(
    val version: String = "",
    val grid: String = "",
    val helices: List<ScadnanoHelix> = emptyList(),
    val strands: List<ScadnanoStrand> = emptyList()
)

/**
 * One crossover, as the **file** shows it: a strand leaving one helix and entering an adjacent one
 * at the same offset.
 *
 * A crossover is a **single** strand crossing. Registering it from both sides — which is the
 * natural thing to write, and which the corpus's own `k_θ` provenance invites by speaking of *"two
 * bonds per crossover"* — is geometrically over-constrained, and `C-0157`'s run records that it
 * does not relax at all: 112 over-stretched bonds against 63 designed crossovers, exactly twice the
 * 49 staple sites. [ScadnanoDesign.checkBuildability] therefore asserts single coverage.
 */
data class DesignCrossover(
    val lowerHelix: Int,
    val upperHelix: Int,
    val offset: Int,
    val onScaffold: Boolean
) {
    /** The interface index, which for a raster of adjacent duplexes is the lower helix. */
    val interfaceIndex: Int get() = lowerHelix
}

/** What this repository's own rules say about an imported design. */
data class BuildabilityReport(
    val rowBasePairs: Int,
    val seamlessRowWidthIsAdmissible: Boolean,
    val everyCrossoverJoinsAdjacentDuplexes: Boolean,
    val noSiteIsCrossedTwice: Boolean,
    val carriesInsertionsOrDeletions: Boolean,
    val violations: List<String>
)

/**
 * A design read from a scadnano file, and the lattice facts derived from it.
 *
 * Construct it from a file or a resource; the secondary constructor exists so a test can build a
 * degenerate design without a file.
 */
class ScadnanoDesign(
    val grid: String,
    val helixCount: Int,
    val strands: List<ScadnanoStrand>
) {

    companion object {

        private val json = Json { ignoreUnknownKeys = true; isLenient = true }

        /** Reads a `.sc` design from the classpath, e.g. `"/gen1-tile.sc"`. */
        fun fromResource(path: String): ScadnanoDesign {
            val stream = requireNotNull(ScadnanoDesign::class.java.getResourceAsStream(path)) {
                "no such resource: $path"
            }
            return fromText(stream.bufferedReader().use { it.readText() })
        }

        /** Reads a `.sc` design from a file. */
        fun fromFile(file: File): ScadnanoDesign = fromText(file.readText())

        /** Reads a `.sc` design from text. */
        fun fromText(text: String): ScadnanoDesign {
            val parsed = json.decodeFromString(ScadnanoFile.serializer(), text)
            require(parsed.helices.isNotEmpty()) { "a design with no helices" }
            require(parsed.strands.isNotEmpty()) { "a design with no strands" }
            return ScadnanoDesign(parsed.grid, parsed.helices.size, parsed.strands)
        }
    }

    /**
     * The crossover lattice this design is drawn on.
     *
     * A grid this project has no lattice for is **refused**, never guessed: inheriting a
     * square-lattice congruence on a honeycomb design is the exact failure `C-0141` had to undo.
     */
    fun lattice(): CrossoverLattice = requireNotNull(crossoverLatticeOfGrid(grid)) {
        "no crossover lattice in this project for grid '$grid' — the grids it knows are " +
            "'square' and 'honeycomb', and guessing between them silently transfers a phase " +
            "congruence, a station ladder and a register departure that do not hold"
    }

    /** The scaffold strand. A design with none, or with several, is refused. */
    fun scaffold(): ScadnanoStrand {
        val scaffolds = strands.filter { it.isScaffold }
        require(scaffolds.size == 1) {
            "a design this reader can answer lattice questions about has exactly one scaffold, " +
                "found ${scaffolds.size}"
        }
        return scaffolds.single()
    }

    /** Every strand that is not the scaffold. */
    fun staples(): List<ScadnanoStrand> = strands.filter { !it.isScaffold }

    /** The row length in base pairs — the longest offset any scaffold domain reaches. */
    fun rowBasePairs(): Int = scaffold().domains.maxOf { it.end }

    /** The tile edge along the helices, at the cited rise. */
    fun edgeAlongHelicesNm(): Double = rowBasePairs() * Gen1Tile.RISE_PER_BASE_PAIR

    /**
     * Every strand crossing in the design: consecutive domains of one strand on **different**
     * helices, scaffold and staple alike.
     *
     * The junction offset is asserted rather than assumed — a crossover is antiparallel, so the
     * offset the strand leaves one domain at and the offset it enters the next at are the same
     * number, and a design where they are not is not a crossover lattice this reader can address.
     */
    fun allStrandCrossings(): List<DesignCrossover> = strands.flatMap { strand ->
        strand.domains.zipWithNext().mapNotNull { (from, to) ->
            if (from.helix == to.helix) null else {
                require(from.exitOffset == to.entryOffset) {
                    "a crossover from helix ${from.helix} at offset ${from.exitOffset} to helix " +
                        "${to.helix} at offset ${to.entryOffset}: an antiparallel crossover " +
                        "occupies ONE offset, and these differ"
                }
                DesignCrossover(
                    lowerHelix = minOf(from.helix, to.helix),
                    upperHelix = maxOf(from.helix, to.helix),
                    offset = from.exitOffset,
                    onScaffold = strand.isScaffold
                )
            }
        }
    }

    /**
     * The sheet's own lattice crossovers — the **staple** crossings.
     *
     * A raster's scaffold crossings are its **turns**, not lattice sites: `CLAUDE.md`'s *"neither
     * rim is cut — a Rothemund raster's across-helix rim is where the scaffold turns"*. They live
     * at the row ends, they are not on the column ladder, and counting them among the crossovers
     * is how a seven-column sheet reads as 63 crossovers instead of 49.
     */
    fun crossovers(): List<DesignCrossover> = allStrandCrossings().filter { !it.onScaffold }

    /** The raster turns: the scaffold's own crossings, at the row ends. */
    fun scaffoldTurns(): List<DesignCrossover> = allStrandCrossings().filter { it.onScaffold }

    /** How many lattice crossovers the sheet builds. */
    fun crossoverCount(): Int = crossovers().size

    /** The distinct offsets lattice crossovers occupy, ascending — the crossover columns. */
    fun crossoverColumns(): List<Int> = crossovers().map { it.offset }.distinct().sorted()

    /** The first column, which is the phase the column lattice was laid out at. */
    fun crossoverPhase(): Int = crossoverColumns().first()

    /**
     * Crossovers per interface, indexed by the lower duplex.
     *
     * On a seven-column single-layer sheet this is the **4/3 split**: seven interfaces carry four
     * and seven carry three, which is what makes `D_⊥` a harmonic mean rather than a smeared one.
     */
    fun crossoversPerInterface(): List<Int> {
        val counts = IntArray(maxOf(helixCount - 1, 0))
        crossovers().forEach { crossover ->
            if (crossover.interfaceIndex < counts.size) counts[crossover.interfaceIndex]++
        }
        return counts.toList()
    }

    /**
     * The register error one row accumulates against B-DNA, in degrees.
     *
     * Single-signed and linear in the row length, so it accumulates rather than cancelling — and
     * on the imported Gen-1 raster it is the −60° `C-0086`'s 112 bp row carries and `C-0157`'s
     * oxDNA run relaxed against.
     */
    fun accumulatedRegisterDepartureDegrees(): Double =
        lattice().registerDepartureDegrees(rowBasePairs())

    /** This repository's own buildability rules, run against the imported design. */
    fun checkBuildability(): BuildabilityReport {
        val violations = mutableListOf<String>()
        val row = rowBasePairs()
        val admissible = seamlessRowWidthIsAdmissible(row)
        if (!admissible) {
            violations += seamlessRowWidthViolation(row)
        }
        val adjacency = allStrandCrossings().all { it.upperHelix - it.lowerHelix == 1 }
        if (!adjacency) {
            violations += "a crossover joins two duplexes that are not adjacent, so the " +
                "row-adjacency graph is not a path and the seam parity argument does not apply"
        }
        val sites = allStrandCrossings().map { Triple(it.lowerHelix, it.offset, it.onScaffold) }
        val single = sites.size == sites.toSet().size
        if (!single) {
            violations += "a crossover site is registered twice: a crossover is a SINGLE strand " +
                "crossing, and a reciprocal pair at one offset is geometrically over-constrained"
        }
        val modified = strands.any { strand ->
            strand.domains.any { it.deletions.isNotEmpty() || it.insertions.isNotEmpty() }
        }
        if (modified) {
            violations += "the design carries insertions or deletions — the standard twist " +
                "correction — so an offset is not a base pair and every length here is nominal"
        }
        return BuildabilityReport(
            rowBasePairs = row,
            seamlessRowWidthIsAdmissible = admissible,
            everyCrossoverJoinsAdjacentDuplexes = adjacency,
            noSiteIsCrossedTwice = single,
            carriesInsertionsOrDeletions = modified,
            violations = violations
        )
    }
}

/**
 * Whether a seamless raster can turn at this row width.
 *
 * `C-0086`: a boustrophedon has only *progressive* scaffold crossovers, so Rothemund's rule — the
 * distance between successive scaffold crossovers must be an **odd number of half turns** — binds
 * the **row length**. At the square lattice's 16 bp half-turn spacing that is the odd multiples of
 * 16 bp: 16, 48, 80, 112, 144. A nominal 40.0 nm (117.6 bp) is not among them, and the nearest
 * buildable tile is 112 bp = 38.08 nm.
 */
fun seamlessRowWidthIsAdmissible(rowBasePairs: Int): Boolean =
    rowBasePairs % SquareCrossoverLattice.samePairPeriodBasePairs ==
        SquareCrossoverLattice.SHEET_DOMAIN_BASE_PAIRS

private fun seamlessRowWidthViolation(rowBasePairs: Int): String {
    val period = SquareCrossoverLattice.samePairPeriodBasePairs
    val nearest = ((rowBasePairs - SquareCrossoverLattice.SHEET_DOMAIN_BASE_PAIRS).toDouble() /
        period).let { Math.round(it) } * period + SquareCrossoverLattice.SHEET_DOMAIN_BASE_PAIRS
    return "a seamless raster cannot turn at $rowBasePairs bp: a boustrophedon needs an ODD " +
        "number of half turns across its row, which on this lattice is the odd multiples of " +
        "${SquareCrossoverLattice.SHEET_DOMAIN_BASE_PAIRS} bp. The nearest buildable width is " +
        "$nearest bp."
}

/** The buildability of a bare row width, for a design that has not been drawn yet. */
fun buildabilityOfRowWidth(rowBasePairs: Int): BuildabilityReport {
    val admissible = seamlessRowWidthIsAdmissible(rowBasePairs)
    return BuildabilityReport(
        rowBasePairs = rowBasePairs,
        seamlessRowWidthIsAdmissible = admissible,
        everyCrossoverJoinsAdjacentDuplexes = true,
        noSiteIsCrossedTwice = true,
        carriesInsertionsOrDeletions = false,
        violations = if (admissible) emptyList() else listOf(seamlessRowWidthViolation(rowBasePairs))
    )
}
