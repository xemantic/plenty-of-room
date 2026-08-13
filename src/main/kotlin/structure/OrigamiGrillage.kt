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

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.thermalEnergy
import org.jetbrains.bio.viktor.F64Array
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * One antiparallel crossover of the lattice, and what it carries.
 *
 * @param lowerBeam the index of the lower-`y` duplex of the pair it joins.
 * @param column the index of the crossover column it sits in.
 * @param x its position along the helices, in nm.
 * @param y the mid-point between the two helix axes, in nm.
 * @param verticalForce the transverse force in pN it transmits between the two duplexes —
 *          the **per-load-path force** the continuum plate smears out.
 * @param hingeMoment the moment in `pN·nm` its two phosphate bonds carry, `k_θ Δφ`.
 */
data class CrossoverForce(
    val lowerBeam: Int,
    val column: Int,
    val x: Double,
    val y: Double,
    val verticalForce: Double,
    val hingeMoment: Double
)

/**
 * The thermal fluctuation of a free grillage on an elastic foundation, split exactly as
 * [PlateThermalFluctuation] splits the continuum plate's, so the two are comparable term
 * by term. All amplitudes are root-mean-square, in nm.
 */
data class GrillageThermalFluctuation(
    val pistonRms: Double,
    val tiltRms: Double,
    val dishingRms: Double,
    val centreRms: Double
)

/**
 * The deflected state of an [OrigamiGrillage] under one load case.
 *
 * `w` is positive **downward**, compressing the polymer layer, and `x` runs along the
 * helices — the `T-5` conventions, unchanged.
 */
class GrillageDeflection internal constructor(
    private val lattice: OrigamiGrillage,
    val coefficients: F64Array,
    private val pressure: PressureField,
    private val pointLoads: List<PointLoad>,
    /**
     * Anchors that are **not** part of the lattice's own stiffness matrix, because they were
     * applied as a rank-one update by [OrigamiGrillage.solveWithAnchor] rather than
     * assembled. They react exactly as an assembled [PointSupport] does, and every
     * equilibrium statement below has to count them.
     */
    private val updatedSupports: List<PointSupport> = emptyList()
) {

    /** The deflection in nm at ([x], [y]). */
    fun deflection(x: Double, y: Double): Double = lattice.evaluate(coefficients, x, y)

    /** The area-averaged deflection in nm — what a rigid-plate model reports as *the* displacement. */
    val meanDeflection: Double by lazy {
        lattice.areaInnerProduct(lattice.pistonMode, coefficients)
    }

    /** The slope in `nm/nm` of the best-fit rigid plane along the helices. */
    val tiltAlongHelices: Double by lazy {
        lattice.areaInnerProduct(lattice.tiltXMode, coefficients) /
                lattice.areaInnerProduct(lattice.tiltXMode, lattice.tiltXMode)
    }

    /** The slope in `nm/nm` of the best-fit rigid plane across the helices. */
    val tiltAcrossHelices: Double by lazy {
        lattice.areaInnerProduct(lattice.tiltYMode, coefficients) /
                lattice.areaInnerProduct(lattice.tiltYMode, lattice.tiltYMode)
    }

    /**
     * The deflection with its area-averaged least-squares best-fit plane removed —
     * the part a rigid tile cannot represent, held in the same nodal coordinates.
     *
     * Formed as a residual **vector** rather than as a difference of two evaluated fields,
     * so that a load case which produces no dishing produces exactly zero rather than the
     * cancellation noise of two large equal numbers.
     */
    val dishingCoefficients: F64Array by lazy {
        val residual = coefficients.copy()
        residual -= lattice.pistonMode * meanDeflection
        residual -= lattice.tiltXMode * tiltAlongHelices
        residual -= lattice.tiltYMode * tiltAcrossHelices
        residual
    }

    /** The dishing in nm at ([x], [y]). */
    fun dishing(x: Double, y: Double): Double = lattice.evaluate(dishingCoefficients, x, y)

    /** The area root-mean-square dishing over the footprint, in nm. */
    val dishingRms: Double by lazy {
        sqrt(max(0.0, lattice.areaInnerProduct(dishingCoefficients, dishingCoefficients)))
    }

    /** The largest absolute dishing over a [samples] × [samples] grid, in nm. */
    fun peakDishing(samples: Int = 81): Double = lattice.overGrid(samples) { x, y ->
        abs(dishing(x, y))
    }

    /** The largest absolute deflection over a [samples] × [samples] grid, in nm. */
    fun peakDeflection(samples: Int = 81): Double = lattice.overGrid(samples) { x, y ->
        abs(deflection(x, y))
    }

    /** The force in pN carried by each anchor, in the order the lattice holds them. */
    val supportForces: List<Double>
        get() = (lattice.supports + updatedSupports).map {
            it.stiffness * deflection(it.x, it.y)
        }

    /** The total force in pN carried by the polymer foundation. */
    val foundationForce: Double
        get() = lattice.foundationStiffness * lattice.area * meanDeflection

    /** The total applied force in pN. */
    val appliedForce: Double
        get() = lattice.integrateOverFootprint { x, y -> pressure.at(x, y) } +
                pointLoads.sumOf { it.force }

    /** What every crossover of the lattice carries. */
    val crossoverForces: List<CrossoverForce> by lazy {
        lattice.crossovers.map { crossover ->
            CrossoverForce(
                lowerBeam = crossover.lowerBeam,
                column = crossover.column,
                x = crossover.x,
                y = crossover.y,
                // signed so that a positive force is one transmitted from the far side of the
                // interface toward the near one, matching [shearAcrossInterface]
                verticalForce = -lattice.linkStiffness *
                        lattice.linkExtension(coefficients, crossover),
                hingeMoment = lattice.sheet.crossoverHingeStiffness *
                        lattice.hingeRotation(coefficients, crossover)
            )
        }
    }

    /** The largest transverse force in pN any single crossover transmits. */
    val peakCrossoverForce: Double
        get() = crossoverForces.maxOf { abs(it.verticalForce) }

    /** The largest moment in `pN·nm` any single crossover hinge carries. */
    val peakHingeMoment: Double
        get() = crossoverForces.maxOf { abs(it.hingeMoment) }

    /**
     * The transverse shear in pN carried by one duplex, at every element of every beam —
     * `EI w'''`, which a Hermite element carries exactly and constantly along its length.
     */
    val duplexShears: List<Double> by lazy { lattice.duplexShears(coefficients) }

    /** The largest transverse shear in pN carried by any single duplex. */
    val peakDuplexShear: Double get() = duplexShears.maxOf { abs(it) }

    /**
     * The total transverse force in pN crossing the interface between beam [lowerBeam] and
     * the one above it, from the equilibrium of everything beyond the cut.
     *
     * The discrete counterpart of [PlateDeflection.shearAcrossCrossoverLine], and the
     * quantity the crossovers on that interface have to sum to.
     */
    fun shearAcrossInterface(lowerBeam: Int): Double {
        val cut = (lattice.beamY[lowerBeam] + lattice.beamY[lowerBeam + 1]) / 2.0
        val top = lattice.lengthY / 2.0
        val load = lattice.integrate(
            -lattice.lengthX / 2.0, lattice.lengthX / 2.0, cut, top
        ) { x, y -> pressure.at(x, y) - lattice.foundationStiffness * deflection(x, y) }
        val point = pointLoads.filter { it.y > cut }.sumOf { it.force }
        val reactions = (lattice.supports + updatedSupports).filter { it.y > cut }
            .sumOf { it.stiffness * deflection(it.x, it.y) }
        return load + point - reactions
    }

}

/**
 * A single-layer DNA-origami sheet as the **discrete beam-and-hinge grillage** it physically
 * is, rather than as the continuum orthotropic plate `C-0006` reduced it to.
 *
 * ## Why this model exists
 *
 * `C-0006` reported its own reduction as marginal: the across-helix bending length is shorter
 * than the crossover spacing across the whole foundation sweep, so at the wavelength that
 * matters the sheet is nearer to a set of quasi-independent duplex beams linked at discrete
 * hinges than to a plate. And a continuum plate cannot resolve the peak force at a discrete
 * anchor at all — it smears the anchor reaction over a contour. This lattice resolves both.
 *
 * ## The ingredients, deliberately identical to `C-0006`'s
 *
 * The comparison is meant to be of *functional form*, not of parameterisation, so every
 * physical input is taken from the same [OrigamiSheet]:
 *
 * - each duplex is an Euler-Bernoulli beam of bending rigidity `EI = L_p k_BT` and torsional
 *   rigidity `GJ`, running along `x`, spaced [OrigamiSheet.interhelicalDistance] apart in `y`;
 * - each crossover is a discrete torsional hinge of constant `k_θ` resisting the *relative
 *   roll* of the two duplexes it joins, plus a stiff vertical link holding their surfaces
 *   together — the link is a constraint, and [linkStiffness] is a penalty whose value the
 *   answer must not depend on;
 * - crossovers recur every [OrigamiSheet.crossoverSpacing] along one interface and **alternate
 *   between a helix's two neighbours**, so the columns are spaced at half that.
 *
 * ## The long-wavelength limit is exact, and is gate 2
 *
 * Imposing a smooth field on the lattice reproduces the plate's three rigidities identically:
 * `w = ½κy²` costs `½ (k_θ d/p) κ²` per unit area, `w = ½κx²` costs `½ (EI/d) κ²`, and
 * `w = τxy` costs `2 (GJ/4d) τ²`. So this lattice **is** `C-0006`'s plate at long wavelength,
 * and any difference it reports is discreteness and nothing else.
 *
 * ## The foundation
 *
 * Winkler springs distributed over each beam's tributary strip of width `d`, giving a line
 * stiffness `k_f d` against deflection **and** `k_f d³/12` against roll — the second term is
 * what the continuum foundation `k_f ∫w² dA` contains implicitly and a node-lumped foundation
 * would lose. Consistent (energy-integrated) element matrices throughout, never lumped.
 *
 * @param sheet the duplex elasticity and sheet geometry, shared with the plate model.
 * @param lengthX the footprint along the helices, in nm; the footprint across them is
 *          `beamCount × d`, because a lattice can only be an integer number of duplexes wide.
 * @param beamCount the number of duplexes.
 * @param foundationStiffness `k_f` in `pN/nm³`.
 * @param columns where the crossover columns sit along the helices and which parity of
 *          interface each serves; one interface uses the columns of one parity only.
 *          `T-14` makes this the design variable it physically is — a **phase**, chosen by
 *          the staple layout — where `T-10` could only vary its count.
 * @param subdivisions the number of beam elements per interval between node columns.
 * @param linkStiffness the penalty stiffness in `pN/nm` of the vertical crossover link.
 * @param supports discrete anchors tying the lattice to ground.
 */
class OrigamiGrillage(
    val sheet: OrigamiSheet,
    val lengthX: Double,
    val beamCount: Int,
    val foundationStiffness: Double,
    val columns: CrossoverLayout,
    val subdivisions: Int = DEFAULT_SUBDIVISIONS,
    val linkStiffness: Double = RIGID_LINK_STIFFNESS,
    val supports: List<PointSupport> = emptyList()
) {

    /**
     * The `T-10` construction: [crossoverColumns] columns at pitch `p/2`, symmetrically
     * centred on the footprint. Retained unchanged so that nothing already published moves.
     */
    constructor(
        sheet: OrigamiSheet,
        lengthX: Double,
        beamCount: Int,
        foundationStiffness: Double,
        crossoverColumns: Int,
        subdivisions: Int = DEFAULT_SUBDIVISIONS,
        linkStiffness: Double = RIGID_LINK_STIFFNESS,
        supports: List<PointSupport> = emptyList()
    ) : this(
        sheet = sheet,
        lengthX = lengthX,
        beamCount = beamCount,
        foundationStiffness = foundationStiffness,
        columns = CrossoverLayout.centred(crossoverColumns, sheet.crossoverSpacing / 2.0),
        subdivisions = subdivisions,
        linkStiffness = linkStiffness,
        supports = supports
    )

    /** One crossover of the lattice, as geometry. */
    data class Crossover(
        val lowerBeam: Int,
        val column: Int,
        val node: Int,
        val x: Double,
        val y: Double
    )

    init {
        require(lengthX > 0.0) { "lengthX must be positive, was: $lengthX" }
        require(beamCount >= 2) { "beamCount must be at least 2, was: $beamCount" }
        require(foundationStiffness > 0.0) {
            "foundationStiffness must be positive, was: $foundationStiffness"
        }
        require(subdivisions >= 1) { "subdivisions must be at least 1, was: $subdivisions" }
        require(linkStiffness > 0.0) { "linkStiffness must be positive, was: $linkStiffness" }
    }

    /** The number of crossover columns across the footprint, counting both parities. */
    val crossoverColumns: Int get() = columns.size

    /** The interhelical distance `d` in nm. */
    val interhelicalDistance: Double get() = sheet.interhelicalDistance

    /** The crossover spacing `p` along one interface, in nm. */
    val crossoverSpacing: Double get() = sheet.crossoverSpacing

    /** The footprint across the helices in nm — an integer number of duplexes wide. */
    val lengthY: Double = beamCount * sheet.interhelicalDistance

    /** The footprint area in nm². */
    val area: Double = lengthX * lengthY

    /** The `y` of each duplex axis, in nm, centred on the footprint. */
    val beamY: List<Double> =
        (0 until beamCount).map { (it - (beamCount - 1) / 2.0) * sheet.interhelicalDistance }

    /** The `x` of each crossover column, in nm, centred on the footprint. */
    val columnX: List<Double> get() = columns.positions

    init {
        require(columnX.last() < lengthX / 2.0 && columnX.first() > -lengthX / 2.0) {
            "the crossover columns must fit strictly inside the footprint: " +
                    "$crossoverColumns columns spanning " +
                    "${columnX.first()} .. ${columnX.last()} nm " +
                    "against a footprint of $lengthX nm"
        }
    }

    /** The `x` of every node of every beam, in nm, ascending. */
    val nodeX: List<Double> = buildList {
        val stations = listOf(-lengthX / 2.0) + columnX + listOf(lengthX / 2.0)
        add(stations.first())
        for (i in 0 until stations.size - 1) {
            val step = (stations[i + 1] - stations[i]) / subdivisions
            for (k in 1..subdivisions) add(stations[i] + k * step)
        }
    }

    /** The node index of each crossover column. */
    private val columnNode: List<Int> = columnX.map { x ->
        nodeX.indices.minByOrNull { abs(nodeX[it] - x) }!!
    }

    /** The number of nodes on one beam. */
    val nodesPerBeam: Int get() = nodeX.size

    /** The number of degrees of freedom of the lattice. */
    val degreesOfFreedom: Int = beamCount * nodeX.size * DOF_PER_NODE

    /**
     * Every crossover of the lattice.
     *
     * Interface `i` takes the columns of one parity and interface `i+1` the other, which is
     * what "crossovers alternate between a helix's two neighbours" means geometrically.
     */
    val crossovers: List<Crossover> = buildList {
        for (beam in 0 until beamCount - 1) {
            for (column in 0 until crossoverColumns) {
                if ((columns.parities[column] + beam) % 2 != 0) continue
                add(
                    Crossover(
                        lowerBeam = beam,
                        column = column,
                        node = columnNode[column],
                        x = nodeX[columnNode[column]],
                        y = (beamY[beam] + beamY[beam + 1]) / 2.0
                    )
                )
            }
        }
    }

    private fun dof(beam: Int, node: Int, component: Int): Int =
        (beam * nodeX.size + node) * DOF_PER_NODE + component

    // ------------------------------------------------------------------ nodal fields

    /**
     * Returns the nodal coordinates of the smooth field whose value is [value], whose slope
     * along the helices is [slopeAlong] and whose slope across them is [slopeAcross].
     *
     * The lattice's kinematics reconstruct `w(x, y) = w_i(x) + φ_i(x)(y − y_i)` on each
     * duplex's tributary strip, so a field is imposed exactly by setting `w`, `dw/dx` and
     * `dw/dy` at the nodes — which is what makes the gate-2 limiting cases exact rather than
     * approximate.
     */
    fun nodalField(
        value: (Double, Double) -> Double,
        slopeAlong: (Double, Double) -> Double,
        slopeAcross: (Double, Double) -> Double
    ): F64Array {
        val field = F64Array(degreesOfFreedom)
        for (beam in 0 until beamCount) {
            val y = beamY[beam]
            for (node in nodeX.indices) {
                val x = nodeX[node]
                field[dof(beam, node, W)] = value(x, y)
                field[dof(beam, node, THETA)] = slopeAlong(x, y)
                field[dof(beam, node, PHI)] = slopeAcross(x, y)
            }
        }
        return field
    }

    /** The rigid translation, `w = 1`. */
    val pistonMode: F64Array by lazy {
        nodalField({ _, _ -> 1.0 }, { _, _ -> 0.0 }, { _, _ -> 0.0 })
    }

    /** The rigid tilt about the across-helix axis, `w = x`. */
    val tiltXMode: F64Array by lazy {
        nodalField({ x, _ -> x }, { _, _ -> 1.0 }, { _, _ -> 0.0 })
    }

    /** The rigid tilt about the along-helix axis, `w = y`. */
    val tiltYMode: F64Array by lazy {
        nodalField({ _, y -> y }, { _, _ -> 0.0 }, { _, _ -> 1.0 })
    }

    /** The pure across-helix bending field `w = ½κy²`, at curvature [curvature] in nm⁻¹. */
    fun curvatureFieldAcrossHelices(curvature: Double): F64Array = nodalField(
        { _, y -> 0.5 * curvature * y * y }, { _, _ -> 0.0 }, { _, y -> curvature * y }
    )

    /** The pure along-helix bending field `w = ½κx²`, at curvature [curvature] in nm⁻¹. */
    fun curvatureFieldAlongHelices(curvature: Double): F64Array = nodalField(
        { x, _ -> 0.5 * curvature * x * x }, { x, _ -> curvature * x }, { _, _ -> 0.0 }
    )

    /** The pure twist field `w = τxy`, at twist [twist] in nm⁻¹. */
    fun twistField(twist: Double): F64Array = nodalField(
        { x, y -> twist * x * y }, { _, y -> twist * y }, { x, _ -> twist * x }
    )

    // ------------------------------------------------------------------ assembly

    /**
     * The foundation matrix at unit `k_f`, divided by the footprint area — so that
     * `qᵀ G q` is the area-averaged mean square of the reconstructed deflection field.
     */
    private val areaGram: F64Array by lazy {
        val matrix = F64Array(degreesOfFreedom, degreesOfFreedom)
        addFoundation(matrix, 1.0 / area)
        matrix
    }

    /**
     * The full stiffness matrix: beams, hinges, links, foundation and anchors.
     *
     * Assembled straight into one array rather than as a sum of separately retained
     * contributions. At a few thousand degrees of freedom a dense `n × n` matrix is tens of
     * megabytes, so keeping five of them alive is what turns a comfortable calculation into
     * an out-of-memory failure; the individual contributions are available as **energies**
     * ([hingeEnergy], [linkEnergy], …) instead, which is all any check of them needs.
     */
    val stiffness: F64Array by lazy {
        val matrix = F64Array(degreesOfFreedom, degreesOfFreedom)
        addBeams(matrix)
        addHinges(matrix)
        addLinks(matrix)
        addFoundation(matrix, foundationStiffness)
        supports.forEach { support ->
            addOuterProduct(matrix, basisAt(support.x, support.y), support.stiffness)
        }
        matrix
    }

    private val factorisation: CholeskyDecomposition by lazy { CholeskyDecomposition(stiffness) }

    /** `(1/A) ∫ w_a w_b dA` for the two nodal fields [a] and [b], in nm². */
    fun areaInnerProduct(a: F64Array, b: F64Array): Double {
        var total = 0.0
        for (i in 0 until degreesOfFreedom) {
            if (a[i] == 0.0) continue
            total += a[i] * areaGram.V[i].dot(b)
        }
        return total
    }

    /** The bending and torsion energy in `pN·nm` the duplexes store in [field]. */
    fun beamEnergy(field: F64Array): Double {
        val ei = sheet.duplex.bendingRigidity
        val gj = sheet.duplex.torsionalRigidity
        var total = 0.0
        for (beam in 0 until beamCount) {
            for (element in 0 until nodeX.size - 1) {
                val length = nodeX[element + 1] - nodeX[element]
                val q = doubleArrayOf(
                    field[dof(beam, element, W)], field[dof(beam, element, THETA)],
                    field[dof(beam, element + 1, W)], field[dof(beam, element + 1, THETA)]
                )
                total += quadraticForm(hermiteBendingMatrix(ei, length), q)
                val twist = field[dof(beam, element + 1, PHI)] - field[dof(beam, element, PHI)]
                total += gj * twist * twist / length
            }
        }
        return 0.5 * total
    }

    /** The energy in `pN·nm` the crossover hinges store in [field] — `½ k_θ Δφ²` each. */
    fun hingeEnergy(field: F64Array): Double = 0.5 * sheet.crossoverHingeStiffness *
            crossovers.sumOf { hingeRotation(field, it).let { rotation -> rotation * rotation } }

    /**
     * The energy in `pN·nm` the crossover vertical links store in [field].
     *
     * The links are a **constraint**, so this is a penalty residual and not a physical
     * energy: it must be negligible against [hingeEnergy] for any field the model is used on.
     */
    fun linkEnergy(field: F64Array): Double = 0.5 * linkStiffness *
            crossovers.sumOf { linkExtension(field, it).let { gap -> gap * gap } }

    /** Beams, hinges and links — everything but the foundation and the anchors. */
    fun structuralEnergy(field: F64Array): Double =
        beamEnergy(field) + hingeEnergy(field) + linkEnergy(field)

    /** The energy in `pN·nm` the polymer foundation stores in [field], `½ k_f ∫ w² dA`. */
    fun foundationEnergy(field: F64Array): Double =
        0.5 * foundationStiffness * area * areaInnerProduct(field, field)

    private fun addBeams(matrix: F64Array) {
        val ei = sheet.duplex.bendingRigidity
        val gj = sheet.duplex.torsionalRigidity
        for (beam in 0 until beamCount) {
            for (element in 0 until nodeX.size - 1) {
                val length = nodeX[element + 1] - nodeX[element]
                scatter(
                    matrix,
                    intArrayOf(
                        dof(beam, element, W), dof(beam, element, THETA),
                        dof(beam, element + 1, W), dof(beam, element + 1, THETA)
                    ),
                    hermiteBendingMatrix(ei, length)
                )
                scatter(
                    matrix,
                    intArrayOf(dof(beam, element, PHI), dof(beam, element + 1, PHI)),
                    arrayOf(
                        doubleArrayOf(gj / length, -gj / length),
                        doubleArrayOf(-gj / length, gj / length)
                    )
                )
            }
        }
    }

    private fun addHinges(matrix: F64Array) {
        val hinge = sheet.crossoverHingeStiffness
        crossovers.forEach { crossover ->
            scatter(
                matrix,
                intArrayOf(
                    dof(crossover.lowerBeam, crossover.node, PHI),
                    dof(crossover.lowerBeam + 1, crossover.node, PHI)
                ),
                arrayOf(doubleArrayOf(hinge, -hinge), doubleArrayOf(-hinge, hinge))
            )
        }
    }

    private fun addLinks(matrix: F64Array) {
        val half = interhelicalDistance / 2.0
        val gradient = doubleArrayOf(1.0, half, -1.0, half)
        val element = Array(4) { i ->
            DoubleArray(4) { j -> linkStiffness * gradient[i] * gradient[j] }
        }
        crossovers.forEach { crossover ->
            scatter(
                matrix,
                intArrayOf(
                    dof(crossover.lowerBeam, crossover.node, W),
                    dof(crossover.lowerBeam, crossover.node, PHI),
                    dof(crossover.lowerBeam + 1, crossover.node, W),
                    dof(crossover.lowerBeam + 1, crossover.node, PHI)
                ),
                element
            )
        }
    }

    /**
     * Adds the Winkler foundation over each duplex's tributary strip.
     *
     * `½ k_f ∫∫ (w + φ Δy)² dΔy dx` over `Δy ∈ [−d/2, d/2]` gives a line stiffness `k_f d`
     * on the deflection and `k_f d³/12` on the roll, with no cross term. Both are needed:
     * dropping the roll term would let each duplex spin against nothing, and it is exactly
     * what the continuum `k_f ∫ w² dA` contains and a node-lumped foundation would lose.
     */
    private fun addFoundation(matrix: F64Array, stiffness: Double) {
        val d = interhelicalDistance
        val lineStiffness = stiffness * d
        val rollStiffness = stiffness * d * d * d / 12.0
        for (beam in 0 until beamCount) {
            for (element in 0 until nodeX.size - 1) {
                val length = nodeX[element + 1] - nodeX[element]
                scatter(
                    matrix,
                    intArrayOf(
                        dof(beam, element, W), dof(beam, element, THETA),
                        dof(beam, element + 1, W), dof(beam, element + 1, THETA)
                    ),
                    hermiteConsistentMatrix(lineStiffness, length)
                )
                val roll = rollStiffness * length / 6.0
                scatter(
                    matrix,
                    intArrayOf(dof(beam, element, PHI), dof(beam, element + 1, PHI)),
                    arrayOf(
                        doubleArrayOf(2.0 * roll, roll),
                        doubleArrayOf(roll, 2.0 * roll)
                    )
                )
            }
        }
    }

    // ------------------------------------------------------------------ evaluation

    private fun beamOf(y: Double): Int {
        val index = ((y - beamY[0]) / interhelicalDistance).roundToInt()
        return index.coerceIn(0, beamCount - 1)
    }

    private fun elementOf(x: Double): Int {
        var element = 0
        while (element < nodeX.size - 2 && nodeX[element + 1] < x) element++
        return element
    }

    /** The deflection in nm of the field [field] at ([x], [y]). */
    fun evaluate(field: F64Array, x: Double, y: Double): Double {
        val beam = beamOf(y)
        val element = elementOf(x)
        val length = nodeX[element + 1] - nodeX[element]
        val t = (x - nodeX[element]) / length
        val hermite = hermiteShapeFunctions(t, length)
        val linear = doubleArrayOf(1.0 - t, t)
        val deflection = hermite[0] * field[dof(beam, element, W)] +
                hermite[1] * field[dof(beam, element, THETA)] +
                hermite[2] * field[dof(beam, element + 1, W)] +
                hermite[3] * field[dof(beam, element + 1, THETA)]
        val roll = linear[0] * field[dof(beam, element, PHI)] +
                linear[1] * field[dof(beam, element + 1, PHI)]
        return deflection + roll * (y - beamY[beam])
    }

    /** The vector `b` for which `b·q` is the deflection at ([x], [y]). */
    internal fun basisAt(x: Double, y: Double): F64Array {
        val beam = beamOf(y)
        val element = elementOf(x)
        val length = nodeX[element + 1] - nodeX[element]
        val t = (x - nodeX[element]) / length
        val hermite = hermiteShapeFunctions(t, length)
        val basis = F64Array(degreesOfFreedom)
        basis[dof(beam, element, W)] = hermite[0]
        basis[dof(beam, element, THETA)] = hermite[1]
        basis[dof(beam, element + 1, W)] = hermite[2]
        basis[dof(beam, element + 1, THETA)] = hermite[3]
        val arm = y - beamY[beam]
        basis[dof(beam, element, PHI)] = (1.0 - t) * arm
        basis[dof(beam, element + 1, PHI)] = t * arm
        return basis
    }

    internal fun overGrid(samples: Int, field: (Double, Double) -> Double): Double {
        require(samples >= 2) { "samples must be at least 2, was: $samples" }
        val halfX = lengthX / 2.0
        val halfY = lengthY / 2.0
        var peak = 0.0
        for (i in 0 until samples) {
            val x = -halfX + 2.0 * halfX * i / (samples - 1)
            for (j in 0 until samples) {
                peak = max(peak, field(x, -halfY + 2.0 * halfY * j / (samples - 1)))
            }
        }
        return peak
    }

    // ------------------------------------------------------------------ member forces

    internal fun linkExtension(field: F64Array, crossover: Crossover): Double {
        val half = interhelicalDistance / 2.0
        return field[dof(crossover.lowerBeam, crossover.node, W)] +
                half * field[dof(crossover.lowerBeam, crossover.node, PHI)] -
                field[dof(crossover.lowerBeam + 1, crossover.node, W)] +
                half * field[dof(crossover.lowerBeam + 1, crossover.node, PHI)]
    }

    internal fun hingeRotation(field: F64Array, crossover: Crossover): Double =
        field[dof(crossover.lowerBeam + 1, crossover.node, PHI)] -
                field[dof(crossover.lowerBeam, crossover.node, PHI)]

    internal fun duplexShears(field: F64Array): List<Double> = buildList {
        val ei = sheet.duplex.bendingRigidity
        for (beam in 0 until beamCount) {
            for (element in 0 until nodeX.size - 1) {
                val length = nodeX[element + 1] - nodeX[element]
                // V = EI w''', constant along a Hermite element
                add(
                    ei * (
                            12.0 * (field[dof(beam, element, W)] -
                                    field[dof(beam, element + 1, W)]) / (length * length * length) +
                                    6.0 * (field[dof(beam, element, THETA)] +
                                    field[dof(beam, element + 1, THETA)]) / (length * length)
                            )
                )
            }
        }
    }

    // ------------------------------------------------------------------ load and solve

    /** Integrates [field] over the whole footprint. */
    internal fun integrateOverFootprint(field: (Double, Double) -> Double): Double =
        integrate(-lengthX / 2.0, lengthX / 2.0, -lengthY / 2.0, lengthY / 2.0, field)

    internal fun integrate(
        fromX: Double,
        toX: Double,
        fromY: Double,
        toY: Double,
        field: (Double, Double) -> Double
    ): Double {
        if (toX <= fromX || toY <= fromY) return 0.0
        var total = 0.0
        val steps = LOAD_INTEGRATION_PANELS
        val stepX = (toX - fromX) / steps
        val stepY = (toY - fromY) / steps
        for (i in 0 until steps) {
            val x0 = fromX + i * stepX
            for (q in 0 until quadrature.points) {
                val x = x0 + stepX * (quadrature.nodes[q] + 1.0) / 2.0
                val wx = quadrature.weights[q] * stepX / 2.0
                for (j in 0 until steps) {
                    val y0 = fromY + j * stepY
                    for (r in 0 until quadrature.points) {
                        val y = y0 + stepY * (quadrature.nodes[r] + 1.0) / 2.0
                        total += wx * quadrature.weights[r] * stepY / 2.0 * field(x, y)
                    }
                }
            }
        }
        return total
    }

    private val quadrature = gaussLegendreRule(QUADRATURE_POINTS)

    /**
     * The consistent load vector of a distributed [pressure] field.
     *
     * Integrated over each element **and across the tributary strip**, so a pressure that
     * varies in `y` — the electrostatic edge taper — produces the rolling moment on the
     * outermost duplexes that it physically does, and not merely a vertical force.
     */
    private fun assembleLoad(pressure: PressureField, pointLoads: List<PointLoad>): F64Array {
        val load = F64Array(degreesOfFreedom)
        val half = interhelicalDistance / 2.0
        for (beam in 0 until beamCount) {
            val axis = beamY[beam]
            for (element in 0 until nodeX.size - 1) {
                val length = nodeX[element + 1] - nodeX[element]
                for (q in 0 until quadrature.points) {
                    val t = (quadrature.nodes[q] + 1.0) / 2.0
                    val x = nodeX[element] + t * length
                    val weightX = quadrature.weights[q] * length / 2.0
                    val hermite = hermiteShapeFunctions(t, length)
                    var vertical = 0.0
                    var moment = 0.0
                    for (r in 0 until quadrature.points) {
                        val arm = half * quadrature.nodes[r]
                        val weight = quadrature.weights[r] * half * pressure.at(x, axis + arm)
                        vertical += weight
                        moment += weight * arm
                    }
                    load[dof(beam, element, W)] += weightX * hermite[0] * vertical
                    load[dof(beam, element, THETA)] += weightX * hermite[1] * vertical
                    load[dof(beam, element + 1, W)] += weightX * hermite[2] * vertical
                    load[dof(beam, element + 1, THETA)] += weightX * hermite[3] * vertical
                    load[dof(beam, element, PHI)] += weightX * (1.0 - t) * moment
                    load[dof(beam, element + 1, PHI)] += weightX * t * moment
                }
            }
        }
        pointLoads.forEach { point ->
            val basis = basisAt(point.x, point.y)
            for (k in 0 until degreesOfFreedom) load[k] += point.force * basis[k]
        }
        return load
    }

    /** Solves one load case: a [pressure] field downward, plus any [pointLoads]. */
    fun solve(
        pressure: PressureField = uniformPressure(0.0),
        pointLoads: List<PointLoad> = emptyList()
    ): GrillageDeflection = GrillageDeflection(
        this, factorisation.solve(assembleLoad(pressure, pointLoads)), pressure, pointLoads
    )

    /**
     * Solves one load case with **one additional discrete [anchor]** that is not part of this
     * lattice's assembled stiffness, by a rank-one (Sherman-Morrison) update of the existing
     * factorisation rather than by re-factorising.
     *
     * An anchor enters the stiffness as `k_a b bᵀ` with `b` the [basisAt] vector of its
     * attachment point — exactly a rank-one term — so
     * `(K + k_a b bᵀ)⁻¹ f = K⁻¹f − k_a (bᵀK⁻¹f)(K⁻¹b)/(1 + k_a bᵀK⁻¹b)`, which is
     * algebraically identical to assembling the anchor and refactorising, and is asserted
     * against it as a test.
     *
     * This is what makes `T-14`'s question askable at all. Where the anchor sits inside the
     * crossover unit cell is a **two-dimensional sweep**, and re-factorising an 855-degree-
     * of-freedom lattice at every registration point costs `O(n³)` each; the update costs two
     * triangular solves, `O(n²)`, so a complete registration map costs roughly what one
     * assembled anchored case costs. Sampling the cell at four points was `C-0009`'s only
     * affordable option and it is what this task exists to replace.
     */
    fun solveWithAnchor(
        anchor: PointSupport,
        pressure: PressureField = uniformPressure(0.0),
        pointLoads: List<PointLoad> = emptyList()
    ): GrillageDeflection {
        val free = factorisation.solve(assembleLoad(pressure, pointLoads))
        val basis = basisAt(anchor.x, anchor.y)
        val response = factorisation.solve(basis)
        val scale = anchor.stiffness * basis.dot(free) /
                (1.0 + anchor.stiffness * basis.dot(response))
        val coefficients = free.copy()
        coefficients -= response * scale
        return GrillageDeflection(this, coefficients, pressure, pointLoads, listOf(anchor))
    }

    /**
     * [solveWithAnchor] for each of [anchors] in turn, sharing the one free solution
     * `K⁻¹f` that all of them update.
     *
     * A registration map moves the anchor and holds the load fixed, so assembling and
     * solving the load vector once instead of once per point is not an optimisation of the
     * inner loop but of the outer one. Asserted equal to mapping [solveWithAnchor].
     */
    fun solveWithEachAnchor(
        anchors: List<PointSupport>,
        pressure: PressureField = uniformPressure(0.0),
        pointLoads: List<PointLoad> = emptyList()
    ): List<GrillageDeflection> {
        val free = factorisation.solve(assembleLoad(pressure, pointLoads))
        return anchors.map { anchor ->
            val basis = basisAt(anchor.x, anchor.y)
            val response = factorisation.solve(basis)
            val scale = anchor.stiffness * basis.dot(free) /
                    (1.0 + anchor.stiffness * basis.dot(response))
            val coefficients = free.copy()
            coefficients -= response * scale
            GrillageDeflection(this, coefficients, pressure, pointLoads, listOf(anchor))
        }
    }

    /**
     * The equilibrium thermal fluctuation of the unloaded lattice at [temperature],
     * from equipartition on the assembled stiffness matrix: the covariance of the generalised
     * coordinates is `k_BT K⁻¹`, so no sampling is needed and the answer is exact for the
     * harmonic model.
     *
     * The mean-square dishing is `k_BT tr(M K⁻¹)` with `M = G − Σ (G u)(G u)ᵀ/(uᵀG u)` over
     * the three rigid modes. `tr(G K⁻¹)` is evaluated by factorising the area Gram matrix
     * as `G = C Cᵀ` and summing `‖L⁻¹C_j‖²`, which needs one **forward** substitution per
     * degree of freedom rather than a full solve.
     */
    fun thermalFluctuation(
        temperature: Double = ROOM_TEMPERATURE
    ): GrillageThermalFluctuation {
        val energy = thermalEnergy(temperature)
        val gramFactor = CholeskyDecomposition(areaGram)
        var trace = 0.0
        for (j in 0 until degreesOfFreedom) {
            val forward = factorisation.forwardSolve(gramFactor.lowerColumn(j))
            trace += forward.dot(forward)
        }
        fun rigidVariance(mode: F64Array): Double {
            val gramProduct = F64Array(degreesOfFreedom) { i -> areaGram.V[i].dot(mode) }
            val norm = mode.dot(gramProduct)
            return energy * gramProduct.dot(factorisation.solve(gramProduct)) / norm
        }
        val piston = rigidVariance(pistonMode)
        val tiltAlong = rigidVariance(tiltXMode)
        val tiltAcross = rigidVariance(tiltYMode)
        val centre = basisAt(0.0, 0.0)
        return GrillageThermalFluctuation(
            pistonRms = sqrt(max(0.0, piston)),
            tiltRms = sqrt(max(0.0, tiltAlong + tiltAcross)),
            dishingRms = sqrt(max(0.0, energy * trace - piston - tiltAlong - tiltAcross)),
            centreRms = sqrt(max(0.0, energy * centre.dot(factorisation.solve(centre))))
        )
    }

    companion object {

        /** The `w` degree of freedom of a node. */
        const val W: Int = 0

        /** The `dw/dx` degree of freedom of a node. */
        const val THETA: Int = 1

        /** The roll (`dw/dy`) degree of freedom of a node. */
        const val PHI: Int = 2

        /** Degrees of freedom per node. */
        const val DOF_PER_NODE: Int = 3

        /**
         * The default penalty stiffness in `pN/nm` of the vertical crossover link.
         *
         * The link is a **constraint**, not a spring: the two helices a crossover joins are
         * held together by a covalently continuous strand. `10⁴ pN/nm` is roughly ten times
         * the duplex stretch modulus per nm and about 5000× the hinge's own equivalent
         * vertical stiffness `k_θ/d²`, and gate 4 shows the transmitted force has stopped
         * moving by then.
         */
        const val RIGID_LINK_STIFFNESS: Double = 1e4

        /** Beam elements per interval between node columns. */
        const val DEFAULT_SUBDIVISIONS: Int = 2

        /** Gauss points per element and per tributary strip. */
        const val QUADRATURE_POINTS: Int = 6

        /** Panels per direction used by the equilibrium integrals over a sub-rectangle. */
        const val LOAD_INTEGRATION_PANELS: Int = 12
    }

}

/** The Hermite cubic shape functions on an element of length [length], at `ξ = `[t]. */
private fun hermiteShapeFunctions(t: Double, length: Double): DoubleArray = doubleArrayOf(
    1.0 - 3.0 * t * t + 2.0 * t * t * t,
    length * (t - 2.0 * t * t + t * t * t),
    3.0 * t * t - 2.0 * t * t * t,
    length * (t * t * t - t * t)
)

/** `∫ EI N''_a N''_b` for a Hermite element of length [length]. */
private fun hermiteBendingMatrix(rigidity: Double, length: Double): Array<DoubleArray> {
    val l = length
    val scale = rigidity / (l * l * l)
    return arrayOf(
        doubleArrayOf(12.0, 6.0 * l, -12.0, 6.0 * l),
        doubleArrayOf(6.0 * l, 4.0 * l * l, -6.0 * l, 2.0 * l * l),
        doubleArrayOf(-12.0, -6.0 * l, 12.0, -6.0 * l),
        doubleArrayOf(6.0 * l, 2.0 * l * l, -6.0 * l, 4.0 * l * l)
    ).map { row -> DoubleArray(row.size) { row[it] * scale } }.toTypedArray()
}

/** `k ∫ N_a N_b` for a Hermite element of length [length] — the consistent foundation matrix. */
private fun hermiteConsistentMatrix(stiffness: Double, length: Double): Array<DoubleArray> {
    val l = length
    val scale = stiffness * l / 420.0
    return arrayOf(
        doubleArrayOf(156.0, 22.0 * l, 54.0, -13.0 * l),
        doubleArrayOf(22.0 * l, 4.0 * l * l, 13.0 * l, -3.0 * l * l),
        doubleArrayOf(54.0, 13.0 * l, 156.0, -22.0 * l),
        doubleArrayOf(-13.0 * l, -3.0 * l * l, -22.0 * l, 4.0 * l * l)
    ).map { row -> DoubleArray(row.size) { row[it] * scale } }.toTypedArray()
}

private fun quadraticForm(matrix: Array<DoubleArray>, vector: DoubleArray): Double {
    var total = 0.0
    for (i in vector.indices) {
        for (j in vector.indices) total += vector[i] * matrix[i][j] * vector[j]
    }
    return total
}

private fun scatter(matrix: F64Array, dofs: IntArray, element: Array<DoubleArray>) {
    for (i in dofs.indices) {
        for (j in dofs.indices) {
            matrix[dofs[i], dofs[j]] += element[i][j]
        }
    }
}

private fun addOuterProduct(matrix: F64Array, vector: F64Array, scale: Double) {
    val nonZero = (0 until vector.length).filter { vector[it] != 0.0 }
    for (i in nonZero) {
        for (j in nonZero) {
            matrix[i, j] += scale * vector[i] * vector[j]
        }
    }
}
