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

import com.xemantic.nano.plentyofroom.structure.BandedCholesky
import com.xemantic.nano.plentyofroom.structure.DnaDuplex
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.HoneycombSublattice
import com.xemantic.nano.plentyofroom.structure.PointLoad
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.gaussLegendreRule
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import org.jetbrains.bio.viktor.F64Array
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

/** One bond of a honeycomb block: the two beams it joins, and the crossover plane it sits in. */
data class HoneycombBondSite(val lowerBeam: Int, val upperBeam: Int, val plane: Int) {

    init {
        require(lowerBeam >= 0) { "lowerBeam must not be negative, was: $lowerBeam" }
        require(upperBeam > lowerBeam) { "upperBeam must exceed lowerBeam, was: $upperBeam" }
        require(plane >= 0) { "plane must not be negative, was: $plane" }
    }

}

/**
 * `T-254` — a scaffold tie the raster's **turn** adds at a node the staple lattice does not reach.
 *
 * A raster turn with zero unpaired nucleotides is a covalent crossover between two duplexes at
 * their **ends**, so it sits at `s = ±L/2` — past the last 7 bp crossover plane. It carries the
 * same three elements a lattice bond does (a dihedral spring, a normal link and an axial slip
 * spring), because it is the same covalent object; what differs is where it sits.
 *
 * @param prestrainRadians the relative roll the tie is built at. `C-0152` measures
 *   `8.5714286°` at **every** allowed honeycomb scaffold crossover and twice that at a forced
 *   one; a prestrain is a **load** (`C-0104`), so it changes no entry of the stiffness matrix.
 */
data class HoneycombScaffoldTurnTie(
    val lowerBeam: Int,
    val upperBeam: Int,
    val node: Int,
    val prestrainRadians: Double = 0.0
) {

    init {
        require(lowerBeam >= 0) { "lowerBeam must not be negative, was: $lowerBeam" }
        require(upperBeam > lowerBeam) { "upperBeam must exceed lowerBeam, was: $upperBeam" }
        require(node >= 0) { "node must not be negative, was: $node" }
        require(prestrainRadians.isFinite()) {
            "prestrainRadians must be finite, was: $prestrainRadians"
        }
    }

}

/** A scaffold turn tie of the assembled lattice, as geometry. */
data class HoneycombTurnElement(
    val tie: HoneycombScaffoldTurnTie,
    val node: Int,
    val s: Double,
    val inPlane: Boolean,
    val unitY: Double,
    val unitZ: Double,
    val y: Double,
    val z: Double
)

/**
 * `T-299` — a raster turn carried as a freely-jointed **tether** rather than as a covalent tie.
 *
 * `C-0193` reads the built `10 × 6` block's own strand diagram: the covalent phosphodiester link
 * sits `14 bp = 4.76 nm` **outboard** of the duplex end on each of the two helices a turn joins,
 * so what stands between the two rim nodes is `28` nucleotides of single-stranded scaffold. A
 * freely-jointed chain transmits a **force** and no **moment**, so this element carries **no**
 * dihedral spring and **no** covalent slip spring — only the two stiffnesses a taut central-force
 * element has, and its own preload.
 *
 * @param secantStiffness `f/x` in pN/nm — the **transverse** stiffness of a taut chain.
 * @param tangentStiffness `df/dx` in pN/nm — the stiffness along the chain's own line.
 * @param tension `f` in pN. A chain held at any `x > 0` pulls, so this is a **load**: it changes
 *   no entry of the stiffness matrix, and `withoutPrestrain` drops it exactly as it drops a tie's
 *   prestrain.
 */
data class HoneycombScaffoldTurnTether(
    val lowerBeam: Int,
    val upperBeam: Int,
    val node: Int,
    val secantStiffness: Double,
    val tangentStiffness: Double,
    val tension: Double = 0.0
) {

    init {
        require(lowerBeam >= 0) { "lowerBeam must not be negative, was: $lowerBeam" }
        require(upperBeam > lowerBeam) { "upperBeam must exceed lowerBeam, was: $upperBeam" }
        require(node >= 0) { "node must not be negative, was: $node" }
        require(secantStiffness >= 0.0 && secantStiffness.isFinite()) {
            "secantStiffness must be finite and not negative, was: $secantStiffness"
        }
        require(tangentStiffness >= 0.0 && tangentStiffness.isFinite()) {
            "tangentStiffness must be finite and not negative, was: $tangentStiffness"
        }
        require(tension >= 0.0 && tension.isFinite()) {
            "tension must be finite and not negative, was: $tension"
        }
    }

}

/**
 * A scaffold turn **tether** of the assembled lattice, as geometry.
 *
 * The chain's own decomposition, `K = (df/dx)n̂n̂ᵀ + (f/x)(I − n̂n̂ᵀ)` with
 * `n̂ = (unitY, unitZ, 0)` and no in-plane transverse coordinate in the model, gives exactly two
 * scalars on the grillage's two existing gradients — [normalStiffness] on the link residual and
 * [axialStiffness] on the slip residual.
 */
data class HoneycombTetherElement(
    val tether: HoneycombScaffoldTurnTether,
    val node: Int,
    val s: Double,
    val inPlane: Boolean,
    val unitY: Double,
    val unitZ: Double,
    val y: Double,
    val z: Double
) {

    /** The stiffness in pN/nm on the **link** residual: `(df/dx)·unitZ² + (f/x)·unitY²`. */
    val normalStiffness: Double =
        tether.tangentStiffness * unitZ * unitZ + tether.secantStiffness * unitY * unitY

    /** The stiffness in pN/nm on the **slip** residual — purely transverse, so the secant. */
    val axialStiffness: Double = tether.secantStiffness

}

/** A bond of the assembled lattice, as geometry. */
data class HoneycombLatticeBond(
    val site: HoneycombBondSite,
    val bondClass: Int,
    val node: Int,
    val plane: Int,
    val inPlane: Boolean,
    val unitY: Double,
    val unitZ: Double,
    val s: Double,
    val y: Double,
    val z: Double
)

/** The undirected bond list of [block] as beam-index pairs, `beam = rasterRow · n + column`. */
fun honeycombBondPairs(block: HoneycombBlock): List<Pair<Int, Int>> = buildList {
    val n = block.helicesPerRow
    block.sites.forEach { site ->
        val own = site.rasterRow * n + site.column
        honeycombAzimuthsOf(site).forEach { azimuth ->
            val r = site.rasterRow + azimuth.rasterRowStep
            val c = site.column + azimuth.columnStep
            if (block.contains(r, c)) {
                val other = r * n + c
                if (other > own) add(own to other)
            }
        }
    }
}

/** The largest number of lattice neighbours any helix of [block] has. */
fun honeycombMaximumDegree(block: HoneycombBlock): Int {
    val degree = IntArray(block.helices)
    honeycombBondPairs(block).forEach { (a, b) -> degree[a]++; degree[b]++ }
    return degree.maxOrNull() ?: 0
}

/** The number of connected components of [block]'s bond graph. */
fun honeycombBondGraphComponents(block: HoneycombBlock): Int {
    val parent = IntArray(block.helices) { it }
    fun find(i: Int): Int {
        var root = i
        while (parent[root] != root) root = parent[root]
        return root
    }
    honeycombBondPairs(block).forEach { (a, b) -> parent[find(a)] = find(b) }
    return (0 until block.helices).map { find(it) }.toSet().size
}

/**
 * The deflected state of a [HoneycombGrillage] under one load case, read on the **face**.
 *
 * The face is the surface the polymer layer and the electrode confront, so it is the surface a
 * flatness verdict is written on; the buried layers exist in the model to carry the block's
 * rigidity and never to be measured.
 */
class HoneycombDeflection internal constructor(
    private val lattice: HoneycombGrillage,
    val coefficients: F64Array,
    private val pressure: PressureField
) {

    /** The deflection in nm at ([s], [y]) on the face. */
    fun deflection(s: Double, y: Double): Double = lattice.evaluate(coefficients, s, y)

    /** The area-averaged face deflection in nm. */
    val meanDeflection: Double by lazy {
        lattice.pistonDual.dot(coefficients) / lattice.area
    }

    private val tiltAlong: Double by lazy {
        lattice.tiltSDual.dot(coefficients) / lattice.tiltSNorm
    }

    private val tiltAcross: Double by lazy {
        lattice.tiltYDual.dot(coefficients) / lattice.tiltYNorm
    }

    /** The face field with its area-averaged best-fit rigid plane removed. */
    val dishingCoefficients: F64Array by lazy {
        val residual = coefficients.copy()
        residual -= lattice.pistonMode * meanDeflection
        residual -= lattice.tiltSMode * tiltAlong
        residual -= lattice.tiltYMode * tiltAcross
        residual
    }

    /** The dishing in nm at ([s], [y]). */
    fun dishing(s: Double, y: Double): Double = lattice.evaluate(dishingCoefficients, s, y)

    /** The largest absolute dishing over a [samples] × [samples] grid on the face, in nm. */
    fun peakDishing(samples: Int = 81): Double = lattice.overFaceGrid(samples) { s, y ->
        abs(dishing(s, y))
    }

    /** The total force in pN the polymer foundation carries. */
    val foundationForce: Double by lazy {
        lattice.foundationStiffness * lattice.pistonDual.dot(coefficients)
    }

    /** The total applied force in pN. */
    val appliedForce: Double by lazy { lattice.integrateOverFace { s, y -> pressure.at(s, y) } }

}

/**
 * `T-253` — a DNA-origami **honeycomb block** as the three-dimensional beam-and-bond lattice it
 * physically is, so that a **load on a named subset of its crossovers** can be solved.
 *
 * ## Why this class exists rather than a parameter on `OrigamiGrillage`
 *
 * `CLAUDE.md` records the gap in two sentences: *"`OrigamiGrillage` NEVER READS `layers` OR
 * `interlayerCoupling`"*, and *"`CrossoverLayout`'s two-parity alternation makes its crossover
 * combinatorics SQUARE-LATTICE"*. The cheap bound of `T-253` says why that is a replacement and
 * not an adaptation, and it is one integer:
 *
 * `OrigamiGrillage` bonds beam `i` to beam `i+1` and to nothing else, so — as `C-0056` and
 * `CH-0066` already record — **its interfaces form a path graph on the duplexes**, whose maximum
 * degree is **two**.
 * A honeycomb site has **three** lattice neighbours ([honeycombMaximumDegree]), so no relabelling
 * of the helices puts every bond between consecutive indices. Four further mismatches follow and
 * are counted in the study rather than argued: the in-plane row spacing alternates `d, 2d` about a
 * mean of `3d/2` where the square lattice is uniform; only half the in-plane adjacent pairs are
 * bonded at all, so **a single layer of a honeycomb block is a set of dimers and not a sheet**
 * ([honeycombBondGraphComponents]); the foundation acts on **one** face; and the parallel-axis
 * enhancement `layers` buys is an **axial** effect, for which `OrigamiGrillage`'s three degrees of
 * freedom per node have no coordinate.
 *
 * ## The model
 *
 * Every helix is an Euler-Bernoulli beam along `s` carrying four degrees of freedom per node:
 * the deflection `W` normal to the face, the bending rotation `Θ = ∂W/∂s`, the roll `Φ` about its
 * own axis, and the **axial** displacement `U`. Every bond carries three elements, and only the
 * first of them is `D_⊥`:
 *
 * - a dihedral spring `k_θ` on the relative roll, with `T-172`'s prestrain `θ₀` — `C-0104`'s
 *   term, unchanged, and the reason this class exists;
 * - a **normal link**, the covalent constraint tying the two duplex surfaces together, carried by
 *   the same penalty `OrigamiGrillage` uses and with the same `d/2` arm resolved onto the bond's
 *   own direction. **Its residual `ΔW + (d/2)·unitY·(Φ_a + Φ_b)` is a function of the SUM of the
 *   two rolls, so it IS the crossover's COMMON azimuthal mode** — `T-297`/`C-0194`, against
 *   `CH-0242`'s reading that this class carries only the relative one. `d/2` is not a choice: it
 *   is the only arm annihilating the linearised rigid roll `Φ ≡ α`, `W = α y`, which leaves
 *   `α·unitY·(2a − d)` at an arm `a`. Read as `½ k (Φ_a + Φ_b)²` the model's common-mode
 *   stiffness is `k_link (d·unitY)²/4`, which at [RIGID_LINK_STIFFNESS] is `336.800449×` the
 *   span law's own value in plane — the lattice sits at the RIGID end of that mode, not at the
 *   free one. See `tile/CrossoverCommonMode.kt` and [turnLinkOffsetResponse];
 * - an **axial slip** spring `k_s`, Chen et al.'s own softened-bond construction
 *   ([Gen1Tile.crossoverInPlaneStiffness]), which is what makes the interlayer coupling an
 *   **output** of the lattice rather than the `NONE`/`RIGID` binary `OrigamiSheet` carries.
 *
 * The crossover combinatorics are the published honeycomb rule and not a parity: a bond of class
 * `c` carries its crossovers at base-pair residues `b₀ + 7c (mod 21)`, so with `b₀ ≡ 0` the planes
 * are every 7 bp and class `c` occupies plane `q ≡ c (mod 3)`. The **in-plane** bonds are all one
 * class and the two **interlayer** ones share the other two — asserted, not assumed.
 *
 * ## Conventions
 *
 * `s` runs along the helices, `y` across them **in the plane of the face** (the `m` direction,
 * pitch `3d/2`), `z` along the block's thickness (the `n` direction, pitch `d√3/2`). `W` is
 * positive **downward**, toward the electrode — `C-0006`'s convention. Lengths nm, forces pN,
 * energies pN·nm, angles rad.
 *
 * @param block the `m × n` cross-section, `C-0141`'s object unchanged.
 * @param rowBasePairs the axial span of a row in base pairs.
 * @param foundationStiffness `k_f` in `pN/nm³`, acting on the face only.
 * @param hingeStiffness `k_θ` in `pN·nm/rad`; **the prestrain couple is always taken at this
 *          value**, so that [hingeStiffnessEnhancement] cannot move a load.
 * @param hingeStiffnessEnhancement a multiplier applied to `k_θ` in the **stiffness matrix only**.
 *          It is the one honest way to bracket the across-helix parallel-axis term this lattice
 *          does not carry: the layers' membrane action across the helices needs an in-plane
 *          transverse coordinate, which this model does not have, so its `D_⊥` is the
 *          **independent** one and therefore a lower bound. `1.0` is that lower bound and
 *          `MultiLayerRigidities.realisedEnhancement` is the calibrated upper one.
 * @param slipStiffness `k_s` in `pN/nm`, the crossover's resistance to axial slip.
 * @param axialPinBeam which beam's first node has its `U` pinned. The axial subsystem has exactly
 *          one rigid mode — a uniform `U` — because the bond graph is connected; pinning it is
 *          exact, and that the choice does not matter is asserted rather than argued.
 * @param bondPrestrains the initial relative roll in radians a bond is built at. A prestrain is a
 *          **load**: it changes no entry of the stiffness matrix, so the field is linear in it and
 *          the triangle inequality bounds any set of sites by their unit responses.
 */
class HoneycombGrillage(
    val block: HoneycombBlock,
    val rowBasePairs: Int,
    val foundationStiffness: Double,
    val hingeStiffness: Double = Gen1Tile.crossoverHingeStiffness(),
    val hingeStiffnessEnhancement: Double = 1.0,
    val slipStiffness: Double = Gen1Tile.crossoverInPlaneStiffness(),
    val duplex: DnaDuplex = DnaDuplex(
        bendingRigidity = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
        torsionalRigidity = Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY,
        stretchModulus = Gen1Tile.DUPLEX_STRETCH_MODULUS
    ),
    val subdivisions: Int = 1,
    val linkStiffness: Double = RIGID_LINK_STIFFNESS,
    val faceColumn: Int = 0,
    val axialPinBeam: Int = 0,
    val bondPrestrains: Map<HoneycombBondSite, Double> = emptyMap(),
    val scaffoldTurnTies: List<HoneycombScaffoldTurnTie> = emptyList(),
    val scaffoldTurnTethers: List<HoneycombScaffoldTurnTether> = emptyList()
) {

    init {
        require(rowBasePairs > 0) { "rowBasePairs must be positive, was: $rowBasePairs" }
        require(foundationStiffness > 0.0) {
            "foundationStiffness must be positive, was: $foundationStiffness"
        }
        require(hingeStiffness > 0.0) { "hingeStiffness must be positive, was: $hingeStiffness" }
        require(hingeStiffnessEnhancement > 0.0) {
            "hingeStiffnessEnhancement must be positive, was: $hingeStiffnessEnhancement"
        }
        require(slipStiffness > 0.0) { "slipStiffness must be positive, was: $slipStiffness" }
        require(subdivisions >= 1) { "subdivisions must be at least 1, was: $subdivisions" }
        require(linkStiffness > 0.0) { "linkStiffness must be positive, was: $linkStiffness" }
        require(faceColumn in 0 until block.helicesPerRow) {
            "faceColumn must be a column of the block, was: $faceColumn"
        }
        require(axialPinBeam in 0 until block.helices) {
            "axialPinBeam must be a beam of the block, was: $axialPinBeam"
        }
        bondPrestrains.forEach { (site, angle) ->
            require(angle.isFinite()) { "the prestrain at $site must be finite, was: $angle" }
        }
    }

    /** The lattice constant `d` in nm. */
    val bondLength: Double get() = block.bondLength

    /** The number of duplex beams — one per helix. */
    val beamCount: Int get() = block.helices

    /** The in-plane row pitch `3d/2` in nm. */
    val rowPitch: Double = HoneycombCrossSectionGeometry.rowPitch(block.bondLength)

    /** The axial span in nm. */
    val lengthS: Double = rowBasePairs * Gen1Tile.RISE_PER_BASE_PAIR

    /** The in-plane width in nm — `C-0141`'s `plateEdgeY`, `m · 3d/2`. */
    val lengthY: Double = block.rasterRows * rowPitch

    /** The face footprint in nm². */
    val area: Double = lengthS * lengthY

    private val rawPositions = block.sites.map { block.position(it) }

    private val faceRawY =
        block.sites.indices.filter { block.sites[it].column == faceColumn }.map { rawPositions[it].second }

    private val yDatum = (faceRawY.min() + faceRawY.max()) / 2.0

    private val zDatum = (block.helicesPerRow - 1) / 2.0 *
            HoneycombCrossSectionGeometry.columnPitch(block.bondLength)

    /** The in-plane coordinate of each beam's axis in nm, centred on the face. */
    val beamY: List<Double> = rawPositions.map { it.second - yDatum }

    /** The through-thickness coordinate of each beam's axis in nm, centred on the block. */
    val beamZ: List<Double> = rawPositions.map { it.first - zDatum }

    /** The beams of the gap-facing face, ascending in `y`. */
    val faceBeams: List<Int> =
        block.sites.indices.filter { block.sites[it].column == faceColumn }.sortedBy { beamY[it] }

    /** The `y` interval in nm the face beam at index [index] of [faceBeams] carries the load over. */
    fun tributary(index: Int): Pair<Double, Double> {
        require(index in faceBeams.indices) { "index must be within ${faceBeams.indices}, was: $index" }
        val axis = beamY[faceBeams[index]]
        return (axis - rowPitch / 2.0) to (axis + rowPitch / 2.0)
    }

    /** The base-pair position of each crossover plane, ascending. */
    val planeBasePairs: List<Int> =
        (0..rowBasePairs step HoneycombCrossoverRule.ANY_AZIMUTH_STEP_BP).toList()

    /**
     * The `s` of every node of every beam, in nm, ascending.
     *
     * The crossover planes are every 7 bp, so a row whose length is a multiple of 7 ends **on** a
     * plane and the ladder alone spans it. A row that does not — `C-0151`'s 116 bp block extent
     * is 16 planes and a 4 bp remainder — carries a **free overhang** past its last crossover
     * column, and the beams have to reach the end of the tile whether or not a bond sits there:
     * otherwise the strip beyond the last plane has no foundation and no load, and the standing
     * uniform-load falsifier duly fires on a correct solver. The trailing segment is subdivided
     * like any other, and where the remainder is zero **no node is added at all**, so every
     * lattice `C-0154` measured is bit-identical.
     */
    val nodeS: List<Double> = buildList {
        val stations = planeBasePairs.map { it * Gen1Tile.RISE_PER_BASE_PAIR - lengthS / 2.0 }
        add(stations.first())
        for (i in 0 until stations.size - 1) {
            val step = (stations[i + 1] - stations[i]) / subdivisions
            for (k in 1..subdivisions) add(stations[i] + k * step)
        }
        val remainder = lengthS / 2.0 - stations.last()
        if (remainder > 1e-9) {
            val step = remainder / subdivisions
            for (k in 1..subdivisions) add(stations.last() + k * step)
        }
    }

    private val planeNode: List<Int> = planeBasePairs.indices.map { it * subdivisions }

    /** The number of nodes on one beam. */
    val nodesPerBeam: Int get() = nodeS.size

    /** The number of degrees of freedom. */
    val degreesOfFreedom: Int = nodeS.size * beamCount * DOF_PER_NODE

    /** The half-bandwidth of the node-major ordering. */
    val bandwidth: Int = beamCount * DOF_PER_NODE + DOF_PER_NODE - 1

    /** Every bond of the lattice. */
    val bonds: List<HoneycombLatticeBond> = buildList {
        val n = block.helicesPerRow
        block.sites.forEach { site ->
            val own = site.rasterRow * n + site.column
            val sublattice =
                if (site.verticalBondUp) HoneycombSublattice.A else HoneycombSublattice.B
            honeycombAzimuthsOf(site).forEach { azimuth ->
                val r = site.rasterRow + azimuth.rasterRowStep
                val c = site.column + azimuth.columnStep
                if (!block.contains(r, c)) return@forEach
                val other = r * n + c
                if (other <= own) return@forEach
                val degrees = Math.toDegrees(kotlin.math.atan2(azimuth.unitY, azimuth.unitX))
                val bondClass = honeycombBondClass(sublattice, Math.floorMod(
                    Math.round(degrees).toInt(), 360
                ).toDouble())
                planeBasePairs.indices.forEach { plane ->
                    if (plane % HoneycombCrossoverRule.CLASSES != bondClass) return@forEach
                    add(
                        HoneycombLatticeBond(
                            site = HoneycombBondSite(own, other, plane),
                            bondClass = bondClass,
                            node = planeNode[plane],
                            plane = plane,
                            inPlane = azimuth.columnStep == 0,
                            unitY = (beamY[other] - beamY[own]) / block.bondLength,
                            unitZ = (beamZ[other] - beamZ[own]) / block.bondLength,
                            s = nodeS[planeNode[plane]],
                            y = (beamY[own] + beamY[other]) / 2.0,
                            z = (beamZ[own] + beamZ[other]) / 2.0
                        )
                    )
                }
            }
        }
    }

    /**
     * `T-254`'s scaffold turn ties, as assembled geometry.
     *
     * Empty by default, so every lattice `C-0154` and `C-0167` measured is **bit-identical**
     * — asserted as a test rather than claimed.
     */
    val turnElements: List<HoneycombTurnElement> = scaffoldTurnTies.map { tie ->
        require(tie.upperBeam < beamCount) {
            "a tie must join two beams of the block, was: ${tie.lowerBeam}, ${tie.upperBeam}"
        }
        require(tie.node in nodeS.indices) {
            "a tie's node must be one of the beam's own, was: ${tie.node}"
        }
        val dy = beamY[tie.upperBeam] - beamY[tie.lowerBeam]
        val dz = beamZ[tie.upperBeam] - beamZ[tie.lowerBeam]
        val length = sqrt(dy * dy + dz * dz)
        require(abs(length - block.bondLength) < 1e-8) {
            "a tie must join two beams exactly one lattice constant apart, was: $length"
        }
        HoneycombTurnElement(
            tie = tie,
            node = tie.node,
            s = nodeS[tie.node],
            inPlane = abs(dz) < 1e-9,
            unitY = dy / length,
            unitZ = dz / length,
            y = (beamY[tie.lowerBeam] + beamY[tie.upperBeam]) / 2.0,
            z = (beamZ[tie.lowerBeam] + beamZ[tie.upperBeam]) / 2.0
        )
    }

    /**
     * `T-299`'s scaffold turn tethers, as assembled geometry.
     *
     * Empty by default, so every lattice `C-0154`, `C-0167` and `C-0175` measured is
     * **bit-identical** — asserted as a test rather than claimed.
     */
    val tetherElements: List<HoneycombTetherElement> = scaffoldTurnTethers.map { tether ->
        require(tether.upperBeam < beamCount) {
            "a tether must join two beams of the block, was: " +
                    "${tether.lowerBeam}, ${tether.upperBeam}"
        }
        require(tether.node in nodeS.indices) {
            "a tether's node must be one of the beam's own, was: ${tether.node}"
        }
        val dy = beamY[tether.upperBeam] - beamY[tether.lowerBeam]
        val dz = beamZ[tether.upperBeam] - beamZ[tether.lowerBeam]
        val length = sqrt(dy * dy + dz * dz)
        require(abs(length - block.bondLength) < 1e-8) {
            "a tether must join two beams exactly one lattice constant apart, was: $length"
        }
        HoneycombTetherElement(
            tether = tether,
            node = tether.node,
            s = nodeS[tether.node],
            inPlane = abs(dz) < 1e-9,
            unitY = dy / length,
            unitZ = dz / length,
            y = (beamY[tether.lowerBeam] + beamY[tether.upperBeam]) / 2.0,
            z = (beamZ[tether.lowerBeam] + beamZ[tether.upperBeam]) / 2.0
        )
    }

    /**
     * The same lattice with no prestrain — the object every influence function must be taken on.
     *
     * A tether's **tension** is a prestrain in exactly `C-0104`'s sense, so it is dropped here
     * with the ties' roll prestrain: an influence taken on a preloaded lattice is that influence
     * *plus* the preload's own response, and the Woodbury matrix then stops being a compliance.
     */
    val withoutPrestrain: HoneycombGrillage by lazy {
        if (bondPrestrains.isEmpty() && scaffoldTurnTies.none { it.prestrainRadians != 0.0 } &&
            scaffoldTurnTethers.none { it.tension != 0.0 }
        ) {
            this
        } else HoneycombGrillage(
            block, rowBasePairs, foundationStiffness, hingeStiffness, hingeStiffnessEnhancement,
            slipStiffness, duplex, subdivisions, linkStiffness, faceColumn, axialPinBeam,
            emptyMap(), scaffoldTurnTies.map { it.copy(prestrainRadians = 0.0) },
            scaffoldTurnTethers.map { it.copy(tension = 0.0) }
        )
    }

    /** The initial relative roll in radians [bond] is built at — zero unless named. */
    fun prestrainOf(bond: HoneycombLatticeBond): Double = bondPrestrains[bond.site] ?: 0.0

    private fun dof(node: Int, beam: Int, component: Int): Int =
        (node * beamCount + beam) * DOF_PER_NODE + component

    private val pinnedDof: Int = dof(0, axialPinBeam, U)

    // ------------------------------------------------------------------ assembly

    private val bandWidthStore = bandwidth + 1

    private fun assemble(foundation: Double): DoubleArray {
        val band = DoubleArray(degreesOfFreedom * bandWidthStore)
        fun add(i: Int, j: Int, value: Double) {
            if (value == 0.0) return
            val row = max(i, j)
            val column = kotlin.math.min(i, j)
            if (row - column > bandwidth) error("bandwidth $bandwidth is too small for $row, $column")
            band[row * bandWidthStore + (column - row + bandwidth)] += value
        }
        fun scatter(dofs: IntArray, element: Array<DoubleArray>) {
            for (a in dofs.indices) for (b in dofs.indices) {
                if (dofs[a] >= dofs[b]) add(dofs[a], dofs[b], element[a][b])
            }
        }
        val ei = duplex.bendingRigidity
        val gj = duplex.torsionalRigidity
        val s = duplex.stretchModulus
        for (beam in 0 until beamCount) {
            for (element in 0 until nodeS.size - 1) {
                val length = nodeS[element + 1] - nodeS[element]
                scatter(
                    intArrayOf(
                        dof(element, beam, W), dof(element, beam, THETA),
                        dof(element + 1, beam, W), dof(element + 1, beam, THETA)
                    ),
                    honeycombHermiteBending(ei, length)
                )
                scatter(
                    intArrayOf(dof(element, beam, PHI), dof(element + 1, beam, PHI)),
                    arrayOf(
                        doubleArrayOf(gj / length, -gj / length),
                        doubleArrayOf(-gj / length, gj / length)
                    )
                )
                scatter(
                    intArrayOf(dof(element, beam, U), dof(element + 1, beam, U)),
                    arrayOf(
                        doubleArrayOf(s / length, -s / length),
                        doubleArrayOf(-s / length, s / length)
                    )
                )
            }
        }
        val half = block.bondLength / 2.0
        bonds.forEach { bond ->
            val a = bond.site.lowerBeam
            val b = bond.site.upperBeam
            val node = bond.node
            val hinge = hingeStiffness * hingeStiffnessEnhancement
            scatter(
                intArrayOf(dof(node, a, PHI), dof(node, b, PHI)),
                arrayOf(doubleArrayOf(hinge, -hinge), doubleArrayOf(-hinge, hinge))
            )
            val armY = half * bond.unitY
            val linkGradient = doubleArrayOf(1.0, armY, -1.0, armY)
            scatter(
                intArrayOf(dof(node, a, W), dof(node, a, PHI), dof(node, b, W), dof(node, b, PHI)),
                Array(4) { i -> DoubleArray(4) { j -> linkStiffness * linkGradient[i] * linkGradient[j] } }
            )
            val armZ = half * bond.unitZ
            val slipGradient = doubleArrayOf(1.0, -armZ, -1.0, -armZ)
            scatter(
                intArrayOf(dof(node, a, U), dof(node, a, THETA), dof(node, b, U), dof(node, b, THETA)),
                Array(4) { i -> DoubleArray(4) { j -> slipStiffness * slipGradient[i] * slipGradient[j] } }
            )
        }
        turnElements.forEach { element ->
            val a = element.tie.lowerBeam
            val b = element.tie.upperBeam
            val node = element.node
            val hinge = hingeStiffness * hingeStiffnessEnhancement
            scatter(
                intArrayOf(dof(node, a, PHI), dof(node, b, PHI)),
                arrayOf(doubleArrayOf(hinge, -hinge), doubleArrayOf(-hinge, hinge))
            )
            val armY = half * element.unitY
            val linkGradient = doubleArrayOf(1.0, armY, -1.0, armY)
            scatter(
                intArrayOf(dof(node, a, W), dof(node, a, PHI), dof(node, b, W), dof(node, b, PHI)),
                Array(4) { i -> DoubleArray(4) { j -> linkStiffness * linkGradient[i] * linkGradient[j] } }
            )
            val armZ = half * element.unitZ
            val slipGradient = doubleArrayOf(1.0, -armZ, -1.0, -armZ)
            scatter(
                intArrayOf(dof(node, a, U), dof(node, a, THETA), dof(node, b, U), dof(node, b, THETA)),
                Array(4) { i -> DoubleArray(4) { j -> slipStiffness * slipGradient[i] * slipGradient[j] } }
            )
        }
        tetherElements.forEach { element ->
            val a = element.tether.lowerBeam
            val b = element.tether.upperBeam
            val node = element.node
            // A freely-jointed chain carries no moment, so there is NO dihedral spring here.
            val armY = half * element.unitY
            val linkGradient = doubleArrayOf(1.0, armY, -1.0, armY)
            val normal = element.normalStiffness
            scatter(
                intArrayOf(dof(node, a, W), dof(node, a, PHI), dof(node, b, W), dof(node, b, PHI)),
                Array(4) { i -> DoubleArray(4) { j -> normal * linkGradient[i] * linkGradient[j] } }
            )
            val armZ = half * element.unitZ
            val slipGradient = doubleArrayOf(1.0, -armZ, -1.0, -armZ)
            val axial = element.axialStiffness
            scatter(
                intArrayOf(dof(node, a, U), dof(node, a, THETA), dof(node, b, U), dof(node, b, THETA)),
                Array(4) { i -> DoubleArray(4) { j -> axial * slipGradient[i] * slipGradient[j] } }
            )
        }
        if (foundation != 0.0) {
            faceBeams.forEachIndexed { index, beam ->
                val (low, high) = tributary(index)
                val axis = beamY[beam]
                for (element in 0 until nodeS.size - 1) {
                    val length = nodeS[element + 1] - nodeS[element]
                    val dofs = intArrayOf(
                        dof(element, beam, W), dof(element, beam, THETA),
                        dof(element + 1, beam, W), dof(element + 1, beam, THETA),
                        dof(element, beam, PHI), dof(element + 1, beam, PHI)
                    )
                    val element6 = Array(6) { DoubleArray(6) }
                    for (q in 0 until honeycombQuadrature.points) {
                        val t = (honeycombQuadrature.nodes[q] + 1.0) / 2.0
                        val weightS = honeycombQuadrature.weights[q] * length / 2.0
                        val hermite = honeycombHermiteShape(t, length)
                        for (r in 0 until honeycombQuadrature.points) {
                            val arm = (low + high) / 2.0 - axis +
                                    (high - low) / 2.0 * honeycombQuadrature.nodes[r]
                            val weight = foundation * weightS *
                                    honeycombQuadrature.weights[r] * (high - low) / 2.0
                            val basis = doubleArrayOf(
                                hermite[0], hermite[1], hermite[2], hermite[3],
                                (1.0 - t) * arm, t * arm
                            )
                            for (i in 0 until 6) for (j in 0 until 6) {
                                element6[i][j] += weight * basis[i] * basis[j]
                            }
                        }
                    }
                    scatter(dofs, element6)
                }
            }
        }
        return band
    }

    private val band: DoubleArray by lazy { assemble(foundationStiffness) }

    /** `K[i, j]` of the assembled stiffness matrix, for `j ≤ i` within the band. */
    fun stiffnessEntry(i: Int, j: Int): Double {
        require(i in 0 until degreesOfFreedom && j in 0 until degreesOfFreedom) {
            "indices must be within 0 until $degreesOfFreedom, were: $i, $j"
        }
        if (j > i || i - j > bandwidth) return 0.0
        return band[i * bandWidthStore + (j - i + bandwidth)]
    }

    private val factorisation: BandedCholesky by lazy {
        BandedCholesky(degreesOfFreedom, bandwidth) { i, j ->
            if (i == pinnedDof || j == pinnedDof) {
                if (i == j) 1.0 else 0.0
            } else band[i * bandWidthStore + (j - i + bandwidth)]
        }
    }

    /**
     * `K[i, j]` read symmetrically — the band stores the lower triangle only.
     */
    private fun symmetricEntry(i: Int, j: Int): Double =
        stiffnessEntry(max(i, j), kotlin.math.min(i, j))

    private val axialFactorisation: BandedCholesky by lazy {
        BandedCholesky(degreesOfFreedom, bandwidth) { i, j ->
            val free = i % DOF_PER_NODE == U && j % DOF_PER_NODE == U &&
                    i != pinnedDof && j != pinnedDof
            if (free) band[i * bandWidthStore + (j - i + bandwidth)]
            else if (i == j) 1.0 else 0.0
        }
    }

    /**
     * [imposed] with its **axial** coordinates relaxed to equilibrium, the bending kinematics
     * `(W, Θ, Φ)` held.
     *
     * This is what turns the interlayer coupling from an assumption into a **measurement**. Under
     * a uniform curvature an infinitely long composite carries a constant axial force in every
     * layer, so no shear flows and the connectors are irrelevant — the partial-composite effect is
     * a **boundary layer** at the free ends, and the fraction of the parallel-axis term a block
     * realises is therefore a property of its **length and load case**, not of its crossovers.
     * Imposing the bending field and minimising over `U` measures exactly that: a Schur complement
     * on the axial block, taken with one extra factorisation and no iteration.
     */
    fun axialRelaxed(imposed: F64Array): F64Array {
        require(imposed.nDim == 1 && imposed.length == degreesOfFreedom) {
            "imposed must be a vector of length $degreesOfFreedom"
        }
        val load = F64Array(degreesOfFreedom)
        for (i in 0 until degreesOfFreedom) {
            if (i % DOF_PER_NODE != U || i == pinnedDof) continue
            var total = 0.0
            for (j in max(0, i - bandwidth)..kotlin.math.min(degreesOfFreedom - 1, i + bandwidth)) {
                // every axial coordinate is unknown, the pinned one being fixed at ZERO — so
                // the imposed field's own `U` never enters the right-hand side. Reading it there
                // instead is what made a vanishing slip spring report a coupled block.
                if (j % DOF_PER_NODE == U) continue
                total -= symmetricEntry(i, j) * imposed[j]
            }
            load[i] = total
        }
        val axial = axialFactorisation.solve(load)
        val relaxed = imposed.copy()
        for (i in 0 until degreesOfFreedom) {
            if (i % DOF_PER_NODE == U) relaxed[i] = if (i == pinnedDof) 0.0 else axial[i]
        }
        return relaxed
    }

    /**
     * The along-helix flexural rigidity in `pN·nm` this lattice **realises** at [curvature] —
     * the energy of the imposed bending field with its axial coordinates relaxed, per unit area.
     *
     * Bounded below by `n·EI/rowPitch` (layers bending independently) and above by the
     * parallel-axis closed form; where it sits between them is the composite fraction, and it is
     * an output.
     */
    fun realisedAlongHelixRigidity(curvature: Double): Double {
        val relaxed = axialRelaxed(alongHelixCurvatureField(curvature))
        val energy = beamEnergy(relaxed) + axialEnergy(relaxed) + slipEnergy(relaxed) +
                hingeEnergy(relaxed) + linkEnergy(relaxed)
        return 2.0 * energy / (curvature * curvature * area)
    }

    // ------------------------------------------------------------------ fields

    /**
     * The nodal coordinates of the smooth field with the stated components, each read at the
     * beam's own `(s, y, z)`.
     */
    fun nodalField(
        deflection: (Double, Double, Double) -> Double,
        slopeAlong: (Double, Double, Double) -> Double,
        roll: (Double, Double, Double) -> Double,
        axial: (Double, Double, Double) -> Double
    ): F64Array {
        val field = F64Array(degreesOfFreedom)
        for (beam in 0 until beamCount) {
            val y = beamY[beam]
            val z = beamZ[beam]
            for (node in nodeS.indices) {
                val s = nodeS[node]
                field[dof(node, beam, W)] = deflection(s, y, z)
                field[dof(node, beam, THETA)] = slopeAlong(s, y, z)
                field[dof(node, beam, PHI)] = roll(s, y, z)
                field[dof(node, beam, U)] = axial(s, y, z)
            }
        }
        return field
    }

    /** The rigid translation of the face, `W = 1`. */
    val pistonMode: F64Array by lazy {
        nodalField({ _, _, _ -> 1.0 }, { _, _, _ -> 0.0 }, { _, _, _ -> 0.0 }, { _, _, _ -> 0.0 })
    }

    /** The rigid tilt about the across-helix axis, `W = s`. */
    val tiltSMode: F64Array by lazy {
        nodalField({ s, _, _ -> s }, { _, _, _ -> 1.0 }, { _, _, _ -> 0.0 }, { _, _, _ -> 0.0 })
    }

    /** The rigid tilt about the along-helix axis, `W = y`. */
    val tiltYMode: F64Array by lazy {
        nodalField({ _, y, _ -> y }, { _, _, _ -> 0.0 }, { _, _, _ -> 1.0 }, { _, _, _ -> 0.0 })
    }

    /**
     * The composite-compatible along-helix bending field at [curvature] — `W = ½κs²` with the
     * axial displacement `U = −z κ s` a rigid composite would adopt. Every bond's slip and every
     * link's extension vanish identically in it, which is what makes the energy exact.
     */
    fun alongHelixCurvatureField(curvature: Double): F64Array = nodalField(
        { s, _, _ -> 0.5 * curvature * s * s },
        { s, _, _ -> curvature * s },
        { _, _, _ -> 0.0 },
        { s, _, z -> -z * curvature * s }
    )

    /** The pure across-helix bending field `W = ½κy²`. */
    fun acrossHelixCurvatureField(curvature: Double): F64Array = nodalField(
        { _, y, _ -> 0.5 * curvature * y * y },
        { _, _, _ -> 0.0 },
        { _, y, _ -> curvature * y },
        { _, _, _ -> 0.0 }
    )

    // ------------------------------------------------------------------ energies

    /** The bending and torsion energy in pN·nm the duplexes store in [field]. */
    fun beamEnergy(field: F64Array): Double {
        var total = 0.0
        for (beam in 0 until beamCount) {
            for (element in 0 until nodeS.size - 1) {
                val length = nodeS[element + 1] - nodeS[element]
                val q = doubleArrayOf(
                    field[dof(element, beam, W)], field[dof(element, beam, THETA)],
                    field[dof(element + 1, beam, W)], field[dof(element + 1, beam, THETA)]
                )
                val matrix = honeycombHermiteBending(duplex.bendingRigidity, length)
                for (i in 0 until 4) for (j in 0 until 4) total += q[i] * matrix[i][j] * q[j]
                val twist = field[dof(element + 1, beam, PHI)] - field[dof(element, beam, PHI)]
                total += duplex.torsionalRigidity * twist * twist / length
            }
        }
        return 0.5 * total
    }

    /** The axial strain energy in pN·nm the duplexes store in [field]. */
    fun axialEnergy(field: F64Array): Double {
        var total = 0.0
        for (beam in 0 until beamCount) {
            for (element in 0 until nodeS.size - 1) {
                val length = nodeS[element + 1] - nodeS[element]
                val stretch = field[dof(element + 1, beam, U)] - field[dof(element, beam, U)]
                total += duplex.stretchModulus * stretch * stretch / length
            }
        }
        return 0.5 * total
    }

    /** The relative roll in rad across [bond] in [field]. */
    fun hingeRotation(field: F64Array, bond: HoneycombLatticeBond): Double =
        field[dof(bond.node, bond.site.upperBeam, PHI)] -
                field[dof(bond.node, bond.site.lowerBeam, PHI)]

    /** The extension in nm of [bond]'s normal link in [field]. */
    fun linkExtension(field: F64Array, bond: HoneycombLatticeBond): Double {
        val arm = block.bondLength / 2.0 * bond.unitY
        return field[dof(bond.node, bond.site.lowerBeam, W)] +
                arm * field[dof(bond.node, bond.site.lowerBeam, PHI)] -
                field[dof(bond.node, bond.site.upperBeam, W)] +
                arm * field[dof(bond.node, bond.site.upperBeam, PHI)]
    }

    /** The axial slip in nm across [bond] in [field]. */
    fun bondSlip(field: F64Array, bond: HoneycombLatticeBond): Double {
        val arm = block.bondLength / 2.0 * bond.unitZ
        return field[dof(bond.node, bond.site.lowerBeam, U)] -
                arm * field[dof(bond.node, bond.site.lowerBeam, THETA)] -
                field[dof(bond.node, bond.site.upperBeam, U)] -
                arm * field[dof(bond.node, bond.site.upperBeam, THETA)]
    }

    /** The energy in pN·nm the dihedral springs store in [field], prestrain included. */
    fun hingeEnergy(field: F64Array): Double = 0.5 * hingeStiffness * hingeStiffnessEnhancement *
            (bonds.sumOf {
                val rotation = hingeRotation(field, it) - prestrainOf(it)
                rotation * rotation
            } + turnElements.sumOf {
                val rotation = turnRotation(field, it) - it.tie.prestrainRadians
                rotation * rotation
            })

    /** The penalty residual in pN·nm of the normal links in [field]. */
    fun linkEnergy(field: F64Array): Double = 0.5 * linkStiffness *
            bonds.sumOf { val gap = linkExtension(field, it); gap * gap }

    /** The energy in pN·nm the axial slip springs store in [field]. */
    fun slipEnergy(field: F64Array): Double = 0.5 * slipStiffness *
            bonds.sumOf { val slip = bondSlip(field, it); slip * slip }

    /** The **link** residual in nm of scaffold turn tether [element] in [field]. */
    fun tetherLinkResidual(field: F64Array, element: HoneycombTetherElement): Double {
        val arm = block.bondLength / 2.0 * element.unitY
        return field[dof(element.node, element.tether.lowerBeam, W)] +
                arm * field[dof(element.node, element.tether.lowerBeam, PHI)] -
                field[dof(element.node, element.tether.upperBeam, W)] +
                arm * field[dof(element.node, element.tether.upperBeam, PHI)]
    }

    /** The **slip** residual in nm across scaffold turn tether [element] in [field]. */
    fun tetherSlip(field: F64Array, element: HoneycombTetherElement): Double {
        val arm = block.bondLength / 2.0 * element.unitZ
        return field[dof(element.node, element.tether.lowerBeam, U)] -
                arm * field[dof(element.node, element.tether.lowerBeam, THETA)] -
                field[dof(element.node, element.tether.upperBeam, U)] -
                arm * field[dof(element.node, element.tether.upperBeam, THETA)]
    }

    /**
     * The change in nm of the chain's own end-to-end distance at [element] in [field].
     *
     * `δ|Δ| = n̂·δ⃗ = −unitZ ·` [tetherLinkResidual], because the model carries no in-plane
     * transverse coordinate — so it is identically zero at an **in-plane** turn, whatever the
     * field. Negative means the chain has **shortened**, which is what its own preload does.
     */
    fun tetherChainExtension(field: F64Array, element: HoneycombTetherElement): Double =
        -element.unitZ * tetherLinkResidual(field, element)

    /**
     * The energy in pN·nm the tethers' linearised stiffness stores in [field], preload excluded.
     *
     * The preload is a **load** and its work is not part of this; what is asserted on it instead
     * is that a rigid roll does zero work against [tetherPreloadLoad].
     */
    fun tetherEnergy(field: F64Array): Double = 0.5 * tetherElements.sumOf {
        val residual = tetherLinkResidual(field, it)
        val slip = tetherSlip(field, it)
        it.normalStiffness * residual * residual + it.axialStiffness * slip * slip
    }

    // ------------------------------------------------------------------ evaluation

    private fun faceBeamOf(y: Double): Int =
        faceBeams.minByOrNull { abs(beamY[it] - y) }!!

    private fun elementOf(s: Double): Int {
        var element = 0
        while (element < nodeS.size - 2 && nodeS[element + 1] < s) element++
        return element
    }

    /** The face deflection in nm of [field] at ([s], [y]). */
    fun evaluate(field: F64Array, s: Double, y: Double): Double {
        val beam = faceBeamOf(y)
        val element = elementOf(s)
        val length = nodeS[element + 1] - nodeS[element]
        val t = (s - nodeS[element]) / length
        val hermite = honeycombHermiteShape(t, length)
        val deflection = hermite[0] * field[dof(element, beam, W)] +
                hermite[1] * field[dof(element, beam, THETA)] +
                hermite[2] * field[dof(element + 1, beam, W)] +
                hermite[3] * field[dof(element + 1, beam, THETA)]
        val roll = (1.0 - t) * field[dof(element, beam, PHI)] + t * field[dof(element + 1, beam, PHI)]
        return deflection + roll * (y - beamY[beam])
    }

    internal fun overFaceGrid(samples: Int, field: (Double, Double) -> Double): Double {
        require(samples >= 2) { "samples must be at least 2, was: $samples" }
        var peak = 0.0
        for (i in 0 until samples) {
            val s = -lengthS / 2.0 + lengthS * i / (samples - 1)
            for (j in 0 until samples) {
                peak = max(peak, field(s, -lengthY / 2.0 + lengthY * j / (samples - 1)))
            }
        }
        return peak
    }

    /** Integrates [field] over the face's tributaries. */
    fun integrateOverFace(field: (Double, Double) -> Double): Double {
        var total = 0.0
        faceBeams.indices.forEach { index ->
            val (low, high) = tributary(index)
            for (element in 0 until nodeS.size - 1) {
                val length = nodeS[element + 1] - nodeS[element]
                for (q in 0 until honeycombQuadrature.points) {
                    val s = nodeS[element] + length * (honeycombQuadrature.nodes[q] + 1.0) / 2.0
                    val weightS = honeycombQuadrature.weights[q] * length / 2.0
                    for (r in 0 until honeycombQuadrature.points) {
                        val y = (low + high) / 2.0 + (high - low) / 2.0 * honeycombQuadrature.nodes[r]
                        total += weightS * honeycombQuadrature.weights[r] * (high - low) / 2.0 * field(s, y)
                    }
                }
            }
        }
        return total
    }

    /**
     * The vector `v` for which `v · q` is `∫ w_field w_q dA` over the face.
     *
     * The face inner product is bilinear, so one such vector per rigid mode turns each of the
     * three projections a dishing decomposition needs from a two-field quadrature into a **dot
     * product** — which is what makes an influence bank over every bond of the lattice
     * affordable. It is the same quadrature [assembleLoad] uses, on the same points, which is
     * asserted rather than argued.
     */
    internal fun faceFunctional(field: F64Array): F64Array {
        val dual = F64Array(degreesOfFreedom)
        faceBeams.forEachIndexed { index, beam ->
            val (low, high) = tributary(index)
            val axis = beamY[beam]
            for (element in 0 until nodeS.size - 1) {
                val length = nodeS[element + 1] - nodeS[element]
                for (q in 0 until honeycombQuadrature.points) {
                    val t = (honeycombQuadrature.nodes[q] + 1.0) / 2.0
                    val s = nodeS[element] + t * length
                    val weightS = honeycombQuadrature.weights[q] * length / 2.0
                    val hermite = honeycombHermiteShape(t, length)
                    var vertical = 0.0
                    var moment = 0.0
                    for (r in 0 until honeycombQuadrature.points) {
                        val y = (low + high) / 2.0 +
                                (high - low) / 2.0 * honeycombQuadrature.nodes[r]
                        val weight = honeycombQuadrature.weights[r] * (high - low) / 2.0 *
                                evaluate(field, s, y)
                        vertical += weight
                        moment += weight * (y - axis)
                    }
                    dual[dof(element, beam, W)] += weightS * hermite[0] * vertical
                    dual[dof(element, beam, THETA)] += weightS * hermite[1] * vertical
                    dual[dof(element + 1, beam, W)] += weightS * hermite[2] * vertical
                    dual[dof(element + 1, beam, THETA)] += weightS * hermite[3] * vertical
                    dual[dof(element, beam, PHI)] += weightS * (1.0 - t) * moment
                    dual[dof(element + 1, beam, PHI)] += weightS * t * moment
                }
            }
        }
        return dual
    }

    /** The three rigid modes as functionals, precomputed once — see [faceFunctional]. */
    internal val pistonDual: F64Array by lazy { faceFunctional(pistonMode) }
    internal val tiltSDual: F64Array by lazy { faceFunctional(tiltSMode) }
    internal val tiltYDual: F64Array by lazy { faceFunctional(tiltYMode) }

    /** `∫ 1 dA` and the two tilt norms, precomputed with them. */
    internal val tiltSNorm: Double by lazy { tiltSDual.dot(tiltSMode) }
    internal val tiltYNorm: Double by lazy { tiltYDual.dot(tiltYMode) }

    /** `(1/A) ∫ w_a w_b dA` over the face, in nm². */
    fun areaInnerProduct(a: F64Array, b: F64Array): Double =
        integrateOverFace { s, y -> evaluate(a, s, y) * evaluate(b, s, y) } / area

    // ------------------------------------------------------------------ load and solve

    internal fun assembleLoad(pressure: PressureField): F64Array {
        val load = F64Array(degreesOfFreedom)
        faceBeams.forEachIndexed { index, beam ->
            val (low, high) = tributary(index)
            val axis = beamY[beam]
            for (element in 0 until nodeS.size - 1) {
                val length = nodeS[element + 1] - nodeS[element]
                for (q in 0 until honeycombQuadrature.points) {
                    val t = (honeycombQuadrature.nodes[q] + 1.0) / 2.0
                    val s = nodeS[element] + t * length
                    val weightS = honeycombQuadrature.weights[q] * length / 2.0
                    val hermite = honeycombHermiteShape(t, length)
                    var vertical = 0.0
                    var moment = 0.0
                    for (r in 0 until honeycombQuadrature.points) {
                        val y = (low + high) / 2.0 + (high - low) / 2.0 * honeycombQuadrature.nodes[r]
                        val weight = honeycombQuadrature.weights[r] * (high - low) / 2.0 * pressure.at(s, y)
                        vertical += weight
                        moment += weight * (y - axis)
                    }
                    load[dof(element, beam, W)] += weightS * hermite[0] * vertical
                    load[dof(element, beam, THETA)] += weightS * hermite[1] * vertical
                    load[dof(element + 1, beam, W)] += weightS * hermite[2] * vertical
                    load[dof(element + 1, beam, THETA)] += weightS * hermite[3] * vertical
                    load[dof(element, beam, PHI)] += weightS * (1.0 - t) * moment
                    load[dof(element + 1, beam, PHI)] += weightS * t * moment
                }
            }
        }
        addPrestrainCouples(load)
        load[pinnedDof] = 0.0
        return load
    }

    /**
     * `T-172`'s initial-stress term: `½ k_θ (Δφ − θ₀)²` has the same quadratic part, so the only
     * new term is a fixed couple `± k_θ θ₀` on the two roll coordinates. **The couple is taken at
     * the physical [hingeStiffness]** and never at the enhanced one, because the enhancement is a
     * smeared stiffness and a prestrain is a real hinge's real couple.
     */
    private fun addPrestrainCouples(load: F64Array) {
        if (bondPrestrains.isNotEmpty()) {
            bonds.forEach { bond ->
                val angle = prestrainOf(bond)
                if (angle == 0.0) return@forEach
                val couple = hingeStiffness * angle
                load[dof(bond.node, bond.site.upperBeam, PHI)] += couple
                load[dof(bond.node, bond.site.lowerBeam, PHI)] -= couple
            }
        }
        turnElements.forEach { element ->
            val angle = element.tie.prestrainRadians
            if (angle == 0.0) return@forEach
            val couple = hingeStiffness * angle
            load[dof(element.node, element.tie.upperBeam, PHI)] += couple
            load[dof(element.node, element.tie.lowerBeam, PHI)] -= couple
        }
        addTetherPreload(load)
    }

    /**
     * `T-299`'s tether preload: a freely-jointed chain held at any `x > 0` is in **tension**, and
     * that tension is a self-equilibrated internal load between the turn's two rim nodes.
     *
     * The chain's own length changes by `δ|Δ| = n̂·δ⃗`, and with `δ_y ≡ 0` (the model has no
     * in-plane transverse coordinate) that is `−unitZ` times the link residual — so the energy
     * `f·δ|Δ|` contributes `+f·unitZ` times the link gradient to the load vector. The **in-plane**
     * turns therefore contribute exactly **zero**: their pull is entirely along `y`, a direction
     * this lattice has no coordinate for, and that is a statement about the model rather than
     * about the chain.
     */
    private fun addTetherPreload(load: F64Array) {
        val half = block.bondLength / 2.0
        tetherElements.forEach { element ->
            val magnitude = element.tether.tension * element.unitZ
            if (magnitude == 0.0) return@forEach
            val a = element.tether.lowerBeam
            val b = element.tether.upperBeam
            val armY = half * element.unitY
            load[dof(element.node, a, W)] += magnitude
            load[dof(element.node, a, PHI)] += magnitude * armY
            load[dof(element.node, b, W)] -= magnitude
            load[dof(element.node, b, PHI)] += magnitude * armY
        }
    }

    /** The preload of every tether alone, with the axial rigid mode's pin honoured. */
    fun tetherPreloadLoad(): F64Array {
        val load = F64Array(degreesOfFreedom)
        addTetherPreload(load)
        load[pinnedDof] = 0.0
        return load
    }

    /**
     * `T-263` — the work-conjugate load vector of a downward point load of [magnitude] pN applied
     * to the **face** at ([s], [y]).
     *
     * It is the exact gradient of [evaluate] at that point, which is what makes it the work
     * conjugate rather than a lumped approximation of one: a station off its beam's own axis is
     * carried through that beam's **roll**, with the offset `y − beamY` as the moment arm, and a
     * station on the axis puts nothing into the roll at all.
     *
     * Two consequences are worth stating, because one of them is a gate and the other is not.
     * The influence matrix a coupling surrogate assembles from this dual is `M = eᵀK⁻¹e`, so
     * Maxwell-Betti holds **by construction** and its residual measures nothing; what does have
     * content is Betti between this point functional and the **pressure quadrature**, which is a
     * different rule on different points, and that is asserted in the tests.
     */
    fun pointLoadDual(s: Double, y: Double, magnitude: Double = 1.0): F64Array {
        val load = F64Array(degreesOfFreedom)
        addPointLoad(load, s, y, magnitude)
        return load
    }

    private fun addPointLoad(load: F64Array, s: Double, y: Double, magnitude: Double) {
        require(abs(s) < lengthS / 2.0 + FACE_TOLERANCE) {
            "a station must lie on the face, |s| <= ${lengthS / 2.0} nm, was: $s"
        }
        require(abs(y) < lengthY / 2.0 + FACE_TOLERANCE) {
            "a station must lie on the face, |y| <= ${lengthY / 2.0} nm, was: $y"
        }
        require(magnitude.isFinite()) { "the point load must be finite, was: $magnitude" }
        val beam = faceBeamOf(y)
        val element = elementOf(s)
        val length = nodeS[element + 1] - nodeS[element]
        val t = (s - nodeS[element]) / length
        val hermite = honeycombHermiteShape(t, length)
        val arm = y - beamY[beam]
        load[dof(element, beam, W)] += magnitude * hermite[0]
        load[dof(element, beam, THETA)] += magnitude * hermite[1]
        load[dof(element + 1, beam, W)] += magnitude * hermite[2]
        load[dof(element + 1, beam, THETA)] += magnitude * hermite[3]
        load[dof(element, beam, PHI)] += magnitude * (1.0 - t) * arm
        load[dof(element + 1, beam, PHI)] += magnitude * t * arm
    }

    /**
     * Solves one load case: a [pressure] field on the face, any [pointLoads] on the face, plus
     * any prestrain.
     *
     * [pointLoads] carry `force` **positive downward**, which is `W`'s own sense — so a coupling's
     * upward support force enters as its negative, and that sign is asserted against the
     * surrogate rather than argued.
     */
    fun solve(
        pressure: PressureField = uniformPressure(0.0),
        pointLoads: List<PointLoad> = emptyList()
    ): HoneycombDeflection {
        val load = assembleLoad(pressure)
        pointLoads.forEach { addPointLoad(load, it.x, it.y, it.force) }
        load[pinnedDof] = 0.0
        return HoneycombDeflection(this, factorisation.solve(load), pressure)
    }

    /**
     * The face field of a unit prestrain at [bond] alone — the influence function the triangle
     * inequality is written on, taken on this lattice's own factorisation.
     */
    fun unitPrestrainResponse(bond: HoneycombLatticeBond): HoneycombDeflection {
        val load = F64Array(degreesOfFreedom)
        val couple = hingeStiffness
        load[dof(bond.node, bond.site.upperBeam, PHI)] += couple
        load[dof(bond.node, bond.site.lowerBeam, PHI)] -= couple
        load[pinnedDof] = 0.0
        return HoneycombDeflection(this, factorisation.solve(load), uniformPressure(0.0))
    }

    /**
     * The face field of a unit prestrain at the scaffold turn tie [element] alone — `T-254`'s
     * influence function, taken on this lattice's own factorisation.
     */
    fun unitTurnResponse(element: HoneycombTurnElement): HoneycombDeflection {
        val load = F64Array(degreesOfFreedom)
        val couple = hingeStiffness
        load[dof(element.node, element.tie.upperBeam, PHI)] += couple
        load[dof(element.node, element.tie.lowerBeam, PHI)] -= couple
        load[pinnedDof] = 0.0
        return HoneycombDeflection(this, factorisation.solve(load), uniformPressure(0.0))
    }

    /**
     * `T-291` — the face field of a per-beam **torsional eigenstrain**: a built-in twist of
     * `twistRadians[beam]` distributed uniformly over that beam's whole length, and exactly zero
     * on any beam the map does not name.
     *
     * This is the coordinate a **common-mode** azimuthal demand actually loads. `CH-0240` shows
     * that displacing a scaffold crossover rotates **both** backbones the same way, so the demand
     * is a roll of each duplex about its **own** axis and has coefficient exactly zero on the
     * relative roll `Φ_upper − Φ_lower` the tie prestrain is the work conjugate of. Where the
     * demands at a beam's two ends differ — which the derived alternation of `C-0187` makes them
     * do at every interior helix — what is left is a twist of that beam, and this is its load.
     *
     * The element torsion spring is `½ (GJ/L_e)(ΔΦ_e − θ_e)²`, so an eigenstrain contributes a
     * fixed couple `∓ GJ θ_e / L_e` at the element's two roll coordinates. A **uniform twist
     * rate** puts `θ_e = θ₀ L_e / L`, which makes `GJ θ_e / L_e = GJ θ₀ / L` the same at every
     * element — so the interior contributions **telescope away** and the whole load is one couple
     * pair at the beam's two **end** nodes, at any node spacing and any [subdivisions]. That is
     * asserted as a test rather than argued.
     *
     * Like every prestrain here it is a **load**: no entry of the stiffness matrix moves, the
     * field is exactly linear in [twistRadians], and one solve fixes the whole axis.
     */
    fun beamTwistResponse(twistRadians: Map<Int, Double>): HoneycombDeflection {
        twistRadians.forEach { (beam, twist) ->
            require(beam in 0 until beamCount) {
                "a twist must name a beam of the block, was: $beam"
            }
            require(twist.isFinite()) { "the twist at beam $beam must be finite, was: $twist" }
        }
        val span = nodeS.last() - nodeS.first()
        val load = F64Array(degreesOfFreedom)
        twistRadians.forEach { (beam, twist) ->
            val couple = duplex.torsionalRigidity * twist / span
            load[dof(nodeS.size - 1, beam, PHI)] += couple
            load[dof(0, beam, PHI)] -= couple
        }
        load[pinnedDof] = 0.0
        return HoneycombDeflection(this, factorisation.solve(load), uniformPressure(0.0))
    }

    /** The relative roll in radians across the turn tie [element] in [field]. */
    fun turnRotation(field: F64Array, element: HoneycombTurnElement): Double =
        field[dof(element.node, element.tie.upperBeam, PHI)] -
                field[dof(element.node, element.tie.lowerBeam, PHI)]

    /**
     * The extension in nm of the turn tie [element]'s normal link in [field].
     *
     * [linkExtension] read on a raster turn rather than on a lattice bond — the same gradient
     * `(1, armY, −1, armY)` over `(W_lower, Φ_lower, W_upper, Φ_upper)`, and therefore the same
     * **common-mode** azimuthal coordinate, `ΔW + (d/2)·unitY·(Φ_lower + Φ_upper)` (`T-297`).
     *
     * `linkEnergy` and `slipEnergy` sum over [bonds] only and are left exactly as they were, so
     * nothing that reads them can move; this is a reader, not a change to either.
     */
    fun turnLinkExtension(field: F64Array, element: HoneycombTurnElement): Double {
        val arm = block.bondLength / 2.0 * element.unitY
        return field[dof(element.node, element.tie.lowerBeam, W)] +
                arm * field[dof(element.node, element.tie.lowerBeam, PHI)] -
                field[dof(element.node, element.tie.upperBeam, W)] +
                arm * field[dof(element.node, element.tie.upperBeam, PHI)]
    }

    /**
     * The load in pN and pN·nm a set of raster-turn **link offsets** applies — the departure on
     * the coordinate it actually lives on (`T-297`, `CH-0242`).
     *
     * A crossover built with **both** backbones rolled by `ρ` off the line of centres is relaxed
     * at the link residual `R₀ = d·unitY·ρ` (`turnLinkOffset`), not at zero. The link element is
     * then `½ k_link (R − R₀)²`, whose quadratic part is unchanged — so this is a **load**, no
     * entry of the stiffness matrix moves, the field is exactly linear in the offsets, and
     * `C-0104`'s influence-bank trap does not arise.
     *
     * The load is `k_link·R₀` times the link's own gradient. Its magnitude therefore scales with
     * the penalty, and the **field** converges as the penalty stiffens, because an offset in a
     * constraint has a well-posed constrained limit — which is asserted rather than argued.
     *
     * @param offsetsByTurnIndex the link offset in nm, keyed on the index of the tie in
     *   [turnElements]. Absent ties carry none.
     */
    fun turnLinkOffsetLoad(offsetsByTurnIndex: Map<Int, Double>): F64Array {
        offsetsByTurnIndex.forEach { (index, offset) ->
            require(index in turnElements.indices) {
                "a link offset must name a raster turn of the lattice, was: $index"
            }
            require(offset.isFinite()) {
                "the link offset at turn $index must be finite, was: $offset"
            }
        }
        val load = F64Array(degreesOfFreedom)
        val half = block.bondLength / 2.0
        offsetsByTurnIndex.forEach { (index, offset) ->
            val element = turnElements[index]
            val a = element.tie.lowerBeam
            val b = element.tie.upperBeam
            val node = element.node
            val armY = half * element.unitY
            val magnitude = linkStiffness * offset
            load[dof(node, a, W)] += magnitude
            load[dof(node, a, PHI)] += magnitude * armY
            load[dof(node, b, W)] -= magnitude
            load[dof(node, b, PHI)] += magnitude * armY
        }
        return load
    }

    /** The field [turnLinkOffsetLoad] produces, with the axial rigid mode's pin honoured. */
    fun turnLinkOffsetResponse(offsetsByTurnIndex: Map<Int, Double>): HoneycombDeflection {
        val load = turnLinkOffsetLoad(offsetsByTurnIndex)
        load[pinnedDof] = 0.0
        return HoneycombDeflection(this, factorisation.solve(load), uniformPressure(0.0))
    }

    companion object {

        /** The face deflection degree of freedom of a node. */
        const val W: Int = 0

        /** The `dW/ds` degree of freedom of a node. */
        const val THETA: Int = 1

        /** The roll degree of freedom of a node. */
        const val PHI: Int = 2

        /** The **axial** degree of freedom of a node — the one `OrigamiGrillage` does not have. */
        const val U: Int = 3

        /** Degrees of freedom per node. */
        const val DOF_PER_NODE: Int = 4

        /** The penalty stiffness in pN/nm of the covalent normal link, `OrigamiGrillage`'s own. */
        const val RIGID_LINK_STIFFNESS: Double = 1e4

        /** Gauss points per element and per tributary strip. */
        const val QUADRATURE_POINTS: Int = 6

        /**
         * How far outside the face in nm a station may be quoted and still be accepted.
         *
         * A tenth of a base-pair rise: a placement is quantised at 0.34 nm, so nothing a design
         * can draw lands inside this, and it exists only so that a station written exactly at the
         * rim survives its own rounding.
         */
        const val FACE_TOLERANCE: Double = 0.034

    }

}

private val honeycombQuadrature = gaussLegendreRule(HoneycombGrillage.QUADRATURE_POINTS)

private fun honeycombHermiteShape(t: Double, length: Double): DoubleArray = doubleArrayOf(
    1.0 - 3.0 * t * t + 2.0 * t * t * t,
    length * (t - 2.0 * t * t + t * t * t),
    3.0 * t * t - 2.0 * t * t * t,
    length * (t * t * t - t * t)
)

private fun honeycombHermiteBending(rigidity: Double, length: Double): Array<DoubleArray> {
    val l = length
    val scale = rigidity / (l * l * l)
    return arrayOf(
        doubleArrayOf(12.0, 6.0 * l, -12.0, 6.0 * l),
        doubleArrayOf(6.0 * l, 4.0 * l * l, -6.0 * l, 2.0 * l * l),
        doubleArrayOf(-12.0, -6.0 * l, 12.0, -6.0 * l),
        doubleArrayOf(6.0 * l, 2.0 * l * l, -6.0 * l, 4.0 * l * l)
    ).map { row -> DoubleArray(row.size) { row[it] * scale } }.toTypedArray()
}

/** The root-mean-square of the face dishing of [deflection], in nm. */
fun honeycombDishingRms(lattice: HoneycombGrillage, deflection: HoneycombDeflection): Double =
    sqrt(max(0.0, lattice.areaInnerProduct(deflection.dishingCoefficients, deflection.dishingCoefficients)))
