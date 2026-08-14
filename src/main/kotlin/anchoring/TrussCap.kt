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
import kotlin.math.PI
import kotlin.math.ceil

/**
 * Task `T-106` — the truss **cap** as a solved body rather than a series spring.
 *
 * ## What `C-0037` asserts and what this file derives
 *
 * `C-0037`'s truss is two duplexes standing normal to the sheet, laid in a row **across** the
 * flexure's axis, sharing a **cap** that ties their heads to the flexure's end. Its whole mechanism
 * is the frame couple `k_frame = series(k_a Σd_i², k_link Σd_i²)`, and its own validity range says:
 *
 * > *"The cap is one rigid body of finite rotational stiffness in series with the legs' axial
 * > couple … Its **geometry** — what physically joins two leg heads 2.72 nm apart to one flexure
 * > duplex — is asserted, not designed."*
 *
 * ## The count and the length that decide the geometry
 *
 * `C-0042`'s **steric floor**: two duplexes standing on one seat cannot be closer than one duplex
 * diameter, `2R = 2.00 nm`. `C-0042`'s **seat contact**: a leg's flat end face makes a line contact
 * `2√(R² − y_c²)` with a duplex whose axis is `y_c` to the side, and **nothing at all** beyond the
 * rim. Put together, a duplex laid **across** the leg row would have to sit `w/2 ≥ R` from each leg
 * — so it seats *neither*, at *every* admissible separation. **The flexure therefore cannot be the
 * cap, and the cap is a separate crossbar duplex laid ALONG the leg row.**
 *
 * The counting theorem says the same thing a second way: the flexure's own end has exactly **two**
 * strand termini, so a cap that *is* the flexure's end has one link per leg — `C-0037`'s `H1`, not
 * its nominal `H2`.
 *
 * ## What a body has that a spring does not
 *
 * 1. **Bending.** The frame-couple path is statically determinate — the two leg forces are `±M/w`
 *    whatever the stiffnesses — so the compliances add and `k_cap,bend = 12 EI/w` exactly, for free
 *    overhangs and moment-free attachments (`16 EI/w` if the attachments clamp).
 * 2. **Torsion.** In the **loaded** plane `Σx_i² = 0`, so there is no frame couple at all and the
 *    head moment reaches the legs through the cap's **torsion** over `w/2` a side:
 *    `k_cap,tors = 4C/w`.
 * 3. **A height.** The cap's axis sits one cap radius above the leg heads and the flexure butts its
 *    side, so the flexure's axis is `e = R` above the leg heads. A rigid offset transforms the tip
 *    flexibility by the unit-determinant congruence `T C Tᵀ`, `T = [[1, e], [0, 1]]`, and enters the
 *    buckling problem as `−½ P e φ²` of geometric softening.
 * 4. **Three more junctions.** Each leg head is a duplex end meeting the cap's side — `C-0029`'s
 *    junction, at the other end of the same leg — and so is the flexure's end. `C-0037` carries the
 *    **axial** path of the first (`k_link = 2 k_bond,s`) and takes the **rotational** path of both
 *    as infinite. It is not: a chord carries `2k_bond,θ + 2k_bond,s r_P² cos²ψ` on one axis and
 *    `2k_bond,θ + 2k_bond,s r_P² sin²ψ` on the other, and their sum is conserved.
 *
 * ## Geometry and sign conventions
 *
 * The sheet is the `x–y` plane and `z` its normal. **`x` is the flexure's own axis** (`C-0037`'s
 * convention), so the **loaded** plane is `x–z` with head coordinates `(u_x, φ_y)` and the **free**
 * plane is `y–z` with `(u_y, φ_x)`. The legs are offset along `ŷ` at `±w/2`. The cap runs along
 * `ŷ`, its axis at `z = ℓ + R`; the flexure butts its side, so the flexure's axis is at the same
 * height and `e = R` above the leg heads. `(u, φ)` keep `C-0030`'s signs.
 */

// ---------------------------------------------------------------- the geometry

/**
 * The line contact, in nm, that **one** leg of a row of separation [legSeparation] would make with
 * a duplex laid **across** the row through the row's centroid — `C-0042`'s [seatContactLength] at a
 * lateral offset of half the separation.
 *
 * It is **zero at every admissible separation**, because `C-0042`'s steric floor already puts
 * `w/2 ≥ R`. That is the whole of cheap bound 1: no duplex across the row can seat both legs, so
 * the flexure cannot be the cap.
 */
fun capSeatContactAcrossRow(
    legSeparation: Double,
    legRadius: Double = BForm.DUPLEX_RADIUS
): Double {
    require(legSeparation > 0.0) { "legSeparation must be positive, was: $legSeparation" }
    return seatContactLength(0.5 * legSeparation, legRadius)
}

/**
 * The cap's plan geometry: a crossbar duplex laid **along** the leg row, long enough to seat both
 * legs' end faces, with its axis one cap radius above them.
 *
 * @property legSeparation the row's pitch `w` in nm.
 * @property rigidHeight `e` — the cap's radius, the height of the flexure's axis above the leg
 *   heads, which `C-0037`'s series spring does not have.
 */
data class TrussCapGeometry(
    val legSeparation: Double,
    val legRadius: Double = BForm.DUPLEX_RADIUS,
    val capRadius: Double = BForm.DUPLEX_RADIUS,
    val rise: Double = Gen1Tile.RISE_PER_BASE_PAIR
) {

    init {
        require(legSeparation > 0.0) { "legSeparation must be positive, was: $legSeparation" }
        require(legRadius > 0.0) { "legRadius must be positive, was: $legRadius" }
        require(capRadius > 0.0) { "capRadius must be positive, was: $capRadius" }
        require(rise > 0.0) { "rise must be positive, was: $rise" }
    }

    /** The shortest crossbar that covers both leg end faces, in nm. */
    val minimumLength: Double get() = legSeparation + 2.0 * legRadius

    /** The same in base pairs, which is what a builder orders. */
    val minimumBasePairs: Int get() = ceil(minimumLength / rise).toInt()

    /** `e` — the flexure's axis sits one cap radius above the leg heads. */
    val rigidHeight: Double get() = capRadius

    /** The line contact a leg makes with a duplex laid ACROSS the row — zero above the floor. */
    val perpendicularSeatContact: Double get() = capSeatContactAcrossRow(legSeparation, legRadius)

    /** The line contact a leg makes with the crossbar laid ALONG the row — its full diameter. */
    val parallelSeatContact: Double get() = 2.0 * legRadius

    /** Whether the cap has to be a body of its own, i.e. cannot be the flexure. */
    val separateBodyRequired: Boolean get() = perpendicularSeatContact <= 0.0

    /** How many 90° junctions the crossbar hosts: one per leg, plus the flexure's own end. */
    val junctionCount: Int get() = 3

    /** How many covalent links those junctions consume, at `C-0029`'s two per duplex end. */
    val covalentLinkCount: Int get() = junctionCount * BForm.TERMINI_PER_DUPLEX_END
}

// ---------------------------------------------------------------- the cap's own constants

/**
 * The cap's **rotational** stiffness in pN·nm/rad against the frame couple it transmits:
 * `endFactor · EI / w`.
 *
 * The frame-couple path is statically determinate — the two leg forces are `±M/w` whatever the
 * stiffnesses — so the strain energies add and Castigliano gives the compliance directly:
 * `θ = M w/(12 EI)` for a crossbar with **free overhangs and moment-free leg attachments**
 * (`endFactor = 12`), and `M w/(16 EI)` if the attachments clamp it (`endFactor = 16`). The pinned
 * reading is the softer one and is adopted; the clamped one is carried as the bracket.
 */
fun capBendingStiffness(
    bendingRigidity: Double,
    legSeparation: Double,
    endFactor: Double = 12.0
): Double {
    require(bendingRigidity > 0.0) { "bendingRigidity must be positive, was: $bendingRigidity" }
    require(legSeparation > 0.0) { "legSeparation must be positive, was: $legSeparation" }
    require(endFactor > 0.0) { "endFactor must be positive, was: $endFactor" }
    return endFactor * bendingRigidity / legSeparation
}

/**
 * The cap's **torsional** stiffness in pN·nm/rad between the flexure's attachment at the crossbar's
 * midpoint and the two leg heads: two lengths of `w/2` in parallel, `4C/w`.
 *
 * This is the loaded plane's whole cap term, because there `Σx_i² = 0` and the frame couple is
 * exactly zero — the head moment reaches the legs as **torsion of the crossbar** and nothing else.
 */
fun capTorsionalStiffness(torsionalRigidity: Double, legSeparation: Double): Double {
    require(torsionalRigidity >= 0.0) {
        "torsionalRigidity must not be negative, was: $torsionalRigidity"
    }
    require(legSeparation > 0.0) { "legSeparation must be positive, was: $legSeparation" }
    return 4.0 * torsionalRigidity / legSeparation
}

/**
 * `C-0037`'s frame couple **recomputed on a solved cap**: the legs' axial path, the head links, and
 * the crossbar's own bending, all in series.
 *
 * `series(k_a Σd², k_link Σd², k_cap,bend)`. The first two terms carry `Σd²` and the third does
 * **not** — the crossbar's bending goes as `1/w` where the couple goes as `w²` — which is why the
 * cap cannot be absorbed into an effective link stiffness and why it binds hardest at the wide end
 * of the row, not at the narrow one.
 */
fun solvedFrameCouple(
    legAxialStiffness: Double,
    linkStiffness: Double,
    secondMoment: Double,
    capBendingStiffness: Double
): Double {
    require(legAxialStiffness >= 0.0) {
        "legAxialStiffness must not be negative, was: $legAxialStiffness"
    }
    require(linkStiffness >= 0.0) { "linkStiffness must not be negative, was: $linkStiffness" }
    require(secondMoment >= 0.0) { "secondMoment must not be negative, was: $secondMoment" }
    require(capBendingStiffness >= 0.0) {
        "capBendingStiffness must not be negative, was: $capBendingStiffness"
    }
    return seriesStiffness(
        seriesStiffness(legAxialStiffness * secondMoment, linkStiffness * secondMoment),
        capBendingStiffness
    )
}

// ---------------------------------------------------------------- the assembled head

/**
 * A tip flexibility carried up a **rigid** offset of [height] nm along the standoff's own axis —
 * the congruence `T C Tᵀ` with `T = [[1, e], [0, 1]]`.
 *
 * Its determinant is invariant, because `det T = 1`; `C22` is untouched, `C12` gains `e C22` and
 * `C11` gains `2e C12 + e² C22`. So the cap's height **raises the off-diagonal**, which is the entry
 * `C-0030` shows supplies the draw-in — the one thing about a solved cap that runs the favourable
 * way.
 */
fun offsetFlexibility(
    flexibility: StandoffTipFlexibility,
    height: Double
): StandoffTipFlexibility {
    require(height >= 0.0) { "height must not be negative, was: $height" }
    val c11 = flexibility.translationUnderForce
    val c12 = flexibility.translationUnderMoment
    val c21 = flexibility.rotationUnderForce
    val c22 = flexibility.rotationUnderMoment
    return StandoffTipFlexibility(
        translationUnderForce = c11 + height * (c12 + c21) + height * height * c22,
        translationUnderMoment = c12 + height * c22,
        rotationUnderForce = c21 + height * c22,
        rotationUnderMoment = c22
    )
}

private fun assembleCapped(
    leg: StandoffTipFlexibility,
    legCount: Int,
    frameCouple: Double,
    headJunctionRotational: Double,
    headJunctionShear: Double,
    capSeriesRotational: Double,
    flexureJunctionRotational: Double,
    flexureJunctionShear: Double,
    rigidHeight: Double
): StandoffTipFlexibility {
    // 1. the junction at the leg's own head — a series spring on each diagonal coordinate
    val withJunction = StandoffTipFlexibility(
        translationUnderForce = leg.translationUnderForce + reciprocal(headJunctionShear),
        translationUnderMoment = leg.translationUnderMoment,
        rotationUnderForce = leg.rotationUnderForce,
        rotationUnderMoment = leg.rotationUnderMoment + reciprocal(headJunctionRotational)
    )
    // 2. the legs act in parallel and the frame couple adds to the rotation-rotation entry
    val determinant = withJunction.translationUnderForce * withJunction.rotationUnderMoment -
            withJunction.translationUnderMoment * withJunction.rotationUnderForce
    require(determinant > 0.0) { "a leg's flexibility is not invertible: $determinant" }
    val n = legCount.toDouble()
    val k11 = n * withJunction.rotationUnderMoment / determinant
    val k12 = -n * withJunction.translationUnderMoment / determinant
    val k21 = -n * withJunction.rotationUnderForce / determinant
    val k22 = n * withJunction.translationUnderForce / determinant + frameCouple
    val assembledDeterminant = k11 * k22 - k12 * k21
    require(assembledDeterminant > 0.0) {
        "the assembled truss is not invertible: $assembledDeterminant"
    }
    val atLegHeads = StandoffTipFlexibility(
        translationUnderForce = k22 / assembledDeterminant,
        translationUnderMoment = -k12 / assembledDeterminant,
        rotationUnderForce = -k21 / assembledDeterminant,
        rotationUnderMoment = k11 / assembledDeterminant
    )
    // 3. the crossbar's torsion, between its midpoint and the leg heads
    val twisted = StandoffTipFlexibility(
        translationUnderForce = atLegHeads.translationUnderForce,
        translationUnderMoment = atLegHeads.translationUnderMoment,
        rotationUnderForce = atLegHeads.rotationUnderForce,
        rotationUnderMoment = atLegHeads.rotationUnderMoment + reciprocal(capSeriesRotational)
    )
    // 4. the cap's own height, as a rigid offset — after the torsion, because a twist of the
    //    crossbar moves the flexure's axis sideways by e times the twist
    val lifted = offsetFlexibility(twisted, rigidHeight)
    // 5. the flexure's own end junction onto the crossbar
    return StandoffTipFlexibility(
        translationUnderForce = lifted.translationUnderForce + reciprocal(flexureJunctionShear),
        translationUnderMoment = lifted.translationUnderMoment,
        rotationUnderForce = lifted.rotationUnderForce,
        rotationUnderMoment = lifted.rotationUnderMoment + reciprocal(flexureJunctionRotational)
    )
}

private fun reciprocal(stiffness: Double): Double {
    require(stiffness > 0.0) { "a series stiffness must be positive, was: $stiffness" }
    return if (stiffness.isInfinite()) 0.0 else 1.0 / stiffness
}

/**
 * The assembled 2 × 2 tip flexibility a **solved** cap hands the flexure's end.
 *
 * It is a strict generalisation of `C-0037`'s [trussTipFlexibility]: with every junction rigid, no
 * cap torsion and zero height it returns that function **entry by entry**, which is the first
 * verification gate.
 */
fun cappedHeadFlexibility(
    bendingRigidity: Double,
    length: Double,
    baseRotationalStiffness: Double,
    legCount: Int,
    frameCouple: Double,
    headJunctionRotational: Double = Double.POSITIVE_INFINITY,
    headJunctionShear: Double = Double.POSITIVE_INFINITY,
    capSeriesRotational: Double = Double.POSITIVE_INFINITY,
    flexureJunctionRotational: Double = Double.POSITIVE_INFINITY,
    flexureJunctionShear: Double = Double.POSITIVE_INFINITY,
    rigidHeight: Double = 0.0
): StandoffTipFlexibility {
    require(legCount >= 1) { "legCount must be at least one, was: $legCount" }
    require(frameCouple >= 0.0) { "frameCouple must not be negative, was: $frameCouple" }
    return assembleCapped(
        standoffTipFlexibility(bendingRigidity, length, baseRotationalStiffness),
        legCount, frameCouple, headJunctionRotational, headJunctionShear,
        capSeriesRotational, flexureJunctionRotational, flexureJunctionShear, rigidHeight
    )
}

/**
 * The same assembly with each leg's flexibility obtained by **quadrature** — `C12` through a double
 * cumulative-Simpson integration and `C21` through a single one — so that Maxwell-Betti can be
 * asserted on the *assembled and capped* object rather than constructed.
 *
 * Nothing in the route through the junction springs, the parallel sum, the frame couple, the
 * torsion and the height congruence forces the assembled off-diagonals to stay equal.
 */
fun cappedHeadFlexibilityByIntegration(
    bendingRigidity: Double,
    length: Double,
    baseRotationalStiffness: Double,
    legCount: Int,
    frameCouple: Double,
    headJunctionRotational: Double = Double.POSITIVE_INFINITY,
    headJunctionShear: Double = Double.POSITIVE_INFINITY,
    capSeriesRotational: Double = Double.POSITIVE_INFINITY,
    flexureJunctionRotational: Double = Double.POSITIVE_INFINITY,
    flexureJunctionShear: Double = Double.POSITIVE_INFINITY,
    rigidHeight: Double = 0.0,
    steps: Int = 1024
): StandoffTipFlexibility {
    require(legCount >= 1) { "legCount must be at least one, was: $legCount" }
    require(frameCouple >= 0.0) { "frameCouple must not be negative, was: $frameCouple" }
    return assembleCapped(
        standoffTipFlexibilityByIntegration(
            bendingRigidity, length, baseRotationalStiffness, steps
        ),
        legCount, frameCouple, headJunctionRotational, headJunctionShear,
        capSeriesRotational, flexureJunctionRotational, flexureJunctionShear, rigidHeight
    )
}

// ---------------------------------------------------------------- the capped beam-column element

/**
 * The critical **total** axial load in pN of a truss whose legs are tied to a cap by joints of
 * finite rotational stiffness, at a rigid cap height.
 *
 * `C-0042`'s [mixedBaseTrussBucklingLoad] is this element with every head junction rigid and the
 * height zero, and it reproduces it exactly — which is what verifies the element matrices written
 * out again here rather than shared out of a filed claim's source.
 *
 * The extension is one degree of freedom per leg — its **own** head rotation — spring-coupled by
 * [headJunctionStiffnesses] to a shared cap rotation that carries [frameCouple], and one geometric
 * term `−½ P e φ²` for the rigid arm the cap's radius puts between the leg heads and the load.
 *
 * Positive definiteness is tested by Sylvester's criterion on the pivots of an `LDLᵀ`
 * factorisation, which is exact and not a tolerance. Returns **exactly zero** for a mechanism.
 */
fun cappedTrussBucklingLoad(
    bendingRigidity: Double,
    length: Double,
    baseRotationalStiffnesses: List<Double>,
    headJunctionStiffnesses: List<Double>,
    frameCouple: Double,
    rigidHeight: Double = 0.0,
    elementsPerLeg: Int = 32
): Double {
    require(bendingRigidity > 0.0) { "bendingRigidity must be positive, was: $bendingRigidity" }
    require(length > 0.0) { "length must be positive, was: $length" }
    require(baseRotationalStiffnesses.isNotEmpty()) { "a truss must have at least one leg" }
    require(headJunctionStiffnesses.size == baseRotationalStiffnesses.size) {
        "every leg needs exactly one head junction, was: ${headJunctionStiffnesses.size} for " +
                "${baseRotationalStiffnesses.size} legs"
    }
    require(baseRotationalStiffnesses.all { it >= 0.0 }) {
        "a base rotational stiffness must not be negative: $baseRotationalStiffnesses"
    }
    require(headJunctionStiffnesses.all { it >= 0.0 }) {
        "a head junction stiffness must not be negative: $headJunctionStiffnesses"
    }
    require(frameCouple >= 0.0) { "frameCouple must not be negative, was: $frameCouple" }
    require(rigidHeight >= 0.0) { "rigidHeight must not be negative, was: $rigidHeight" }
    require(elementsPerLeg >= 1) { "elementsPerLeg must be at least one, was: $elementsPerLeg" }

    val legs = baseRotationalStiffnesses.size
    // per leg: the base rotation, 2 per interior node, and — only where the head junction is
    // finite — its own head rotation. A degree of freedom nobody uses is a zero pivot, which
    // `LDLᵀ` reports as a mechanism, so the rigid case must not allocate one.
    val offsets = IntArray(legs)
    val topDofs = IntArray(legs)
    var next = 2
    for (leg in 0 until legs) {
        offsets[leg] = next
        next += 2 * elementsPerLeg - 1
        topDofs[leg] = if (headJunctionStiffnesses[leg].isInfinite()) 1 else next++
    }
    val size = next
    val elastic = Array(size) { DoubleArray(size) }
    val geometric = Array(size) { DoubleArray(size) }
    val h = length / elementsPerLeg
    val ke = capBeamElasticMatrix(bendingRigidity, h)
    val kg = capBeamGeometricMatrix(h)

    baseRotationalStiffnesses.forEachIndexed { leg, baseStiffness ->
        val offset = offsets[leg]
        val topRotation = topDofs[leg]
        val junction = headJunctionStiffnesses[leg]
        fun dof(node: Int, component: Int): Int = when {
            node == 0 -> if (component == 0) -1 else offset
            node == elementsPerLeg -> if (component == 0) 0 else topRotation
            else -> offset + 2 * node - 1 + component
        }
        for (e in 0 until elementsPerLeg) {
            val map = intArrayOf(dof(e, 0), dof(e, 1), dof(e + 1, 0), dof(e + 1, 1))
            for (a in 0..3) {
                val ga = map[a]
                if (ga < 0) continue
                for (b in 0..3) {
                    val gb = map[b]
                    if (gb < 0) continue
                    elastic[ga][gb] += ke[a][b]
                    geometric[ga][gb] += kg[a][b]
                }
            }
        }
        elastic[offset][offset] += baseStiffness
        if (!junction.isInfinite()) {
            elastic[topRotation][topRotation] += junction
            elastic[1][1] += junction
            elastic[topRotation][1] -= junction
            elastic[1][topRotation] -= junction
        }
    }
    elastic[1][1] += frameCouple
    // the rigid arm: the load descends an extra e φ²/2, and `positiveDefinite` scales the whole
    // geometric matrix by the per-leg share, so the entry carries the leg count back out
    geometric[1][1] += legs * rigidHeight

    val lower = Array(size) { DoubleArray(size) }
    val pivots = DoubleArray(size)
    fun stable(total: Double): Boolean =
        capPositiveDefinite(elastic, geometric, total, legs, lower, pivots)

    if (!stable(0.0)) return 0.0
    var high = legs * PI * PI * bendingRigidity / (length * length)
    var doubled = 0
    while (stable(high) && doubled < 40) {
        high *= 2.0
        doubled++
    }
    var low = 0.0
    repeat(200) {
        val middle = 0.5 * (low + high)
        if (stable(middle)) low = middle else high = middle
        if (high - low <= 1.0e-14 * high) return 0.5 * (low + high)
    }
    return 0.5 * (low + high)
}

/**
 * The Hermite-cubic beam element's elastic stiffness, DOFs `(w_i, θ_i, w_j, θ_j)`.
 *
 * Written out again rather than shared out of `C-0042`'s source, which is a filed claim's; that the
 * two are the same matrix is not asserted by inspection but **verified**, because
 * [cappedTrussBucklingLoad] reproduces [mixedBaseTrussBucklingLoad] in its own rigid limit.
 */
private fun capBeamElasticMatrix(bendingRigidity: Double, h: Double): Array<DoubleArray> {
    val c = bendingRigidity / (h * h * h)
    return arrayOf(
        doubleArrayOf(12.0 * c, 6.0 * h * c, -12.0 * c, 6.0 * h * c),
        doubleArrayOf(6.0 * h * c, 4.0 * h * h * c, -6.0 * h * c, 2.0 * h * h * c),
        doubleArrayOf(-12.0 * c, -6.0 * h * c, 12.0 * c, -6.0 * h * c),
        doubleArrayOf(6.0 * h * c, 2.0 * h * h * c, -6.0 * h * c, 4.0 * h * h * c)
    )
}

/** The consistent geometric stiffness **per unit axial compression**. */
private fun capBeamGeometricMatrix(h: Double): Array<DoubleArray> {
    val c = 1.0 / (30.0 * h)
    return arrayOf(
        doubleArrayOf(36.0 * c, 3.0 * h * c, -36.0 * c, 3.0 * h * c),
        doubleArrayOf(3.0 * h * c, 4.0 * h * h * c, -3.0 * h * c, -h * h * c),
        doubleArrayOf(-36.0 * c, -3.0 * h * c, 36.0 * c, -3.0 * h * c),
        doubleArrayOf(3.0 * h * c, -h * h * c, -3.0 * h * c, 4.0 * h * h * c)
    )
}

/** `LDLᵀ` with no pivoting — Sylvester's criterion on the leading principal minors. */
private fun capPositiveDefinite(
    elastic: Array<DoubleArray>,
    geometric: Array<DoubleArray>,
    total: Double,
    legs: Int,
    l: Array<DoubleArray>,
    d: DoubleArray
): Boolean {
    val size = elastic.size
    val share = total / legs
    for (j in 0 until size) {
        var pivot = elastic[j][j] - share * geometric[j][j]
        for (k in 0 until j) pivot -= l[j][k] * l[j][k] * d[k]
        if (pivot <= 0.0) return false
        d[j] = pivot
        for (i in j + 1 until size) {
            var value = elastic[i][j] - share * geometric[i][j]
            for (k in 0 until j) value -= l[i][k] * l[j][k] * d[k]
            l[i][j] = value / pivot
        }
    }
    return true
}

// ---------------------------------------------------------------- the whole solved cap

/**
 * A solved truss cap: a crossbar duplex laid along a two-leg row of [separationBasePairs] pitch,
 * seating both legs, hosting the flexure's own end at its midpoint.
 *
 * Every quantity `C-0037` asserts is exposed beside the one this file derives, so the departure is
 * a subtraction and never a re-derivation:
 *
 * - [assertedFrameCouple] against [frameCouple];
 * - [assertedFlexibility] against [flexibility];
 * - [assertedFreeCriticalLoad] against [freeCriticalLoad].
 *
 * @property capJunctionMisalignment the chord azimuth `ψ` of the **leg-head** junctions, from the
 *   flexure's axis. `0` puts the strong constant in the loaded plane (where it buys draw-in supply)
 *   and `π/2` puts it in the free plane (where it buys buckling), and the sum is conserved.
 */
class SolvedTrussCap(
    val separationBasePairs: Int,
    val legLength: Double,
    val base: TwoLinkBase,
    val capJunctionMisalignment: Double = 0.0,
    val flexureJunctionRotational: Double = maximumBaseRotationalStiffness(BForm.PHOSPHATE_RADIUS),
    val flexureJunctionShear: Double = 2.0 * bondSlideStiffness(),
    val linkStiffness: Double = 2.0 * bondSlideStiffness(),
    val capEndFactor: Double = 12.0,
    val torsionalRigidity: Double = Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY,
    val bendingRigidity: Double = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
    val stretchModulus: Double = Gen1Tile.DUPLEX_STRETCH_MODULUS,
    val rise: Double = Gen1Tile.RISE_PER_BASE_PAIR,
    val backbone: DuplexBackbone = DuplexBackbone(minorGrooveAngle = 180.0),
    val elementsPerLeg: Int = 32,
    /** Set to `true` to hold the cap rigid and its height zero — `C-0037`'s own reading. */
    val asserted: Boolean = false
) {

    init {
        require(separationBasePairs >= 1) {
            "separationBasePairs must be at least one, was: $separationBasePairs"
        }
        require(legLength > 0.0) { "legLength must be positive, was: $legLength" }
        require(capJunctionMisalignment >= 0.0) {
            "capJunctionMisalignment must not be negative, was: $capJunctionMisalignment"
        }
        require(elementsPerLeg >= 1) { "elementsPerLeg must be at least one, was: $elementsPerLeg" }
    }

    val legCount: Int get() = 2

    val separation: Double get() = separationBasePairs * rise

    val geometry: TrussCapGeometry = TrussCapGeometry(separation, rise = rise)

    /** `C-0037`'s cross row: the whole second moment is in the free plane. */
    val layout: TrussLayout = TrussLayout.row(
        2, separation, PI / 2.0, "$separationBasePairs bp cross row"
    )

    /** The two axes of a leg-head junction chord, at [capJunctionMisalignment]. */
    private val headAxes = chordBaseAxes(backbone, capJunctionMisalignment)

    /** The head junction's rotational stiffness in the **loaded** plane. */
    val headJunctionLoaded: Double get() = headAxes.loaded

    /** The head junction's rotational stiffness in the **free** plane. */
    val headJunctionFree: Double get() = headAxes.free

    val legAxial: Double = legAxialStiffness(legLength, base, stretchModulus)

    val capBending: Double = capBendingStiffness(bendingRigidity, separation, capEndFactor)

    val capTorsion: Double = capTorsionalStiffness(torsionalRigidity, separation)

    val rigidHeight: Double get() = if (asserted) 0.0 else geometry.rigidHeight

    /** `C-0037`'s frame couple: the legs' axial path and the head links, and nothing else. */
    val assertedFrameCouple: Double = trussFrameCouple(
        layout.acrossSecondMoment, legAxial, linkStiffness * layout.acrossSecondMoment
    )

    /** The same couple with the crossbar's own bending in series. */
    val frameCouple: Double =
        if (asserted) assertedFrameCouple
        else solvedFrameCouple(
            legAxial, linkStiffness, layout.acrossSecondMoment, capBending
        )

    /** `C-0037`'s head flexibility in the loaded plane — a rigid cap, rigidly bonded. */
    val assertedFlexibility: StandoffTipFlexibility = trussTipFlexibility(
        bendingRigidity, legLength, base.restrainedAxis, legCount, 0.0
    )

    /** The loaded plane's flexibility on the solved cap. `Σx_i² = 0`, so there is no frame couple. */
    val flexibility: StandoffTipFlexibility =
        if (asserted) assertedFlexibility
        else cappedHeadFlexibility(
            bendingRigidity, legLength, base.restrainedAxis, legCount, 0.0,
            headJunctionRotational = headAxes.loaded,
            headJunctionShear = linkStiffness,
            capSeriesRotational = capTorsion,
            flexureJunctionRotational = flexureJunctionRotational,
            flexureJunctionShear = flexureJunctionShear,
            rigidHeight = rigidHeight
        )

    /** The loaded plane's critical load — unchanged in form, because its frame couple is zero. */
    val loadedCriticalLoad: Double = cappedTrussBucklingLoad(
        bendingRigidity, legLength,
        List(legCount) { base.restrainedAxis },
        List(legCount) { if (asserted) Double.POSITIVE_INFINITY else headAxes.loaded },
        0.0, rigidHeight, elementsPerLeg
    )

    /** `C-0037`'s free-plane critical load: rigid cap, rigid junctions, no height. */
    val assertedFreeCriticalLoad: Double = trussBucklingLoad(
        bendingRigidity, legLength, base.freeAxis, legCount, assertedFrameCouple
    )

    /** The free plane's critical load on the solved cap — where the truss exists to be stiff. */
    val freeCriticalLoad: Double = cappedTrussBucklingLoad(
        bendingRigidity, legLength,
        List(legCount) { base.freeAxis },
        List(legCount) { if (asserted) Double.POSITIVE_INFINITY else headAxes.free },
        frameCouple, rigidHeight, elementsPerLeg
    )

    /** **The adopted `P6` number — a column buckles about its softest axis.** */
    val criticalLoad: Double get() = minOf(loadedCriticalLoad, freeCriticalLoad)

    val governingPlane: String
        get() = if (freeCriticalLoad <= loadedCriticalLoad) "free" else "loaded"

    /** The head's transverse support, the legs' axial path in series with the head links. */
    val transverseStiffness: Double
        get() = legCount * seriesStiffness(legAxial, linkStiffness)
}
