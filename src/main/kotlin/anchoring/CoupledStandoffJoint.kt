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
import kotlin.math.max
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Task `T-65` — `C-0025`'s and `C-0028`'s two independent joint springs, solved as the one
 * **2 × 2 tip flexibility** they are the diagonal of.
 *
 * ## What both upstream claims did, and why it is not a small thing
 *
 * `C-0025` gives the normal duplex standoff a rotational spring `k_θ = EI/ℓ` and an axial (sway)
 * spring `k_a = 3EI/ℓ³`; `C-0028` puts each in series with a base spring and uses them, still, as
 * two **independent** scalars. They are the two diagonal entries of
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`[u; φ] = C [H; M]`, &nbsp;
 * `C = [[ℓ³/3EI + ℓ²/k_θb, ℓ²/2EI + ℓ/k_θb], [·, ℓ/EI + 1/k_θb]]`,
 *
 * read with the **other load zero** — and `C-0028` bounds the off-diagonal it drops (correlation
 * `√3/2` at a clamped base, the other-DOF-fixed reading larger by exactly 4) without solving it.
 *
 * ## The term that was dropped is larger than the term that was kept
 *
 * At a flexure's end the joint carries the beam's end **moment** and the beam's inward **tension**
 * together, and both tilt the standoff's head the same way — inward. So the head's translation
 * under the beam's own end moment, `C12·M`, is a draw-in the standoff **supplies for free**. And
 * because `M` is first order in the midspan deflection while the arc-length demand is second
 * order, that supply is
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;**`Φ δ`**, &nbsp; `Φ ≡ 24 EI C12/(L² A)`, &nbsp; `A ≡ 1 + 8 EI C22/L`,
 *
 * against a demand `e(δ) ≈ δ²/L`. At `C-0028`'s own design point the supply is **2.5×** the
 * demand at the 3 nm placement stroke, so the beam is in axial **compression**, not tension, over
 * most of §3's range: the membrane term that `C-0023` found turns the beam into a cable **changes
 * sign**.
 *
 * ## Which means the sign of the whole effect is a mounting choice
 *
 * `Φδ` is odd in `δ` and `e(δ)` is even, so the coupled law is **not odd**. Sag the flexure
 * **toward** the plane its standoff bases stand on — the sense in which a sagging beam pulls its
 * supports together, as a cable does — and the heads tilt inward and relieve it
 * ([FlexureOrientation.FAVOURABLE]); deflect it the other way and the same moments splay the heads
 * outward and **load** it ([FlexureOrientation.ADVERSE]). Which one the device sees is decided by
 * **which body carries the standoffs**, which is free to a builder and was not a design variable
 * before — and the favourable one costs a **clearance**, because the midspan then descends toward
 * a body that is ℓ away.
 */

// ---------------------------------------------------------------- the 2 x 2 tip flexibility

/**
 * The **2 × 2 tip flexibility** of a standoff — a cantilever of rigidity `EI` and length `ℓ` on a
 * base rotational spring `k_θb` — in the two in-plane degrees of freedom its head shares with the
 * flexure's end.
 *
 * The coordinates are `(u, φ)`: `u` the head's translation along the beam's axis counted positive
 * **inward**, and `φ = du/dz` the head's tangent rotation counted positive in the sense a positive
 * `u` produces. They are work-conjugate to `(H, M)`, an inward force and the moment the beam
 * applies to the head, which is why every entry below is positive and why the matrix is symmetric.
 *
 * @property translationUnderForce `C11` in `nm/pN`; its reciprocal is `C-0028`'s sway stiffness.
 * @property rotationUnderForce `C21` in `rad/pN`.
 * @property translationUnderMoment `C12` in `nm/(pN·nm)`; **the entry both upstream claims drop**.
 * @property rotationUnderMoment `C22` in `rad/(pN·nm)`; its reciprocal is `C-0028`'s head
 *   rotational stiffness.
 */
data class StandoffTipFlexibility(
    val translationUnderForce: Double,
    val rotationUnderForce: Double,
    val translationUnderMoment: Double,
    val rotationUnderMoment: Double
) {

    init {
        require(translationUnderForce > 0.0) {
            "translationUnderForce must be positive, was: $translationUnderForce"
        }
        require(rotationUnderMoment > 0.0) {
            "rotationUnderMoment must be positive, was: $rotationUnderMoment"
        }
    }

    /** `C11 C22 − C12 C21`, strictly positive for any real standoff. */
    val determinant: Double
        get() = translationUnderForce * rotationUnderMoment -
                translationUnderMoment * rotationUnderForce

    /** `C12/√(C11 C22)` — exactly `√3/2` at a clamped base, rising toward 1 as the base softens. */
    val correlation: Double
        get() = translationUnderMoment / sqrt(translationUnderForce * rotationUnderMoment)

    /**
     * `1/(1 − r²)` — the factor by which the **other-displacement-fixed** reading of either spring
     * exceeds the **other-load-zero** reading `C-0025` and `C-0028` use. The same number for both
     * springs, which is why `C-0028` could bound it without choosing one.
     */
    val otherDisplacementFixedFactor: Double
        get() = translationUnderForce * rotationUnderMoment / determinant

    /** `1/C11` — `C-0028`'s `k_sway`, the other load zero. */
    val swayStiffness: Double get() = 1.0 / translationUnderForce

    /** `1/C22` — `C-0028`'s `k_θ_head`, the other load zero. */
    val headRotationalStiffness: Double get() = 1.0 / rotationUnderMoment

    /** `C22/det` — the sway stiffness with the head's **rotation** held, exactly 4× stiffer at a clamp. */
    val swayStiffnessRotationFixed: Double get() = rotationUnderMoment / determinant

    /** `C11/det` — the rotational stiffness with the head's **translation** held. */
    val headRotationalStiffnessTranslationFixed: Double get() = translationUnderForce / determinant

    /**
     * The same joint with the off-diagonal removed — **`C-0025`'s and `C-0028`'s reading**, named
     * rather than assumed, so that every comparison in `T-65` is against a model that reproduces
     * them identically rather than approximately.
     */
    fun decoupled(): StandoffTipFlexibility = copy(
        rotationUnderForce = 0.0,
        translationUnderMoment = 0.0
    )
}

/**
 * The closed-form [StandoffTipFlexibility] of a standoff of rigidity [bendingRigidity] and
 * [length] on a base rotational spring [baseRotationalStiffness].
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`u = Hℓ³/3EI + Mℓ²/2EI + (Hℓ + M)ℓ/k_θb`,
 * &nbsp;&nbsp;`φ = Hℓ²/2EI + Mℓ/EI + (Hℓ + M)/k_θb`.
 *
 * The base spring contributes `ℓ²`, `ℓ` and `1` to the three entries — in perfect proportion — so
 * a softer base **raises** the correlation, which is why `C-0028`'s crossover base sits at 0.947
 * where its clamp sits at `√3/2`.
 *
 * A **pinned** base is not covered: it is `C-0028`'s mechanism corner, where the flexibility is
 * unbounded and the standoff is not a strut at all.
 */
fun standoffTipFlexibility(
    bendingRigidity: Double,
    length: Double,
    baseRotationalStiffness: Double
): StandoffTipFlexibility {
    require(bendingRigidity > 0.0) { "bendingRigidity must be positive, was: $bendingRigidity" }
    require(length > 0.0) { "length must be positive, was: $length" }
    require(baseRotationalStiffness > 0.0) {
        "baseRotationalStiffness must be positive (a pinned base is C-0028's mechanism, not a " +
                "flexibility), was: $baseRotationalStiffness"
    }
    val base = if (baseRotationalStiffness.isInfinite()) 0.0 else 1.0 / baseRotationalStiffness
    val offDiagonal = length * length / (2.0 * bendingRigidity) + length * base
    return StandoffTipFlexibility(
        translationUnderForce =
            length * length * length / (3.0 * bendingRigidity) + length * length * base,
        rotationUnderForce = offDiagonal,
        translationUnderMoment = offDiagonal,
        rotationUnderMoment = length / bendingRigidity + base
    )
}

/**
 * The same flexibility obtained by **quadrature**, so that Maxwell-Betti can be asserted rather
 * than constructed.
 *
 * The bending moment in the standoff at height `x` under a tip force `H` and tip moment `M` is
 * `m(x) = H(ℓ − x) + M`. From it,
 *
 * - `φ(x) = m(0)/k_θb + ∫₀ˣ m/EI` — **one** integration;
 * - `u(x) = ∫₀ˣ φ` — **two**.
 *
 * So `C12` (the tip *translation* under a unit tip *moment*) is a double integral of a constant
 * integrand and `C21` (the tip *rotation* under a unit tip *force*) is a single integral of a
 * linear one. Nothing in either construction makes them equal; that they are is the reciprocal
 * theorem, and `CoupledStandoffJointTest` asserts it at nine `(ℓ, k_θb)` pairs.
 *
 * Cumulative Simpson on [steps] intervals, which must be even.
 */
fun standoffTipFlexibilityByIntegration(
    bendingRigidity: Double,
    length: Double,
    baseRotationalStiffness: Double,
    steps: Int = 1024
): StandoffTipFlexibility {
    require(bendingRigidity > 0.0) { "bendingRigidity must be positive, was: $bendingRigidity" }
    require(length > 0.0) { "length must be positive, was: $length" }
    require(baseRotationalStiffness > 0.0) {
        "baseRotationalStiffness must be positive, was: $baseRotationalStiffness"
    }
    require(steps >= 8 && steps % 2 == 0) { "steps must be even and at least 8, was: $steps" }
    val base = if (baseRotationalStiffness.isInfinite()) 0.0 else 1.0 / baseRotationalStiffness
    val h = length / steps

    fun tip(force: Double, moment: Double): Pair<Double, Double> {
        fun curvature(x: Double) = (force * (length - x) + moment) / bendingRigidity
        // phi(x) node by node, by cumulative Simpson over successive pairs of intervals
        val rotation = DoubleArray(steps + 1)
        rotation[0] = (force * length + moment) * base
        var i = 0
        while (i + 2 <= steps) {
            val x0 = i * h
            val panel = h / 3.0 *
                    (curvature(x0) + 4.0 * curvature(x0 + h) + curvature(x0 + 2.0 * h))
            // the intermediate node uses one Simpson half-panel, exact for a linear integrand
            rotation[i + 1] = rotation[i] + 0.5 * h * (curvature(x0) + curvature(x0 + h))
            rotation[i + 2] = rotation[i] + panel
            i += 2
        }
        var translation = 0.0
        i = 0
        while (i + 2 <= steps) {
            translation += h / 3.0 * (rotation[i] + 4.0 * rotation[i + 1] + rotation[i + 2])
            i += 2
        }
        return translation to rotation[steps]
    }

    val underForce = tip(1.0, 0.0)
    val underMoment = tip(0.0, 1.0)
    return StandoffTipFlexibility(
        translationUnderForce = underForce.first,
        rotationUnderForce = underForce.second,
        translationUnderMoment = underMoment.first,
        rotationUnderMoment = underMoment.second
    )
}

// ---------------------------------------------------------------- the braced column

/**
 * The buckling determinant of a **braced** column — ends held against translation — carrying equal
 * rotational springs of dimensionless strength [restraint] at both ends:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`D(u) = ρ sin(u/2) + u cos(u/2)`.
 *
 * This is the *flexure's own* stability problem, which only the coupled model needs: the decoupled
 * reading puts the beam in tension at every stroke and can never ask the question. It is a
 * different determinant from [swayColumnDeterminant] and its two limits are the other two textbook
 * effective-length factors, `K = 1` at `ρ = 0` and `K = 0.5` at `ρ → ∞`.
 */
fun bracedColumnDeterminant(u: Double, restraint: Double): Double =
    if (restraint.isInfinite()) sin(u / 2.0) else restraint * sin(u / 2.0) + u * cos(u / 2.0)

/** The first root of [bracedColumnDeterminant] in `(π, 2π]`, scanned then bisected on the bracket width. */
fun bracedColumnWavenumber(restraint: Double, scanSteps: Int = 512): Double {
    require(restraint >= 0.0) { "restraint must not be negative, was: $restraint" }
    require(scanSteps >= 16) { "scanSteps must be at least 16, was: $scanSteps" }
    if (restraint == 0.0) return PI
    if (restraint.isInfinite()) return 2.0 * PI
    var low = PI
    var high = 2.0 * PI
    val step = (high - low) / scanSteps
    val atLow = bracedColumnDeterminant(low, restraint)
    var scan = low
    for (i in 1..scanSteps) {
        val next = low + i * step
        if (bracedColumnDeterminant(next, restraint) * atLow <= 0.0) {
            low = scan
            high = next
            break
        }
        scan = next
    }
    val sign = if (bracedColumnDeterminant(low, restraint) > 0.0) 1.0 else -1.0
    repeat(400) {
        val middle = 0.5 * (low + high)
        if (sign * bracedColumnDeterminant(middle, restraint) > 0.0) low = middle else high = middle
        if (high - low <= 1.0e-15 * high) return 0.5 * (low + high)
    }
    return 0.5 * (low + high)
}

/** `P_c = u² EI/L²` for the braced column of [bracedColumnWavenumber]. */
fun bracedColumnBucklingLoad(
    bendingRigidity: Double,
    span: Double,
    restraint: Double,
    scanSteps: Int = 512
): Double {
    require(bendingRigidity > 0.0) { "bendingRigidity must be positive, was: $bendingRigidity" }
    require(span > 0.0) { "span must be positive, was: $span" }
    val u = bracedColumnWavenumber(restraint, scanSteps)
    return u * u * bendingRigidity / (span * span)
}

// ---------------------------------------------------------------- the coupled flexure

/**
 * Where the flexure's arc-length excess is charged from — `C-0023`'s **chord** geometry, which is
 * what produces the tension in its force law, or `C-0025`'s **deflected shape** `g(ρ)δ²/L`, which
 * is what measures the demand. `T-43` records that the two differ by 1.13–1.20× and that only the
 * first produces the tension; the chord is used everywhere a number is quoted and the shape is
 * carried as a sensitivity, exactly as `C-0025` does.
 */
enum class DrawInModel { CHORD, SHAPE }

/**
 * Which way the device deflects the flexure, i.e. **which body carries the standoffs** — the design
 * variable the coupled joint creates and the decoupled one cannot see.
 *
 * [FAVOURABLE] is the sense in which the beam sags **toward** the plane its standoff bases stand
 * on, so its end moments tilt the heads **inward** and supply the draw-in — and the midspan's
 * travel is then bounded by the standoff length, which is [favourableStrokeClearance]. [ADVERSE]
 * is the other one, in which the same moments splay the heads outward and the joint **adds** to
 * the demand, at unlimited clearance.
 */
enum class FlexureOrientation(val sense: Double) {
    FAVOURABLE(1.0),
    ADVERSE(-1.0)
}

/**
 * `C-0023`'s transverse duplex flexure with `C-0025`'s partially restrained ends, seated on
 * standoffs whose tips are one **2 × 2 flexibility** rather than two independent springs.
 *
 * At a signed midspan deflection `δ` the beam's own exact kinematics give
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`M = 24EIδ/L² − 8EIθ/L`, &nbsp;&nbsp; `P_b = 192EIδ/L³ − 48EIθ/L²`,
 *
 * and the joint closes them with `θ = C12 T + C22 M`, `u = C11 T + C12 M` and the axial
 * compatibility `e(δ) = T a/S + u`. Eliminating `θ` and `M`:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;**`T(δ) = (e(δ) − Φδ)/G`**, &nbsp;
 * `Φ = 24EI C12/(L²A)`, &nbsp; `G = a/S + C11 − 8EI C12²/(LA)`, &nbsp; `A = 1 + 8EI C22/L`,
 * &nbsp;&nbsp; **`R(δ) = c₀ EI δ/L³ + T(δ)·(2δ/r − 2Φ)`**, &nbsp; `c₀ = 48(A+3)/A`, `r = √(a²+δ²)`.
 *
 * Three things are worth naming in that result.
 *
 * 1. **`c₀ ≡ c(ρ)` identically.** The off-diagonal does not change the bending *coefficient* at
 *    all; it adds a term proportional to the axial force, which is why the *effective* end
 *    condition [effectiveMidspanFactor] becomes a function of the **deflection**.
 * 2. **`G < C11 + a/S`.** Against a *net* demand the coupled joint is axially **stiffer** — 2.06×
 *    at `C-0028`'s design — the opposite of the sign its bound suggested.
 * 3. **`Φδ` is odd where `e(δ)` is even.** The law is signed but no longer odd, and the two
 *    limbs differ by 1.9–3.9× at the Gen-1 design.
 *
 * Setting `C12 = 0` returns `c(ρ) = 192(ρ+2)/(ρ+8)` and `S_eff = S/(1 + 2S/(k_a L))` exactly, and
 * the whole of `C-0025`'s and `C-0028`'s pipeline with them; that is the first verification gate.
 */
class CoupledJointFlexure(
    val bendingRigidity: Double,
    val span: Double,
    val flexibility: StandoffTipFlexibility,
    val stretchModulus: Double = AnchorMaterials.DUPLEX_STRETCH_MODULUS,
    val drawInModel: DrawInModel = DrawInModel.CHORD
) : SignedCouplingElement {

    init {
        require(bendingRigidity > 0.0) { "bendingRigidity must be positive, was: $bendingRigidity" }
        require(span > 0.0) { "span must be positive, was: $span" }
        require(stretchModulus > 0.0) { "stretchModulus must be positive, was: $stretchModulus" }
    }

    private val half: Double get() = span / 2.0

    /** `ρ = k_θ_head L/EI`, on the other-load-zero reading — `C-0025`'s and `C-0028`'s own. */
    val restraint: Double =
        endRestraintParameter(flexibility.headRotationalStiffness, bendingRigidity, span)

    /** `A = 1 + 8EI C22/L`, which is `1 + 8/ρ`. */
    val rotationalSeries: Double = 1.0 + 8.0 * bendingRigidity * flexibility.rotationUnderMoment / span

    /** `c₀ = 48(A+3)/A` — and it is `c(ρ)` to the last digit, coupled or not. */
    val bendingFactor: Double = 48.0 * (rotationalSeries + 3.0) / rotationalSeries

    /**
     * **`Φ`** — the draw-in the standoff's own tilt supplies per unit midspan deflection, and the
     * whole of the term `C-0025` and `C-0028` dropped. Zero exactly for a decoupled joint.
     */
    val couplingFactor: Double =
        24.0 * bendingRigidity * flexibility.translationUnderMoment /
                (span * span * rotationalSeries)

    /** `G` in `nm/pN` — the axial compliance the *net* demand is charged against. */
    val effectiveAxialCompliance: Double =
        half / stretchModulus + flexibility.translationUnderForce -
                8.0 * bendingRigidity * flexibility.translationUnderMoment *
                flexibility.translationUnderMoment / (span * rotationalSeries)

    /** `a/G` in pN — `C-0025`'s `S_eff` generalised, and 2.06× its decoupled value at the design. */
    val effectiveStretchModulus: Double = half / effectiveAxialCompliance

    /** `g(β)`, the deflected shape's own draw-in factor, carried for [DrawInModel.SHAPE]. */
    val drawInShapeFactor: Double = drawInFactor(restraint)

    /** `c₀EI/L³` in `pN/nm` — the linear part, unchanged by the coupling. */
    val bendingStiffness: Double
        get() = bendingFactor * bendingRigidity / (span * span * span)

    /** The arc-length excess **one end** must absorb at [displacement], always non-negative. */
    fun chordExtension(displacement: Double): Double = when (drawInModel) {
        DrawInModel.CHORD -> sqrt(half * half + displacement * displacement) - half
        DrawInModel.SHAPE -> drawInShapeFactor * displacement * displacement / (2.0 * span)
    }

    private fun chordExtensionSlope(displacement: Double): Double = when (drawInModel) {
        DrawInModel.CHORD -> displacement / sqrt(half * half + displacement * displacement)
        DrawInModel.SHAPE -> drawInShapeFactor * displacement / span
    }

    /**
     * The **signed** axial force in the beam at [displacement], `T = (e(δ) − Φδ)/G` — positive in
     * tension, and **negative wherever the standoff's tilt supplies more draw-in than the geometry
     * demands**, which at the Gen-1 design is the whole of `0 < δ < 9.4 nm`.
     */
    fun axialForce(displacement: Double): Double =
        (chordExtension(displacement) - couplingFactor * displacement) / effectiveAxialCompliance

    /** The moment the beam applies to one standoff head at [displacement], in `pN·nm`. */
    fun headMoment(displacement: Double): Double =
        24.0 * bendingRigidity * displacement / (span * span * rotationalSeries) -
                8.0 * bendingRigidity * flexibility.translationUnderMoment *
                axialForce(displacement) / (span * rotationalSeries)

    /** The head's rotation in rad at [displacement], `θ = C12 T + C22 M`. */
    fun headRotation(displacement: Double): Double =
        flexibility.translationUnderMoment * axialForce(displacement) +
                flexibility.rotationUnderMoment * headMoment(displacement)

    /** The head's **inward** translation in nm at [displacement], `u = C11 T + C12 M`. */
    fun headDrawIn(displacement: Double): Double =
        flexibility.translationUnderForce * axialForce(displacement) +
                flexibility.translationUnderMoment * headMoment(displacement)

    /** The bending part of the midspan reaction, `c₀EIδ/L³ − 2ΦT`. */
    fun bendingReaction(displacement: Double): Double =
        bendingStiffness * displacement - 2.0 * couplingFactor * axialForce(displacement)

    /** The membrane part, `2Tδ/r` — **negative** wherever the beam is in compression. */
    fun membraneForce(displacement: Double): Double {
        if (displacement == 0.0) return 0.0
        val r = sqrt(half * half + displacement * displacement)
        return 2.0 * axialForce(displacement) * displacement / r
    }

    /**
     * The **effective** end-condition factor at [displacement], `R_bend L³/(EIδ)`.
     *
     * `C-0025`'s `c` is a constant of the joint and the span; under coupling it is a function of
     * the **stroke**, running 139 → 92 over `δ = 0 → 10 nm` at `C-0028`'s design.
     */
    fun effectiveMidspanFactor(displacement: Double): Double {
        require(displacement != 0.0) { "the effective factor is undefined at zero deflection" }
        return bendingReaction(displacement) * span * span * span /
                (bendingRigidity * displacement)
    }

    /**
     * The **effective** stretch modulus at [displacement], `a T/e(δ)` — negative wherever the joint
     * supplies more than the geometry demands, which is why it is a function and not a constant.
     */
    fun effectiveMembraneModulus(displacement: Double): Double {
        val extension = chordExtension(displacement)
        require(extension > 0.0) { "the effective modulus is undefined at zero extension" }
        return half * axialForce(displacement) / extension
    }

    /** The transverse shear each end joint reacts, `|R|/2` — the standoff's compression duty. */
    fun endShear(displacement: Double): Double = abs(reaction(displacement)) / 2.0

    override fun reaction(displacement: Double): Double {
        if (displacement == 0.0) return 0.0
        val r = sqrt(half * half + displacement * displacement)
        return bendingStiffness * displacement +
                axialForce(displacement) * (2.0 * displacement / r - 2.0 * couplingFactor)
    }

    override fun tangentStiffness(displacement: Double): Double {
        val r = sqrt(half * half + displacement * displacement)
        val slope = (chordExtensionSlope(displacement) - couplingFactor) / effectiveAxialCompliance
        return bendingStiffness +
                slope * (2.0 * displacement / r - 2.0 * couplingFactor) +
                axialForce(displacement) * 2.0 * half * half / (r * r * r)
    }

    // ---------------------------------------------------------------- the stroke, unsigned

    /** The reaction magnitude at an unsigned [stroke] driven in [orientation]. */
    fun strokeReaction(stroke: Double, orientation: FlexureOrientation): Double {
        require(stroke >= 0.0) { "stroke must not be negative, was: $stroke" }
        return orientation.sense * reaction(orientation.sense * stroke)
    }

    /** `dR/ds` at an unsigned [stroke] — even in the sense, so no sign is applied. */
    fun strokeTangentStiffness(stroke: Double, orientation: FlexureOrientation): Double =
        tangentStiffness(orientation.sense * stroke)

    /** `R(s)/s` at an unsigned [stroke] — the quantity §3's placement condition is written on. */
    fun strokeSecantStiffness(stroke: Double, orientation: FlexureOrientation): Double {
        require(stroke > 0.0) { "a secant stiffness is undefined at zero stroke" }
        return strokeReaction(stroke, orientation) / stroke
    }

    /** The standoff's compression duty at an unsigned [stroke]. */
    fun strokeEndShear(stroke: Double, orientation: FlexureOrientation): Double =
        endShear(orientation.sense * stroke)

    /** The beam's signed axial force at an unsigned [stroke]. */
    fun strokeAxialForce(stroke: Double, orientation: FlexureOrientation): Double =
        axialForce(orientation.sense * stroke)
}

// ---------------------------------------------------------------- the design root

/**
 * The span in nm at which [count] coupled flexures present a **secant** stiffness of
 * [targetStiffness] at [workingDisplacement] in [orientation] — §3's placement condition, solved
 * as a root exactly as `C-0025`'s [flexureSpanForJoint] does, and reproducing it identically when
 * the flexibility is [StandoffTipFlexibility.decoupled].
 *
 * Scans then bisects, exiting on the **bracket width** (`CLAUDE.md`): the coupled law is not
 * monotone in the *deflection*, so the first sign change is taken rather than a monotone bisection
 * assumed — `C-0018`'s lesson, in a new place.
 */
fun coupledFlexureSpan(
    bendingRigidity: Double,
    flexibility: StandoffTipFlexibility,
    count: Int,
    targetStiffness: Double,
    workingDisplacement: Double,
    orientation: FlexureOrientation = FlexureOrientation.FAVOURABLE,
    stretchModulus: Double = AnchorMaterials.DUPLEX_STRETCH_MODULUS,
    drawInModel: DrawInModel = DrawInModel.CHORD,
    scanSteps: Int = 256
): Double {
    require(count > 0) { "count must be positive, was: $count" }
    require(targetStiffness > 0.0) { "targetStiffness must be positive, was: $targetStiffness" }
    require(workingDisplacement > 0.0) {
        "workingDisplacement must be positive, was: $workingDisplacement"
    }
    require(scanSteps >= 16) { "scanSteps must be at least 16, was: $scanSteps" }
    fun assembled(span: Double): Double = count * CoupledJointFlexure(
        bendingRigidity, span, flexibility, stretchModulus, drawInModel
    ).strokeSecantStiffness(workingDisplacement, orientation)
    var low = max(workingDisplacement * 1.0e-3, 1.0e-3)
    var high = low
    var grown = 0
    while (assembled(high) >= targetStiffness && grown < 400) {
        high *= 1.5
        grown++
    }
    require(assembled(high) < targetStiffness) {
        "no span reaches a stiffness as low as $targetStiffness"
    }
    val step = (high - low) / scanSteps
    var scan = low
    for (i in 1..scanSteps) {
        val next = scan + step
        if (assembled(next) < targetStiffness) {
            low = scan
            high = next
            break
        }
        scan = next
    }
    repeat(400) {
        val middle = 0.5 * (low + high)
        if (assembled(middle) > targetStiffness) low = middle else high = middle
        if (high - low <= 1.0e-15 * high) return 0.5 * (low + high)
    }
    return 0.5 * (low + high)
}

/**
 * The stroke in nm at which a coupled flexure's own end shear reaches [criticalLoad] — the stroke
 * at which its standoff buckles, `C-0028`'s [bucklingStroke] on the coupled element.
 *
 * Returns `+∞` if the standoff never reaches it inside [maximumStroke]. The coupled reaction is not
 * monotone in the stroke, so the **first** crossing is taken.
 */
fun coupledBucklingStroke(
    flexure: CoupledJointFlexure,
    orientation: FlexureOrientation,
    criticalLoad: Double,
    maximumStroke: Double = 60.0,
    scanSteps: Int = 4096
): Double {
    require(criticalLoad >= 0.0) { "criticalLoad must not be negative, was: $criticalLoad" }
    require(maximumStroke > 0.0) { "maximumStroke must be positive, was: $maximumStroke" }
    require(scanSteps >= 16) { "scanSteps must be at least 16, was: $scanSteps" }
    if (criticalLoad <= 0.0) return 0.0
    val step = maximumStroke / scanSteps
    var low = 0.0
    var high = -1.0
    for (i in 1..scanSteps) {
        val s = i * step
        if (flexure.strokeEndShear(s, orientation) >= criticalLoad) {
            high = s
            low = s - step
            break
        }
    }
    if (high < 0.0) return Double.POSITIVE_INFINITY
    repeat(400) {
        val middle = 0.5 * (low + high)
        if (flexure.strokeEndShear(middle, orientation) < criticalLoad) low = middle else high = middle
        if (high - low <= 1.0e-15 * high) return 0.5 * (low + high)
    }
    return 0.5 * (low + high)
}

/**
 * The **peak** axial compression in pN a coupled flexure carries anywhere inside [maximumStroke],
 * scanned on [scanSteps] — the quantity `P7` is written on, and one the decoupled reading cannot
 * produce at all, because it puts the beam in tension at every stroke.
 */
fun peakFlexureCompression(
    flexure: CoupledJointFlexure,
    orientation: FlexureOrientation,
    maximumStroke: Double,
    scanSteps: Int = 2000
): Double {
    require(maximumStroke > 0.0) { "maximumStroke must be positive, was: $maximumStroke" }
    require(scanSteps >= 16) { "scanSteps must be at least 16, was: $scanSteps" }
    val step = maximumStroke / scanSteps
    var peak = 0.0
    for (i in 0..scanSteps) {
        val compression = -flexure.strokeAxialForce(i * step, orientation)
        if (compression > peak) peak = compression
    }
    return peak
}

/**
 * The bending rigidity implied by Fields et al.'s **measured** buckling of a naked duplex — a
 * 40.5 bp strand losing its resistance to a 9 pN compressive load, so `EI = P_c L²/π²`.
 *
 * `C-0028` records it as **25 % below** CanDo's model input, so every buckling load in this
 * programme is computed on the optimistic rigidity. `T-65` reports every margin on both, and says
 * which it is quoting.
 */
val FIELDS_BENDING_RIGIDITY: Double =
    9.0 * (40.5 * Gen1Tile.RISE_PER_BASE_PAIR) * (40.5 * Gen1Tile.RISE_PER_BASE_PAIR) / (PI * PI)

/**
 * The largest stroke in nm the **favourable** mounting has room for: the midspan descends toward the
 * body the standoff bases stand on, so it bottoms out when the flexure duplex's axis reaches one
 * interhelical distance from that body's own duplexes.
 *
 * A **geometric** ceiling, not an elastic one, and it exists only in the favourable mounting — which
 * is the price of everything that mounting buys. It is **reported beside** `T-65`'s predicates and
 * not adopted as one, because §3 does not say what the standoff-carrying body is: if it is the solid
 * 40 x 40 nm tile the ceiling is real, and if it is `C-0017`'s unspecified superstructure it is a
 * design choice. Same class as §3's unspecified electrode material.
 */
fun favourableStrokeClearance(
    standoffLength: Double,
    contactDistance: Double = Gen1Tile.INTERHELICAL_SHEET
): Double {
    require(standoffLength > 0.0) { "standoffLength must be positive, was: $standoffLength" }
    require(contactDistance >= 0.0) { "contactDistance must not be negative, was: $contactDistance" }
    return max(0.0, standoffLength - contactDistance)
}

/** The sign of [value] as a `+1`/`−1`/`0` triple, used only by the study's own reporting. */
internal fun signLabel(value: Double): String =
    if (abs(value) < 1.0e-12) "0" else if (sign(value) > 0.0) "tension" else "compression"
