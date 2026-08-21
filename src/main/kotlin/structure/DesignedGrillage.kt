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

package com.xemantic.nano.plentyofroom.structure

import com.xemantic.nano.plentyofroom.design.DESIGN_DIRECTORY
import com.xemantic.nano.plentyofroom.design.DesignCrossover
import com.xemantic.nano.plentyofroom.design.ScadnanoDesign
import com.xemantic.nano.plentyofroom.lattice.CrossoverLattice
import com.xemantic.nano.plentyofroom.lattice.crossoverLatticeOfGrid
import kotlin.math.abs

/**
 * `T-267` — the seam that makes a placement result a property of a **design** rather than of a set
 * of constants.
 *
 * ## What it cost not to have this
 *
 * `OrigamiGrillage` takes its lattice from [Gen1Tile]'s constants, so every placement, phase,
 * plan-ceiling and flatness number in this corpus is a statement about those constants. When
 * iterations 33–34 found that the four-layer cross-section all of them were solved on **is not a
 * honeycomb** — every `edgeY` exactly 1.5× too small — the results were **invalid rather than
 * re-runnable**. That is the whole price of a missing constructor, and it has been paid once.
 *
 * ## The cheap bound, re-read before anything was written
 *
 * `CLAUDE.md`: *"`OrigamiGrillage` NEVER READS `layers` OR `interlayerCoupling` — it takes exactly
 * five scalars from its sheet"* (`interhelicalDistance`, `crossoverSpacing`,
 * `crossoverHingeStiffness`, `duplex.bendingRigidity`, `duplex.torsionalRigidity`). So the surface
 * a design has to supply is small, and exactly one of the five is a **lattice** number rather than
 * a material one: the crossover spacing is `azimuths × step`, which is
 * [CrossoverLattice.samePairPeriodBasePairs] and not a `Gen1Tile` constant. That is the whole
 * substance of [latticeCrossoverSpacing].
 *
 * ## What this is additive to
 *
 * Everything. [OrigamiGrillage]'s `Gen1Tile`-flavoured constructors are untouched, and the gate
 * that says so is `P3`: a grillage built here from a lattice and a cross-section has a load vector
 * **bit-identical** to the one `T-10` builds from constants. `CLAUDE.md` fixes how that comparison
 * must be taken — bit-identity is assertable on `assembleLoad`, a fixed-order scatter-add, and
 * **not** on a solved field, where two identically constructed grillages differ by ~4 ulp inside
 * one JVM.
 */

/** The third-party design this repository grades but did not draw — see `gpd/designs/README.md`. */
const val THIRD_PARTY_RECTANGLE_DESIGN: String =
    "$DESIGN_DIRECTORY/third-party/scadnano-origami-rectangle-16x8.sc"

/**
 * The elasticity, which is the part of a grillage **no design file carries**.
 *
 * A `.sc` file states a lattice, a cross-section and a routing. It does not state a persistence
 * length, a crossover hinge constant or a stretch modulus, and it never will — those are material
 * measurements, and this repository's own are `Gen1Tile`'s cited constants. Separating them from
 * the geometry is what lets an imported design be graded at all: the design supplies what it knows
 * and the caller supplies, **by name**, what it does not.
 *
 * @param duplex the duplex's own `EI`, `GJ` and `S`.
 * @param crossoverHingeStiffness `k_θ` in `pN·nm/rad` for one antiparallel crossover.
 * @param risePerBasePair the axial rise in nm, which converts every base-pair count in a design
 *          file into a length. A design that states its own rise **overrides** this one, and the
 *          import says so.
 * @param interfaceTwistStiffness the optional inter-crossover duplex twist in series;
 *          infinite by default, which makes `D_⊥` an upper bound.
 * @param source where these numbers came from, carried into the result file.
 */
data class DuplexMechanics(
    val duplex: DnaDuplex,
    val crossoverHingeStiffness: Double,
    val risePerBasePair: Double,
    val interfaceTwistStiffness: Double = Double.POSITIVE_INFINITY,
    val source: String
) {

    init {
        require(crossoverHingeStiffness > 0.0) {
            "crossoverHingeStiffness must be positive, was: $crossoverHingeStiffness"
        }
        require(risePerBasePair > 0.0) {
            "risePerBasePair must be positive, was: $risePerBasePair"
        }
        require(interfaceTwistStiffness > 0.0) {
            "interfaceTwistStiffness must be positive, was: $interfaceTwistStiffness"
        }
        require(source.isNotBlank()) { "a mechanics must say where its numbers came from" }
    }

    companion object {

        /**
         * This repository's own cited constants, exactly as [origamiSheet] assembles them.
         *
         * `crossoverAlpha` is Chen et al.'s fitted bracket (0.6–1.2, 1.0 chosen) and
         * `duplexBendingRigidity` CanDo's 230 pN·nm², both swept by the studies that use them.
         */
        fun gen1(
            crossoverAlpha: Double = 1.0,
            includeInterfaceTwist: Boolean = false,
            duplexBendingRigidity: Double = Gen1Tile.DUPLEX_BENDING_RIGIDITY
        ): DuplexMechanics = DuplexMechanics(
            duplex = DnaDuplex(
                bendingRigidity = duplexBendingRigidity,
                torsionalRigidity = Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY,
                stretchModulus = Gen1Tile.DUPLEX_STRETCH_MODULUS
            ),
            crossoverHingeStiffness = Gen1Tile.crossoverHingeStiffness(crossoverAlpha),
            risePerBasePair = Gen1Tile.RISE_PER_BASE_PAIR,
            interfaceTwistStiffness =
                if (includeInterfaceTwist) Gen1Tile.INTERFACE_TWIST_STIFFNESS
                else Double.POSITIVE_INFINITY,
            source = "Gen1Tile: CanDo EI/GJ, Wang et al. S, Chen et al. k_theta at alpha = " +
                crossoverAlpha + ", Douglas et al. rise"
        )
    }
}

/**
 * How many duplexes the sheet is, and how far apart — the part a design file **does** carry.
 *
 * @param duplexes the beam count; on an imported design it is the helix count and nothing else.
 * @param interhelicalDistance `d` in nm, centre to centre.
 * @param layers carried for provenance only, and deliberately: `CLAUDE.md` records that
 *          `OrigamiGrillage` never reads it, so a specification that quietly accepted `layers > 1`
 *          would promise a thickness the object cannot represent. It is refused here rather than
 *          ignored there.
 * @param source where the two numbers came from — a file's own geometry block, or a constant.
 */
data class SheetCrossSection(
    val duplexes: Int,
    val interhelicalDistance: Double,
    val layers: Int = 1,
    val source: String
) {

    init {
        require(duplexes >= 2) { "duplexes must be at least 2, was: $duplexes" }
        require(interhelicalDistance > 0.0) {
            "interhelicalDistance must be positive, was: $interhelicalDistance"
        }
        require(layers == 1) {
            "a single-layer grillage cannot represent $layers layers: OrigamiGrillage never reads " +
                "`layers`, and the parallel-axis enhancement a second layer buys is an AXIAL " +
                "effect its three degrees of freedom per node have no coordinate for (C-0154). " +
                "Use HoneycombGrillage, which does."
        }
        require(source.isNotBlank()) { "a cross-section must say where its numbers came from" }
    }
}

/**
 * The crossover spacing **along one interface** on [lattice], in nm.
 *
 * `samePairPeriod = azimuths × step`: 4 × 8 = 32 bp on the square lattice, 3 × 7 = 21 on the
 * honeycomb. This is the one of `OrigamiGrillage`'s five scalars that is a *lattice* number, and
 * before this function it was `Gen1Tile.CROSSOVER_SPACING_SHEET_BP` — a constant with a lattice
 * baked into its name.
 */
fun latticeCrossoverSpacing(lattice: CrossoverLattice, mechanics: DuplexMechanics): Double =
    lattice.samePairPeriodBasePairs * mechanics.risePerBasePair

/**
 * A grillage's lattice, cross-section, elasticity and column layout, each with its source declared.
 *
 * The discipline is [com.xemantic.nano.plentyofroom.environment.Regime]'s, one layer down: a
 * specification carries **the state it was read at**, and [reasonToRefuse] returns a *reason*
 * rather than a boolean, because a bare boolean is what a reader ignores.
 *
 * @param columns where the crossover columns sit and which interface parity each serves. Held
 *          explicitly rather than derived from a phase, because a design's own columns need not
 *          alternate in parity at all — a **seam** breaks the alternation, and every phase sweep in
 *          this corpus is over the alternating family that [CrossoverLayout.centred] and
 *          [CrossoverLayout.phased] generate.
 * @param absentCrossovers sites the parity lattice offers and the design does **not** build. This
 *          is what makes an import *exact* rather than approximate: the difference between the
 *          lattice and the design is expressible, in `T-110`'s own design variable.
 */
data class GrillageSpecification(
    val name: String,
    val lattice: CrossoverLattice,
    val crossSection: SheetCrossSection,
    val mechanics: DuplexMechanics,
    val lengthX: Double,
    val columns: CrossoverLayout,
    val absentCrossovers: Set<CrossoverSite> = emptySet(),
    val source: String
) {

    init {
        require(name.isNotBlank()) { "a specification must be named" }
        require(lattice.singleLayerInterfacesFormAPath) {
            "a single-layer sheet on the ${lattice.name} lattice does not have path-graph " +
                "interfaces, and OrigamiGrillage bonds beam i to beam i+1 and to nothing else " +
                "(C-0056, CH-0066). A honeycomb site has three lattice neighbours and only half " +
                "its in-plane adjacent pairs are bonded, so one layer of such a block is a set of " +
                "DIMERS and not a sheet (C-0154). Use HoneycombGrillage."
        }
        require(lengthX > 0.0) { "lengthX must be positive, was: $lengthX" }
        require(source.isNotBlank()) { "a specification must say where it came from" }
    }

    /** The crossover spacing along one interface, in nm — the lattice's period at this rise. */
    val crossoverSpacing: Double get() = latticeCrossoverSpacing(lattice, mechanics)

    /** The footprint across the helices, in nm. */
    val lengthY: Double get() = crossSection.duplexes * crossSection.interhelicalDistance

    /**
     * The [OrigamiSheet] this specification describes.
     *
     * Assembled to be **identical** to [origamiSheet]'s output on the same inputs, which is a
     * gate rather than an intention: `T-10`'s reproduction rests on it.
     */
    val sheet: OrigamiSheet
        get() = OrigamiSheet(
            duplex = mechanics.duplex,
            interhelicalDistance = crossSection.interhelicalDistance,
            crossoverSpacing = crossoverSpacing,
            crossoverHingeStiffness = mechanics.crossoverHingeStiffness,
            interfaceTwistStiffness = mechanics.interfaceTwistStiffness,
            layers = crossSection.layers,
            layerSpacing = crossSection.interhelicalDistance
        )

    /** The lattice, at a stated foundation. The environment is never part of a specification. */
    fun grillage(
        foundationStiffness: Double,
        subdivisions: Int = OrigamiGrillage.DEFAULT_SUBDIVISIONS,
        linkStiffness: Double = OrigamiGrillage.RIGID_LINK_STIFFNESS,
        supports: List<PointSupport> = emptyList(),
        softenedCrossovers: Map<CrossoverSite, CrossoverSoftening> = emptyMap(),
        crossoverPrestrains: Map<CrossoverSite, Double> = emptyMap()
    ): OrigamiGrillage = OrigamiGrillage(
        sheet = sheet,
        lengthX = lengthX,
        beamCount = crossSection.duplexes,
        foundationStiffness = foundationStiffness,
        columns = columns,
        subdivisions = subdivisions,
        linkStiffness = linkStiffness,
        supports = supports,
        consumedCrossovers = absentCrossovers,
        softenedCrossovers = softenedCrossovers,
        crossoverPrestrains = crossoverPrestrains
    )

    /**
     * Why a number solved on [source] may not be quoted about this specification, or `null`.
     *
     * The order of the checks is the order this corpus has been bitten in: the **lattice** first,
     * because that is the honeycomb correction of iterations 33–34; then the cross-section, then
     * the footprint, then the column layout — which is last only because it is the one a reader is
     * most likely to notice.
     */
    fun reasonToRefuse(source: GrillageSpecification): String? {
        if (lattice.name != source.lattice.name) {
            return "$name is on the ${lattice.name} lattice and ${source.name} on the " +
                "${source.lattice.name} one; the crossover period, the station ladder and the " +
                "register departure are all different, and transferring one lattice's phase " +
                "congruence onto another is what C-0141 had to undo"
        }
        if (crossSection.duplexes != source.crossSection.duplexes) {
            return "$name is ${crossSection.duplexes} duplexes and ${source.name} is " +
                "${source.crossSection.duplexes}; the across-helix rigidity is a harmonic mean " +
                "over the interfaces, so a duplex count is not a scale factor"
        }
        if (!close(crossSection.interhelicalDistance, source.crossSection.interhelicalDistance)) {
            return "$name is drawn at d = ${crossSection.interhelicalDistance.roundedForProse()} nm " +
                "and ${source.name} at " +
                "${source.crossSection.interhelicalDistance.roundedForProse()} nm"
        }
        if (!close(crossoverSpacing, source.crossoverSpacing)) {
            return "$name carries a crossover spacing of ${crossoverSpacing.roundedForProse()} nm per " +
                "interface and ${source.name} ${source.crossoverSpacing.roundedForProse()} nm"
        }
        if (!close(lengthX, source.lengthX)) {
            return "$name is ${lengthX.roundedForProse()} nm along the helices and ${source.name} " +
                "is ${source.lengthX.roundedForProse()} nm; a plan ceiling, an edge collar and " +
                "a crossover column " +
                "count are all functions of the footprint"
        }
        if (columns.parities != source.columns.parities ||
            columns.positions.size != source.columns.positions.size ||
            columns.positions.indices.any { !close(columns.positions[it], source.columns.positions[it]) }
        ) {
            return "$name and ${source.name} carry different crossover column layouts, and the " +
                "phase is a design variable rather than a discretisation (C-0015)"
        }
        return null
    }

    private fun close(a: Double, b: Double): Boolean = abs(a - b) <= 1e-12 * maxOf(1.0, abs(a))
}

/**
 * `T-10`'s construction, as a specification: [crossoverColumns] columns at pitch `p/2`,
 * symmetrically centred on the footprint, with `p` taken from the **lattice**.
 *
 * Retained separately from [GrillageSpecification]'s primary constructor because the column
 * spacing depends on the lattice and the mechanics, so a caller cannot build the layout before it
 * has both — and asking it to is how a `Gen1Tile` constant creeps back in.
 */
fun centredSheetSpecification(
    name: String,
    lattice: CrossoverLattice,
    crossSection: SheetCrossSection,
    mechanics: DuplexMechanics,
    lengthX: Double,
    crossoverColumns: Int,
    source: String
): GrillageSpecification = GrillageSpecification(
    name = name,
    lattice = lattice,
    crossSection = crossSection,
    mechanics = mechanics,
    lengthX = lengthX,
    columns = CrossoverLayout.centred(
        crossoverColumns, latticeCrossoverSpacing(lattice, mechanics) / 2.0
    ),
    source = source
)

// --------------------------------------------------------------- the design is the constructor

/**
 * How two strand crossings between one helix pair at **adjacent** offsets are read.
 *
 * This corpus's own `.sc` registers one strand crossing per crossover, and `C-0157`'s oxDNA run
 * records that registering the same offset twice does not relax — 112 over-stretched bonds against
 * 63 designed crossovers. The **reference** implementation's own origami rectangle does something
 * neither of those: it writes crossings at `o` and `o+1` for the same pair, one per staple meeting
 * at the column boundary. Those are 0.34 nm apart, so a lattice that treats them as two columns
 * puts two dihedral springs where the drawing has one junction — which is a **modelling** decision
 * and is therefore named rather than defaulted.
 */
enum class AdjacentCrossingReading {

    /** Every distinct offset is its own crossover column — the file, uninterpreted. */
    AS_DRAWN,

    /** Crossings at `o` and `o+1` between the same helix pair are one junction. */
    ONE_JUNCTION
}

/**
 * What an imported design says about the grillage it can be graded as — refusals included.
 *
 * [specification] is `null` exactly when [refusals] is non-empty, so a caller cannot get a lattice
 * out of a design this repository cannot express. [notes] carry what is *representable but worth
 * saying*, which is a third state a boolean report cannot hold — the distinction
 * `LatticeBuildabilityReport` had to introduce one layer up.
 */
data class GrillageImport(
    val designName: String,
    val lattice: String,
    val duplexes: Int,
    val axialWindowBasePairs: Int,
    val lengthX: Double,
    /** `null` where the design is not a single-layer sheet, so a sheet width is not a fact about it. */
    val lengthY: Double?,
    val risePerBasePair: Double,
    val interhelicalDistance: Double?,
    val columnBasePairs: List<Double>,
    val columnParities: List<Int>,
    val columnsAsDrawn: Int,
    val columnsAsJunctions: Int,
    val columnParitiesAlternate: Boolean,
    val designCrossovers: Int,
    val designJunctions: Int,
    val latticeCrossovers: Int,
    val absentCrossovers: List<CrossoverSite>,
    val refusals: List<String>,
    val notes: List<String>,
    val specification: GrillageSpecification?
)

/**
 * The grillage an imported [ScadnanoDesign] is, or the reasons it is not one.
 *
 * Everything geometric is taken **from the file**: the duplex count is the helix count, the
 * footprint is the axial window of every strand in it, the crossover columns are the offsets its
 * **staple** crossings occupy (a raster's scaffold crossings are its *turns* and are not lattice
 * sites), the column parities are the parities of the lower helices those crossings join, and the
 * interhelical distance and the rise are the design's own geometry block where it has one.
 *
 * Only [mechanics] comes from the caller, and it comes named.
 *
 * @param interhelicalDistance supplied only where the file states none. A file that states one and
 *          a caller that supplies a different one is a **refusal**, not a silent override.
 */
fun ScadnanoDesign.grillageImport(
    name: String,
    mechanics: DuplexMechanics = DuplexMechanics.gen1(),
    interhelicalDistance: Double? = null,
    reading: AdjacentCrossingReading = AdjacentCrossingReading.AS_DRAWN
): GrillageImport {
    val refusals = mutableListOf<String>()
    val notes = mutableListOf<String>()

    val crossoverLattice = crossoverLatticeOfGrid(grid)
    if (crossoverLattice == null) {
        refusals += "this project has no crossover lattice for the grid '$grid', and guessing " +
            "between 'square' and 'honeycomb' silently transfers a phase congruence, a station " +
            "ladder and a register departure that do not hold"
    } else if (!crossoverLattice.singleLayerInterfacesFormAPath) {
        refusals += "the ${crossoverLattice.name} lattice's single-layer interfaces are not a " +
            "path graph, and OrigamiGrillage bonds beam i to beam i+1 and to nothing else " +
            "(C-0056, CH-0066, C-0154) — a honeycomb block is HoneycombGrillage's object"
    }
    if (helixCount < 2) {
        refusals += "a grillage needs at least two duplexes, and this design has $helixCount"
    }

    val fileRise = risePerBasePairOrNull()
    val rise = fileRise ?: mechanics.risePerBasePair
    if (fileRise == null) {
        notes += "the file states no rise per base pair; the caller's " +
            "${mechanics.risePerBasePair.roundedForProse()} nm is used, and every length below is " +
            "nominal in it"
    } else if (abs(fileRise - mechanics.risePerBasePair) > 1e-12) {
        notes += "the file is drawn at ${fileRise.roundedForProse()} nm per base pair and the " +
            "supplied mechanics carries ${mechanics.risePerBasePair.roundedForProse()}; the " +
            "FILE's rise is used, because a length in " +
            "a design is the design's statement"
    }

    val fileDistance = interhelicalDistanceNm()
    val distance: Double? = when {
        fileDistance != null && interhelicalDistance != null &&
            abs(fileDistance - interhelicalDistance) > 1e-12 -> {
            refusals += "the file states an interhelical distance of " +
                "${fileDistance.roundedForProse()} nm and the caller supplied " +
                "${interhelicalDistance.roundedForProse()} nm; a geometry a design states is not " +
                "overridable, because every crossover census in this corpus is indexed by it"
            fileDistance
        }
        fileDistance != null -> fileDistance
        interhelicalDistance != null -> {
            notes += "the file states no interhelical distance; the caller's " +
                "${interhelicalDistance.roundedForProse()} nm is used and is declared here rather than " +
                "defaulted"
            interhelicalDistance
        }
        else -> {
            refusals += "the file states no interhelical distance and none was supplied — a " +
                "design that does not say what gap it was drawn at has not said 2.69 nm, and " +
                "guessing one is the same class of error as guessing a grid"
            null
        }
    }

    if (crossoverLattice != null) {
        val fileTwist = geometry?.basesPerTurn
        if (fileTwist != null && abs(fileTwist - crossoverLattice.designBasesPerTurn) > 1e-9) {
            notes += "the file is drawn at ${fileTwist.roundedForProse()} bp/turn and this project's " +
                "${crossoverLattice.name} lattice at " +
                "${crossoverLattice.designBasesPerTurn.roundedForProse()} — the rounded literal against " +
                "the exact " +
                "ratio; nothing in a grillage reads the design twist, but a register departure " +
                "does"
        }
    }

    val crossings = allStrandCrossings()
    if (crossings.any { it.upperHelix - it.lowerHelix != 1 }) {
        refusals += "a strand crossing joins two helices that are not consecutive in this " +
            "design's own helix ordering, so the interfaces are not a path on that ordering"
    }
    if (strands.any { s -> s.domains.any { it.deletions.isNotEmpty() || it.insertions.isNotEmpty() } }) {
        refusals += "the design carries insertions or deletions — the standard twist correction " +
            "— so an offset is not a base pair and no length derived here would be one"
    }

    val window = axialWindowBasePairs()
    val span = axialSpanBasePairs()
    val centre = (window.first + window.last + 1) / 2.0
    val lengthX = span * rise

    // the sheet's own lattice sites are the STAPLE crossings; a raster's scaffold crossings are
    // its turns, and counting them here is how a seven-column sheet reads as 63 crossovers.
    val siteCrossovers = crossovers()
    val offsets = siteCrossovers.map { it.offset }.distinct().sorted()
    val groups: List<List<Int>> = when (reading) {
        AdjacentCrossingReading.AS_DRAWN -> offsets.map { listOf(it) }
        AdjacentCrossingReading.ONE_JUNCTION -> mergeAdjacentOffsets(offsets, siteCrossovers)
    }
    val junctionGroups = mergeAdjacentOffsets(offsets, siteCrossovers)
    if (junctionGroups.size != offsets.size) {
        notes += "the design registers ${offsets.size} distinct crossover offsets that are " +
            "${junctionGroups.size} junctions: a pair at o and o+1 between one helix pair is " +
            "${rise.roundedForProse()} nm apart, which is one column boundary drawn as two strand " +
            "crossings. " +
            "This reading is ${reading.name}"
    }

    val parities = groups.map { group ->
        val lowers = siteCrossovers.filter { it.offset in group }.map { it.lowerHelix }
        val distinct = lowers.map { Math.floorMod(it, 2) }.distinct()
        if (distinct.size != 1) {
            refusals += "the crossover column at offsets $group carries crossings on BOTH " +
                "parities of lower helix, and a CrossoverLayout column serves one parity of " +
                "interface only — interface b takes the columns whose parity matches b mod 2"
            0
        } else distinct.single()
    }
    if (groups.size < 2) {
        refusals += "a lattice needs at least two crossover columns and this design offers " +
            "${groups.size}"
    }

    val columnBasePairs = groups.map { group -> group.sumOf { it.toDouble() } / group.size }
    val positions = columnBasePairs.map { (it - centre) * rise }
    if (positions.isNotEmpty() &&
        (positions.first() <= -lengthX / 2.0 || positions.last() >= lengthX / 2.0)
    ) {
        refusals += "a crossover column sits on or outside the footprint edge, which would seed " +
            "a zero-length beam element"
    }

    val duplexes = helixCount
    val latticeSites = if (groups.size >= 2) buildList {
        for (beam in 0 until duplexes - 1) {
            for (column in groups.indices) {
                if (Math.floorMod(parities[column] + beam, 2) == 0) add(CrossoverSite(beam, column))
            }
        }
    } else emptyList()
    val designSites = siteCrossovers.mapNotNull { crossover ->
        val column = groups.indexOfFirst { crossover.offset in it }
        if (column < 0) null else CrossoverSite(crossover.lowerHelix, column)
    }.toSet()
    val latticeSiteSet = latticeSites.toSet()
    val absent = latticeSites.filter { it !in designSites }
    val extra = designSites.filter { it !in latticeSiteSet }
    if (extra.isNotEmpty()) {
        refusals += "the design builds ${extra.size} crossover(s) the parity lattice does not " +
            "offer, so its columns do not serve one interface parity each: $extra"
    }
    if (absent.isNotEmpty()) {
        notes += "the parity lattice offers ${absent.size} site(s) the design does not build; " +
            "they are carried as T-110's consumedCrossovers, so the imported grillage builds " +
            "exactly the crossovers the file draws"
    }

    val specification = if (refusals.isEmpty() && crossoverLattice != null) GrillageSpecification(
        name = name,
        lattice = crossoverLattice,
        crossSection = SheetCrossSection(
            duplexes = duplexes,
            interhelicalDistance = distance!!,
            source = if (fileDistance != null) "the design's own geometry block"
            else "supplied by the caller; the file states none"
        ),
        mechanics = mechanics.copy(risePerBasePair = rise),
        lengthX = lengthX,
        columns = CrossoverLayout(positions = positions, parities = parities),
        absentCrossovers = absent.toSet(),
        source = "imported from $name"
    ) else null

    return GrillageImport(
        designName = name,
        lattice = crossoverLattice?.name ?: grid,
        duplexes = duplexes,
        axialWindowBasePairs = span,
        lengthX = lengthX,
        lengthY = if (crossoverLattice?.singleLayerInterfacesFormAPath == true) {
            distance?.let { duplexes * it }
        } else null,
        risePerBasePair = rise,
        interhelicalDistance = distance,
        columnBasePairs = columnBasePairs,
        columnParities = parities,
        columnsAsDrawn = offsets.size,
        columnsAsJunctions = junctionGroups.size,
        columnParitiesAlternate = parities.zipWithNext().all { (a, b) -> a != b },
        designCrossovers = siteCrossovers.size,
        designJunctions = siteCrossovers.map { crossover ->
            crossover.lowerHelix to junctionGroups.indexOfFirst { crossover.offset in it }
        }.distinct().size,
        latticeCrossovers = latticeSites.size,
        absentCrossovers = absent,
        refusals = refusals,
        notes = notes,
        specification = specification
    )
}

/**
 * Offsets grouped so that `o` and `o + 1` are one group **when they join the same helix pairs**.
 *
 * The second condition is what stops this from being a tolerance: two columns a base pair apart
 * that serve *different* interfaces are two lattice positions and not one junction drawn twice.
 */
private fun mergeAdjacentOffsets(
    offsets: List<Int>,
    crossovers: List<DesignCrossover>
): List<List<Int>> {
    fun lowersAt(offset: Int): Set<Int> =
        crossovers.filter { it.offset == offset }.map { it.lowerHelix }.toSet()
    val groups = mutableListOf<MutableList<Int>>()
    offsets.forEach { offset ->
        val last = groups.lastOrNull()
        if (last != null && offset - last.last() == 1 && lowersAt(offset) == lowersAt(last.last())) {
            last += offset
        } else {
            groups += mutableListOf(offset)
        }
    }
    return groups
}
