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
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Task `T-40` — the **base** of `C-0025`'s normal duplex standoff, modelled instead of assumed.
 *
 * ## What `C-0025` left standing on nothing
 *
 * `C-0025` found exactly one joint that supports a transverse flexure and still lets its ends
 * draw in: a duplex standing **normal** to the single-layer sheet. It gave that standoff three
 * constants,
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`k_θ = EI/ℓ`, &nbsp; `k_a = 3EI/ℓ³`, &nbsp; `k_⊥ = S/ℓ`,
 *
 * and named their common assumption as its own second open question: **all three are the
 * constants of a cantilever BUILT IN at its base**, and nothing in a Rothemund sheet is a
 * built-in support. Every one of them is really a **series** with whatever holds the base:
 *
 * - [standoffHeadRotationalStiffness] `= (EI/ℓ)·ρ_b/(ρ_b + 1)`,
 * - [standoffSwayStiffness] `= (3EI/ℓ³)·ρ_b/(ρ_b + 3)`,
 * - [seriesStiffness] `(S/ℓ, k_z_base)`,
 *
 * with `ρ_b = k_θ_base ℓ/EI` ([baseRestraintParameter]). At `C-0009`'s crossover constant on a
 * 3–10 nm standoff that parameter is **0.18–0.59**, so a single crossover delivers between an
 * eighth and a third of the clamp `C-0025` assumed.
 *
 * ## The sway IS the draw-in, and that is the whole of the buckling result
 *
 * The standoff bends in the plane containing the flexure's axis and the sheet normal. Its head's
 * translation in that plane has two names in this programme — the column's **sway** and the
 * flexure's **draw-in** — and they are one coordinate. So:
 *
 * > **The head cannot be held against sway without being held against draw-in, and holding it
 * > against draw-in is exactly `C-0023`'s *ends held axially* reading, whose 91.13 pN/nm tangent
 * > is what the whole of `T-30` was spent escaping.** The buckling bracket available to this
 * > design runs from a free head to a guided one and no further.
 *
 * [swayColumnWavenumber] solves that column with an elastic spring at **both** ends,
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;**`sin u·(u² − ρ_b ρ_h) − cos u·(ρ_b + ρ_h)·u = 0`**, &nbsp; `P_c = u²EI/ℓ²`,
 *
 * whose four corners are the four textbook effective-length factors — and one of them is not a
 * strut at all: a **pinned base with a free head has `P_c = 0` exactly**. `C-0025`'s 8.87 pN and
 * 35.5 pN are the two corners with `ρ_b = ∞`, and they are reproduced here as gate-5 tests.
 */

// ---------------------------------------------------------------- the series reductions

/**
 * The dimensionless base restraint `ρ_b = k_θ_base ℓ/EI` of a standoff of rigidity
 * [bendingRigidity] and length [length] on a base of rotational stiffness [rotationalStiffness].
 *
 * The same construction as `C-0025`'s [endRestraintParameter], one level down — and, as there,
 * it carries the **length**, so the same base is nearer a pin under a long standoff and nearer a
 * clamp under a short one.
 */
fun baseRestraintParameter(
    rotationalStiffness: Double,
    bendingRigidity: Double,
    length: Double
): Double = endRestraintParameter(rotationalStiffness, bendingRigidity, length)

/**
 * Two springs in series, in `pN/nm` — infinite when both are, and exactly zero when either is.
 *
 * Written out rather than inlined because it is the statement `C-0025` omitted three times: a
 * joint's stiffness is never its own member's stiffness, it is that member in series with its
 * ground.
 */
fun seriesStiffness(first: Double, second: Double): Double {
    require(first >= 0.0) { "first must not be negative, was: $first" }
    require(second >= 0.0) { "second must not be negative, was: $second" }
    if (first == 0.0 || second == 0.0) return 0.0
    if (first.isInfinite()) return second
    if (second.isInfinite()) return first
    return 1.0 / (1.0 / first + 1.0 / second)
}

/**
 * The rotational stiffness in `pN·nm/rad` a standoff of rigidity [bendingRigidity] and [length]
 * presents to the flexure's end, when its own base carries restraint [baseRestraint]:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`k_θ_head = (EI/ℓ)·ρ_b/(ρ_b + 1)`.
 *
 * From `θ_head = Mℓ/EI + M/k_θ_base` — the cantilever's own tip rotation in series with the base
 * spring's. `C-0025`'s `EI/ℓ` is the `ρ_b → ∞` limit and is asserted as such.
 */
fun standoffHeadRotationalStiffness(
    bendingRigidity: Double,
    length: Double,
    baseRestraint: Double
): Double {
    require(bendingRigidity > 0.0) { "bendingRigidity must be positive, was: $bendingRigidity" }
    require(length > 0.0) { "length must be positive, was: $length" }
    require(baseRestraint >= 0.0) { "baseRestraint must not be negative, was: $baseRestraint" }
    val cantilever = bendingRigidity / length
    if (baseRestraint.isInfinite()) return cantilever
    return cantilever * baseRestraint / (baseRestraint + 1.0)
}

/**
 * The **sway** stiffness in `pN/nm` of the same standoff — the flexure's draw-in release, and the
 * column's buckling coordinate, which are the same thing:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`k_sway = (3EI/ℓ³)·ρ_b/(ρ_b + 3)`.
 *
 * From `δ_head = Fℓ³/(3EI) + Fℓ²/k_θ_base`. `C-0025`'s `3EI/ℓ³` is the `ρ_b → ∞` limit; a
 * **pinned** base gives exactly zero, which is the design's ideal draw-in release and its worst
 * possible strut in the same number.
 */
fun standoffSwayStiffness(
    bendingRigidity: Double,
    length: Double,
    baseRestraint: Double
): Double {
    require(bendingRigidity > 0.0) { "bendingRigidity must be positive, was: $bendingRigidity" }
    require(length > 0.0) { "length must be positive, was: $length" }
    require(baseRestraint >= 0.0) { "baseRestraint must not be negative, was: $baseRestraint" }
    val cantilever = 3.0 * bendingRigidity / (length * length * length)
    if (baseRestraint.isInfinite()) return cantilever
    return cantilever * baseRestraint / (baseRestraint + 3.0)
}

/**
 * The rotational restraint the **flexure** presents back to the standoff's head, made
 * dimensionless in the standoff's own terms: `ρ_h = m·ℓ/L`.
 *
 * A simply supported beam of span `L` and the same rigidity rotates `ML/(2EI)` under a
 * **symmetric** pair of end moments and `ML/(6EI)` under an **antisymmetric** one, so its end
 * rotational stiffness is `2EI/L` or `6EI/L` and [multiplier] is 2 or 6. Both are *realised*
 * readings strictly inside the free-to-guided bracket, and both are reported rather than chosen.
 */
fun beamHeadRestraint(
    standoffLength: Double,
    span: Double,
    multiplier: Double = 2.0
): Double {
    require(standoffLength > 0.0) { "standoffLength must be positive, was: $standoffLength" }
    require(span > 0.0) { "span must be positive, was: $span" }
    require(multiplier > 0.0) { "multiplier must be positive, was: $multiplier" }
    return multiplier * standoffLength / span
}

// ---------------------------------------------------------------- the sway column

/**
 * The buckling determinant of a **sway** column (head free to translate) carrying rotational
 * springs [baseRestraint] and [headRestraint] at its two ends, at dimensionless load [u]:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`D(u) = sin u·(u² − ρ_b ρ_h) − cos u·(ρ_b + ρ_h)·u`.
 *
 * It is **symmetric** in the two springs — Maxwell-Betti for a uniform strut — which is asserted
 * as a gate-3 test rather than built in. An infinite spring is handled by dividing through by it,
 * which is why the two textbook one-spring equations `u tan u = ρ` and `u cot u = −ρ` are limits
 * of this one function and not separate formulae.
 */
fun swayColumnDeterminant(
    u: Double,
    baseRestraint: Double,
    headRestraint: Double
): Double {
    val bothInfinite = baseRestraint.isInfinite() && headRestraint.isInfinite()
    if (bothInfinite) return sin(u)
    if (baseRestraint.isInfinite()) return -(headRestraint * sin(u) + u * cos(u))
    if (headRestraint.isInfinite()) return -(baseRestraint * sin(u) + u * cos(u))
    return sin(u) * (u * u - baseRestraint * headRestraint) -
            cos(u) * (baseRestraint + headRestraint) * u
}

/**
 * The first positive root `u` in `(0, π]` of [swayColumnDeterminant] — the dimensionless critical
 * load `u = ℓ√(P_c/EI)` of a sway column with elastic rotational springs at both ends.
 *
 * | `ρ_b` | `ρ_h` | `u` | `K` |
 * |---|---|---|---|
 * | ∞ | 0 | `π/2` | 2 |
 * | ∞ | ∞ | `π` | 1 |
 * | 0 | ∞ | `π/2` | 2 |
 * | **0** | **0** | **0** | ∞ — **a mechanism, not a strut** |
 *
 * Scans then bisects, and exits on the **bracket width** rather than on a residual (`CLAUDE.md`):
 * the determinant spans many decades in the two restraints and no residual tolerance is reachable
 * across them.
 */
fun swayColumnWavenumber(
    baseRestraint: Double,
    headRestraint: Double,
    scanSteps: Int = 512
): Double {
    require(baseRestraint >= 0.0) { "baseRestraint must not be negative, was: $baseRestraint" }
    require(headRestraint >= 0.0) { "headRestraint must not be negative, was: $headRestraint" }
    require(scanSteps >= 16) { "scanSteps must be at least 16, was: $scanSteps" }
    // a pin at each end of a swaying column is a four-bar linkage: it carries no axial load at all
    if (baseRestraint == 0.0 && headRestraint == 0.0) return 0.0
    if (baseRestraint.isInfinite() && headRestraint.isInfinite()) return PI
    var low = 1.0e-12
    var high = PI
    val step = (high - low) / scanSteps
    val atLow = swayColumnDeterminant(low, baseRestraint, headRestraint)
    var scan = low
    for (i in 1..scanSteps) {
        val next = low + i * step
        if (swayColumnDeterminant(next, baseRestraint, headRestraint) * atLow <= 0.0) {
            low = scan
            high = next
            break
        }
        scan = next
    }
    val sign = if (swayColumnDeterminant(low, baseRestraint, headRestraint) < 0.0) 1.0 else -1.0
    repeat(400) {
        val middle = 0.5 * (low + high)
        if (sign * swayColumnDeterminant(middle, baseRestraint, headRestraint) < 0.0) low = middle
        else high = middle
        if (high - low <= 1.0e-15 * high) return 0.5 * (low + high)
    }
    return 0.5 * (low + high)
}

/**
 * The critical axial load in pN of a standoff of rigidity [bendingRigidity] and [length] whose
 * base carries [baseRestraint] and whose head carries [headRestraint]: `P_c = u² EI/ℓ²`.
 *
 * `C-0025`'s 8.87 pN (*"pinned head"*) and 35.5 pN (*"guided"*) at `ℓ = 8 nm` are the two
 * `ρ_b = ∞` corners and are reproduced here to machine precision, against `C-0014`'s own
 * [eulerBucklingLoad] as well as against the published numbers.
 */
fun standoffBucklingLoad(
    bendingRigidity: Double,
    length: Double,
    baseRestraint: Double,
    headRestraint: Double,
    scanSteps: Int = 512
): Double {
    require(bendingRigidity > 0.0) { "bendingRigidity must be positive, was: $bendingRigidity" }
    require(length > 0.0) { "length must be positive, was: $length" }
    val u = swayColumnWavenumber(baseRestraint, headRestraint, scanSteps)
    return u * u * bendingRigidity / (length * length)
}

// ---------------------------------------------------------------- the base motifs

/**
 * One way of grounding a normal standoff on a single-layer Rothemund sheet, as the two stiffnesses
 * and one dead band a designer chooses by picking a motif.
 *
 * @property name the motif.
 * @property rotationalStiffness `k_θ_base` in `pN·nm/rad` about the standoff's bending axis —
 *   what decides how much of `C-0025`'s `EI/ℓ` and `3EI/ℓ³` survive, and the whole of the
 *   buckling load.
 * @property axialStiffness `k_z_base` in `pN/nm` **normal to the sheet** — in series with the
 *   standoff's own `S/ℓ`, and therefore part of the flexure's transverse support.
 * @property transverseDeadBand in nm, zero for every covalent motif.
 * @property buildable whether the motif exists at all as a 90° junction.
 * @property provenance where each number comes from, and whether it is measured, fitted or derived.
 */
data class StandoffBase(
    val name: String,
    val rotationalStiffness: Double,
    val axialStiffness: Double,
    val transverseDeadBand: Double = 0.0,
    val buildable: Boolean = true,
    val provenance: String = ""
) {

    init {
        require(rotationalStiffness >= 0.0) {
            "rotationalStiffness must not be negative, was: $rotationalStiffness"
        }
        require(axialStiffness >= 0.0) {
            "axialStiffness must not be negative, was: $axialStiffness"
        }
        require(transverseDeadBand >= 0.0) {
            "transverseDeadBand must not be negative, was: $transverseDeadBand"
        }
    }

    companion object {

        /** **`B0`** — `C-0025`'s own assumption, carried so that it can be reproduced and named. */
        fun idealClamp(): StandoffBase = StandoffBase(
            name = "ideal clamp",
            rotationalStiffness = Double.POSITIVE_INFINITY,
            axialStiffness = Double.POSITIVE_INFINITY,
            provenance = "IDEALISATION — C-0025's assumed base, the rho_b -> infinity limit"
        )

        /**
         * **`B1`/`B2`/`B3`** — [count] antiparallel crossovers tying the standoff's base to that
         * many *adjacent* sheet duplexes, at the SAXS-measured interhelical distance.
         *
         * The bonds' own `k_θ` is not what makes a multi-crossover base stiff; the **couple** is,
         * `k_s Σ d_i²` over the offsets from the base's centroid. And a couple has an **axis**:
         * it restrains rotation about the line perpendicular to the row of crossovers and does
         * **nothing** about the line along it. So the same two crossovers are worth 261.2 or
         * 27.06 pN·nm/rad depending only on which way the pair is laid relative to the flexure —
         * a factor of 9.65 for free, and it is [favourableOrientation].
         */
        fun crossovers(
            count: Int,
            favourableOrientation: Boolean = true,
            alpha: Double = 1.0,
            inPlaneMultiplier: Double = 1.0,
            interhelical: Double = Gen1Tile.INTERHELICAL_SHEET
        ): StandoffBase {
            require(count >= 1) { "count must be at least one, was: $count" }
            require(interhelical > 0.0) { "interhelical must be positive, was: $interhelical" }
            val slide = Gen1Tile.crossoverInPlaneStiffness(alpha) * inPlaneMultiplier
            val offsets =
                (0 until count).map { it * interhelical - (count - 1) * interhelical / 2.0 }
            val couple = if (favourableOrientation) slide * offsets.sumOf { it * it } else 0.0
            val orientation =
                if (count == 1) "" else if (favourableOrientation) ", favourable orientation"
                else ", unfavourable orientation"
            return StandoffBase(
                name = "$count antiparallel crossover${if (count > 1) "s" else ""}$orientation",
                rotationalStiffness = count * Gen1Tile.crossoverHingeStiffness(alpha) + couple,
                axialStiffness = count * slide,
                provenance = "k_theta CITED+FITTED (Chen et al. 2014, via C-0009); k_s DERIVED " +
                        "(C-0020), NOT measured; d = 2.69 nm CITED+MEASURED by SAXS " +
                        "(Fischer et al. 2016)"
            )
        }

        /**
         * **`B4`** — a nicked or scaffold continuation, and it does **not exist** as a base for a
         * normal standoff.
         *
         * `C-0025` establishes that a single nick is a clamp and a double nick is a crossover, and
         * both statements are about a duplex continuing **along its own axis**. A nick preserves
         * the helix axis: there is no backbone geometry in which a B-form duplex continues through
         * a nick at 90° to itself. The motif is carried in the catalogue with `buildable = false`
         * so that it is reported as checked rather than silently omitted.
         */
        fun nickedContinuation(): StandoffBase = StandoffBase(
            name = "nicked / scaffold continuation at 90 degrees",
            rotationalStiffness = 0.0,
            axialStiffness = 0.0,
            buildable = false,
            provenance = "STRUCTURALLY UNAVAILABLE — a nick preserves the helix axis, so a " +
                    "continuation cannot turn 90 degrees; carried to record that it was checked"
        )

        /**
         * **`B5`** — an [nucleotides]-nt poly-T junction between the sheet and the standoff's base.
         *
         * `CH-0031` one level down, and with the same outcome: it is the softest base available and
         * therefore the best draw-in release in the catalogue, and it is isotropic, so it hands the
         * standoff's own support path the same softness and a dead band of its full contour.
         */
        fun polyTJunction(
            nucleotides: Int,
            kuhnLength: Double = SsDnaTether.KUHN_LENGTH_ZERO_FORCE,
            contourPerNucleotide: Double = SsDnaTether.CONTOUR_PER_NUCLEOTIDE
        ): StandoffBase {
            val hinge = FlexureEndJoint.singleStrandedHinge(
                nucleotides, kuhnLength, contourPerNucleotide
            )
            return StandoffBase(
                name = "$nucleotides nt poly-T junction",
                rotationalStiffness = hinge.rotationalStiffness,
                axialStiffness = hinge.axialStiffness,
                transverseDeadBand = hinge.transverseDeadBand,
                provenance = hinge.provenance
            )
        }
    }
}

/**
 * `C-0025`'s **`J5`** normal duplex standoff of length [length], with its base modelled as [base]
 * instead of assumed rigid.
 *
 * The returned object is `C-0025`'s own [FlexureEndJoint], so the whole downstream pipeline —
 * [PartiallyRestrainedFlexure], [flexureSpanForJoint], [bondedLengthForTension] — runs unchanged
 * and every number here is comparable with the filed one. `StandoffBase.idealClamp()` reproduces
 * `C-0025`'s `J5` **identically**, which is a gate-2 test.
 */
fun basedNormalStandoff(
    length: Double,
    base: StandoffBase,
    bendingRigidity: Double = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
    stretchModulus: Double = Gen1Tile.DUPLEX_STRETCH_MODULUS
): FlexureEndJoint {
    require(length > 0.0) { "length must be positive, was: $length" }
    val restraint = baseRestraintParameter(base.rotationalStiffness, bendingRigidity, length)
    return FlexureEndJoint(
        name = "normal duplex standoff, ${"%.1f".format(length)} nm on ${base.name}",
        rotationalStiffness =
            standoffHeadRotationalStiffness(bendingRigidity, length, restraint),
        axialStiffness = standoffSwayStiffness(bendingRigidity, length, restraint),
        transverseStiffness = seriesStiffness(stretchModulus / length, base.axialStiffness),
        transverseDeadBand = base.transverseDeadBand,
        standoffLength = length,
        provenance = "EI CITED as a CanDo MODEL INPUT (Kim et al. 2012), S CITED+MEASURED " +
                "(Wang et al. 1997); base: ${base.provenance}"
    )
}

// ---------------------------------------------------------------- the two inversions

/**
 * The stroke in nm at which the flexure's own end shear reaches [criticalLoad] — the stroke at
 * which the standoff **buckles**.
 *
 * This is the honest form of a buckling margin, because the duty is not a constant: the flexure
 * strain-stiffens, so its end shear grows faster than the mandate secant does. Returns `0` if the
 * standoff is already past its critical load at rest and `+∞` if it never reaches it inside
 * [maximumStroke]. Exits on the bracket width.
 */
fun bucklingStroke(
    flexure: PartiallyRestrainedFlexure,
    criticalLoad: Double,
    maximumStroke: Double = 60.0
): Double {
    require(criticalLoad >= 0.0) { "criticalLoad must not be negative, was: $criticalLoad" }
    require(maximumStroke > 0.0) { "maximumStroke must be positive, was: $maximumStroke" }
    if (criticalLoad <= 0.0) return 0.0
    if (flexure.endShear(maximumStroke) < criticalLoad) return Double.POSITIVE_INFINITY
    var low = 0.0
    var high = maximumStroke
    repeat(400) {
        val middle = 0.5 * (low + high)
        if (flexure.endShear(middle) < criticalLoad) low = middle else high = middle
        if (high - low <= 1.0e-15 * high) return 0.5 * (low + high)
    }
    return 0.5 * (low + high)
}

/**
 * The base rotational stiffness in `pN·nm/rad` a standoff of [length] needs in order that its
 * critical load reach its own compression duty at [stroke] — `P6`, read backwards.
 *
 * The whole design is re-solved at every candidate, because the base moves the span, the span
 * moves the end shear, and the end shear is the duty. Returns `+∞` when no base suffices, which
 * is the honest failure and is not clipped. Bisects in the **logarithm**, since the answer spans
 * decades (`CLAUDE.md`).
 */
fun baseRotationalStiffnessThreshold(
    length: Double,
    stroke: Double,
    headRestraint: Double = 0.0,
    pathCount: Int = 45,
    targetStiffness: Double = 100.0 / 3.0,
    workingDisplacement: Double = 3.0,
    axialBase: Double = Gen1Tile.crossoverInPlaneStiffness(),
    bendingRigidity: Double = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
    stretchModulus: Double = Gen1Tile.DUPLEX_STRETCH_MODULUS
): Double {
    require(length > 0.0) { "length must be positive, was: $length" }
    require(stroke > 0.0) { "stroke must be positive, was: $stroke" }
    fun margin(rotational: Double): Double {
        val base = StandoffBase("probe", rotational, axialBase)
        val joint = basedNormalStandoff(length, base, bendingRigidity, stretchModulus)
        val span = flexureSpanForJoint(
            bendingRigidity, joint, pathCount, targetStiffness, workingDisplacement, stretchModulus
        )
        val flexure = PartiallyRestrainedFlexure(bendingRigidity, span, joint, stretchModulus)
        val restraint = baseRestraintParameter(rotational, bendingRigidity, length)
        val critical = standoffBucklingLoad(bendingRigidity, length, restraint, headRestraint)
        return critical - flexure.endShear(stroke)
    }
    var low = 1.0e-3
    var high = 1.0e9
    if (margin(high) < 0.0) return Double.POSITIVE_INFINITY
    if (margin(low) > 0.0) return low
    repeat(400) {
        val middle = kotlin.math.sqrt(low * high)
        if (margin(middle) < 0.0) low = middle else high = middle
        if (high - low <= 1.0e-12 * high) return 0.5 * (low + high)
    }
    return 0.5 * (low + high)
}

// ---------------------------------------------------------------- the off-diagonal, bounded

/**
 * The correlation `C_12/√(C_11 C_22)` of the standoff's own **2 × 2** tip compliance — how much
 * of `C-0025`'s open question 1 is left in the two independent springs this file, like `C-0025`,
 * uses.
 *
 * A cantilever's tip translation and tip rotation are not independent: `δ = Fℓ³/3EI + Mℓ²/2EI`
 * and `θ = Fℓ²/2EI + Mℓ/EI`, plus the base spring's `(Fℓ + M)/k_θ_base` in both. At a **clamped**
 * base the correlation is `√3/2 = 0.866` exactly and the two diagonal readings — the other load
 * zero, which is `C-0025`'s and this file's, against the other *displacement* zero — differ by
 * **exactly 4** in both entries. A compliant base raises the correlation, because the base spring
 * contributes to `C_11`, `C_12` and `C_22` in perfect proportion.
 *
 * The **sign** is argued rather than solved here: at a flexure end the joint carries the beam's
 * end moment and the beam's inward tension together, and both rotate the standoff's head the same
 * way, so the coupled joint is **softer** than the two independent springs. That makes the
 * compliance verdict conservative and the buckling verdict **not** conservative, and it is queued
 * as its own task rather than resolved here.
 */
fun standoffTipCompliance(
    bendingRigidity: Double,
    length: Double,
    baseRotationalStiffness: Double
): Triple<Double, Double, Double> {
    require(bendingRigidity > 0.0) { "bendingRigidity must be positive, was: $bendingRigidity" }
    require(length > 0.0) { "length must be positive, was: $length" }
    require(baseRotationalStiffness >= 0.0) {
        "baseRotationalStiffness must not be negative, was: $baseRotationalStiffness"
    }
    val base = if (baseRotationalStiffness.isInfinite()) 0.0 else 1.0 / baseRotationalStiffness
    val c11 = length * length * length / (3.0 * bendingRigidity) + length * length * base
    val c12 = length * length / (2.0 * bendingRigidity) + length * base
    val c22 = length / bendingRigidity + base
    return Triple(c11, c12, c22)
}

/**
 * The factor by which the **other-displacement-fixed** reading of a standoff's two springs exceeds
 * the **other-load-zero** reading `C-0025` and this file use — `K_ii C_ii`, which is
 * `1/(1 − r²)` with `r` the correlation of [standoffTipCompliance] and is therefore the **same
 * number for both springs**.
 *
 * Exactly **4** at a clamped base; **9.70** at a single-crossover base at 8 nm. It bounds the
 * magnitude of the term neither claim models, not its sign.
 */
fun offDiagonalFactor(
    bendingRigidity: Double,
    length: Double,
    baseRotationalStiffness: Double
): Double {
    val (c11, c12, c22) = standoffTipCompliance(bendingRigidity, length, baseRotationalStiffness)
    val determinant = c11 * c22 - c12 * c12
    require(determinant > 0.0) { "the compliance matrix is not positive definite" }
    return c11 * c22 / determinant
}

/** The correlation coefficient of [standoffTipCompliance] — `√3/2` exactly at a clamped base. */
fun offDiagonalCorrelation(
    bendingRigidity: Double,
    length: Double,
    baseRotationalStiffness: Double
): Double {
    val (c11, c12, c22) = standoffTipCompliance(bendingRigidity, length, baseRotationalStiffness)
    return c12 / kotlin.math.sqrt(c11 * c22)
}

/** Whether [value] is within [tolerance] of [target], used by the study's own reporting. */
internal fun near(value: Double, target: Double, tolerance: Double = 1.0e-9): Boolean =
    abs(value - target) <= tolerance * kotlin.math.max(1.0, abs(target))
