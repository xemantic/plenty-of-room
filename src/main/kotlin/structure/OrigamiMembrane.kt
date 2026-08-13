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

import org.jetbrains.bio.viktor.F64Array
import kotlin.math.abs
import kotlin.math.hypot

import kotlin.math.roundToInt

/** A concentrated **in-plane** load of ([forceAlong], [forceAcross]) pN applied at ([x], [y]). */
data class InPlanePointLoad(
    val x: Double,
    val y: Double,
    val forceAlong: Double,
    val forceAcross: Double
)

/**
 * A surface-parallel anchor: a linear spring tying the sheet to ground **in its own plane**
 * at ([x], [y]), of [stiffnessAlong] and [stiffnessAcross] in pN/nm.
 *
 * The two components are separate because `C-0014`'s decisive finding is that an anchor's
 * *orientation* decides everything: a tangential in-plane tether is stiff along its own axis
 * and soft across it, by four orders of magnitude.
 */
data class InPlanePointSupport(
    val x: Double,
    val y: Double,
    val stiffnessAlong: Double,
    val stiffnessAcross: Double
) {

    init {
        require(stiffnessAlong > 0.0) { "stiffnessAlong must be positive, was: $stiffnessAlong" }
        require(stiffnessAcross > 0.0) {
            "stiffnessAcross must be positive, was: $stiffnessAcross"
        }
    }

}

/**
 * What one crossover transmits **in the plane of the sheet**.
 *
 * Both components are the force the connector exerts on the **lower-`y` duplex** of the pair
 * it joins, so that the sum over one interface equals the in-plane force crossing that
 * interface from above — the discrete cut-equilibrium identity, asserted as a gate-3 test.
 *
 * @param shearForce the force along the helices in pN, from relative sliding.
 * @param normalForce the force across the helices in pN, from relative separation.
 */
data class InPlaneCrossoverForce(
    val lowerBeam: Int,
    val column: Int,
    val x: Double,
    val y: Double,
    val shearForce: Double,
    val normalForce: Double
) {

    /** The magnitude of the in-plane force in pN, which is what an allowable is judged on. */
    val magnitude: Double get() = hypot(shearForce, normalForce)

}

/**
 * One hybridisation bond of a tether attachment: the staple extension is base-paired to
 * duplex [duplex] at station [x] nm along the helices, and that bond carries the fraction
 * [share] of the tether's tension.
 *
 * `T-19`'s unit of *entry topology*. `C-0020` modelled an attachment as one point on one
 * duplex, which is one bond with `share = 1`.
 */
data class EntryBond(
    val duplex: Int,
    val x: Double,
    val share: Double
)

/**
 * How one end of a tether actually enters the sheet — a set of [EntryBond]s whose shares
 * partition the tether's tension.
 *
 * ## Why this exists
 *
 * `C-0020`'s headline, that the in-plane transfer ratio is exactly 1 for a tether aligned
 * with the helices, holds **because** the tether is given one point of one duplex to enter
 * through, which makes the attachment the most loaded member by construction. That is a
 * *sequence-design choice* — how many duplexes the staple extension hybridises to, over how
 * many bases, and whether it lands on a crossover — and not a property of the sheet. This
 * class is that choice, made explicit and swept.
 *
 * ## The bound the shares obey before any lattice runs
 *
 * The duplex axial forces on a cut sum to the applied force (`C-0020`, gate 3), so on a tile
 * of `D` duplexes some duplex carries at least `1/D` — the pigeonhole floor, which no entry
 * topology can beat. And if the bond spans `m` duplexes at a station short compared with the
 * neighbour-exchange length, only the bonded duplexes have received anything at the entry
 * element, so the peak is the **largest share** and reaches `1/m` exactly for an equal split.
 */
class EntryTopology(
    val name: String,
    val bonds: List<EntryBond>
) {

    init {
        require(bonds.isNotEmpty()) { "an entry topology must have at least one bond" }
        require(bonds.all { it.share > 0.0 }) {
            "every bond must carry a positive share, was: ${bonds.map { it.share }}"
        }
        require(abs(bonds.sumOf { it.share } - 1.0) < SHARE_TOLERANCE) {
            "the bond shares must partition the tether tension, summed to " +
                    bonds.sumOf { it.share }
        }
    }

    /** The number of duplexes the attachment spans. */
    val duplexSpan: Int get() = bonds.map { it.duplex }.distinct().size

    /** The distinct base-pair stations the attachment occupies, ascending. */
    val stations: List<Double> get() = bonds.map { it.x }.distinct().sorted()

    /** The largest fraction of the tension any single bond carries. */
    val largestShare: Double get() = bonds.maxOf { it.share }

    /** The same topology with [shares] in place of the current ones, in bond order. */
    fun withShares(shares: List<Double>): EntryTopology {
        require(shares.size == bonds.size) {
            "expected ${bonds.size} shares, got ${shares.size}"
        }
        return EntryTopology(name, bonds.mapIndexed { i, bond -> bond.copy(share = shares[i]) })
    }

    override fun toString(): String = "$name(${bonds.size} bonds, span $duplexSpan)"

    companion object {

        /** Shares are required to sum to one to within this. */
        const val SHARE_TOLERANCE: Double = 1e-9

        /** `C-0020`'s attachment: one point on one duplex. */
        fun singlePoint(name: String, duplex: Int, x: Double): EntryTopology =
            EntryTopology(name, listOf(EntryBond(duplex, x, 1.0)))

        /**
         * A staple extension hybridising to [duplexes] adjacent duplexes at one station,
         * with the tension split equally — the compliant-staple limit. The rigid-staple
         * limit is [OrigamiMembrane.compatibleShares].
         */
        fun duplexBand(
            name: String,
            firstDuplex: Int,
            duplexes: Int,
            x: Double
        ): EntryTopology {
            require(duplexes >= 1) { "duplexes must be at least one, was: $duplexes" }
            return EntryTopology(
                name,
                (0 until duplexes).map {
                    EntryBond(firstDuplex + it, x, 1.0 / duplexes)
                }
            )
        }

        /**
         * A tether bonded **onto a crossover**, where the two duplexes are already tied
         * together — the two-duplex band at that crossover's own station, which is
         * necessarily an interior one because the columns sit strictly inside the footprint.
         */
        fun onCrossover(name: String, crossover: OrigamiMembrane.Crossover): EntryTopology =
            duplexBand(name, crossover.lowerBeam, 2, crossover.x)

        /**
         * A staple extension hybridised over [bases] consecutive base pairs of **one**
         * duplex, running [inward] from [from], with the tension shared equally among them.
         *
         * The realistic case: a hybridised extension has a footprint of 8–20 bp, and the
         * bonds are placed at exact multiples of the rise so that each lands on a node.
         */
        fun baseFootprint(
            name: String,
            duplex: Int,
            from: Double,
            bases: Int,
            rise: Double,
            inward: Boolean
        ): EntryTopology {
            require(bases >= 1) { "bases must be at least one, was: $bases" }
            val direction = if (inward) 1.0 else -1.0
            return EntryTopology(
                name,
                (0 until bases).map {
                    EntryBond(duplex, from + direction * it * rise, 1.0 / bases)
                }
            )
        }

        /**
         * The same footprint with the load introduced at its **two ends only** — the
         * shear-lag limit of the joint itself, in which an overlap transfers its load at the
         * ends rather than uniformly. Run beside [baseFootprint] to show the peak is
         * insensitive to how the load is distributed *within* the footprint.
         */
        fun endLoadedFootprint(
            name: String,
            duplex: Int,
            from: Double,
            bases: Int,
            rise: Double,
            inward: Boolean
        ): EntryTopology {
            require(bases >= 2) { "an end-loaded footprint needs at least two bases" }
            val direction = if (inward) 1.0 else -1.0
            return EntryTopology(
                name,
                listOf(
                    EntryBond(duplex, from, 0.5),
                    EntryBond(duplex, from + direction * (bases - 1) * rise, 0.5)
                )
            )
        }

    }

}

/** The in-plane state of an [OrigamiMembrane] under one load case. */
class MembraneDeflection internal constructor(
    private val lattice: OrigamiMembrane,
    val coefficients: F64Array,
    private val loads: List<InPlanePointLoad>
) {

    /** The along-helix displacement in nm at ([x], [y]). */
    fun displacementAlong(x: Double, y: Double): Double =
        lattice.evaluateAlong(coefficients, x, y)

    /** The across-helix displacement in nm at ([x], [y]). */
    fun displacementAcross(x: Double, y: Double): Double =
        lattice.evaluateAcross(coefficients, x, y)

    /** What every crossover of the lattice transmits in plane. */
    val crossoverForces: List<InPlaneCrossoverForce> by lazy {
        lattice.crossovers.map { crossover ->
            InPlaneCrossoverForce(
                lowerBeam = crossover.lowerBeam,
                column = crossover.column,
                x = crossover.x,
                y = crossover.y,
                shearForce = lattice.crossoverShearStiffness *
                        lattice.shearExtension(coefficients, crossover),
                normalForce = lattice.crossoverNormalStiffness *
                        lattice.normalExtension(coefficients, crossover)
            )
        }
    }

    /** The largest in-plane force in pN any single crossover transmits. */
    val peakCrossoverForce: Double get() = crossoverForces.maxOf { it.magnitude }

    /** The axial force in pN in every element of every duplex, positive in tension. */
    val duplexAxialForces: List<Double> by lazy { lattice.duplexAxialForces(coefficients) }

    /** The largest axial force in pN any single duplex carries. */
    val peakDuplexAxialForce: Double get() = duplexAxialForces.maxOf { abs(it) }

    /** The in-plane transverse shear in pN in every element of every duplex. */
    val duplexInPlaneShears: List<Double> by lazy {
        lattice.duplexInPlaneShears(coefficients)
    }

    /** The largest in-plane transverse shear in pN any single duplex carries. */
    val peakDuplexInPlaneShear: Double get() = duplexInPlaneShears.maxOf { abs(it) }

    /** The along-helix force in pN each anchor exerts **on the sheet**. */
    val supportForcesAlong: List<Double>
        get() = lattice.supports.map { -it.stiffnessAlong * displacementAlong(it.x, it.y) }

    /** The across-helix force in pN each anchor exerts on the sheet. */
    val supportForcesAcross: List<Double>
        get() = lattice.supports.map { -it.stiffnessAcross * displacementAcross(it.x, it.y) }

    /** The total along-helix force in pN the regularising bed exerts on the sheet. */
    val regularisationForceAlong: Double
        get() = -lattice.regularisation * lattice.sumOfNodal(coefficients, OrigamiMembrane.ALONG)

    /** The total across-helix force in pN the regularising bed exerts on the sheet. */
    val regularisationForceAcross: Double
        get() = -lattice.regularisation * lattice.sumOfNodal(coefficients, OrigamiMembrane.ACROSS)

    /** The total applied along-helix force in pN. */
    val appliedForceAlong: Double get() = loads.sumOf { it.forceAlong }

    /** The total applied across-helix force in pN. */
    val appliedForceAcross: Double get() = loads.sumOf { it.forceAcross }

    private fun cutOf(lowerBeam: Int): Double =
        (lattice.beamY[lowerBeam] + lattice.beamY[lowerBeam + 1]) / 2.0

    /**
     * The along-helix force in pN crossing the interface above duplex [lowerBeam], from the
     * equilibrium of everything beyond the cut.
     *
     * The in-plane counterpart of `C-0009`'s [GrillageDeflection.shearAcrossInterface], and
     * the quantity the crossovers on that interface must sum to.
     */
    fun shearAcrossInterface(lowerBeam: Int): Double {
        val cut = cutOf(lowerBeam)
        return loads.filter { it.y > cut }.sumOf { it.forceAlong } +
                lattice.supports.filter { it.y > cut }
                    .sumOf { -it.stiffnessAlong * displacementAlong(it.x, it.y) } -
                lattice.regularisation *
                lattice.sumOfNodalAbove(coefficients, OrigamiMembrane.ALONG, lowerBeam)
    }

    /** The across-helix force in pN crossing the interface above duplex [lowerBeam]. */
    fun normalAcrossInterface(lowerBeam: Int): Double {
        val cut = cutOf(lowerBeam)
        return loads.filter { it.y > cut }.sumOf { it.forceAcross } +
                lattice.supports.filter { it.y > cut }
                    .sumOf { -it.stiffnessAcross * displacementAcross(it.x, it.y) } -
                lattice.regularisation *
                lattice.sumOfNodalAbove(coefficients, OrigamiMembrane.ACROSS, lowerBeam)
    }

}

/**
 * A single-layer DNA-origami sheet loaded **in its own plane** — the membrane problem that
 * `C-0009`'s out-of-plane grillage does not contain and cannot answer.
 *
 * ## Why this model exists
 *
 * `C-0014` found that lateral confinement of the tile is decided by anchor *orientation*, and
 * that the schemes which work put their load path **in** the surface. It then had to price
 * the resulting per-load-path force with `C-0009`'s **out-of-plane** concentration factor of
 * 2.3–7.6×, applied as a conservative stand-in, and said so in its own validity range. The
 * two are different problems: an out-of-plane anchor is a *reaction* that gathers the
 * foundation load from an area of order `ℓ²` around itself, while a lateral tether gathers
 * nothing at all — the layer's lateral restoring stiffness is exactly zero by symmetry
 * (`C-0010`) — so its own tension is the whole of the load and every internal path carries a
 * fraction of it.
 *
 * ## The elements
 *
 * Deliberately the **same sheet** as [OrigamiGrillage]: the same [OrigamiSheet], the same
 * [CrossoverLayout], the same node stations, the same crossover topology, and the same three
 * degrees of freedom per node — here `u` along the helices, `v` across them, and the in-plane
 * rotation `dv/dx`. For a flat sheet the membrane and bending problems decouple at linear
 * order, which is why this is a sibling class rather than five more degrees of freedom bolted
 * onto a matrix that would then be solving two independent problems at once.
 *
 * - each duplex is a **bar** of stretch modulus `S` in `u` and an Euler-Bernoulli **beam** of
 *   the same `EI` in `(v, dv/dx)` — a duplex has a circular section, so its in-plane and
 *   out-of-plane bending rigidities are the same number;
 * - each crossover is a two-component in-plane **connector** attached at the interface line,
 *   [connectorArm] from each duplex axis: a shear spring `k_s` resisting
 *   `(u_{b+1} + c θ_{b+1}) − (u_b − c θ_b)` and a normal spring `k_n` resisting
 *   `v_{b+1} − v_b`. The arm is what makes a rigid in-plane rotation cost exactly nothing,
 *   and it is the exact analogue of `C-0009`'s vertical link extension `w + (d/2)φ`;
 * - a vanishing isotropic **regularising bed** removes the three rigid-body modes. There is
 *   no in-plane foundation in the physics — that is `C-0010`'s exact zero being consumed —
 *   so the bed must carry no measurable part of the load, and that is a gate-4 test.
 *
 * ## The long-wavelength limit is exact, and is gate 2
 *
 * `u = εx` costs `½(S/d)ε²` per unit area; `u = γy` costs `½(k_s d/p)γ²` and `v = εy` costs
 * `½(k_n d/p)ε²`, each to within the integer crossover count over the continuum areal
 * density — **the identical `56/55.147` excess `C-0009` found for `D_⊥`**, because it is the
 * identical count. So this lattice is the shear-lag membrane at long wavelength, and any
 * difference it reports is discreteness and nothing else.
 *
 * @param columns where the crossover columns sit, as a phase — `T-14`'s design variable,
 *          reused unchanged so that a layout means the same thing in both planes.
 * @param crossoverShearStiffness `k_s` in pN/nm; **not measured anywhere** — see
 *          [Gen1Tile.crossoverInPlaneStiffness] — and swept.
 * @param crossoverNormalStiffness `k_n` in pN/nm.
 * @param connectorArm the distance in nm from a duplex axis to the interface line at which
 *          the connector attaches; `d/2` physically, and settable to zero for the classical
 *          shear-lag kinematics the transfer-length formula is derived under.
 * @param regularisation the in-plane bed stiffness in pN/nm per node.
 * @param extraStations extra node stations in nm along the helices, so that a load applied
 *          away from a crossover column still lands on a node and the axial force it
 *          produces is resolved rather than averaged across an element.
 */
class OrigamiMembrane(
    val sheet: OrigamiSheet,
    val lengthX: Double,
    val beamCount: Int,
    val columns: CrossoverLayout,
    val crossoverShearStiffness: Double,
    val crossoverNormalStiffness: Double,
    val subdivisions: Int = DEFAULT_SUBDIVISIONS,
    val connectorArm: Double = sheet.interhelicalDistance / 2.0,
    val regularisation: Double = DEFAULT_REGULARISATION,
    val supports: List<InPlanePointSupport> = emptyList(),
    val extraStations: List<Double> = emptyList()
) {

    /** One crossover of the lattice, as geometry — the same record [OrigamiGrillage] uses. */
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
        require(crossoverShearStiffness > 0.0) {
            "crossoverShearStiffness must be positive, was: $crossoverShearStiffness"
        }
        require(crossoverNormalStiffness > 0.0) {
            "crossoverNormalStiffness must be positive, was: $crossoverNormalStiffness"
        }
        require(subdivisions >= 1) { "subdivisions must be at least 1, was: $subdivisions" }
        require(connectorArm >= 0.0) { "connectorArm must not be negative, was: $connectorArm" }
        require(regularisation > 0.0) {
            "regularisation must be positive, was: $regularisation"
        }
    }

    /** The number of crossover columns across the footprint, counting both parities. */
    val crossoverColumns: Int get() = columns.size

    /** The footprint across the helices in nm — an integer number of duplexes wide. */
    val lengthY: Double = beamCount * sheet.interhelicalDistance

    /** The footprint area in nm². */
    val area: Double = lengthX * lengthY

    /** The `y` of each duplex axis, in nm, centred on the footprint. */
    val beamY: List<Double> =
        (0 until beamCount).map { (it - (beamCount - 1) / 2.0) * sheet.interhelicalDistance }

    /** The `y` of duplex [beam] in nm. */
    fun duplexY(beam: Int): Double = beamY[beam]

    /** The `x` of each crossover column, in nm. */
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
        val interior = (columnX + extraStations.filter { abs(it) < lengthX / 2.0 - 1e-9 })
            .sorted()
            .fold(mutableListOf<Double>()) { kept, station ->
                if (kept.isEmpty() || station - kept.last() > STATION_MERGE_TOLERANCE) {
                    kept.add(station)
                }
                kept
            }
        val stations = listOf(-lengthX / 2.0) + interior + listOf(lengthX / 2.0)
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

    /** Every crossover of the lattice — the same topology [OrigamiGrillage] builds. */
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
     * Returns the nodal coordinates of the in-plane field whose along-helix displacement is
     * [along], whose across-helix displacement is [across] and whose in-plane rotation is
     * [rotation].
     */
    fun nodalField(
        along: (Double, Double) -> Double,
        across: (Double, Double) -> Double,
        rotation: (Double, Double) -> Double
    ): F64Array {
        val field = F64Array(degreesOfFreedom)
        for (beam in 0 until beamCount) {
            val y = beamY[beam]
            for (node in nodeX.indices) {
                val x = nodeX[node]
                field[dof(beam, node, ALONG)] = along(x, y)
                field[dof(beam, node, ACROSS)] = across(x, y)
                field[dof(beam, node, ROTATION)] = rotation(x, y)
            }
        }
        return field
    }

    // ------------------------------------------------------------------ energies

    /** The axial (stretching) energy in `pN·nm` the duplexes store in [field]. */
    fun axialEnergy(field: F64Array): Double {
        val modulus = sheet.duplex.stretchModulus
        var total = 0.0
        for (beam in 0 until beamCount) {
            for (element in 0 until nodeX.size - 1) {
                val length = nodeX[element + 1] - nodeX[element]
                val extension = field[dof(beam, element + 1, ALONG)] -
                        field[dof(beam, element, ALONG)]
                total += modulus * extension * extension / length
            }
        }
        return 0.5 * total
    }

    /** The in-plane bending energy in `pN·nm` the duplexes store in [field]. */
    fun bendingEnergy(field: F64Array): Double {
        val rigidity = sheet.duplex.bendingRigidity
        var total = 0.0
        for (beam in 0 until beamCount) {
            for (element in 0 until nodeX.size - 1) {
                val length = nodeX[element + 1] - nodeX[element]
                val q = doubleArrayOf(
                    field[dof(beam, element, ACROSS)], field[dof(beam, element, ROTATION)],
                    field[dof(beam, element + 1, ACROSS)],
                    field[dof(beam, element + 1, ROTATION)]
                )
                total += inPlaneQuadraticForm(inPlaneBendingMatrix(rigidity, length), q)
            }
        }
        return 0.5 * total
    }

    /** The energy in `pN·nm` the crossover shear springs store in [field]. */
    fun crossoverShearEnergy(field: F64Array): Double = 0.5 * crossoverShearStiffness *
            crossovers.sumOf { shearExtension(field, it).let { e -> e * e } }

    /** The energy in `pN·nm` the crossover normal springs store in [field]. */
    fun crossoverNormalEnergy(field: F64Array): Double = 0.5 * crossoverNormalStiffness *
            crossovers.sumOf { normalExtension(field, it).let { e -> e * e } }

    /** Bars, beams and connectors — everything but the regularising bed and the anchors. */
    fun structuralEnergy(field: F64Array): Double = axialEnergy(field) + bendingEnergy(field) +
            crossoverShearEnergy(field) + crossoverNormalEnergy(field)

    /** The energy in `pN·nm` the regularising bed stores in [field] — a numerical residual. */
    fun regularisationEnergy(field: F64Array): Double {
        var total = 0.0
        for (beam in 0 until beamCount) {
            for (node in nodeX.indices) {
                val along = field[dof(beam, node, ALONG)]
                val across = field[dof(beam, node, ACROSS)]
                total += along * along + across * across
            }
        }
        return 0.5 * regularisation * total
    }

    // ------------------------------------------------------------------ assembly

    /**
     * The full in-plane stiffness matrix: bars, beams, connectors, the bed and the anchors.
     *
     * Assembled straight into one array. At a few thousand degrees of freedom a dense
     * `n × n` matrix is tens of megabytes, so keeping one per element type alive is what
     * turns a comfortable calculation into an out-of-memory failure; the contributions are
     * available as **energies** instead, which is all any check of them needs.
     */
    val stiffness: F64Array by lazy {
        val matrix = F64Array(degreesOfFreedom, degreesOfFreedom)
        addBars(matrix)
        addBeams(matrix)
        addConnectors(matrix)
        addRegularisation(matrix)
        supports.forEach { support ->
            addInPlaneOuterProduct(
                matrix, basisAlong(support.x, support.y), support.stiffnessAlong
            )
            addInPlaneOuterProduct(
                matrix, basisAcross(support.x, support.y), support.stiffnessAcross
            )
        }
        matrix
    }

    private val factorisation: CholeskyDecomposition by lazy { CholeskyDecomposition(stiffness) }

    private fun addBars(matrix: F64Array) {
        val modulus = sheet.duplex.stretchModulus
        for (beam in 0 until beamCount) {
            for (element in 0 until nodeX.size - 1) {
                val axial = modulus / (nodeX[element + 1] - nodeX[element])
                inPlaneScatter(
                    matrix,
                    intArrayOf(dof(beam, element, ALONG), dof(beam, element + 1, ALONG)),
                    arrayOf(
                        doubleArrayOf(axial, -axial),
                        doubleArrayOf(-axial, axial)
                    )
                )
            }
        }
    }

    private fun addBeams(matrix: F64Array) {
        val rigidity = sheet.duplex.bendingRigidity
        for (beam in 0 until beamCount) {
            for (element in 0 until nodeX.size - 1) {
                val length = nodeX[element + 1] - nodeX[element]
                inPlaneScatter(
                    matrix,
                    intArrayOf(
                        dof(beam, element, ACROSS), dof(beam, element, ROTATION),
                        dof(beam, element + 1, ACROSS), dof(beam, element + 1, ROTATION)
                    ),
                    inPlaneBendingMatrix(rigidity, length)
                )
            }
        }
    }

    private fun addConnectors(matrix: F64Array) {
        val shearGradient = doubleArrayOf(-1.0, connectorArm, 1.0, connectorArm)
        val shearElement = Array(4) { i ->
            DoubleArray(4) { j -> crossoverShearStiffness * shearGradient[i] * shearGradient[j] }
        }
        val normalElement = arrayOf(
            doubleArrayOf(crossoverNormalStiffness, -crossoverNormalStiffness),
            doubleArrayOf(-crossoverNormalStiffness, crossoverNormalStiffness)
        )
        crossovers.forEach { crossover ->
            inPlaneScatter(
                matrix,
                intArrayOf(
                    dof(crossover.lowerBeam, crossover.node, ALONG),
                    dof(crossover.lowerBeam, crossover.node, ROTATION),
                    dof(crossover.lowerBeam + 1, crossover.node, ALONG),
                    dof(crossover.lowerBeam + 1, crossover.node, ROTATION)
                ),
                shearElement
            )
            inPlaneScatter(
                matrix,
                intArrayOf(
                    dof(crossover.lowerBeam, crossover.node, ACROSS),
                    dof(crossover.lowerBeam + 1, crossover.node, ACROSS)
                ),
                normalElement
            )
        }
    }

    private fun addRegularisation(matrix: F64Array) {
        for (beam in 0 until beamCount) {
            for (node in nodeX.indices) {
                matrix[dof(beam, node, ALONG), dof(beam, node, ALONG)] += regularisation
                matrix[dof(beam, node, ACROSS), dof(beam, node, ACROSS)] += regularisation
            }
        }
    }

    // ------------------------------------------------------------------ evaluation

    private fun beamOf(y: Double): Int {
        val index = ((y - beamY[0]) / sheet.interhelicalDistance).roundToInt()
        return index.coerceIn(0, beamCount - 1)
    }

    private fun elementOf(x: Double): Int {
        var element = 0
        while (element < nodeX.size - 2 && nodeX[element + 1] < x) element++
        return element
    }

    /** The vector `b` for which `b·q` is the along-helix displacement at ([x], [y]). */
    internal fun basisAlong(x: Double, y: Double): F64Array {
        val beam = beamOf(y)
        val element = elementOf(x)
        val length = nodeX[element + 1] - nodeX[element]
        val t = (x - nodeX[element]) / length
        val basis = F64Array(degreesOfFreedom)
        basis[dof(beam, element, ALONG)] = 1.0 - t
        basis[dof(beam, element + 1, ALONG)] = t
        val arm = y - beamY[beam]
        if (arm != 0.0) {
            // a material point off the duplex axis moves along the helices by minus the arm
            // times the in-plane rotation, which is the Hermite slope of the across field
            val slope = inPlaneHermiteSlope(t, length)
            basis[dof(beam, element, ACROSS)] -= arm * slope[0]
            basis[dof(beam, element, ROTATION)] -= arm * slope[1]
            basis[dof(beam, element + 1, ACROSS)] -= arm * slope[2]
            basis[dof(beam, element + 1, ROTATION)] -= arm * slope[3]
        }
        return basis
    }

    /** The vector `b` for which `b·q` is the across-helix displacement at ([x], [y]). */
    internal fun basisAcross(x: Double, y: Double): F64Array {
        val beam = beamOf(y)
        val element = elementOf(x)
        val length = nodeX[element + 1] - nodeX[element]
        val t = (x - nodeX[element]) / length
        val shape = inPlaneHermiteShape(t, length)
        val basis = F64Array(degreesOfFreedom)
        basis[dof(beam, element, ACROSS)] = shape[0]
        basis[dof(beam, element, ROTATION)] = shape[1]
        basis[dof(beam, element + 1, ACROSS)] = shape[2]
        basis[dof(beam, element + 1, ROTATION)] = shape[3]
        return basis
    }

    internal fun evaluateAlong(field: F64Array, x: Double, y: Double): Double =
        basisAlong(x, y).dot(field)

    internal fun evaluateAcross(field: F64Array, x: Double, y: Double): Double =
        basisAcross(x, y).dot(field)

    internal fun sumOfNodal(field: F64Array, component: Int): Double {
        var total = 0.0
        for (beam in 0 until beamCount) {
            for (node in nodeX.indices) total += field[dof(beam, node, component)]
        }
        return total
    }

    internal fun sumOfNodalAbove(field: F64Array, component: Int, lowerBeam: Int): Double {
        var total = 0.0
        for (beam in lowerBeam + 1 until beamCount) {
            for (node in nodeX.indices) total += field[dof(beam, node, component)]
        }
        return total
    }

    // ------------------------------------------------------------------ member forces

    internal fun shearExtension(field: F64Array, crossover: Crossover): Double =
        field[dof(crossover.lowerBeam + 1, crossover.node, ALONG)] +
                connectorArm * field[dof(crossover.lowerBeam + 1, crossover.node, ROTATION)] -
                field[dof(crossover.lowerBeam, crossover.node, ALONG)] +
                connectorArm * field[dof(crossover.lowerBeam, crossover.node, ROTATION)]

    internal fun normalExtension(field: F64Array, crossover: Crossover): Double =
        field[dof(crossover.lowerBeam + 1, crossover.node, ACROSS)] -
                field[dof(crossover.lowerBeam, crossover.node, ACROSS)]

    internal fun duplexAxialForces(field: F64Array): List<Double> = buildList {
        val modulus = sheet.duplex.stretchModulus
        for (beam in 0 until beamCount) {
            for (element in 0 until nodeX.size - 1) {
                val length = nodeX[element + 1] - nodeX[element]
                add(
                    modulus * (field[dof(beam, element + 1, ALONG)] -
                            field[dof(beam, element, ALONG)]) / length
                )
            }
        }
    }

    /** The axial force in pN carried by duplex [beam] at station [x], positive in tension. */
    fun axialForceAt(solution: MembraneDeflection, beam: Int, x: Double): Double {
        val element = elementOf(x)
        val length = nodeX[element + 1] - nodeX[element]
        return sheet.duplex.stretchModulus *
                (solution.coefficients[dof(beam, element + 1, ALONG)] -
                        solution.coefficients[dof(beam, element, ALONG)]) / length
    }

    internal fun duplexInPlaneShears(field: F64Array): List<Double> = buildList {
        val rigidity = sheet.duplex.bendingRigidity
        for (beam in 0 until beamCount) {
            for (element in 0 until nodeX.size - 1) {
                val length = nodeX[element + 1] - nodeX[element]
                add(
                    rigidity * (
                            12.0 * (field[dof(beam, element, ACROSS)] -
                                    field[dof(beam, element + 1, ACROSS)]) /
                                    (length * length * length) +
                                    6.0 * (field[dof(beam, element, ROTATION)] +
                                    field[dof(beam, element + 1, ROTATION)]) / (length * length)
                            )
                )
            }
        }
    }

    // ------------------------------------------------------------------ solve

    /** Solves one in-plane load case. */
    fun solve(loads: List<InPlanePointLoad>): MembraneDeflection {
        val vector = F64Array(degreesOfFreedom)
        loads.forEach { load ->
            if (load.forceAlong != 0.0) {
                val basis = basisAlong(load.x, load.y)
                for (k in 0 until degreesOfFreedom) vector[k] += load.forceAlong * basis[k]
            }
            if (load.forceAcross != 0.0) {
                val basis = basisAcross(load.x, load.y)
                for (k in 0 until degreesOfFreedom) vector[k] += load.forceAcross * basis[k]
            }
        }
        return MembraneDeflection(this, factorisation.solve(vector), loads)
    }

    // ------------------------------------------------------------------ entry topologies

    /** The tension-weighted centroid of [topology] along the helices, in nm. */
    fun centroidAlong(topology: EntryTopology): Double =
        topology.bonds.sumOf { it.share * it.x }

    /** The tension-weighted centroid of [topology] across the helices, in nm. */
    fun centroidAcross(topology: EntryTopology): Double =
        topology.bonds.sumOf { it.share * duplexY(it.duplex) }

    /**
     * The load case of one tether pulling with [force] pN, entering the sheet through [near]
     * and leaving through [far].
     *
     * The pull direction is the line joining the two topologies' **centroids**, which keeps
     * the two resultants collinear and the load case moment-free — the generalisation of
     * `C-0020`'s single-point chord, and the condition under which the tile does not have to
     * be given a support it does not have (`C-0010`'s lateral stiffness is exactly zero).
     */
    fun tetherLoads(
        near: EntryTopology,
        far: EntryTopology,
        force: Double = 1.0
    ): List<InPlanePointLoad> {
        val dx = centroidAlong(far) - centroidAlong(near)
        val dy = centroidAcross(far) - centroidAcross(near)
        val span = hypot(dx, dy)
        require(span > 0.0) {
            "the two ends of a tether must have distinct centroids, both were " +
                    "(${centroidAlong(near)}, ${centroidAcross(near)})"
        }
        val along = dx / span
        val across = dy / span
        return near.bonds.map { bond ->
            InPlanePointLoad(
                bond.x, duplexY(bond.duplex),
                -force * along * bond.share, -force * across * bond.share
            )
        } + far.bonds.map { bond ->
            InPlanePointLoad(
                bond.x, duplexY(bond.duplex),
                force * along * bond.share, force * across * bond.share
            )
        }
    }

    /**
     * How a **rigid** staple splits its tension among its bonds — the other end of the
     * bracket from the equal split [EntryTopology.duplexBand] assumes.
     *
     * A rigid bond forces all its attachment points to move together along the pull, so the
     * `m` paths are `m` springs in parallel between two rigid ends and the split is
     * `a = C⁻¹1 / (1ᵀC⁻¹1)` with `C` the tile's own compliance matrix between the bonded
     * stations. `C` is symmetric by Maxwell-Betti and positive definite, so Cholesky is the
     * right factorisation and its success is itself a check on the assembly.
     *
     * **Aligned pulls only.** Each basis load case is a *collinear opposed pair* on one
     * duplex, which is self-equilibrated **and** moment-free; an unequal split at one end of
     * an *oblique* chord carries a couple that nothing in this model reacts, and the
     * regularising bed would absorb it at a stiffness eight orders below any structural one.
     * The two topologies must therefore pair bond for bond on the same duplex.
     *
     * @return the shares, in bond order, summing to one.
     */
    fun compatibleShares(near: EntryTopology, far: EntryTopology): List<Double> {
        require(near.bonds.size == far.bonds.size) {
            "the two ends must pair bond for bond, were ${near.bonds.size} and " +
                    "${far.bonds.size}"
        }
        require(near.bonds.indices.all { near.bonds[it].duplex == far.bonds[it].duplex }) {
            "a compatible split is defined for an ALIGNED pull only, so each bond must pair " +
                    "with one on the same duplex"
        }
        val count = near.bonds.size
        if (count == 1) return listOf(1.0)
        val responses = (0 until count).map { i ->
            val y = duplexY(near.bonds[i].duplex)
            solve(
                listOf(
                    InPlanePointLoad(near.bonds[i].x, y, -1.0, 0.0),
                    InPlanePointLoad(far.bonds[i].x, y, 1.0, 0.0)
                )
            )
        }
        val flexibility = F64Array(count, count)
        for (j in 0 until count) {
            for (i in 0 until count) {
                val y = duplexY(near.bonds[i].duplex)
                flexibility[i, j] = responses[j].displacementAlong(far.bonds[i].x, y) -
                        responses[j].displacementAlong(near.bonds[i].x, y)
            }
        }
        // Maxwell-Betti makes it symmetric analytically; symmetrise against round-off so
        // that the Cholesky pivot test measures the physics and not the last bit
        for (i in 0 until count) {
            for (j in 0 until i) {
                val mean = 0.5 * (flexibility[i, j] + flexibility[j, i])
                flexibility[i, j] = mean
                flexibility[j, i] = mean
            }
        }
        val ones = F64Array(count)
        for (i in 0 until count) ones[i] = 1.0
        val raw = CholeskyDecomposition(flexibility).solve(ones)
        var total = 0.0
        for (i in 0 until count) total += raw[i]
        return (0 until count).map { raw[it] / total }
    }

    companion object {

        /** The along-helix displacement degree of freedom of a node. */
        const val ALONG: Int = 0

        /** The across-helix displacement degree of freedom of a node. */
        const val ACROSS: Int = 1

        /** The in-plane rotation (`dv/dx`) degree of freedom of a node. */
        const val ROTATION: Int = 2

        /** Degrees of freedom per node. */
        const val DOF_PER_NODE: Int = 3

        /** Beam elements per interval between node stations. */
        const val DEFAULT_SUBDIVISIONS: Int = 2

        /**
         * The default in-plane bed stiffness in pN/nm per node.
         *
         * **There is no in-plane foundation in the physics.** `C-0010` shows that a laterally
         * homogeneous grafted layer under a non-adsorbing tile has a translation-invariant
         * free energy, so its lateral restoring stiffness is exactly zero — a symmetry
         * statement, not a small number. The bed exists only to remove the three rigid-body
         * modes from a matrix that would otherwise be singular, and against a
         * self-equilibrated load it must carry nothing measurable. `10⁻⁴ pN/nm` is eight
         * orders below a duplex's own `S/L`, and its share of the load is asserted below
         * `10⁻⁶` as a gate-4 test.
         */
        const val DEFAULT_REGULARISATION: Double = 1e-4

        /** Stations closer than this in nm are merged, so no zero-length element is created. */
        const val STATION_MERGE_TOLERANCE: Double = 1e-6
    }

}

/** The Hermite cubic shape functions on an element of length [length], at `ξ = `[t]. */
private fun inPlaneHermiteShape(t: Double, length: Double): DoubleArray = doubleArrayOf(
    1.0 - 3.0 * t * t + 2.0 * t * t * t,
    length * (t - 2.0 * t * t + t * t * t),
    3.0 * t * t - 2.0 * t * t * t,
    length * (t * t * t - t * t)
)

/** The `d/dx` of [inPlaneHermiteShape]. */
private fun inPlaneHermiteSlope(t: Double, length: Double): DoubleArray = doubleArrayOf(
    (-6.0 * t + 6.0 * t * t) / length,
    1.0 - 4.0 * t + 3.0 * t * t,
    (6.0 * t - 6.0 * t * t) / length,
    3.0 * t * t - 2.0 * t
)

/** `∫ EI N''_a N''_b` for a Hermite element of length [length]. */
private fun inPlaneBendingMatrix(rigidity: Double, length: Double): Array<DoubleArray> {
    val l = length
    val scale = rigidity / (l * l * l)
    return arrayOf(
        doubleArrayOf(12.0, 6.0 * l, -12.0, 6.0 * l),
        doubleArrayOf(6.0 * l, 4.0 * l * l, -6.0 * l, 2.0 * l * l),
        doubleArrayOf(-12.0, -6.0 * l, 12.0, -6.0 * l),
        doubleArrayOf(6.0 * l, 2.0 * l * l, -6.0 * l, 4.0 * l * l)
    ).map { row -> DoubleArray(row.size) { row[it] * scale } }.toTypedArray()
}

private fun inPlaneQuadraticForm(matrix: Array<DoubleArray>, vector: DoubleArray): Double {
    var total = 0.0
    for (i in vector.indices) {
        for (j in vector.indices) total += vector[i] * matrix[i][j] * vector[j]
    }
    return total
}

private fun inPlaneScatter(matrix: F64Array, dofs: IntArray, element: Array<DoubleArray>) {
    for (i in dofs.indices) {
        for (j in dofs.indices) {
            matrix[dofs[i], dofs[j]] += element[i][j]
        }
    }
}

private fun addInPlaneOuterProduct(matrix: F64Array, vector: F64Array, scale: Double) {
    val nonZero = (0 until vector.length).filter { vector[it] != 0.0 }
    for (i in nonZero) {
        for (j in nonZero) {
            matrix[i, j] += scale * vector[i] * vector[j]
        }
    }
}
