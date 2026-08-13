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

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.ShearJointAllowable
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign
import kotlin.math.sqrt

/**
 * Task `T-30` — the **origami joint** at the end of a transverse duplex flexure: does it draw in,
 * and does it clamp?
 *
 * ## What `C-0023` left open, and why it is one model rather than two brackets
 *
 * `C-0023` placed the flexure with **two brackets it deliberately did not collapse** — the end
 * condition (pinned `c = 48` against clamped `c = 192`, exactly 4×) and the axial restraint (ends
 * free to draw in against ends held, so that the membrane term turns the beam into a cable).
 * Together they are 2.2× in span and 2.7× in tangent stiffness, and the restrained reading fails
 * `C-0023`'s own 40 pN/nm compliance ceiling and breaks the 65 pN nicked ceiling at §3's desired
 * 10 nm stroke.
 *
 * **Both brackets are the two limits of one two-parameter joint**, and this file is that joint:
 *
 * - a **rotational** spring `k_θ` at each end, whose dimensionless form `ρ = k_θ L/EI` gives
 *   [midspanFactor] `c(ρ) = 192(ρ + 2)/(ρ + 8)` — exactly 48 at `ρ = 0` and exactly 192 as
 *   `ρ → ∞`, so the end-condition bracket is the *interior* of a continuum and not a choice of two;
 * - an **axial** spring `k_a` at each end, in series with the beam's own `S/L`, giving
 *   [effectiveStretchModulus] `S_eff = S/(1 + 2S/(k_a L))` — exactly `S` when the ends are held
 *   and exactly `0` when they are free.
 *
 * Everything else — the cable geometry, the analytic tangent, the odd law — is `C-0023`'s
 * unchanged, with `S_eff` substituted for `S`; and that the substitution reproduces both of its
 * readings *identically* is asserted as a gate-2 test rather than claimed.
 *
 * ## The third number, which decides the answer
 *
 * A joint at a beam end does not only restrain rotation and axial motion: it has to **react the
 * beam's end shear, in both directions**, or the flexure is not a flexure. That is
 * [FlexureEndJoint.transverseStiffness], and its ratio to the axial one, [FlexureEndJoint.anisotropy],
 * is the whole design content:
 *
 * > **A joint has to be stiff across the beam and soft along it. For any *flexible link* those are
 * > the same number** — a chain's transverse secant and its axial secant are both `f/x` — **so an
 * > isotropic element cannot do both.** `C-0023`'s proposed remedy, a two-nucleotide single-stranded
 * > hinge at each end, is isotropic: it buys `0.65 n` nm of axial draw-in at the price of `0.65 n` nm
 * > of transverse **dead band**. This is `C-0014`'s convexity theorem (`k_lat/k_norm ≤ 1` for a
 * > flexible link) in a new place.
 *
 * The escape is the same one `C-0023` used to leave the axial trade-off: **bending has a direction**.
 * A duplex standing **normal** to the sheet carries the beam's transverse reaction along its own
 * axis (`S/ℓ`) and releases the beam's draw-in by bending (`3EI/ℓ³`), an anisotropy of `S ℓ²/(3EI)`
 * that the designer sets with a length. See [FlexureEndJoint.normalStandoff].
 */

// ---------------------------------------------------------------- the end-restraint algebra

/**
 * The dimensionless end restraint `ρ = k_θ L/EI` of a rotational end spring [rotationalStiffness]
 * on a beam of rigidity [bendingRigidity] and span [span].
 *
 * It carries the span, which is why `c` is **not** a constant of the joint: the same crossover is
 * closer to a pin on a short beam and closer to a clamp on a long one.
 */
fun endRestraintParameter(
    rotationalStiffness: Double,
    bendingRigidity: Double,
    span: Double
): Double {
    require(rotationalStiffness >= 0.0) {
        "rotationalStiffness must not be negative, was: $rotationalStiffness"
    }
    require(bendingRigidity > 0.0) { "bendingRigidity must be positive, was: $bendingRigidity" }
    require(span > 0.0) { "span must be positive, was: $span" }
    if (rotationalStiffness.isInfinite()) return Double.POSITIVE_INFINITY
    return rotationalStiffness * span / bendingRigidity
}

/**
 * The `c` in `k = c EI/L³` for a midspan point load with equal rotational end springs of
 * dimensionless strength [restraint]:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;**`c(ρ) = 192 (ρ + 2)/(ρ + 8)`**.
 *
 * Derived by superposing the simply supported central-load solution with a pair of end moments
 * `M = P L ρ/(8(ρ + 2))` and imposing `θ_end = M/k_θ`. `c(0) = 48` and `c(∞) = 192` **exactly**,
 * and `c(4) = 96`; the whole of `C-0023`'s "exactly 4×" bracket is the range of this one function.
 */
fun midspanFactor(restraint: Double): Double {
    require(restraint >= 0.0) { "restraint must not be negative, was: $restraint" }
    if (restraint.isInfinite()) return 192.0
    return 192.0 * (restraint + 2.0) / (restraint + 8.0)
}

/**
 * The fraction `β = 3ρ/(ρ + 2) ∈ [0, 3]` of the clamped end moment that an elastic end restraint
 * of strength [restraint] actually develops — the natural variable of the deflected **shape**, and
 * therefore of [drawInFactor].
 */
fun endMomentFraction(restraint: Double): Double {
    require(restraint >= 0.0) { "restraint must not be negative, was: $restraint" }
    if (restraint.isInfinite()) return 3.0
    return 3.0 * restraint / (restraint + 2.0)
}

/**
 * The `g` in `Δ = g δ²/L`, the total axial draw-in of the two ends at midspan deflection `δ`, for
 * an end restraint of strength [restraint]:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`g(β) = (2.4 − 1.25 β + β²/6)/(1 − β/4)²`, &nbsp;&nbsp; `β = 3ρ/(ρ + 2)`,
 *
 * from `Δ = ∫(1/2) w′² dx` over the partially restrained shape.
 *
 * **`C-0023` records 2.4 for both end conditions and calls it "not obvious". It is right at the
 * endpoints and wrong in between:** `g(0) = g(∞) = 2.4` exactly, and the function has an interior
 * **minimum of exactly 9/4 at β = 2.4, i.e. `ρ = 8`, `c = 120`.** So the draw-in demand a real
 * joint places is up to 6.25 % *below* what either bracket charges, and 2.4 is a ceiling over the
 * whole continuum rather than a constant.
 */
fun drawInFactor(restraint: Double): Double {
    require(restraint >= 0.0) { "restraint must not be negative, was: $restraint" }
    val beta = endMomentFraction(restraint)
    val numerator = 2.4 - 1.25 * beta + beta * beta / 6.0
    val denominator = (1.0 - beta / 4.0) * (1.0 - beta / 4.0)
    return numerator / denominator
}

/**
 * The **effective** stretch modulus in pN of a beam of modulus [stretchModulus] and span [span]
 * whose two ends are held by axial springs of stiffness [axialStiffness] each:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`S_eff = S/(1 + 2S/(k_a L))`,
 *
 * i.e. the beam's own `S/L` in series with the two joints. `S` at `k_a → ∞` (`C-0023`'s *ends held
 * axially*) and `0` at `k_a → 0` (*ends free to draw in*), so substituting it into `C-0023`'s
 * membrane term reproduces both of its readings exactly and interpolates between them.
 */
fun effectiveStretchModulus(
    stretchModulus: Double,
    axialStiffness: Double,
    span: Double
): Double {
    require(stretchModulus > 0.0) { "stretchModulus must be positive, was: $stretchModulus" }
    require(axialStiffness >= 0.0) { "axialStiffness must not be negative, was: $axialStiffness" }
    require(span > 0.0) { "span must be positive, was: $span" }
    if (axialStiffness.isInfinite()) return stretchModulus
    if (axialStiffness == 0.0) return 0.0
    return stretchModulus / (1.0 + 2.0 * stretchModulus / (axialStiffness * span))
}

// ---------------------------------------------------------------- the joint itself

/**
 * One end joint of a transverse flexure, as the three stiffnesses and one dead band a designer
 * actually chooses by picking a motif.
 *
 * @property name the motif.
 * @property rotationalStiffness `k_θ` in `pN·nm/rad` about the bending axis; `∞` is a clamp,
 *   `0` a pin.
 * @property axialStiffness `k_a` in `pN/nm` along the beam's own axis; `∞` holds the end,
 *   `0` lets it draw in freely.
 * @property transverseStiffness in `pN/nm` **across** the beam — the direction the end shear is
 *   reacted in. A joint that is not stiff here does not support the beam, whatever else it does.
 * @property transverseDeadBand in nm — the free play before the joint reacts transversely at all.
 *   Zero for every covalent motif; `0.65 n` for a motif containing `n` unpaired nucleotides.
 * @property provenance where each number comes from, and whether it is measured, fitted or derived.
 */
data class FlexureEndJoint(
    val name: String,
    val rotationalStiffness: Double,
    val axialStiffness: Double,
    val transverseStiffness: Double,
    val transverseDeadBand: Double = 0.0,
    val contourLength: Double = 0.0,
    val kuhnLength: Double = 0.0,
    val standoffLength: Double = 0.0,
    val provenance: String = ""
) {

    init {
        require(rotationalStiffness >= 0.0) {
            "rotationalStiffness must not be negative, was: $rotationalStiffness"
        }
        require(axialStiffness >= 0.0) {
            "axialStiffness must not be negative, was: $axialStiffness"
        }
        require(transverseStiffness >= 0.0) {
            "transverseStiffness must not be negative, was: $transverseStiffness"
        }
        require(transverseDeadBand >= 0.0) {
            "transverseDeadBand must not be negative, was: $transverseDeadBand"
        }
    }

    /**
     * `k_transverse/k_axial` — **exactly 1 for any isotropic element**, and the design content of
     * the whole task. A flexible link has no direction of its own, so it cannot support the beam
     * across and release it along; only a *bending* element can, and then the ratio is a length
     * the designer sets.
     */
    val anisotropy: Double
        get() = when {
            transverseStiffness.isInfinite() && axialStiffness.isInfinite() -> 1.0
            axialStiffness == 0.0 -> Double.POSITIVE_INFINITY
            else -> transverseStiffness / axialStiffness
        }

    companion object {

        /** `C-0023`'s *pinned ends, free to draw in* — its `E3a`, and the low corner of both brackets. */
        fun pinnedAndFree(): FlexureEndJoint = FlexureEndJoint(
            name = "ideal pin, free to draw in",
            rotationalStiffness = 0.0,
            axialStiffness = 0.0,
            transverseStiffness = Double.POSITIVE_INFINITY,
            provenance = "IDEALISATION — C-0023's E3a corner, carried to reproduce it"
        )

        /** `C-0023`'s *pinned ends, held axially* — its `E3b`. */
        fun pinnedAndHeld(): FlexureEndJoint = FlexureEndJoint(
            name = "ideal pin, held axially",
            rotationalStiffness = 0.0,
            axialStiffness = Double.POSITIVE_INFINITY,
            transverseStiffness = Double.POSITIVE_INFINITY,
            provenance = "IDEALISATION — C-0023's E3b corner, carried to reproduce it"
        )

        /** `C-0023`'s *clamped ends, free to draw in*. */
        fun clamped(): FlexureEndJoint = FlexureEndJoint(
            name = "ideal clamp, free to draw in",
            rotationalStiffness = Double.POSITIVE_INFINITY,
            axialStiffness = 0.0,
            transverseStiffness = Double.POSITIVE_INFINITY,
            provenance = "IDEALISATION — C-0023's clamped/free corner"
        )

        /** `C-0023`'s *clamped ends, held axially* — the worst corner of both brackets. */
        fun clampedAndHeld(): FlexureEndJoint = FlexureEndJoint(
            name = "ideal clamp, held axially",
            rotationalStiffness = Double.POSITIVE_INFINITY,
            axialStiffness = Double.POSITIVE_INFINITY,
            transverseStiffness = Double.POSITIVE_INFINITY,
            provenance = "IDEALISATION — C-0023's clamped/held corner"
        )

        /**
         * **`J1`** — one antiparallel crossover, the sheet's own motif.
         *
         * `k_θ = 2αB/(100a)` is `C-0009`'s **cited, fitted** constant (Chen et al., *JACS*
         * **136**:6995, 2014, SI §S2) and `k_s = 2αS/(100a)` is `C-0020`'s **derived** construction
         * on the same softened bond — not measured, and swept over four decades there. A crossover
         * is a covalent tie with no direction of its own, so its transverse stiffness is the same
         * softened-bond constant: **isotropic, anisotropy exactly 1**.
         */
        fun crossover(
            alpha: Double = 1.0,
            inPlaneMultiplier: Double = 1.0
        ): FlexureEndJoint {
            require(alpha > 0.0) { "alpha must be positive, was: $alpha" }
            require(inPlaneMultiplier > 0.0) {
                "inPlaneMultiplier must be positive, was: $inPlaneMultiplier"
            }
            val slide = Gen1Tile.crossoverInPlaneStiffness(alpha) * inPlaneMultiplier
            return FlexureEndJoint(
                name = "direct antiparallel crossover",
                rotationalStiffness = Gen1Tile.crossoverHingeStiffness(alpha),
                axialStiffness = slide,
                transverseStiffness = slide,
                provenance = "k_theta CITED+FITTED (Chen et al. 2014, via C-0009); " +
                        "k_s DERIVED construction (C-0020), NOT measured"
            )
        }

        /**
         * **`J2`** — a nicked continuation: the flexure duplex *is* the post's duplex, continuing
         * through a nick on **one** backbone.
         *
         * The intact backbone is not a softened bond — it carries the duplex's own rigidity over
         * the rise — so the joint is the sum of a full `B/a` and one nicked `αB/(100a)`, i.e. the
         * softened term is worth 1 % and the joint is **effectively clamped and effectively held**:
         * the worst corner of both of `C-0023`'s brackets.
         */
        fun nickedContinuation(alpha: Double = 1.0): FlexureEndJoint {
            require(alpha > 0.0) { "alpha must be positive, was: $alpha" }
            val rise = Gen1Tile.RISE_PER_BASE_PAIR
            val axial = Gen1Tile.DUPLEX_STRETCH_MODULUS * (1.0 + alpha / 100.0) / rise
            return FlexureEndJoint(
                name = "nicked continuation, one backbone intact",
                rotationalStiffness =
                    Gen1Tile.DUPLEX_BENDING_RIGIDITY * (1.0 + alpha / 100.0) / rise,
                axialStiffness = axial,
                transverseStiffness = axial,
                provenance = "the intact backbone carries B/a and S/a over one rise; " +
                        "the nicked one adds Chen et al.'s softened bond, worth 1 %"
            )
        }

        /**
         * **`J2b`** — a *doubly* nicked continuation, both backbones cut at the same base pair.
         *
         * Nothing continuous is left, so the joint is two softened bonds in parallel — which is
         * exactly a crossover. **A double nick IS a crossover**, and that is a result rather than
         * an assumption: the two motifs are the same joint.
         */
        fun doublyNickedContinuation(alpha: Double = 1.0): FlexureEndJoint =
            crossover(alpha).copy(
                name = "doubly nicked continuation, both backbones cut",
                provenance = "identical to a crossover: two Chen-softened bonds in parallel"
            )

        /**
         * **`J3`** — `C-0023`'s own proposed remedy: an `n`-nucleotide single-stranded hinge on
         * both backbones at each end.
         *
         * - axially it is the chain's own Gaussian constant `3k_BT/(L_c b)`, and it is **soft**,
         *   which is what it was proposed for;
         * - **transversely it is the same number**, because a flexible link has no direction — so
         *   it cannot support the beam;
         * - and it carries a **dead band** of its full contour `0.65 n` nm before it reacts at all.
         *
         * The Kuhn length is the **zero-force** end of the method-systematic bracket (2.10 nm,
         * Chen et al., *PNAS* **109**:799, 2012), because these elements carry ~1 pN, an order
         * below the lowest force the 10–40 pN spectroscopy fits cover; the contour per nucleotide
         * is the **inextensible** 0.65 nm that travels with it.
         */
        fun singleStrandedHinge(
            nucleotides: Int,
            kuhnLength: Double = SsDnaTether.KUHN_LENGTH_ZERO_FORCE,
            contourPerNucleotide: Double = SsDnaTether.CONTOUR_PER_NUCLEOTIDE,
            temperature: Double = ROOM_TEMPERATURE
        ): FlexureEndJoint {
            require(nucleotides > 0) { "nucleotides must be positive, was: $nucleotides" }
            require(kuhnLength > 0.0) { "kuhnLength must be positive, was: $kuhnLength" }
            require(contourPerNucleotide > 0.0) {
                "contourPerNucleotide must be positive, was: $contourPerNucleotide"
            }
            val contour = nucleotides * contourPerNucleotide
            val chain = FreelyJointedChain(contour, kuhnLength, temperature)
            // a worm-like segment of persistence l_p = b/2 turning through theta over its whole
            // contour stores (l_p k_BT/2) theta^2/L_c, i.e. a rotational spring l_p k_BT/L_c
            val rotational = 0.5 * kuhnLength * thermalEnergy(temperature) / contour
            return FlexureEndJoint(
                name = "$nucleotides nt single-stranded hinge",
                rotationalStiffness = rotational,
                axialStiffness = chain.gaussianStiffness,
                transverseStiffness = chain.gaussianStiffness,
                transverseDeadBand = contour,
                contourLength = contour,
                kuhnLength = kuhnLength,
                provenance = "Kuhn length CITED+MEASURED at the ZERO-FORCE end (Chen et al. 2012); " +
                        "contour per nucleotide the inextensible 0.65 nm that travels with it"
            )
        }

        /**
         * **`J4`** — a rigid multi-crossover clamp: [crossovers] antiparallel crossovers spaced at
         * the sheet's own 32 bp interface pitch along the beam's axis.
         *
         * The bonds' own `k_θ` is not what makes it a clamp — the **couple** is: crossovers at
         * offsets `d_i` from the joint centroid react an end moment as `k_s Σd_i²`, which at the
         * 10.88 nm pitch is two orders above the sum of the hinge constants.
         */
        fun multiCrossoverClamp(
            crossovers: Int,
            alpha: Double = 1.0,
            inPlaneMultiplier: Double = 1.0
        ): FlexureEndJoint {
            require(crossovers >= 1) { "crossovers must be at least one, was: $crossovers" }
            val slide = Gen1Tile.crossoverInPlaneStiffness(alpha) * inPlaneMultiplier
            val pitch = Gen1Tile.CROSSOVER_SPACING_SHEET_BP * Gen1Tile.RISE_PER_BASE_PAIR
            val offsets = (0 until crossovers).map { it * pitch - (crossovers - 1) * pitch / 2.0 }
            val couple = slide * offsets.sumOf { it * it }
            return FlexureEndJoint(
                name = "$crossovers-crossover clamp at the 32 bp pitch",
                rotationalStiffness =
                    crossovers * Gen1Tile.crossoverHingeStiffness(alpha) + couple,
                axialStiffness = crossovers * slide,
                transverseStiffness = crossovers * slide,
                provenance = "Chen et al.'s bonds in parallel plus their couple over the " +
                        "32 bp interface pitch (Rothemund 2006, via C-0009)"
            )
        }

        /**
         * **`J5`** — the joint this task proposes: the beam's end seated on a duplex standing
         * **normal** to the sheet, of length [length] nm.
         *
         * The only **anisotropic** motif in the catalogue, and the reason is structural rather than
         * material: the standoff carries the beam's transverse reaction **along its own axis**
         * (`S/ℓ`) and releases the beam's axial draw-in by **bending** (`3EI/ℓ³`), so
         *
         * &nbsp;&nbsp;&nbsp;&nbsp;`anisotropy = S ℓ²/(3 EI)`,
         *
         * which the designer sets with a length and which grows as `ℓ²`. Its rotational restraint
         * is the same cantilever's `EI/ℓ`. **This is `C-0023`'s own escape — bending is signed and
         * has a direction — applied one level down, to the joint instead of the element.**
         */
        fun normalStandoff(
            length: Double,
            bendingRigidity: Double = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
            stretchModulus: Double = Gen1Tile.DUPLEX_STRETCH_MODULUS
        ): FlexureEndJoint {
            require(length > 0.0) { "length must be positive, was: $length" }
            return FlexureEndJoint(
                name = "normal duplex standoff, ${"%.1f".format(length)} nm",
                rotationalStiffness = bendingRigidity / length,
                axialStiffness = 3.0 * bendingRigidity / (length * length * length),
                transverseStiffness = stretchModulus / length,
                standoffLength = length,
                provenance = "EI CITED as a CanDo MODEL INPUT (Kim et al. 2012), S CITED+MEASURED " +
                        "(Wang et al. 1997); the anisotropy is geometry, not material"
            )
        }
    }
}

// ---------------------------------------------------------------- the flexure

/**
 * A transverse duplex flexure of rigidity [bendingRigidity] and [span], with [joint] at **both**
 * ends — `C-0023`'s `E3` with its two brackets replaced by the one two-parameter joint they are
 * the limits of.
 *
 * The law is `C-0023`'s unchanged: `R(δ) = c(ρ) EI δ/L³ + membrane(δ)`, odd, two-sided, with the
 * membrane term built from `C-0014`'s own [cableTension] and [cableNormalForce] — and `S_eff` in
 * place of `S`.
 */
class PartiallyRestrainedFlexure(
    val bendingRigidity: Double,
    val span: Double,
    val joint: FlexureEndJoint,
    val stretchModulus: Double = AnchorMaterials.DUPLEX_STRETCH_MODULUS
) : SignedCouplingElement {

    init {
        require(bendingRigidity > 0.0) {
            "bendingRigidity must be positive, was: $bendingRigidity"
        }
        require(span > 0.0) { "span must be positive, was: $span" }
        require(stretchModulus > 0.0) { "stretchModulus must be positive, was: $stretchModulus" }
    }

    private val half: Double get() = span / 2.0

    /** `ρ = k_θ L/EI` at this span — the joint's restraint, made dimensionless. */
    val restraint: Double =
        endRestraintParameter(joint.rotationalStiffness, bendingRigidity, span)

    /** `c(ρ)`, between 48 and 192. */
    val midspanFactor: Double = midspanFactor(restraint)

    /** `S_eff`, between 0 and `S`. */
    val effectiveStretchModulus: Double =
        effectiveStretchModulus(stretchModulus, joint.axialStiffness, span)

    /** `c EI/L³` in `pN/nm` — the linear part, and the whole element when the ends draw in freely. */
    val bendingStiffness: Double
        get() = midspanFactor * bendingRigidity / (span * span * span)

    /** The membrane part of the reaction in pN, odd and cubic; zero when `S_eff` is zero. */
    fun membraneForce(displacement: Double): Double {
        if (effectiveStretchModulus == 0.0 || displacement == 0.0) return 0.0
        return sign(displacement) * 2.0 *
                cableNormalForce(effectiveStretchModulus, half, abs(displacement))
    }

    /**
     * The axial tension in pN the beam and its two joints carry at [displacement] — **even**, and
     * the quantity every allowable in `C-0006` and `CH-0029` is judged against.
     */
    fun axialTension(displacement: Double): Double =
        if (effectiveStretchModulus == 0.0) 0.0
        else cableTension(effectiveStretchModulus, half, abs(displacement))

    /**
     * The total draw-in of the two ends in nm, `g(ρ) δ²/L`, from the deflected **shape** — the
     * demand the joints have to meet, and 2.25–2.40 δ²/L rather than `C-0023`'s flat 2.4.
     */
    fun drawInDemand(displacement: Double): Double =
        drawInFactor(restraint) * displacement * displacement / span

    /** The extension in nm of **one** end joint at [displacement], `T/k_a`; zero for a held end. */
    fun jointExtension(displacement: Double): Double =
        if (joint.axialStiffness.isInfinite() || joint.axialStiffness == 0.0) 0.0
        else axialTension(displacement) / joint.axialStiffness

    /** The transverse shear in pN each end joint reacts, `|R|/2`. */
    fun endShear(displacement: Double): Double = abs(reaction(displacement)) / 2.0

    override fun reaction(displacement: Double): Double =
        bendingStiffness * displacement + membraneForce(displacement)

    override fun tangentStiffness(displacement: Double): Double {
        if (effectiveStretchModulus == 0.0) return bendingStiffness
        val a = half
        val r = sqrt(a * a + displacement * displacement)
        return bendingStiffness +
                2.0 * effectiveStretchModulus / a * (1.0 - a * a * a / (r * r * r))
    }
}

// ---------------------------------------------------------------- the design root

/**
 * The span in nm at which [count] flexures with [joint] at both ends present a **secant** stiffness
 * of [targetStiffness] at [workingDisplacement] — §3's placement condition, solved as a root.
 *
 * The assembled secant falls with the span even though `c(ρ)` **rises** with it: `c` grows at most
 * as `L^(1/3)` (its logarithmic derivative `6ρ/((ρ+2)(ρ+8))` peaks at exactly `1/3`, at `ρ = 4`)
 * against the `L³` in the denominator, so the function is monotone and a scan-then-bisect is safe.
 * Exits on the **bracket width**, never on a residual (`CLAUDE.md`).
 */
fun flexureSpanForJoint(
    bendingRigidity: Double,
    joint: FlexureEndJoint,
    count: Int,
    targetStiffness: Double,
    workingDisplacement: Double,
    stretchModulus: Double = AnchorMaterials.DUPLEX_STRETCH_MODULUS,
    scanSteps: Int = 256
): Double {
    require(count > 0) { "count must be positive, was: $count" }
    require(targetStiffness > 0.0) { "targetStiffness must be positive, was: $targetStiffness" }
    require(workingDisplacement > 0.0) {
        "workingDisplacement must be positive, was: $workingDisplacement"
    }
    require(scanSteps >= 16) { "scanSteps must be at least 16, was: $scanSteps" }
    fun assembled(span: Double): Double = count * PartiallyRestrainedFlexure(
        bendingRigidity, span, joint, stretchModulus
    ).secantStiffness(workingDisplacement)
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

// ---------------------------------------------------------------- CH-0029, inverted

/**
 * The bonded length in base pairs a hybridised joint needs in order to carry [tension] pN in shear
 * at [loadingRate] — `CH-0029`'s ladder, read the other way round.
 *
 * The ladder is what makes this a design statement rather than a pass/fail: a load below the
 * allowable of a *short* domain is free, and one approaching the 68.1 pN saturation cannot be
 * carried by **any** length, which is the honest failure and is thrown rather than clipped.
 */
fun bondedLengthForTension(
    tension: Double,
    allowable: ShearJointAllowable = ShearJointAllowable(),
    loadingRate: Double = ShearJointAllowable.REFERENCE_LOADING_RATE
): Double {
    require(tension > 0.0) { "tension must be positive, was: $tension" }
    require(tension < allowable.saturationForce) {
        "tension $tension is at or above the loading-rate-free saturation " +
                "${allowable.saturationForce}: no bonded length carries it in shear"
    }
    var low = 1.0e-3
    var high = 1.0e6
    require(allowable.ruptureForce(high, loadingRate) > tension) {
        "the allowable does not reach $tension even at $high base pairs"
    }
    repeat(400) {
        val middle = 0.5 * (low + high)
        if (allowable.ruptureForce(middle, loadingRate) < tension) low = middle else high = middle
        if (high - low <= 1.0e-14 * high) return 0.5 * (low + high)
    }
    return 0.5 * (low + high)
}
