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
import com.xemantic.nano.plentyofroom.lattice.HoneycombCrossoverLattice
import com.xemantic.nano.plentyofroom.lattice.SquareCrossoverLattice
import com.xemantic.nano.plentyofroom.lattice.crossoverLatticeOfGrid
import com.xemantic.nano.plentyofroom.structure.HoneycombCell
import com.xemantic.nano.plentyofroom.structure.honeycombAzimuthDegrees
import com.xemantic.nano.plentyofroom.structure.neighbourClassDifference
import com.xemantic.nano.plentyofroom.tile.honeycombBondClass

/**
 * `T-270` — this repository's buildability rules, applied **on the lattice the design is drawn on**
 * and derived from the **file**.
 *
 * ## The defect this file repairs
 *
 * `ScadnanoDesign.lattice()` refuses a grid this project has no lattice for, and says why:
 * *"guessing between them silently transfers a phase congruence, a station ladder and a register
 * departure that do not hold"*. Twelve lines below it, `checkBuildability()` used to apply
 * `C-0086`'s **square**-lattice width rule — an odd number of half turns across the row, i.e. the
 * odd multiples of 16 bp — to whatever design it was handed, so on this programme's own
 * recommended honeycomb block it reported a violation naming a ladder a 21 bp period has nothing
 * to do with. `C-0160` declared that as falsifier `F2`, it **fired**, and it was filed as `T-270`
 * rather than repaired quietly.
 *
 * ## The one rule, read on two lattices
 *
 * `CLAUDE.md` states it once: *an integral crossover lattice is necessary and not sufficient — the
 * crossover must also point at the neighbour the raster needs next*, so an admissible run length is
 * `N ≡ (exit residue) − (entry residue) (mod period)`. Written that way the two lattices are one
 * predicate with three declared constants:
 *
 * | | azimuths | step | period | scaffold offset | a boustrophedon's admissible run |
 * |---|---|---|---|---|---|
 * | square | 4 | 8 bp | 32 bp | none | `N ≡ 16 (mod 32)` — `C-0086` exactly |
 * | honeycomb | 3 | 7 bp | 21 bp | `±5 bp` | `N ≡ 7Δ + {0, 10, 11} (mod 21)` — `C-0136` exactly |
 *
 * And the reason the square rule is **unconditional** while the honeycomb's needs a turn sense is
 * one line of arithmetic, which [admissibleRunResidues] makes executable: a raster's axial sign
 * alternates, so the rule survives it exactly when `−Δ = Δ`, and on the square sheet the two
 * in-plane neighbours are `Δ = 2` apart, which **is** self-inverse modulo 4. Modulo 3 neither 1 nor
 * 2 is.
 *
 * ## What makes it answerable about somebody ELSE's file
 *
 * Two things the format carries and this repository was not reading:
 *
 * - **the level**, from the domain boundaries. A raster crossover sits on the **edge of the axial
 *   window the helix turns at** — `end` for a forward domain, `start` for a reverse one — and the
 *   two sides of one crossover give the same number, which [importedRasterCrossovers] asserts
 *   rather than assumes. The *offset* the file records is `level − 1` for a forward domain and
 *   `level` for a reverse one, so reading offsets instead of edges perturbs half the residues by
 *   one and is not a datum at all.
 * - **the neighbour class**, from `grid_position`, through [honeycombCellOfGridPosition].
 *
 * A global datum shift moves every reduced residue alike, so `C-0148`'s closure condition is
 * **convention-free** in the file's own origin — which is what lets a design nobody here drew be
 * graded at all.
 */

/**
 * scadnano's honeycomb `grid_position` `(h, v)` as this corpus's own integer cross-section cell.
 *
 * The inverse of what `HoneycombBlockDesign` emits, and it is derived rather than reconstructed:
 * scadnano's published `grid_position_to_position` puts `(h, v)` at
 * `x = h·d√3/2`, `y = ((3v + v mod 2)/2)·d` for even `h` and `((3v − v mod 2 + 1)/2)·d` for odd,
 * while this corpus puts cell `(x, y)` at `(x·d√3/2, y·d/2)` — so `x = h` and `y` is the negation
 * below, scadnano's `y` increasing downward. `C-0160`'s `F4` checked the forward composition at
 * departure `0.0`; this is the same map read the other way, and every `(h, v)` it returns is a
 * site of the lattice, which [HoneycombCell] itself refuses otherwise.
 */
fun honeycombCellOfGridPosition(h: Int, v: Int): HoneycombCell = HoneycombCell(
    x = h,
    y = if (Math.floorMod(h, 2) == 0) -(3 * v + Math.floorMod(v, 2))
    else -(3 * v - Math.floorMod(v, 2) + 1)
)

/** The cross-section cell of every helix of a honeycomb design, indexed by helix. */
fun ScadnanoDesign.honeycombCells(): List<HoneycombCell> {
    require(crossoverLatticeOfGrid(grid) === HoneycombCrossoverLattice) {
        "a cross-section cell is a honeycomb quantity and this design is on grid '$grid'"
    }
    require(helices.size == helixCount) {
        "a design with ${helices.size} helix records and $helixCount helices carries no cell map"
    }
    return helices.map { helix ->
        require(helix.gridPosition.size == 2) {
            "a honeycomb helix needs a two-coordinate grid_position, had: ${helix.gridPosition}"
        }
        honeycombCellOfGridPosition(helix.gridPosition[0], helix.gridPosition[1])
    }
}

/** Whether two cells of the cross-section are bonded — a honeycomb site has exactly three. */
fun areHoneycombNeighbours(from: HoneycombCell, to: HoneycombCell): Boolean = to in from.neighbours

/** The axial level a domain is **left** at: the edge of its own window the strand turns at. */
internal fun exitLevelBasePairs(domain: ScadnanoDomain): Int =
    if (domain.forward) domain.end else domain.start

/** The axial level a domain is **entered** at, which is its other edge. */
internal fun entryLevelBasePairs(domain: ScadnanoDomain): Int =
    if (domain.forward) domain.start else domain.end

/** One raster crossover of a honeycomb design, as the **file** shows it. */
data class ImportedRasterCrossover(
    val fromHelix: Int,
    val toHelix: Int,
    val levelBasePairs: Int,
    val neighbourClass: Int,
    val reducedResidue: Int
)

/**
 * Every scaffold crossing of a honeycomb design, reduced by its own bond class.
 *
 * `C-0148`: a scaffold crossover sits `±5 bp` from its pair's staple position and **one** lattice
 * constant `b₀` serves the whole design, so `(level − 7·class) mod 21` must leave at most two
 * values, exactly ten apart.
 */
fun ScadnanoDesign.importedRasterCrossovers(): List<ImportedRasterCrossover> {
    val cells = honeycombCells()
    val step = HoneycombCrossoverLattice.anyAzimuthStepBasePairs
    val period = HoneycombCrossoverLattice.samePairPeriodBasePairs
    return scaffold().domains.zipWithNext().mapNotNull { (from, to) ->
        if (from.helix == to.helix) null else {
            val level = exitLevelBasePairs(from)
            require(level == entryLevelBasePairs(to)) {
                "the two sides of the crossover from helix ${from.helix} to helix ${to.helix} " +
                    "disagree about its level, $level against ${entryLevelBasePairs(to)}: a " +
                    "crossover is one plane, and a design where it is not is not a raster this " +
                    "reader can reduce"
            }
            val here = cells[from.helix]
            val there = cells[to.helix]
            require(areHoneycombNeighbours(here, there)) {
                "helix ${from.helix} at $here and helix ${to.helix} at $there are not honeycomb " +
                    "neighbours, so this crossing has no bond class"
            }
            val bondClass = honeycombBondClass(
                here.sublattice, honeycombAzimuthDegrees(there.x - here.x, there.y - here.y)
            )
            ImportedRasterCrossover(
                fromHelix = from.helix,
                toHelix = to.helix,
                levelBasePairs = level,
                neighbourClass = bondClass,
                reducedResidue = Math.floorMod(level - step * bondClass, period)
            )
        }
    }
}

/** `C-0148`'s closure, answered from an imported design rather than from a construction. */
class ImportedHoneycombClosure(val crossovers: List<ImportedRasterCrossover>) {

    private val period = HoneycombCrossoverLattice.samePairPeriodBasePairs
    private val offset = HoneycombCrossoverLattice.scaffoldCrossoverOffsetBasePairs

    private fun admitted(classZeroResidue: Int): Set<Int> = setOf(
        Math.floorMod(classZeroResidue + offset, period),
        Math.floorMod(classZeroResidue - offset, period)
    )

    /** `(level − 7·class) mod 21` at every raster crossover, in path order. */
    val reducedResidues: List<Int> = crossovers.map { it.reducedResidue }

    /** Their distinct values, ascending. */
    val distinctReducedResidues: List<Int> = reducedResidues.distinct().sorted()

    /** Every `b₀` that admits **all** of them. */
    val classZeroResidueCandidates: List<Int> =
        (0 until period).filter { b0 -> distinctReducedResidues.all { it in admitted(b0) } }

    /** Whether the raster closes on caDNAno's own `±5 bp` scaffold rule. */
    val closes: Boolean get() = classZeroResidueCandidates.isNotEmpty()

    /** The fewest crossovers a design at these levels would have to **force**. */
    val forcedCrossovers: Int =
        (0 until period).minOf { b0 -> reducedResidues.count { it !in admitted(b0) } }
}

/** [ImportedHoneycombClosure] of this design. */
fun ScadnanoDesign.honeycombClosure(): ImportedHoneycombClosure =
    ImportedHoneycombClosure(importedRasterCrossovers())

/**
 * One run of the scaffold along one helix, with the neighbours it enters and leaves by.
 *
 * A run whose entry or exit is a strand end rather than a crossover is **not interior**, and the
 * residue rule says nothing about it: the rule is a statement about the distance between two
 * *successive scaffold crossovers*, and a path end has only one.
 */
data class ScaffoldRun(
    val helix: Int,
    val domainIndex: Int,
    val lengthBasePairs: Int,
    val forward: Boolean,
    val entryNeighbourHelix: Int?,
    val exitNeighbourHelix: Int?
) {
    /** Whether a crossover sits at **both** ends, which is what the residue rule needs. */
    val isInterior: Boolean get() = entryNeighbourHelix != null && exitNeighbourHelix != null
}

/** Every run of the scaffold, in path order. */
fun ScadnanoDesign.scaffoldRuns(): List<ScaffoldRun> {
    val domains = scaffold().domains
    return domains.mapIndexed { index, domain ->
        val before = domains.getOrNull(index - 1)
        val after = domains.getOrNull(index + 1)
        ScaffoldRun(
            helix = domain.helix,
            domainIndex = index,
            lengthBasePairs = domain.length,
            forward = domain.forward,
            entryNeighbourHelix = before?.helix?.takeIf { it != domain.helix },
            exitNeighbourHelix = after?.helix?.takeIf { it != domain.helix }
        )
    }
}

/**
 * The run lengths a lattice admits at effective neighbour-class difference [effectiveSense].
 *
 * `{0, +2·offset, −2·offset}` because each of the run's two ends independently takes the `+` or the
 * `−` scaffold position, and only their **difference** reaches the length. On the square lattice
 * the offset is zero and the set is a single residue; on the honeycomb it is `C-0136`'s
 * `7Δ + {0, 10, 11}`.
 */
fun admissibleRunResidues(lattice: CrossoverLattice, effectiveSense: Int): Set<Int> {
    val period = lattice.samePairPeriodBasePairs
    val offset = lattice.scaffoldCrossoverOffsetBasePairs
    return setOf(0, 2 * offset, -2 * offset).map {
        Math.floorMod(lattice.anyAzimuthStepBasePairs * effectiveSense + it, period)
    }.toSet()
}

/** A run whose length cannot carry the azimuth its own two crossovers need. */
data class InadmissibleRun(
    val run: ScaffoldRun,
    val effectiveSense: Int,
    val admissibleResidues: List<Int>,
    val residue: Int
) {
    /** The sentence a report carries. */
    fun describe(lattice: CrossoverLattice): String =
        "the scaffold run of ${run.lengthBasePairs} bp on helix ${run.helix} enters from helix " +
            "${run.entryNeighbourHelix} and leaves to helix ${run.exitNeighbourHelix}, which on " +
            "the ${lattice.name} lattice needs a length congruent to " +
            "${admissibleResidues.joinToString(" or ")} modulo " +
            "${lattice.samePairPeriodBasePairs} bp; it is $residue"
}

/**
 * The interior scaffold runs whose length is not admissible on this design's own lattice.
 *
 * This is a **per element** rule, and `CLAUDE.md` is explicit that a per-element rule which is
 * NECESSARY is not SUFFICIENT once the elements share a boundary: on the honeycomb the global
 * condition is [honeycombClosure], and `C-0140`'s withdrawn `112 / 108` pair passes **this** and
 * fails **that**.
 */
fun ScadnanoDesign.inadmissibleScaffoldRuns(): List<InadmissibleRun> {
    val lattice = lattice()
    val cells = if (lattice === HoneycombCrossoverLattice) honeycombCells() else null
    return scaffoldRuns().filter { it.isInterior }.mapNotNull { run ->
        val arrive = neighbourAzimuthDegrees(lattice, cells, run.helix, run.entryNeighbourHelix!!)
        val leave = neighbourAzimuthDegrees(lattice, cells, run.helix, run.exitNeighbourHelix!!)
        val geometric = neighbourClassDifference(arrive, leave, lattice.azimuths)
        val effective = Math.floorMod((if (run.forward) 1 else -1) * geometric, lattice.azimuths)
        val admissible = admissibleRunResidues(lattice, effective)
        val residue = Math.floorMod(run.lengthBasePairs, lattice.samePairPeriodBasePairs)
        if (residue in admissible) null else InadmissibleRun(
            run = run,
            effectiveSense = effective,
            admissibleResidues = admissible.sorted(),
            residue = residue
        )
    }
}

/**
 * The azimuth of the bond from [helix] to [neighbour] on this lattice.
 *
 * On the honeycomb it is the cross-section's own; on the **square** sheet, whose interfaces form a
 * path graph, the two in-plane neighbours are 180° apart and a helix index is all that is needed —
 * which is why the square rule needs no grid position and the honeycomb's does.
 */
private fun neighbourAzimuthDegrees(
    lattice: CrossoverLattice,
    cells: List<HoneycombCell>?,
    helix: Int,
    neighbour: Int
): Double = if (cells != null) {
    val here = cells[helix]
    val there = cells[neighbour]
    require(areHoneycombNeighbours(here, there)) {
        "helix $helix at $here and helix $neighbour at $there are not honeycomb neighbours"
    }
    honeycombAzimuthDegrees(there.x - here.x, there.y - here.y)
} else {
    require(kotlin.math.abs(neighbour - helix) == 1) {
        "on the ${lattice.name} lattice this reader takes a sheet's neighbours from the helix " +
            "ordering, and helix $helix is not adjacent to helix $neighbour"
    }
    if (neighbour < helix) 180.0 else 0.0
}

/**
 * What a buildability report can say, and the third state a boolean cannot hold.
 *
 * `INCONCLUSIVE` exists because an **empty** violation list is otherwise indistinguishable from a
 * clean one: a design on a grid this project has no lattice for breaks no rule here, and reporting
 * that as a pass is the same class of error as answering a rule that does not hold.
 */
enum class BuildabilityVerdict {

    /** Every rule this repository has for this design's lattice applies, and passes. */
    ADMISSIBLE,

    /** At least one rule is broken. A violation is a violation whatever else was withheld. */
    VIOLATIONS,

    /** Nothing is broken, and at least one rule could not be answered — see `notApplicable`. */
    INCONCLUSIVE
}

/**
 * This repository's buildability rules, run on an imported design's **own** lattice.
 *
 * Every lattice-dependent field is `null` where the rule did not apply, and the reason is in
 * [notApplicable] whenever the rule was **withheld** rather than replaced. A rule that a design's
 * lattice simply does not have — `C-0086`'s seamless row width on a honeycomb block — is not
 * withheld; it is answered by the rule that lattice does have.
 */
data class LatticeBuildabilityReport(
    val grid: String,
    /** The lattice's name, or `null` where this project has none for [grid]. */
    val lattice: String?,
    val verdict: BuildabilityVerdict,
    /** `null` where the design carries no single scaffold, so it has no row to have a width. */
    val rowBasePairs: Int?,
    val axialSpanBasePairs: Int,
    /** Whether the scaffold traverses every helix exactly once — a boustrophedon with no seam. */
    val isSeamlessRaster: Boolean,
    /** `C-0086`'s row-width rule; non-null only on a **square** design that **is** seamless. */
    val seamlessRowWidthIsAdmissible: Boolean?,
    /** The general rule: every interior scaffold run carries the azimuth its two ends need. */
    val everyScaffoldRunIsAdmissible: Boolean?,
    /** `C-0148`'s `±5 bp` closure; non-null only on a honeycomb design. */
    val honeycombRasterCloses: Boolean?,
    val honeycombForcedCrossovers: Int?,
    val honeycombClassZeroResidues: List<Int>,
    val everyStrandCrossingJoinsLatticeNeighbours: Boolean?,
    val noSiteIsCrossedTwice: Boolean,
    val carriesInsertionsOrDeletions: Boolean,
    val violations: List<String>,
    val notApplicable: List<String>
)

/**
 * This repository's own rules, run against an imported design — **on its own lattice**.
 *
 * The capability `ARCHITECTURE.md` says nothing in the field has: caDNAno will happily let you
 * draw a row width a boustrophedon cannot turn at, and it will just as happily let you draw a
 * honeycomb raster no lattice constant `b₀` serves.
 *
 * **A design whose grid this project has no lattice for is REPORTED, not refused, and that is a
 * decision.** `lattice()` refuses because it must return a lattice and there is none; a *report*
 * has somewhere to put *"I have no rule here"*, and the two rules that are statements about
 * strands rather than about a lattice — a site registered twice, insertions or deletions — are
 * still worth answering to whoever handed the file over. [BuildabilityVerdict.INCONCLUSIVE] is
 * what stops that from reading as a pass.
 */
fun ScadnanoDesign.checkBuildability(): LatticeBuildabilityReport {
    val lattice = crossoverLatticeOfGrid(grid)
    val violations = mutableListOf<String>()
    val notApplicable = mutableListOf<String>()
    val crossings = allStrandCrossings()

    // -- the two rules that are statements about strands, and hold on any lattice or none
    val sites = crossings.map { Triple(it.lowerHelix, it.offset, it.onScaffold) }
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

    if (lattice == null) {
        notApplicable += "this project has no crossover lattice for grid '$grid' — it knows " +
            "'square' and 'honeycomb' — so every rule that needs an azimuth, a period or a " +
            "neighbour is withheld rather than guessed, which is what `lattice()` refuses for"
    }
    val cells = if (lattice === HoneycombCrossoverLattice) {
        runCatching { honeycombCells() }.getOrElse {
            notApplicable += "this honeycomb design carries no usable grid positions, so no " +
                "crossing has a bond class: " + it.message
            null
        }
    } else null

    // -- adjacency, which on the honeycomb is a bond of the cross-section and not an index step
    val adjacency: Boolean? = when {
        lattice == null -> null
        cells != null -> crossings.all {
            areHoneycombNeighbours(cells[it.lowerHelix], cells[it.upperHelix])
        }.also {
            if (!it) violations += "a strand crossing joins two helices that are not honeycomb " +
                "neighbours in this design's own cross-section"
        }
        lattice === HoneycombCrossoverLattice -> null
        else -> crossings.all { it.upperHelix - it.lowerHelix == 1 }.also {
            if (!it) violations += "a strand crossing joins two duplexes that are not adjacent " +
                "in this design's own helix ordering, so the row-adjacency graph is not a path " +
                "and the seam parity argument does not apply"
        }
    }

    // -- the width rules, which need a scaffold to be a rule about anything
    val scaffold = strands.filter { it.isScaffold }.singleOrNull()
    val domainsByHelix = scaffold?.domains?.groupBy { it.helix }
    val seamless = domainsByHelix != null &&
        domainsByHelix.size == helixCount && domainsByHelix.values.all { it.size == 1 }
    if (scaffold == null) {
        notApplicable += "this design carries ${strands.count { it.isScaffold }} scaffold " +
            "strands, and both the run-length rule and the closure are statements about the " +
            "distance between two successive scaffold crossovers"
    }

    val widthApplies = scaffold != null && lattice === SquareCrossoverLattice && seamless
    val rowAdmissible: Boolean? = if (widthApplies) {
        squareSeamlessRowWidthIsAdmissible(rowBasePairs()).also {
            if (!it) violations += squareSeamlessRowWidthViolation(rowBasePairs())
        }
    } else null

    val runRuleApplies = scaffold != null && lattice != null && adjacency == true
    val runsAdmissible: Boolean? = if (runRuleApplies) {
        val bad = inadmissibleScaffoldRuns()
        bad.forEach { violations += it.describe(lattice!!) }
        bad.isEmpty()
    } else null

    // -- the honeycomb's own global condition, which is not a width rule at all
    val closure = if (cells != null && adjacency == true && scaffold != null) {
        honeycombClosure()
    } else null
    if (closure != null && !closure.closes) {
        violations += "no lattice constant b₀ serves this raster: reduced by their own bond " +
            "class its scaffold crossovers occupy ${closure.distinctReducedResidues}, and " +
            "caDNAno's ±5 bp rule admits at most two values exactly ten apart, so " +
            "${closure.forcedCrossovers} of ${closure.crossovers.size} crossovers would have to " +
            "be FORCED"
    }
    if (lattice === HoneycombCrossoverLattice && crossings.any { !it.onScaffold }) {
        notApplicable += "the honeycomb STAPLE-crossover residue rule — a staple crossover sits " +
            "at b₀ + 7·class exactly — is withheld: this corpus has never determined a " +
            "honeycomb staple routing, and nothing in it fixes the datum relating a staple " +
            "crossing's offset to the scaffold's own level"
    }

    return LatticeBuildabilityReport(
        grid = grid,
        lattice = lattice?.name,
        verdict = when {
            violations.isNotEmpty() -> BuildabilityVerdict.VIOLATIONS
            notApplicable.isNotEmpty() -> BuildabilityVerdict.INCONCLUSIVE
            else -> BuildabilityVerdict.ADMISSIBLE
        },
        rowBasePairs = if (scaffold != null) rowBasePairs() else null,
        axialSpanBasePairs = axialSpanBasePairs(),
        isSeamlessRaster = seamless,
        seamlessRowWidthIsAdmissible = rowAdmissible,
        everyScaffoldRunIsAdmissible = runsAdmissible,
        honeycombRasterCloses = closure?.closes,
        honeycombForcedCrossovers = closure?.forcedCrossovers,
        honeycombClassZeroResidues = closure?.classZeroResidueCandidates ?: emptyList(),
        everyStrandCrossingJoinsLatticeNeighbours = adjacency,
        noSiteIsCrossedTwice = single,
        carriesInsertionsOrDeletions = modified,
        violations = violations,
        notApplicable = notApplicable
    )
}
