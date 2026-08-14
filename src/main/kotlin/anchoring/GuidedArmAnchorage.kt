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

/**
 * Task `T-70` — **what holds `E5g16`'s guided arm**, and whether `C-0029`'s asserted `c = 12`
 * survives its own anchorage.
 *
 * ## What `C-0029` left standing on an assertion
 *
 * `C-0029` hands the Gen-1 output coupling to `E5g16`, *"a 12.24 nm = 36 bp **guided** arm on 16
 * crossovers"*, and the whole design turns on one letter in a cube root:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`r ≤ (c·n·EI/k_target)^(1/3)` — **9.767 nm** at `c = 3`, **15.50 nm** at
 * `c = 12`, against §3's **desired** 10 nm stroke.
 *
 * Its own validity range says the quiet part: *"A guided arm (`c = 12`) is **asserted, not
 * designed**. What holds `E5g16`'s far end against rotation is a second anchorage on the lever,
 * and its own compliance is not modelled here."*
 *
 * ## The continuum, which is the method
 *
 * `c` is not a choice between two textbook numbers, exactly as `C-0025` found for a **different**
 * end pair. The arm's own boundary-value problem — near end clamped in the bending sense (the
 * hinge's rigid rotation is `C-0023`'s separate series term), far end on a rotational spring
 * `k_far`, transverse tip force — superposes the cantilever tip-load and tip-moment solutions:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`θ_B = FL²/(2EI) + ML/EI`, &nbsp; `δ = FL³/(3EI) + ML²/(2EI)`,
 * &nbsp; `M = −k_far θ_B`
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;→ &nbsp; **`c(ρ) = 12(1 + ρ)/(4 + ρ)`, &nbsp; `ρ ≡ k_far r/EI`**,
 *
 * exactly **3** at `ρ = 0`, exactly **12** at `ρ = ∞`, and exactly **6** at `ρ = 2`. The two
 * textbook values are a factor of four apart, as in `C-0025`'s 48/192 — and for the same reason.
 *
 * ## And `ρ` carries the ARM, so the cap is a fixed point
 *
 * `C-0025`'s lesson applies here verbatim: *"`ρ` carries the span, so the same joint is nearer a
 * pin on a short beam and nearer a clamp on a long one"*. So the cap cannot be evaluated at an
 * asserted `c` — it is the **fixed point**
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;**`r_cap = (c(k_far·r_cap/EI)·n·EI/k_target)^(1/3)`**,
 *
 * and that is what makes the answer come out the way it does: a longer arm buys its own restraint,
 * so **every two-link anchorage puts the cap above §3's desired 10 nm stroke** while a **one-link**
 * anchorage — `C-0029`'s `R3` ball joint, and Rothemund's own observed failure — collapses to the
 * cantilever's 9.767 nm.
 *
 * ## The series composition is exact at ONE corner only
 *
 * `C-0023`'s `1/k = r²/(n k_θ) + r³/(c EI)` treats the hinge's rotation and the arm's bending as
 * independent. [TwoSpringArm] solves the same beam with **both** joints as springs and shows the
 * composition is exact **iff the far end carries no moment** — because a guide carries part of the
 * end moment and therefore **relieves the hinge**. `c = 12` and that composition cannot both be
 * right, and the composition is the **soft** reading.
 */

// ------------------------------------------------------------------ the restraint parameter

/**
 * The far end's **restraint parameter** `ρ = k L/EI`, dimensionless.
 *
 * It carries the **arm length**, not only the joint: the same anchorage is nearer a pin on a short
 * arm and nearer a guide on a long one. That is why [anchoredArmCeiling] is a fixed point.
 */
fun armRestraintParameter(
    rotationalStiffness: Double,
    armLength: Double,
    bendingRigidity: Double = Gen1Tile.DUPLEX_BENDING_RIGIDITY
): Double {
    require(rotationalStiffness >= 0.0) {
        "rotationalStiffness must not be negative, was: $rotationalStiffness"
    }
    require(armLength > 0.0) { "armLength must be positive, was: $armLength" }
    require(bendingRigidity > 0.0) {
        "bendingRigidity must be positive, was: $bendingRigidity"
    }
    return rotationalStiffness * armLength / bendingRigidity
}

/**
 * **The continuum this task exists to deliver**: the arm's end-condition factor `c`, for a beam
 * clamped in bending at the hinge end and elastically restrained by [farRestraint] `= ρ` at the
 * anchorage end, loaded by a transverse tip force.
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;**`c(ρ) = 12(1 + ρ)/(4 + ρ)`**
 *
 * `c(0) = 3` is the **cantilever** (`C-0023`'s reading) and `c(∞) = 12` the **guided** arm
 * (`C-0029`'s assertion). Both are limits, and no realisable anchorage is at either.
 */
fun guidedArmFactor(farRestraint: Double): Double {
    require(farRestraint >= 0.0) { "farRestraint must not be negative, was: $farRestraint" }
    if (farRestraint.isInfinite()) return 12.0
    return 12.0 * (1.0 + farRestraint) / (4.0 + farRestraint)
}

/**
 * The end-condition factor of the **whole** two-spring beam — near end on [nearRestraint], far end
 * on [farRestraint], the two bodies translating relative to each other without rotating:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;**`c(ρ_n, ρ_f) = 12(ρ_nρ_f + ρ_n + ρ_f)/(ρ_nρ_f + 4ρ_n + 4ρ_f + 12)`**
 *
 * Its four corners are the four textbook cases and **none of them is assumed**: `(∞,∞) = 12`
 * guided, `(∞,0) = (0,∞) = 3` cantilever, and `(0,0) = 0` — a **mechanism**, not a weaker beam,
 * which is the same statement `C-0028` makes about a pinned-base sway column.
 *
 * It is **symmetric** in its two arguments, which the assembled design is not: the hinge must
 * rotate and the anchorage must not.
 */
fun twoSpringArmFactor(nearRestraint: Double, farRestraint: Double): Double {
    require(nearRestraint >= 0.0) { "nearRestraint must not be negative, was: $nearRestraint" }
    require(farRestraint >= 0.0) { "farRestraint must not be negative, was: $farRestraint" }
    if (nearRestraint.isInfinite()) return guidedArmFactor(farRestraint)
    if (farRestraint.isInfinite()) return guidedArmFactor(nearRestraint)
    val product = nearRestraint * farRestraint
    return 12.0 * (product + nearRestraint + farRestraint) /
            (product + 4.0 * nearRestraint + 4.0 * farRestraint + 12.0)
}

// ------------------------------------------------------------------ the two-spring beam

/**
 * The arm as an Euler-Bernoulli beam with a rotational spring at **each** end, its two bodies
 * translating relative to each other by `δ` and not rotating.
 *
 * Condensing the two end rotations out of the element stiffness matrix gives
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`(4 + ρ_n)θ_A + 2θ_B = 6δ/L`, &nbsp;&nbsp; `2θ_A + (4 + ρ_f)θ_B = 6δ/L`,
 *
 * from which [armFactor], [nearRotation], [farRotation] and the two end moments follow with no
 * further assumption. This class exists to answer one question `C-0023`'s composition cannot:
 * **does a guide at the far end relieve the hinge?** It does — see [seriesDeparture].
 */
class TwoSpringArm(
    val bendingRigidity: Double,
    val length: Double,
    val nearStiffness: Double,
    val farStiffness: Double
) {

    init {
        require(bendingRigidity > 0.0) {
            "bendingRigidity must be positive, was: $bendingRigidity"
        }
        require(length > 0.0) { "length must be positive, was: $length" }
        require(nearStiffness >= 0.0) {
            "nearStiffness must not be negative, was: $nearStiffness"
        }
        require(farStiffness >= 0.0) { "farStiffness must not be negative, was: $farStiffness" }
    }

    /** `ρ_n = k_near L/EI`, dimensionless. */
    val nearRestraint: Double = armRestraintParameter(nearStiffness, length, bendingRigidity)

    /** `ρ_f = k_far L/EI`, dimensionless. */
    val farRestraint: Double = armRestraintParameter(farStiffness, length, bendingRigidity)

    /** The whole beam's end-condition factor, `c(ρ_n, ρ_f)`. */
    val armFactor: Double = twoSpringArmFactor(nearRestraint, farRestraint)

    /** The transverse stiffness of **one** arm in `pN/nm` — `c EI/L³`. */
    val stiffness: Double get() = armFactor * bendingRigidity / (length * length * length)

    private val determinant: Double
        get() = (4.0 + nearRestraint) * (4.0 + farRestraint) - 4.0

    /** The near end's tangent rotation in radians at a relative end translation of [displacement]. */
    fun nearRotation(displacement: Double): Double =
        6.0 * displacement / length * (2.0 + farRestraint) / determinant

    /** The far end's tangent rotation in radians at [displacement] — **zero** for a true guide. */
    fun farRotation(displacement: Double): Double =
        6.0 * displacement / length * (2.0 + nearRestraint) / determinant

    /** The moment in `pN·nm` the near joint carries at [displacement]. */
    fun nearMoment(displacement: Double): Double = nearStiffness * nearRotation(displacement)

    /** The moment in `pN·nm` the far anchorage carries at [displacement]. */
    fun farMoment(displacement: Double): Double = farStiffness * farRotation(displacement)

    /**
     * `C-0023`'s **series composition** for the same arm: the hinge's rigid rotation `k_near/L²` in
     * series with the arm's own bending at [guidedArmFactor] of the far restraint.
     */
    val seriesStiffness: Double
        get() {
            if (nearStiffness == 0.0) return 0.0
            val hinge = nearStiffness / (length * length)
            val arm = guidedArmFactor(farRestraint) * bendingRigidity /
                    (length * length * length)
            return 1.0 / (1.0 / hinge + 1.0 / arm)
        }

    /**
     * How much of the true stiffness the series composition retains — **exactly 1 at a free far
     * end and below 1 at any restrained one**, because a guide carries part of the end moment and
     * therefore relieves the hinge, which the composition charges in full.
     */
    val seriesDeparture: Double get() = seriesStiffness / stiffness
}

// ------------------------------------------------------------------ the anchorage catalogue

/**
 * A candidate **second anchorage** for the arm's far end: what physically holds `E5`'s lever to the
 * tile, priced on motifs that exist.
 *
 * Two links on a chord restrain **one** axis (`C-0029`'s counting theorem), so every entry carries
 * both readings — [rotationalStiffness] about the chord's perpendicular bisector and
 * [chordAxisStiffness] about the chord itself.
 *
 * @property linkCount the number of covalent links the motif presents, which is what decides the
 *   verdict: **one** link is a ball joint and collapses the cap to the cantilever's 9.77 nm.
 */
data class ArmAnchorage(
    val id: String,
    val name: String,
    val rotationalStiffness: Double,
    val chordAxisStiffness: Double,
    val transverseStiffness: Double,
    val linkCount: Int,
    val realisable: Boolean = true,
    val provenance: String = ""
) {

    init {
        require(rotationalStiffness >= 0.0) {
            "rotationalStiffness must not be negative, was: $rotationalStiffness"
        }
        require(chordAxisStiffness >= 0.0) {
            "chordAxisStiffness must not be negative, was: $chordAxisStiffness"
        }
        require(linkCount >= 0) { "linkCount must not be negative, was: $linkCount" }
    }

    /**
     * The couple that survives a [misalignment] in radians between the chord's perpendicular
     * bisector and the axis the arm's bending demands — `cos²`, because a couple is a rank-one
     * tensor, with the chord reading left underneath it.
     */
    fun atMisalignment(misalignment: Double): Double {
        val projection = couplePhaseProjection(misalignment)
        return chordAxisStiffness + (rotationalStiffness - chordAxisStiffness) * projection
    }

    companion object {

        /**
         * **`A0`** — `C-0029`'s assertion: a perfect guide. Not a motif; the limit `c → 12`.
         */
        fun idealGuide(): ArmAnchorage = ArmAnchorage(
            id = "A0",
            name = "ideal guide (C-0029's assertion)",
            rotationalStiffness = Double.POSITIVE_INFINITY,
            chordAxisStiffness = Double.POSITIVE_INFINITY,
            transverseStiffness = Double.POSITIVE_INFINITY,
            linkCount = Int.MAX_VALUE,
            realisable = false,
            provenance = "asserted in C-0029, not designed — this task's subject"
        )

        /**
         * **`A1`** — one covalent link: a hairpin overhang, a sticky end, the literature's pin.
         * `C-0029`'s `R3`, and Rothemund's own observed failure — *"the duplex markers, because
         * they are attached to the origami by only one covalent bond, appear to be flexible."*
         *
         * A single link restrains **no** rotation at all, so `c = 3` and the cap is the
         * cantilever's 9.767 nm — **below §3's desired stroke**.
         */
        fun singleLink(): ArmAnchorage = ArmAnchorage(
            id = "A1",
            name = "one covalent link — a ball joint",
            rotationalStiffness = 0.0,
            chordAxisStiffness = 0.0,
            transverseStiffness = bondSlideStiffness(),
            linkCount = 1,
            provenance = "C-0029's R3; Rothemund 2006 SI reports exactly this and it was flexible"
        )

        /**
         * **`A2`** — the arm's own duplex **end** on the tile: two strand termini on the terminal
         * chord, at [leverArm] nm from its centre. `C-0029`'s counting theorem, applied at the far
         * end instead of the base — and its ceiling `leverArm ≤ r_P = 1.0 nm` is a **count**, not a
         * model.
         */
        fun twoTerminus(
            leverArm: Double = BForm.PHOSPHATE_RADIUS,
            alpha: Double = 1.0,
            inPlaneMultiplier: Double = 1.0
        ): ArmAnchorage {
            require(leverArm >= 0.0) { "leverArm must not be negative, was: $leverArm" }
            return ArmAnchorage(
                id = "A2",
                name = "duplex end, two strand termini at ${"%.3f".format(leverArm)} nm",
                rotationalStiffness =
                    maximumBaseRotationalStiffness(leverArm, alpha, inPlaneMultiplier),
                chordAxisStiffness = 2.0 * bondHingeStiffness(alpha),
                transverseStiffness = 2.0 * bondSlideStiffness(alpha) * inPlaneMultiplier,
                linkCount = 2,
                provenance = "C-0029's counting theorem: a duplex end has exactly two termini"
            )
        }

        /**
         * **`A3`** — a **doubly** nicked continuation of a tile duplex: both backbones cut at the
         * same base pair. Nothing continuous is left, so it is two softened bonds in parallel —
         * which **is** a crossover, exactly as `C-0025`'s `J2b` found.
         */
        fun doublyNickedContinuation(alpha: Double = 1.0): ArmAnchorage =
            twoTerminus(BForm.PHOSPHATE_RADIUS, alpha).copy(
                id = "A3",
                name = "doubly nicked continuation of a tile duplex",
                provenance = "C-0025's J2b: a double nick IS a crossover"
            )

        /**
         * **`A4`** — a **singly** nicked continuation of a tile duplex, one backbone intact.
         *
         * `C-0025`'s `J2`: the intact backbone is not a softened bond, so it carries the duplex's
         * own `B/a`. And a nick **preserves the helix axis** — which is precisely the kinematic
         * content of *"guided"*: the arm arrives collinear with the tile duplex it continues.
         */
        fun nickedContinuation(alpha: Double = 1.0): ArmAnchorage {
            val joint = FlexureEndJoint.nickedContinuation(alpha)
            return ArmAnchorage(
                id = "A4",
                name = "singly nicked continuation of a tile duplex",
                rotationalStiffness = joint.rotationalStiffness,
                chordAxisStiffness = joint.rotationalStiffness,
                transverseStiffness = joint.transverseStiffness,
                linkCount = 3,
                provenance = "C-0025's J2: one intact backbone carries B/a; a nick keeps the axis"
            )
        }

        /**
         * **`A5`** — the arm's far segment laid alongside a tile duplex and tied by [crossovers]
         * antiparallel crossovers at the sheet's own 32 bp pitch: `C-0025`'s `J4`, the standard
         * origami clamp. The couple is `k_s Σd_i²` over the 10.88 nm pitch and it is two orders
         * above the bonds' own hinges.
         */
        fun multiCrossoverClamp(
            crossovers: Int,
            alpha: Double = 1.0,
            inPlaneMultiplier: Double = 1.0
        ): ArmAnchorage {
            val joint = FlexureEndJoint.multiCrossoverClamp(crossovers, alpha, inPlaneMultiplier)
            return ArmAnchorage(
                id = "A5-$crossovers",
                name = joint.name,
                rotationalStiffness = joint.rotationalStiffness,
                chordAxisStiffness = joint.rotationalStiffness,
                transverseStiffness = joint.transverseStiffness,
                linkCount = 4 * crossovers,
                provenance = "C-0025's J4: Chen et al.'s bonds plus their couple over the 32 bp pitch"
            )
        }
    }
}

// ------------------------------------------------------------------ what the anchorage carries

/**
 * The moment in `pN·nm` the far anchorage reacts when a transverse tip force [force] is applied to
 * an arm of [armLength] whose far end is restrained by [farStiffness].
 *
 * From the same boundary-value problem [guidedArmFactor] comes from, `θ_B = FL²/(2EI(1+ρ))`, so
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;**`M_far = F L ρ/(2(1+ρ))`** — zero at a ball joint, `FL/2` at a guide.
 *
 * The guide's moment is what **relieves the hinge**, and it is also the load the anchorage's own
 * links have to carry: a guide is not free.
 */
fun farAnchorageMoment(
    force: Double,
    armLength: Double,
    farStiffness: Double,
    bendingRigidity: Double = Gen1Tile.DUPLEX_BENDING_RIGIDITY
): Double {
    require(force >= 0.0) { "force must not be negative, was: $force" }
    val restraint = armRestraintParameter(farStiffness, armLength, bendingRigidity)
    if (restraint.isInfinite()) return 0.5 * force * armLength
    return force * armLength * restraint / (2.0 * (1.0 + restraint))
}

/**
 * The force in pN on **one** of a two-link anchorage's covalent links, reacting [moment] as a
 * couple over a chord of half-width [leverArm]: `M/(2a)`.
 */
fun farAnchorageLinkForce(
    moment: Double,
    leverArm: Double = BForm.PHOSPHATE_RADIUS
): Double {
    require(moment >= 0.0) { "moment must not be negative, was: $moment" }
    require(leverArm > 0.0) { "leverArm must be positive, was: $leverArm" }
    return moment / (2.0 * leverArm)
}

// ------------------------------------------------------------------ the cap, as a fixed point

/**
 * **The arm cap recomputed on the REALISED end condition** — the fixed point
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`r = (c(k_far·r/EI)·n·EI/k_target)^(1/3)`.
 *
 * `C-0029` evaluated `(c n EI/k)^(1/3)` at an **asserted** `c`; because `ρ` carries the arm length,
 * `c` is not a constant of the joint and the cap has to be solved rather than evaluated. It
 * reduces to `C-0029`'s [hingeArmCeiling] exactly at both ends — `9.767 nm` at `k_far = 0` and
 * `15.50 nm` as `k_far → ∞`.
 *
 * Bisects on `r³ − c(ρ(r))·n·EI/k`, which is strictly increasing, and exits on the bracket width.
 */
fun anchoredArmCeiling(
    farStiffness: Double,
    count: Int = 45,
    bendingRigidity: Double = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
    targetStiffness: Double = 100.0 / 3.0
): Double {
    require(farStiffness >= 0.0) { "farStiffness must not be negative, was: $farStiffness" }
    require(count > 0) { "count must be positive, was: $count" }
    require(bendingRigidity > 0.0) {
        "bendingRigidity must be positive, was: $bendingRigidity"
    }
    require(targetStiffness > 0.0) { "targetStiffness must be positive, was: $targetStiffness" }
    val scale = count * bendingRigidity / targetStiffness
    fun residual(arm: Double): Double =
        arm * arm * arm -
                guidedArmFactor(armRestraintParameter(farStiffness, arm, bendingRigidity)) * scale
    var low = Math.cbrt(3.0 * scale) * 0.999
    var high = Math.cbrt(12.0 * scale) * 1.001
    repeat(300) {
        val middle = 0.5 * (low + high)
        if (residual(middle) < 0.0) low = middle else high = middle
        if (high - low <= 1.0e-14 * high) return 0.5 * (low + high)
    }
    return 0.5 * (low + high)
}

/**
 * The arm length in nm at which [count] **two-spring** arms present [targetStiffness] — the same
 * placement condition solved on the boundary-value problem instead of on `C-0023`'s series
 * composition.
 *
 * In this reading the hinge is *one of the two springs* rather than a separate series term, so the
 * placement length and the cap are the **same equation**: `r³ = c(ρ_n(r), ρ_f(r))·n·EI/k_target`.
 * It is a **small-deflection** reading and [anchoredArmForStiffness] is the large-rotation one; the
 * two bracket the arm and both are reported.
 */
fun twoSpringArmForStiffness(
    hingeStiffness: Double,
    hingeCount: Int,
    farStiffness: Double,
    bendingRigidity: Double = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
    count: Int = 45,
    targetStiffness: Double = 100.0 / 3.0,
    maximumArm: Double = 200.0
): Double {
    require(hingeStiffness > 0.0) { "hingeStiffness must be positive, was: $hingeStiffness" }
    require(hingeCount > 0) { "hingeCount must be positive, was: $hingeCount" }
    require(farStiffness >= 0.0) { "farStiffness must not be negative, was: $farStiffness" }
    require(count > 0) { "count must be positive, was: $count" }
    require(targetStiffness > 0.0) { "targetStiffness must be positive, was: $targetStiffness" }
    fun assembled(arm: Double): Double =
        count * TwoSpringArm(
            bendingRigidity, arm, hingeCount * hingeStiffness, farStiffness
        ).stiffness
    var low = 1.0e-3
    var high = maximumArm
    require(assembled(high) < targetStiffness) {
        "no arm as long as $maximumArm nm reaches a stiffness as low as $targetStiffness"
    }
    repeat(300) {
        val middle = 0.5 * (low + high)
        if (assembled(middle) > targetStiffness) low = middle else high = middle
        if (high - low <= 1.0e-13 * high) return 0.5 * (low + high)
    }
    return 0.5 * (low + high)
}

/**
 * The arm length in nm at which [count] hinge-arm flexures present [targetStiffness] as a
 * **secant** at [workingDisplacement], with the end-condition factor **re-evaluated at every
 * candidate arm** from the realised anchorage rather than asserted.
 *
 * `C-0029`'s [RotatingHingeArm] — exact rotation, `δ = r sin θ` — is re-used unchanged as the
 * element; the only change is that `armFactor` is now a function of the arm rather than a constant.
 * Exits on the bracket width.
 */
fun anchoredArmForStiffness(
    hingeStiffness: Double,
    hingeCount: Int,
    farStiffness: Double,
    bendingRigidity: Double = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
    count: Int = 45,
    targetStiffness: Double = 100.0 / 3.0,
    workingDisplacement: Double = 3.0,
    exact: Boolean = true,
    maximumArm: Double = 200.0
): Double {
    require(hingeStiffness > 0.0) { "hingeStiffness must be positive, was: $hingeStiffness" }
    require(hingeCount > 0) { "hingeCount must be positive, was: $hingeCount" }
    require(farStiffness >= 0.0) { "farStiffness must not be negative, was: $farStiffness" }
    require(count > 0) { "count must be positive, was: $count" }
    require(targetStiffness > 0.0) { "targetStiffness must be positive, was: $targetStiffness" }
    require(workingDisplacement > 0.0) {
        "workingDisplacement must be positive, was: $workingDisplacement"
    }
    fun assembled(arm: Double): Double {
        val factor = guidedArmFactor(
            armRestraintParameter(farStiffness, arm, bendingRigidity)
        )
        val element = RotatingHingeArm(hingeStiffness, arm, bendingRigidity, hingeCount, factor)
        return count * (
                if (exact) element.secantStiffness(workingDisplacement)
                else element.smallRotationStiffness
                )
    }
    var low = 1.001 * workingDisplacement
    var high = maximumArm
    require(assembled(high) < targetStiffness) {
        "no arm as long as $maximumArm nm reaches a stiffness as low as $targetStiffness"
    }
    require(assembled(low) > targetStiffness) {
        "even the shortest admissible arm is softer than $targetStiffness"
    }
    repeat(300) {
        val middle = 0.5 * (low + high)
        if (assembled(middle) > targetStiffness) low = middle else high = middle
        if (high - low <= 1.0e-13 * high) return 0.5 * (low + high)
    }
    return 0.5 * (low + high)
}
