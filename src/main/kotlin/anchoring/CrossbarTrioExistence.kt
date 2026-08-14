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
import org.openrndr.math.Vector3
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * `T-127` — does a torsion-feasible trio exist on `C-0048`'s cap crossbar **at all**?
 *
 * `C-0059` re-derived the truss branch's three junctions on `C-0057`'s torsion-feasible set and
 * found that the single junction closes exactly aligned, the pair closes at every separation, and
 * the **trio does not close** — *"0 of the 24 best-aligned of 750 reach-feasible lattices, 134
 * junction solves"*. It labelled that a **"not found within the budget"** rather than a refusal,
 * and named the budget: 24 of 750 lattices, **two** candidate azimuths per junction, **two**
 * crossbar lengths, **one** row pitch.
 *
 * This file is the instrument that decides whether that negative belongs to the geometry or to
 * the search. It contains three things.
 *
 * ### 1. A per-assignment-pruned closure verdict, which is the cheap bound applied to the solver
 *
 * `bestLinkClosure` solves **32** assignments of donor end, strand polarity and sugar pucker, each
 * a two-dimensional grid search with refinements — 0.77 s per junction at `C-0059`'s own grid.
 * `C-0057`'s reach bound is a **per-assignment proof of exclusion**: an assignment whose
 * `O3′···C5′` separation lies outside `[0.2228, 0.4095]` nm closes at **no** torsion whatever, and
 * a closure with every residual inside the 3 σ ceiling necessarily lies inside it. So an
 * assignment that fails the bound need not be solved. **12 % of assignments survive, which is a
 * 17.2× speedup**, and [linkClosesOnSomeAssignment] is asserted equal to `bestLinkClosure(…).closes`
 * as a gate-2 test. That is what makes a 30 000-lattice sweep a 30-minute study.
 *
 * ### 2. A marginal closure census, which is the cheap bound applied to the question
 *
 * The three junctions are **conditionally independent given the lattice** — they share the
 * crossbar's helical phase, its axial phase and the lateral seat and nothing else, and the only
 * couplings (six distinct targets, no termini in contact) are arithmetic. So the per-junction rate
 * `q` at which a lattice carries at least one torsion-closing placement is measurable *before* any
 * joint search, and [independenceYield] says what a budget of `N` lattices should be expected to
 * return. **If any `q` is zero the trio is dead by a necessary condition, cheaply**; if none is,
 * the prediction says whether `C-0059`'s 24 lattices could have found anything.
 *
 * ### 3. The deepened sweep, whose budget is a reported number
 *
 * Every lattice of a stated `(phase × axial phase × lateral seat)` grid, at every
 * `(crossbar length, row pitch)` configuration in the admissible band, with **all** reach-feasible
 * candidate azimuths solved at every junction rather than the best-aligned two.
 *
 * ### Conventions, restated
 *
 * Lengths nm, angles radians unless a name says degrees. The crossbar is a lone duplex along `x̂`
 * through the origin; a leg stands along `+ẑ` below it; the flexure arrives along `−ŷ`. Rise
 * 0.34 nm/bp, 10.67 bp/turn, phosphate radius 1.00 nm, minor groove 120° — `C-0029`'s geometry,
 * which `C-0052`, `C-0057` and `C-0059` all run on. **A torsion check is a NECESSARY condition and
 * never a sufficient one**, so a *"closes"* verdict here is an upper bound on buildability.
 */

// ---------------------------------------------------------------- the lattice

/**
 * The crossbar's phosphate lattice at a helical [helicalPhase] and axial phase [axialPhase] —
 * `C-0059`'s own [crossbarLinkAnchor] construction, exposed as the list a placement search needs.
 *
 * Asserted target-for-target equal to [crossbarLinkAnchor] as a gate-3 test, so that the lattice
 * this task sweeps **is** the lattice `C-0059` solved.
 */
fun crossbarPhosphateLattice(
    backbone: DuplexBackbone,
    crossbarBasePairs: Int,
    helicalPhase: Double,
    axialPhase: Double
): List<Pair<CrossbarTarget, Vector3>> {
    require(crossbarBasePairs >= 2) {
        "crossbarBasePairs must be at least two, was: $crossbarBasePairs"
    }
    val groove = backbone.minorGrooveAngle * PI / 180.0
    val out = ArrayList<Pair<CrossbarTarget, Vector3>>(2 * crossbarBasePairs)
    for (index in 0 until crossbarBasePairs) {
        for (strand in 0..1) {
            val angle = helicalPhase + index * backbone.twistPerBasePair + strand * groove
            out += CrossbarTarget(strand, index) to Vector3(
                axialPhase + (index - 0.5 * (crossbarBasePairs - 1)) * backbone.risePerBasePair,
                backbone.phosphateRadius * cos(angle),
                backbone.phosphateRadius * sin(angle)
            )
        }
    }
    return out
}

// ---------------------------------------------------------------- the pruned closure verdict

/**
 * **Does this link close at ANY assignment the design may choose?**
 *
 * Identical in value to `bestLinkClosure(link.seat, link.standoff, …).closes`, and 17.2× cheaper,
 * for two reasons that are both exact rather than approximate.
 *
 * 1. **The reach bound prunes assignments.** `linkReach` is a closed-form interval in the single
 *    torsion `α`, widened by three measured σ on every bond and angle the chain
 *    `O3′–P–O5′–C5′` contains. A closure whose residuals are all inside the 3 σ strain ceiling
 *    therefore has its `O3′···C5′` separation inside that interval, so `!freeFeasible` implies
 *    *cannot close* — for **that** assignment, which is the granularity `bestLinkReach` throws away
 *    by reporting the best over all 32.
 * 2. **A boolean needs no ranking.** `closePhosphodiester` minimises `(excess, −occupancy, strain)`
 *    lexicographically, so its optimum has zero excess whenever any grid point does and carries the
 *    best-populated torsions among those; `closes` is therefore a property of the *set* of grid
 *    points and not of the order the assignments are visited.
 */
fun linkClosesOnSomeAssignment(
    link: JunctionLinkEnds,
    reading: PhosphateReading = PhosphateReading.FREE,
    templates: List<NucleotideTemplate> = NucleotideTemplate.ALL,
    gridSteps: Int = 60,
    refinements: Int = 4
): Boolean {
    require(templates.isNotEmpty()) { "templates must not be empty" }
    for (donorIsFirst in listOf(true, false)) {
        val donorAnchor = if (donorIsFirst) link.seat else link.standoff
        val acceptorAnchor = if (donorIsFirst) link.standoff else link.seat
        for (donorPolarity in listOf(1, -1)) {
            for (acceptorPolarity in listOf(1, -1)) {
                for (donorTemplate in templates) {
                    for (acceptorTemplate in templates) {
                        val donor = PlacedResidue(
                            DuplexSite(
                                donorAnchor.phosphate, donorAnchor.axisPoint,
                                donorAnchor.axisDirection, donorPolarity
                            ),
                            donorTemplate
                        )
                        val acceptor = PlacedResidue(
                            DuplexSite(
                                acceptorAnchor.phosphate, acceptorAnchor.axisPoint,
                                acceptorAnchor.axisDirection, acceptorPolarity
                            ),
                            acceptorTemplate
                        )
                        if (!linkReach(donor, acceptor).freeFeasible) continue
                        if (closePhosphodiester(
                                donor, acceptor, reading, gridSteps, refinements, donorIsFirst
                            ).closes
                        ) {
                            return true
                        }
                    }
                }
            }
        }
    }
    return false
}

/** Every link of one junction closing — `C-0057`'s junction-level verdict, pruned. */
fun junctionClosesOnSomeAssignment(
    links: List<JunctionLinkEnds>,
    gridSteps: Int = 60,
    refinements: Int = 4
): Boolean {
    require(links.isNotEmpty()) { "links must not be empty" }
    return links.all { linkClosesOnSomeAssignment(it, gridSteps = gridSteps, refinements = refinements) }
}

// ---------------------------------------------------------------- the budget, as a first-class number

/**
 * **What a negative existence result costs, stated as counts.**
 *
 * `C-0059`'s trio verdict was *"0 of the 24 best-aligned of 750 reach-feasible lattices, 134
 * junction solves"* — a negative that names its budget, which is the only kind that is falsifiable.
 * This carries the same statement one level of detail further, and adds componentwise so that a
 * sweep over many configurations reports one budget.
 */
data class SearchBudget(
    val configurations: Int,
    /** Lattices enumerated — `phaseSteps × axialSteps × lateralSeats` per configuration. */
    val lattices: Int,
    /** Lattices at which a torsion solve was actually attempted — `C-0059`'s **24**. */
    val latticesSolved: Int,
    /** `junctions × latticesExamined` — how many junction placement problems were posed. */
    val junctionSlots: Int,
    /** Covalent, reach-feasible candidate azimuths found across those slots. */
    val candidateAzimuths: Int,
    /** Junction closure solves actually run — the expensive count. */
    val junctionSolves: Int,
    /** Inverse-kinematic link closures actually run — two per junction solve. */
    val linkClosures: Int,
    /** Lattices carrying a complete torsion-closing trio. */
    val closingLattices: Int
) {

    init {
        require(configurations >= 0 && lattices >= 0 && junctionSolves >= 0) {
            "a budget is a set of counts and must not be negative"
        }
    }

    operator fun plus(other: SearchBudget): SearchBudget = SearchBudget(
        configurations = configurations + other.configurations,
        lattices = lattices + other.lattices,
        latticesSolved = latticesSolved + other.latticesSolved,
        junctionSlots = junctionSlots + other.junctionSlots,
        candidateAzimuths = candidateAzimuths + other.candidateAzimuths,
        junctionSolves = junctionSolves + other.junctionSolves,
        linkClosures = linkClosures + other.linkClosures,
        closingLattices = closingLattices + other.closingLattices
    )

    companion object {

        val EMPTY: SearchBudget = SearchBudget(0, 0, 0, 0, 0, 0, 0, 0)
    }
}

// ---------------------------------------------------------------- the cheap bound: the marginal census

/**
 * How often **one** junction, on its own, finds a torsion-closing placement on a lattice.
 *
 * @property lattices lattices examined for this junction.
 * @property candidates covalent, reach-feasible azimuths found across them.
 * @property closingCandidates how many of those close at torsion level.
 * @property closingLattices lattices carrying **at least one** closing placement — the quantity a
 *   trio needs, because the trio needs one placement per junction and not a particular one.
 */
data class MarginalClosureCensus(
    val junction: String,
    val lattices: Int,
    val candidates: Int,
    val closingCandidates: Int,
    val closingLattices: Int
) {

    init {
        require(lattices > 0) { "lattices must be positive, was: $lattices" }
        require(closingLattices <= lattices) { "a closing lattice is a lattice" }
        require(closingCandidates <= candidates) { "a closing candidate is a candidate" }
    }

    /** The rate a trio actually needs: `P(this junction closes somewhere on a lattice)`. */
    val latticeRate: Double get() = closingLattices.toDouble() / lattices

    /** The per-placement rate, reported because it is what a per-junction solve cap consumes. */
    val candidateRate: Double
        get() = if (candidates == 0) 0.0 else closingCandidates.toDouble() / candidates
}

/**
 * **The prediction the whole task turns on**: how many of [lattices] should carry a closing trio if
 * the junctions are independent given the lattice — `N × Π q_j`.
 *
 * It is a *predictor*, not a bound, in the direction of a positive; but it is a **bound** in the
 * direction of a negative, because `q_j = 0` for any junction makes a trio impossible outright.
 */
fun independenceYield(census: List<MarginalClosureCensus>, lattices: Int): Double {
    require(census.isNotEmpty()) { "census must not be empty" }
    require(lattices >= 0) { "lattices must not be negative, was: $lattices" }
    return lattices * census.fold(1.0) { product, entry -> product * entry.latticeRate }
}

// ---------------------------------------------------------------- one configuration

/**
 * A `(crossbar length, leg row pitch)` pair — the two discrete design variables `C-0059` did not
 * sweep, and the pair that must be *the same number at the base and at the cap*, because the row
 * pitch is the legs' separation and a leg has only one of those.
 */
data class TrioConfiguration(
    val crossbarBasePairs: Int,
    val separationBasePairs: Int,
    val rise: Double = Gen1Tile.RISE_PER_BASE_PAIR
) {

    init {
        require(separationBasePairs >= 1) {
            "separationBasePairs must be at least one, was: $separationBasePairs"
        }
        require(crossbarBasePairs >= minimumCrossbar(separationBasePairs, rise)) {
            "a crossbar of $crossbarBasePairs bp does not cover a $separationBasePairs bp row"
        }
    }

    val geometry: CrossbarGeometry
        get() = CrossbarGeometry(crossbarBasePairs, separationBasePairs, rise)

    val overhangBasePairs: Int
        get() = crossbarBasePairs - minimumCrossbar(separationBasePairs, rise)

    override fun toString(): String =
        "$crossbarBasePairs bp crossbar / $separationBasePairs bp row"

    companion object {

        /** `C-0048`'s `ceil((w + 2R)/rise)` — the row plus six, at the standard rise and radius. */
        fun minimumCrossbar(
            separationBasePairs: Int,
            rise: Double = Gen1Tile.RISE_PER_BASE_PAIR,
            legRadius: Double = BForm.DUPLEX_RADIUS
        ): Int = ceil((separationBasePairs * rise + 2.0 * legRadius) / rise).toInt()

        /**
         * The admissible band this task sweeps: every row pitch `C-0042` admits, at the minimum
         * crossbar its own `ceil` demands and the next [overhangs] lengths above it.
         */
        fun band(
            rows: IntRange = 6..12,
            overhangs: Int = 2,
            rise: Double = Gen1Tile.RISE_PER_BASE_PAIR
        ): List<TrioConfiguration> = rows.flatMap { row ->
            (0..overhangs).map { extra ->
                TrioConfiguration(minimumCrossbar(row, rise) + extra, row, rise)
            }
        }
    }
}

// ---------------------------------------------------------------- the trio a sweep returns

/** One complete torsion-closing trio, with everything a design or a re-solve needs. */
data class ClosingTrio(
    val crossbarBasePairs: Int,
    val separationBasePairs: Int,
    val helicalPhaseDegrees: Double,
    val axialPhase: Double,
    val lateralSeat: Double,
    val worstMisalignmentDegrees: Double,
    val legMisalignmentDegrees: Double,
    val flexureMisalignmentDegrees: Double,
    val azimuthDegrees: List<Double>,
    val chordAzimuthDegrees: List<Double>,
    val worstGap: Double,
    val minimumTerminusSeparation: Double,
    val minimumSeatContact: Double,
    val distinctTargets: Boolean
)

/** What a sweep of one configuration, or of a whole band, returns. */
data class TrioExistenceOutcome(
    val configuration: String,
    val crossbarBasePairs: Int,
    val separationBasePairs: Int,
    val budget: SearchBudget,
    /** Lattices at which every junction had a covalent, reach-feasible placement. */
    val reachFeasibleLattices: Int,
    val closes: Boolean,
    /** Every closing trio found, best-aligned first, capped for the record's size. */
    val trios: List<ClosingTrio>,
    /**
     * How often the per-junction closer cap bound the combination stage — a lattice at which every
     * junction closed but no *collected* combination had six distinct targets. Reported because it
     * is the one place a cap could manufacture a negative.
     */
    val combinationsExhausted: Int,
    /**
     * The closing trio the **design** wants, which is not the one the headline reports: the
     * mechanics is driven by the *leg's* cap chord (`C-0048`'s `capMisalignment`), and the flexure's
     * own chord enters separately. Minimising the worst of the three is the honest existence
     * headline; minimising the leg's is what feeds `feasibleTrussDesign`.
     */
    val bestForCap: ClosingTrio?,
    val verdict: String
) {

    val best: ClosingTrio? get() = trios.firstOrNull()
}

// ---------------------------------------------------------------- the deepened sweep

/**
 * `C-0052`'s three junctions on one lone crossbar, swept for **existence** rather than for
 * alignment.
 *
 * The differences from `C-0059`'s [TorsionFeasibleTrioSearch], which are the whole task:
 *
 * | axis | `C-0059` | here |
 * |---|---|---|
 * | lattices solved | the **24** best-aligned | **every** lattice of the grid (or [latticeCap]) |
 * | candidate azimuths per junction | **2** | **all** reach-feasible ones |
 * | crossbar lengths | 13, 15 | the whole admissible band |
 * | row pitches | 7 | 6–12 |
 * | ranking | by alignment, and it decides which lattices are solved | by alignment, and it decides only which trio is *reported* |
 *
 * Every junction placement problem is independent given the lattice, so the sweep parallelises over
 * lattices; each unit is a pure function of its own arguments and the results are recombined in
 * lattice-index order, so the outcome is **deterministic** and independent of the thread count.
 */
class CrossbarTrioExistenceSearch(
    val configuration: TrioConfiguration,
    val backbone: DuplexBackbone = DuplexBackbone(),
    val junctions: List<TrioJunctionSpec> =
        TrioJunctionSpec.cap(configuration.separationBasePairs, configuration.rise),
    val azimuthSteps: Int = 120,
    val phaseSteps: Int = 90,
    val axialSteps: Int = 4,
    val lateralSeats: List<Double> = listOf(-0.4, -0.2, 0.0, 0.2, 0.4),
    val contactFloor: Double = 1.6,
    /** `C-0059` used **2**; the default here is every reach-feasible azimuth. */
    val candidatesPerJunction: Int = Int.MAX_VALUE,
    /** How many closing placements to collect per junction before combining. */
    val closersPerJunction: Int = 8,
    /** `C-0059` used **24**; the default here is every lattice. */
    val latticeCap: Int = Int.MAX_VALUE,
    /** `C-0059` ranked the lattices by alignment and solved the best; here that is opt-in. */
    val rankByAlignment: Boolean = false,
    val gridSteps: Int = 60,
    val refinements: Int = 4,
    val reportedTrios: Int = 8,
    val threads: Int = min(6, Runtime.getRuntime().availableProcessors())
) {

    init {
        require(junctions.isNotEmpty()) { "junctions must not be empty" }
        require(azimuthSteps >= 1) { "azimuthSteps must be positive, was: $azimuthSteps" }
        require(phaseSteps >= 1) { "phaseSteps must be positive, was: $phaseSteps" }
        require(axialSteps >= 1) { "axialSteps must be positive, was: $axialSteps" }
        require(lateralSeats.isNotEmpty()) { "lateralSeats must not be empty" }
        require(candidatesPerJunction >= 1) {
            "candidatesPerJunction must be positive, was: $candidatesPerJunction"
        }
        require(closersPerJunction >= 1) {
            "closersPerJunction must be positive, was: $closersPerJunction"
        }
        require(latticeCap >= 0) { "latticeCap must not be negative, was: $latticeCap" }
        require(threads >= 1) { "threads must be positive, was: $threads" }
    }

    /** `C-0059`'s own placement test, re-run as a library — nothing about the geometry is new. */
    private val placements = TorsionFeasibleTrioSearch(
        backbone = backbone,
        crossbarBasePairs = configuration.crossbarBasePairs,
        separationBasePairs = configuration.separationBasePairs,
        junctions = junctions,
        azimuthSteps = azimuthSteps,
        contactFloor = contactFloor,
        gridSteps = gridSteps,
        refinements = refinements
    )

    private val geometry = configuration.geometry

    private data class LatticeSite(val phase: Double, val axial: Double, val lateral: Double)

    private val sites: List<LatticeSite> = buildList {
        val axialLimit = backbone.risePerBasePair
        lateralSeats.forEach { lateral ->
            for (p in 0 until phaseSteps) {
                for (a in 0 until axialSteps) {
                    add(
                        LatticeSite(
                            2.0 * PI * p / phaseSteps, axialLimit * a / axialSteps, lateral
                        )
                    )
                }
            }
        }
    }

    /** Every covalent, reach-feasible placement of one junction at one lattice, best-aligned first. */
    private fun candidatesAt(
        spec: TrioJunctionSpec,
        site: LatticeSite,
        lattice: List<Pair<CrossbarTarget, Vector3>>
    ): List<TrioPlacement> {
        val covalent = ArrayList<TrioPlacement>()
        for (i in 0 until azimuthSteps) {
            val azimuth = i * 2.0 * PI / azimuthSteps
            val placement = placements.placementAt(spec, site.lateral, azimuth, lattice) ?: continue
            if (!placement.covalent) continue
            covalent += placement
        }
        val out = ArrayList<TrioPlacement>()
        covalent.sortedWith(compareBy({ it.misalignment }, { it.azimuth })).forEach { placement ->
            if (out.size >= candidatesPerJunction) return@forEach
            val links = junctionLinks(
                backbone, configuration.crossbarBasePairs, site.phase, site.axial, placement
            )
            if (reachVerdict(links).feasible) out += placement
        }
        return out
    }

    private data class LatticeOutcome(
        val site: LatticeSite,
        val allJunctionsReachable: Boolean,
        val solved: Boolean,
        val candidates: Int,
        val solves: Int,
        val trio: ClosingTrio?,
        val combinationExhausted: Boolean
    )

    /**
     * One lattice, evaluated whole. Pure: it reads only its arguments and the immutable
     * configuration, which is what licenses the parallel sweep.
     */
    private fun evaluate(site: LatticeSite): LatticeOutcome {
        val lattice = crossbarPhosphateLattice(
            backbone, configuration.crossbarBasePairs, site.phase, site.axial
        )
        val candidates = junctions.map { candidatesAt(it, site, lattice) }
        val candidateCount = candidates.sumOf { it.size }
        if (candidates.any { it.isEmpty() }) {
            return LatticeOutcome(site, false, false, candidateCount, 0, null, false)
        }
        // The junction with the fewest candidates is solved first, so a lattice that cannot carry
        // a trio is abandoned at the cheapest possible point. It changes no verdict: a trio needs
        // all three, so any junction closing nowhere kills the lattice whichever order they run in.
        val order = junctions.indices.sortedWith(
            compareBy({ candidates[it].size }, { it })
        )
        var solves = 0
        val closers = arrayOfNulls<List<TrioPlacement>>(junctions.size)
        order.forEach { index ->
            if (closers.any { it != null && it.isEmpty() }) return@forEach
            val found = ArrayList<TrioPlacement>()
            for (placement in candidates[index]) {
                if (found.size >= closersPerJunction) break
                solves++
                val links = junctionLinks(
                    backbone, configuration.crossbarBasePairs, site.phase, site.axial, placement
                )
                if (junctionClosesOnSomeAssignment(links, gridSteps, refinements)) found += placement
            }
            closers[index] = found
        }
        if (closers.any { it == null || it.isEmpty() }) {
            return LatticeOutcome(site, true, true, candidateCount, solves, null, false)
        }
        val collected = closers.map { it!! }
        var best: CrossbarTrioClosure? = null
        combinations(collected).forEach { combination ->
            val closure = CrossbarTrioClosure(
                placements = combination,
                crossbarBasePairs = configuration.crossbarBasePairs,
                separationBasePairs = configuration.separationBasePairs,
                helicalPhase = site.phase,
                axialPhase = site.axial,
                lateralOffset = site.lateral,
                legFlexureClearance = geometry.legFlexureClearance,
                minimumSeatContact = combination.minOf { it.seatContact }
            )
            if (!closure.distinctTargets || !closure.terminiClear) return@forEach
            val incumbent = best
            if (incumbent == null || closure.worstMisalignment < incumbent.worstMisalignment) {
                best = closure
            }
        }
        val found = best
        return LatticeOutcome(
            site = site,
            allJunctionsReachable = true,
            solved = true,
            candidates = candidateCount,
            solves = solves,
            trio = found?.let { trioOf(it, site) },
            combinationExhausted = found == null
        )
    }

    private fun trioOf(closure: CrossbarTrioClosure, site: LatticeSite): ClosingTrio {
        val legs = closure.placements.filter { it.kind == TrioJunctionKind.LEG }
        val flexure = closure.placements.firstOrNull { it.kind == TrioJunctionKind.FLEXURE }
        return ClosingTrio(
            crossbarBasePairs = configuration.crossbarBasePairs,
            separationBasePairs = configuration.separationBasePairs,
            helicalPhaseDegrees = site.phase * 180.0 / PI,
            axialPhase = site.axial,
            lateralSeat = site.lateral,
            worstMisalignmentDegrees = closure.worstMisalignment * 180.0 / PI,
            legMisalignmentDegrees = (legs.maxOfOrNull { it.misalignment } ?: 0.0) * 180.0 / PI,
            flexureMisalignmentDegrees = (flexure?.misalignment ?: 0.0) * 180.0 / PI,
            azimuthDegrees = closure.placements.map { it.azimuth * 180.0 / PI },
            chordAzimuthDegrees = closure.placements.map { it.chordAzimuth * 180.0 / PI },
            worstGap = closure.worstGap,
            minimumTerminusSeparation = closure.minimumTerminusSeparation,
            minimumSeatContact = closure.minimumSeatContact,
            distinctTargets = closure.distinctTargets
        )
    }

    private fun combinations(sets: List<List<TrioPlacement>>): Sequence<List<TrioPlacement>> =
        sequence {
            val indices = IntArray(sets.size)
            while (true) {
                yield(sets.indices.map { sets[it][indices[it]] })
                var carry = sets.size - 1
                while (carry >= 0) {
                    indices[carry]++
                    if (indices[carry] < sets[carry].size) break
                    indices[carry] = 0
                    carry--
                }
                if (carry < 0) break
            }
        }

    /**
     * The whole sweep. Deterministic: the lattice order is fixed, each lattice is a pure function,
     * and the results are recombined in index order whatever the thread count.
     */
    fun sweep(): TrioExistenceOutcome {
        // The reach filter is cheap, so when the lattices are ranked (C-0059's mode) it runs on
        // every one of them first and only the best-aligned `latticeCap` are solved.
        val outcomes: List<LatticeOutcome> = if (rankByAlignment) {
            val scored = solveAll(sites) { site ->
                val lattice = crossbarPhosphateLattice(
                    backbone, configuration.crossbarBasePairs, site.phase, site.axial
                )
                val candidates = junctions.map { candidatesAt(it, site, lattice) }
                Triple(
                    site,
                    candidates,
                    if (candidates.any { it.isEmpty() }) PI
                    else candidates.maxOf { it.first().misalignment }
                )
            }
            val admissible = scored.filter { it.third < PI }
            val ranked = admissible.sortedWith(
                compareBy({ it.third }, { it.first.lateral }, { it.first.phase }, { it.first.axial })
            ).take(latticeCap)
            val solved = solveAll(ranked.map { it.first }) { evaluate(it) }
            val chosen = ranked.map { it.first }.toSet()
            solved + scored.filter { it.first !in chosen }.map {
                LatticeOutcome(
                    it.first, it.third < PI, false, it.second.sumOf { c -> c.size }, 0, null, false
                )
            }
        } else {
            solveAll(sites.take(if (latticeCap == Int.MAX_VALUE) sites.size else latticeCap)) {
                evaluate(it)
            }
        }
        val reachFeasible = outcomes.count { it.allJunctionsReachable }
        val trios = outcomes.mapNotNull { it.trio }
            .sortedWith(
                compareBy(
                    { it.worstMisalignmentDegrees }, { it.helicalPhaseDegrees },
                    { it.axialPhase }, { it.lateralSeat }
                )
            )
        val budget = SearchBudget(
            configurations = 1,
            lattices = sites.size,
            latticesSolved = outcomes.count { it.solved },
            junctionSlots = junctions.size * outcomes.size,
            candidateAzimuths = outcomes.sumOf { it.candidates },
            junctionSolves = outcomes.sumOf { it.solves },
            linkClosures = 2 * outcomes.sumOf { it.solves },
            closingLattices = trios.size
        )
        return TrioExistenceOutcome(
            configuration = configuration.toString(),
            crossbarBasePairs = configuration.crossbarBasePairs,
            separationBasePairs = configuration.separationBasePairs,
            budget = budget,
            reachFeasibleLattices = reachFeasible,
            closes = trios.isNotEmpty(),
            trios = trios.take(reportedTrios),
            combinationsExhausted = outcomes.count { it.combinationExhausted },
            bestForCap = trios.minWithOrNull(
                compareBy(
                    { it.legMisalignmentDegrees }, { it.flexureMisalignmentDegrees },
                    { it.worstMisalignmentDegrees }, { it.helicalPhaseDegrees }
                )
            ),
            verdict = when {
                trios.isNotEmpty() ->
                    ("A TORSION-CLOSING TRIO EXISTS on the %s — %d of %d lattices carry one, " +
                            "best worst chord %.1f° off the direction its own body wants").format(
                        configuration, trios.size, reachFeasible,
                        trios.first().worstMisalignmentDegrees
                    )

                reachFeasible == 0 ->
                    "NO lattice of the ${sites.size} admits a reach-feasible placement of all " +
                            "${junctions.size} junctions on the $configuration"

                else -> ("NO torsion-closing trio in the %d lattices SOLVED of %d reach-feasible " +
                        "of %d enumerated on the %s, %d junction solves").format(
                    budget.latticesSolved, reachFeasible, sites.size, configuration,
                    budget.junctionSolves
                )
            }
        )
    }

    /**
     * The marginal closure census of one junction — the cheap bound, and the only thing that can
     * settle the question without a joint search.
     */
    fun marginalCensus(spec: TrioJunctionSpec, latticeSample: Int = 90): MarginalClosureCensus {
        require(latticeSample >= 1) { "latticeSample must be positive, was: $latticeSample" }
        val step = maxOf(1, sites.size / latticeSample)
        val sampled = sites.filterIndexed { index, _ -> index % step == 0 }.take(latticeSample)
        val results = solveAll(sampled) { site ->
            val lattice = crossbarPhosphateLattice(
                backbone, configuration.crossbarBasePairs, site.phase, site.axial
            )
            val candidates = candidatesAt(spec, site, lattice)
            val closing = candidates.count { placement ->
                junctionClosesOnSomeAssignment(
                    junctionLinks(
                        backbone, configuration.crossbarBasePairs, site.phase, site.axial, placement
                    ),
                    gridSteps, refinements
                )
            }
            candidates.size to closing
        }
        return MarginalClosureCensus(
            junction = spec.name,
            lattices = results.size,
            candidates = results.sumOf { it.first },
            closingCandidates = results.sumOf { it.second },
            closingLattices = results.count { it.second > 0 }
        )
    }

    private fun <T, R> solveAll(items: List<T>, work: (T) -> R): List<R> {
        if (items.isEmpty()) return emptyList()
        if (threads == 1 || items.size == 1) return items.map(work)
        val pool = Executors.newFixedThreadPool(threads)
        try {
            val futures = items.map { item -> pool.submit(Callable { work(item) }) }
            return futures.map { it.get() }
        } finally {
            pool.shutdown()
            pool.awaitTermination(1, TimeUnit.DAYS)
        }
    }
}
