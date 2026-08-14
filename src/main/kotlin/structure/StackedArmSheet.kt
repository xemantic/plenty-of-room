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
import kotlinx.serialization.Serializable
import org.jetbrains.bio.viktor.F64Array
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sqrt

/**
 * `T-121` — what **34 duplexes stacked above the tile** do to the sheet that carries them.
 *
 * ## What is being modelled, and what it is not
 *
 * `C-0055` established that a single-layer square-lattice sheet occupies two of the four
 * crossover azimuths its own lattice offers, and that the unoccupied pair points **out of the
 * sheet plane**. An `E5a` flexure arm can therefore be rooted **above** the sheet, on an
 * unoccupied `+z` site, without consuming any interface crossover — which is why `C-0054`'s
 * tables (all computed on *consumed* interfaces) do not describe the resulting host at all.
 * `C-0055` names what replaces them as the largest open item it leaves: 34 arms of 8.164 nm
 * are **278 nm of duplex added out of plane**, i.e. mass, drag and rigidity in a direction no
 * model in this programme contains.
 *
 * This file contains the three channels, and the cheap bound that decides which of them
 * matters before any of them is computed:
 *
 * 1. **Rigidity** — [armRootCondensation] and [StackedArmGrillage]. A body attached to a
 *    structure at a **single point** and otherwise free contributes **exactly zero** static
 *    stiffness there, because any motion the interface imposes is matched by a rigid-body
 *    motion of the attached body at zero strain energy. It is the same class of statement as
 *    *"a uniform load on a uniform foundation dishes exactly zero"*: a symmetry, not a small
 *    number. [compositeBendingRigidity] is the other end of the bracket — what a **tied**
 *    second layer would add — and [secondRootReachable] is the geometric fact that decides
 *    which end applies.
 * 2. **Mass** — [duplexMassPerLength], [armContourFraction]. It enters only the quality
 *    factor, which `C-0004` puts at `~5e−4`.
 * 3. **Drag** — [slenderBodyTransverseDrag]. The arms stand on the side of the tile **away**
 *    from the polymer layer and the electrode, so they are not in the squeeze film at all;
 *    what they add is bulk dissipation on the upper face, in parallel with the tile's own
 *    Stokes drag and against `C-0004`'s squeeze drag, which is 40× larger.
 *
 * ## Geometry and sign conventions, restated rather than inherited
 *
 * `x` runs **along** the helices, `y` **across** them in the sheet plane, `z` **normal** and
 * positive **upward** — away from the grafted layer, which is below the tile (`C-0055`'s own
 * convention). `w` is positive **downward**, compressing the layer (`C-0006`, `C-0009`).
 * Lengths nm, forces pN, stiffness pN/nm, rigidities `pN·nm²`, drag `pN·s/nm`, mass
 * `pN·s²/nm`; `k_BT = 4.141947 pN·nm` at 300 K in aqueous 2 mM MgCl₂.
 *
 * An **arm** is a duplex lying parallel to its host duplex, one interhelical distance above
 * it, held by **one** antiparallel crossover at its root — `C-0055`'s motif exactly. It is
 * *not* a duplex standing normal to the sheet: the crossover motif requires parallel helices,
 * and `C-0029`'s literature finding on that is unchanged and upstream of everything here.
 */

/** `C-0039`'s `E5a1` arm length in nm at `C-0055`'s self-consistent 34 paths. */
const val C0055_ARM_LENGTH: Double = 8.16439083

/** `C-0055`'s self-consistent upward arm count on a 40 nm tile. */
const val C0055_ARM_COUNT: Int = 34

/**
 * The mean mass of one base pair of B-form DNA in daltons, including its two backbone
 * phosphates and counterions taken as none — **CITED**, the standard 650 Da/bp used
 * throughout the nucleic-acid literature for double-stranded DNA (two nucleotides of
 * mean residue mass ~325 Da).
 *
 * It enters exactly one quantity here, the quality factor, which `C-0004` already puts three
 * orders below the overdamped boundary; a 10 % error in it is invisible.
 */
const val DUPLEX_MASS_PER_BASE_PAIR_DALTON: Double = 650.0

/** One dalton in `pN·s²/nm` — `1 kg = 1e3 pN·s²/nm` and `1 Da = 1.66053907e−27 kg`. */
const val DALTON: Double = 1.66053907e-27 * 1e3

// ---------------------------------------------------------------------------- the geometry

/**
 * One arm duplex stacked above the sheet, rooted on an unoccupied `+z` crossover site.
 *
 * @param row the index of the **host** duplex the arm is rooted to, `0` at the lowest `y`.
 * @param rootX the `x` of the root crossover in nm, measured from the tile centre. It must be
 *          a site of the upward column lattice, whose pitch is the full per-interface
 *          crossover spacing `p = 32 bp = 10.88 nm` (`C-0055`: an upward site belongs to
 *          **one** duplex, so nothing is shared and the pitch is the bare 32 bp).
 * @param length the arm's own contour length in nm — `C-0039`'s elastica at the path count.
 * @param towardPositiveX which way the arm points from its root.
 */
@Serializable
data class StackedArm(
    val row: Int,
    val rootX: Double,
    val length: Double,
    val towardPositiveX: Boolean = true
) {

    init {
        require(row >= 0) { "row must not be negative, was: $row" }
        require(length > 0.0) { "length must be positive, was: $length" }
        require(rootX.isFinite()) { "rootX must be finite, was: $rootX" }
    }

    /** The `x` of the arm's free tip in nm. */
    val tipX: Double get() = if (towardPositiveX) rootX + length else rootX - length

}

/** The `y` in nm of the axis of duplex [row] of a [duplexes]-wide sheet centred on the origin. */
fun armRootY(row: Int, duplexes: Int, interhelicalDistance: Double): Double {
    require(duplexes >= 2) { "duplexes must be at least two, was: $duplexes" }
    require(row in 0 until duplexes) { "row must be within 0 until $duplexes, was: $row" }
    require(interhelicalDistance > 0.0) {
        "interhelicalDistance must be positive, was: $interhelicalDistance"
    }
    return (row - (duplexes - 1) / 2.0) * interhelicalDistance
}

/**
 * The fraction of the tile's plan area the arm array covers, taking each arm's footprint as
 * the strip of width [stripWidth] above its host duplex — `C-0055`'s own plan convention.
 *
 * The **first** cheap bound of `T-121`, and it is knowable before any solve.
 */
fun armPlanFootprintFraction(
    arms: List<StackedArm>,
    stripWidth: Double,
    tileArea: Double
): Double {
    require(arms.isNotEmpty()) { "arms must not be empty" }
    require(stripWidth > 0.0) { "stripWidth must be positive, was: $stripWidth" }
    require(tileArea > 0.0) { "tileArea must be positive, was: $tileArea" }
    return arms.sumOf { it.length } * stripWidth / tileArea
}

/** The arms' total contour length as a fraction of the host sheet's own, [hostContourLength] nm. */
fun armContourFraction(arms: List<StackedArm>, hostContourLength: Double): Double {
    require(arms.isNotEmpty()) { "arms must not be empty" }
    require(hostContourLength > 0.0) {
        "hostContourLength must be positive, was: $hostContourLength"
    }
    return arms.sumOf { it.length } / hostContourLength
}

/** The linear mass density of a B-form duplex in `pN·s²/nm` per nm, at a rise of [risePerBasePair]. */
fun duplexMassPerLength(risePerBasePair: Double): Double {
    require(risePerBasePair > 0.0) {
        "risePerBasePair must be positive, was: $risePerBasePair"
    }
    return DUPLEX_MASS_PER_BASE_PAIR_DALTON * DALTON / risePerBasePair
}

/**
 * Whether an arm of [armLength] can reach a **second** upward root at pitch [rootPitch].
 *
 * This is the inequality that decides the whole rigidity question, and it has no free
 * parameter in it: an upward site belongs to one duplex, so its lattice pitch is the bare
 * 32 bp = 10.88 nm, while `C-0039`'s elastica gives 8.164 nm at `C-0055`'s self-consistent
 * count and **9.131 nm even at §3's own 45 paths**. Over the whole design range the arm is
 * shorter than the pitch, so it **cannot** be tied twice — the single-point attachment is
 * forced by the lattice rather than chosen by the modeller, and [armRootCondensation]'s
 * exact zero is therefore a property of the design and not an idealisation of it.
 */
fun secondRootReachable(armLength: Double, rootPitch: Double): Boolean {
    require(armLength > 0.0) { "armLength must be positive, was: $armLength" }
    require(rootPitch > 0.0) { "rootPitch must be positive, was: $rootPitch" }
    return armLength >= rootPitch
}

/**
 * The bending rigidity in `pN·nm²` of **two** duplexes at centre-to-centre separation [offset]
 * tied so rigidly that they share axial load — `2 EI + S δ²/2` by the parallel-axis theorem,
 * with the duplex stretch modulus [stretchModulus] standing in for `EA`.
 *
 * The far end of the bracket, and the reason the exact zero matters: at the Gen-1 numbers this
 * is **19.3×** one duplex's own `EI`. Reported and **not adopted** — it requires the crossover
 * to transmit axial shear, whose constant (`Gen1Tile.crossoverInPlaneStiffness`) is a
 * *construction* rather than a measurement, and it requires at least two ties, which
 * [secondRootReachable] shows the lattice forbids.
 */
fun compositeBendingRigidity(
    bendingRigidity: Double,
    stretchModulus: Double,
    offset: Double
): Double {
    require(bendingRigidity > 0.0) {
        "bendingRigidity must be positive, was: $bendingRigidity"
    }
    require(stretchModulus > 0.0) { "stretchModulus must be positive, was: $stretchModulus" }
    require(offset > 0.0) { "offset must be positive, was: $offset" }
    return 2.0 * bendingRigidity + stretchModulus * offset * offset / 2.0
}

/**
 * The transverse (broadside) drag coefficient in `pN·s/nm` of a rigid cylinder of [length] and
 * [radius] translating perpendicular to its own axis in an unbounded fluid of [viscosity]:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`γ_⊥ = 4π η L / (ln p + ν_⊥)`, `p = L/2a`,
 * `ν_⊥ = 0.839 + 0.185/p + 0.233/p²`
 *
 * — **CITED**, Tirado & García de la Torre's numerical end-correction for short rods
 * (*J. Chem. Phys.* **71**:2581, 1979 and **73**:1986, 1980), the standard slender-body form
 * for DNA fragments of exactly this aspect ratio.
 *
 * **It is an UPPER bound on what an arm adds**, for two reasons that both run the same way:
 * the arm sits about one duplex diameter above a plate that is itself translating, so the
 * fluid around it is already moving with it, and 34 arms at a 2.7 nm pitch screen one another.
 * The lower bound is exactly zero — an arm fully entrained by the tile's own flow adds
 * nothing — and `T-121` quotes the pair rather than a single number.
 */
fun slenderBodyTransverseDrag(
    viscosity: Double,
    length: Double,
    radius: Double
): Double {
    require(viscosity > 0.0) { "viscosity must be positive, was: $viscosity" }
    require(length > 0.0) { "length must be positive, was: $length" }
    require(radius > 0.0) { "radius must be positive, was: $radius" }
    require(2.0 * radius < length) {
        "a slender body must be longer than its own diameter: length $length, radius $radius"
    }
    val aspect = length / (2.0 * radius)
    val correction = 0.839 + 0.185 / aspect + 0.233 / (aspect * aspect)
    return 4.0 * PI * viscosity * length / (ln(aspect) + correction)
}

// ------------------------------------------------------------------- the condensation theorem

/**
 * What an attached arm adds to the **host's** stiffness at its root, obtained by static
 * condensation of the arm's own degrees of freedom.
 *
 * @param addedDeflectionStiffness the added `pN/nm` resisting the host's deflection at the root.
 * @param addedRollStiffness the added `pN·nm/rad` resisting the host's roll at the root.
 * @param addedStiffnessNorm the largest absolute entry of the whole added block, which is the
 *          number the exact-zero statement is made on.
 */
@Serializable
data class ArmRootCondensation(
    val ties: Int,
    val regularisation: Double,
    val addedDeflectionStiffness: Double,
    val addedRollStiffness: Double,
    val addedStiffnessNorm: Double
)

/**
 * The condensed stiffness a free arm adds to its host, at [ties] crossover ties.
 *
 * ## Why this is exactly zero at one tie
 *
 * The arm's own energy is `½ uᵀ K u` with `K` positive **semi**-definite: in the grillage's
 * out-of-plane kinematics its null space is three-dimensional (a translation `w = 1`, a pitch
 * `w = x`, and a roll `φ = 1`), those being the arm's rigid-body motions. One crossover
 * constrains exactly two of the host's degrees of freedom at the root — the deflection through
 * the vertical link, and the roll through the dihedral spring `k_θ` — and both are spanned by
 * that null space. So for **any** motion the host imposes there is a zero-energy arm motion
 * that follows it, the minimised arm energy is identically zero, and the Schur complement
 * `K_hh − K_ha K_aa⁻¹ K_ah` vanishes term by term.
 *
 * At two ties the null space no longer spans the imposed motion — the arm must bend — and the
 * added stiffness is finite. That is the counterfactual, and [secondRootReachable] says the
 * lattice cannot build it.
 *
 * @param regularisation a weak spring in `pN/nm` on every arm degree of freedom, present only
 *          so that the arm block can be inverted at all; the answer must vanish **linearly**
 *          in it, which is gate 4 rather than an assumption.
 */
fun armRootCondensation(
    bendingRigidity: Double,
    torsionalRigidity: Double,
    length: Double,
    linkStiffness: Double,
    hingeStiffness: Double,
    regularisation: Double,
    ties: Int = 1
): ArmRootCondensation {
    require(bendingRigidity > 0.0) {
        "bendingRigidity must be positive, was: $bendingRigidity"
    }
    require(torsionalRigidity > 0.0) {
        "torsionalRigidity must be positive, was: $torsionalRigidity"
    }
    require(length > 0.0) { "length must be positive, was: $length" }
    require(linkStiffness > 0.0) { "linkStiffness must be positive, was: $linkStiffness" }
    require(hingeStiffness > 0.0) { "hingeStiffness must be positive, was: $hingeStiffness" }
    require(regularisation > 0.0) {
        "regularisation must be positive, was: $regularisation"
    }
    require(ties == 1 || ties == 2) { "ties must be 1 or 2, was: $ties" }

    // arm degrees of freedom: w0, theta0, phi0, w1, theta1, phi1
    val arm = F64Array(ARM_DOF, ARM_DOF)
    val bending = hermiteBending(bendingRigidity, length)
    val bendingDofs = intArrayOf(0, 1, 3, 4)
    for (i in bendingDofs.indices) {
        for (j in bendingDofs.indices) {
            arm[bendingDofs[i], bendingDofs[j]] += bending[i][j]
        }
    }
    val torsion = torsionalRigidity / length
    arm[2, 2] += torsion
    arm[5, 5] += torsion
    arm[2, 5] -= torsion
    arm[5, 2] -= torsion
    for (i in 0 until ARM_DOF) arm[i, i] += regularisation

    // host degrees of freedom: (w, phi) at each tie; tie k sits at arm node k
    val hostDof = 2 * ties
    val hostBlock = F64Array(hostDof, hostDof)
    val coupling = F64Array(hostDof, ARM_DOF)
    for (tie in 0 until ties) {
        val node = tie * 3
        hostBlock[2 * tie, 2 * tie] += linkStiffness
        hostBlock[2 * tie + 1, 2 * tie + 1] += hingeStiffness
        arm[node, node] += linkStiffness
        arm[node + 2, node + 2] += hingeStiffness
        coupling[2 * tie, node] -= linkStiffness
        coupling[2 * tie + 1, node + 2] -= hingeStiffness
    }

    val factorisation = CholeskyDecomposition(arm)
    val added = F64Array(hostDof, hostDof)
    for (i in 0 until hostDof) {
        val solved = factorisation.solve(F64Array(ARM_DOF) { coupling[i, it] })
        for (j in 0 until hostDof) {
            var product = 0.0
            for (k in 0 until ARM_DOF) product += coupling[j, k] * solved[k]
            added[i, j] = hostBlock[i, j] - product
        }
    }
    var norm = 0.0
    for (i in 0 until hostDof) {
        for (j in 0 until hostDof) norm = max(norm, abs(added[i, j]))
    }
    return ArmRootCondensation(
        ties = ties,
        regularisation = regularisation,
        addedDeflectionStiffness = added[0, 0],
        addedRollStiffness = added[1, 1],
        addedStiffnessNorm = norm
    )
}

private const val ARM_DOF: Int = 6

/**
 * The default weak spring in `pN/nm` on an arm degree of freedom.
 *
 * Eight orders below the softest real stiffness in the assembly (the foundation's `k_f d` of
 * about `0.034 pN/nm` per nm of duplex) and well above the double-precision floor of the link
 * penalty, which is the window gate 4 measures.
 *
 * It is a spring **to ground**, so at finite `ε` the arms act as vanishingly weak anchors and
 * the tile dishes by `O(ε)` under a load that should dish exactly zero. That is not a defect
 * of the falsifier but the way it is measured: the exact zero is recovered in the limit, and
 * the rate at which it is recovered is gate 4.
 */
const val DEFAULT_ARM_REGULARISATION: Double = 1e-9

/** How near a host node an arm root must land, in nm, before it is refused rather than snapped. */
const val ARM_ROOT_NODE_TOLERANCE: Double = 1e-6

// ------------------------------------------------------------------- the augmented lattice

/**
 * The deflected state of a [StackedArmGrillage] under one load case.
 *
 * The host part is wrapped in an ordinary [GrillageDeflection], so **every published `C-0009`
 * quantity is read through the same code path it was published from** — a dishing, a peak
 * crossover force and a peak duplex shear here are the same functions of the same coefficients,
 * and the only thing that has changed is the matrix they were solved from.
 */
class StackedArmDeflection internal constructor(
    private val lattice: StackedArmGrillage,
    private val coefficients: F64Array,
    pressure: PressureField,
    pointLoads: List<PointLoad>
) {

    /** The host lattice's own nodal coordinates. */
    val hostCoefficients: F64Array = F64Array(lattice.host.degreesOfFreedom) { coefficients[it] }

    /** The host sheet's deflection, as `C-0009`'s own type. */
    val deflection: GrillageDeflection =
        GrillageDeflection(lattice.host, hostCoefficients, pressure, pointLoads)

    /** The largest absolute departure in nm from a reference host solution. */
    fun hostDeparture(reference: F64Array): Double {
        require(reference.length == hostCoefficients.length) {
            "reference must have ${hostCoefficients.length} entries, had ${reference.length}"
        }
        var peak = 0.0
        for (i in 0 until hostCoefficients.length) {
            peak = max(peak, abs(hostCoefficients[i] - reference[i]))
        }
        return peak
    }

    /** The strain energy in `pN·nm` stored in the arms and their root crossovers. */
    val armEnergy: Double by lazy { lattice.armEnergy(coefficients) }

    /** The largest force in pN any root crossover's vertical link transmits. */
    val peakRootLinkForce: Double by lazy { lattice.peakRootLinkForce(coefficients) }

}

/**
 * `C-0009`'s grillage with [arms] **attached to** it rather than cut from it.
 *
 * The arms are extra degrees of freedom in the **same** stiffness matrix — `CLAUDE.md`'s rule:
 * assemble into one matrix and expose the contributions as energies, because a dense `n × n`
 * matrix per element type is what turns a comfortable lattice solve into an out-of-memory
 * failure the JVM misreports as `NoClassDefFoundError`.
 *
 * The load vector is recovered as `K_host q_host` from the bare lattice's own solution, which
 * is exact and needs no access to its private assembly; the arms carry **no** applied load,
 * because the electrostatic load acts on the tile's underside and the arms stand on the other
 * face.
 *
 * @param regularisation a weak spring in `pN/nm` on every arm degree of freedom. It is needed
 *          because a lever held by one crossover **is a mechanism** — its pitch rotation is a
 *          zero-energy mode, which is exactly the property the `E5a` hinge exists to have — so
 *          the augmented matrix is singular without it. Every result must vanish linearly in
 *          it, and gate 4 checks that rather than assuming it.
 */
class StackedArmGrillage(
    val host: OrigamiGrillage,
    val arms: List<StackedArm>,
    val tiesPerArm: Int = 1,
    val regularisation: Double = DEFAULT_ARM_REGULARISATION,
    val linkStiffness: Double = OrigamiGrillage.RIGID_LINK_STIFFNESS
) {

    init {
        require(tiesPerArm == 1 || tiesPerArm == 2) {
            "tiesPerArm must be 1 or 2, was: $tiesPerArm"
        }
        require(regularisation > 0.0) {
            "regularisation must be positive, was: $regularisation"
        }
        require(linkStiffness > 0.0) { "linkStiffness must be positive, was: $linkStiffness" }
        arms.forEach { arm ->
            require(arm.row < host.beamCount) {
                "arm row ${arm.row} is outside a ${host.beamCount}-duplex sheet"
            }
            require(nodeNear(arm.rootX) != null) {
                "an arm root must land on a node of the host lattice, and ${arm.rootX} nm " +
                        "does not — the upward column lattice and the host's own node " +
                        "stations are not being confused silently"
            }
            if (tiesPerArm == 2) {
                require(nodeNear(arm.tipX) != null) {
                    "a second tie must land on a host node too, and ${arm.tipX} nm does not"
                }
            }
        }
    }

    private fun nodeNear(x: Double): Int? {
        val index = host.nodeX.indices.minByOrNull { abs(host.nodeX[it] - x) } ?: return null
        return if (abs(host.nodeX[index] - x) < ARM_ROOT_NODE_TOLERANCE) index else null
    }

    private fun hostDof(beam: Int, node: Int, component: Int): Int =
        (beam * host.nodesPerBeam + node) * OrigamiGrillage.DOF_PER_NODE + component

    private fun armDof(arm: Int, node: Int, component: Int): Int =
        host.degreesOfFreedom + arm * ARM_DOF + node * OrigamiGrillage.DOF_PER_NODE + component

    /** The number of degrees of freedom of the host and its arms together. */
    val degreesOfFreedom: Int = host.degreesOfFreedom + arms.size * ARM_DOF

    /** The number of degrees of freedom the arms contribute. */
    val armDegreesOfFreedom: Int = arms.size * ARM_DOF

    private val stiffness: F64Array by lazy {
        val matrix = F64Array(degreesOfFreedom, degreesOfFreedom)
        val hostStiffness = host.stiffness
        for (i in 0 until host.degreesOfFreedom) {
            for (j in 0 until host.degreesOfFreedom) {
                matrix[i, j] = hostStiffness[i, j]
            }
        }
        arms.forEachIndexed { index, arm ->
            val bending = hermiteBending(host.sheet.duplex.bendingRigidity, arm.length)
            val bendingDofs = intArrayOf(
                armDof(index, 0, OrigamiGrillage.W), armDof(index, 0, OrigamiGrillage.THETA),
                armDof(index, 1, OrigamiGrillage.W), armDof(index, 1, OrigamiGrillage.THETA)
            )
            for (i in bendingDofs.indices) {
                for (j in bendingDofs.indices) {
                    matrix[bendingDofs[i], bendingDofs[j]] += bending[i][j]
                }
            }
            val torsion = host.sheet.duplex.torsionalRigidity / arm.length
            val phi0 = armDof(index, 0, OrigamiGrillage.PHI)
            val phi1 = armDof(index, 1, OrigamiGrillage.PHI)
            matrix[phi0, phi0] += torsion
            matrix[phi1, phi1] += torsion
            matrix[phi0, phi1] -= torsion
            matrix[phi1, phi0] -= torsion
            for (k in 0 until ARM_DOF) {
                val dof = host.degreesOfFreedom + index * ARM_DOF + k
                matrix[dof, dof] += regularisation
            }
            ties(index, arm).forEach { (hostSide, armSide) ->
                addPair(matrix, hostSide.first, armSide.first, linkStiffness)
                addPair(
                    matrix, hostSide.second, armSide.second, host.sheet.crossoverHingeStiffness
                )
            }
        }
        matrix
    }

    /** The (host `w`, host `φ`) and (arm `w`, arm `φ`) degree-of-freedom pairs of each tie. */
    private fun ties(
        index: Int,
        arm: StackedArm
    ): List<Pair<Pair<Int, Int>, Pair<Int, Int>>> = (0 until tiesPerArm).map { tie ->
        val x = if (tie == 0) arm.rootX else arm.tipX
        val node = nodeNear(x)!!
        (hostDof(arm.row, node, OrigamiGrillage.W) to
                hostDof(arm.row, node, OrigamiGrillage.PHI)) to
                (armDof(index, tie, OrigamiGrillage.W) to
                        armDof(index, tie, OrigamiGrillage.PHI))
    }

    private fun addPair(matrix: F64Array, a: Int, b: Int, stiffness: Double) {
        matrix[a, a] += stiffness
        matrix[b, b] += stiffness
        matrix[a, b] -= stiffness
        matrix[b, a] -= stiffness
    }

    private val factorisation: CholeskyDecomposition by lazy {
        CholeskyDecomposition(stiffness)
    }

    /**
     * Solves one load case, taking the applied load from the **bare** host lattice's own
     * assembly through `f = K_host q_host`.
     */
    fun solve(
        pressure: PressureField = uniformPressure(0.0),
        pointLoads: List<PointLoad> = emptyList()
    ): StackedArmDeflection {
        val bare = host.solve(pressure, pointLoads).coefficients
        val hostStiffness = host.stiffness
        val load = F64Array(degreesOfFreedom)
        for (i in 0 until host.degreesOfFreedom) load[i] = hostStiffness.V[i].dot(bare)
        return StackedArmDeflection(this, factorisation.solve(load), pressure, pointLoads)
    }

    /**
     * The **bending and torsion** strain energy in `pN·nm` the arms themselves hold in [field].
     *
     * Their own element matrices only — not the regularisation, and not the root crossovers,
     * whose load is reported separately by [peakRootLinkForce]. Written as an energy rather
     * than as a retained matrix, per `CLAUDE.md`.
     */
    fun armEnergy(field: F64Array): Double {
        var total = 0.0
        arms.forEachIndexed { index, arm ->
            val bending = hermiteBending(host.sheet.duplex.bendingRigidity, arm.length)
            val q = doubleArrayOf(
                field[armDof(index, 0, OrigamiGrillage.W)],
                field[armDof(index, 0, OrigamiGrillage.THETA)],
                field[armDof(index, 1, OrigamiGrillage.W)],
                field[armDof(index, 1, OrigamiGrillage.THETA)]
            )
            for (i in q.indices) {
                for (j in q.indices) total += q[i] * bending[i][j] * q[j]
            }
            val twist = field[armDof(index, 1, OrigamiGrillage.PHI)] -
                    field[armDof(index, 0, OrigamiGrillage.PHI)]
            total += host.sheet.duplex.torsionalRigidity * twist * twist / arm.length
        }
        return 0.5 * total
    }

    /** The largest force in pN any root crossover's vertical link transmits in [field]. */
    fun peakRootLinkForce(field: F64Array): Double {
        var peak = 0.0
        arms.forEachIndexed { index, arm ->
            ties(index, arm).forEach { (hostSide, armSide) ->
                peak = max(
                    peak, linkStiffness * abs(field[hostSide.first] - field[armSide.first])
                )
            }
        }
        return peak
    }

    /**
     * The RMS fluctuation in nm of the host's deflection at the **material point** ([x], [y]),
     * from `⟨w²⟩ = k_BT bᵀ K⁻¹ b` on the **augmented** matrix — so that any stiffness the arms
     * added would show up here, and any they did not is a statement rather than an assumption.
     */
    fun pointFluctuationRms(
        x: Double,
        y: Double,
        temperature: Double = ROOM_TEMPERATURE
    ): Double {
        val hostBasis = host.basisAt(x, y)
        val basis = F64Array(degreesOfFreedom)
        for (i in 0 until host.degreesOfFreedom) basis[i] = hostBasis[i]
        return sqrt(
            max(0.0, thermalEnergy(temperature) * basis.dot(factorisation.solve(basis)))
        )
    }

}

/** `∫ EI N''_a N''_b` for a Hermite element of [length] — the arm's own bending matrix. */
private fun hermiteBending(rigidity: Double, length: Double): Array<DoubleArray> {
    val l = length
    val scale = rigidity / (l * l * l)
    return arrayOf(
        doubleArrayOf(12.0, 6.0 * l, -12.0, 6.0 * l),
        doubleArrayOf(6.0 * l, 4.0 * l * l, -6.0 * l, 2.0 * l * l),
        doubleArrayOf(-12.0, -6.0 * l, 12.0, -6.0 * l),
        doubleArrayOf(6.0 * l, 2.0 * l * l, -6.0 * l, 4.0 * l * l)
    ).map { row -> DoubleArray(row.size) { row[it] * scale } }.toTypedArray()
}
