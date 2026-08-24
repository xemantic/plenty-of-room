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

import com.xemantic.nano.plentyofroom.electrostatics.OsmoticStressEquationOfState
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import kotlin.math.exp

/**
 * `T-310` — a crossover bond's normal link is **two** mechanisms, resolved by the bond's own
 * direction, and the radial one is the half nothing in this corpus prices.
 *
 * ## The coordinate, and why the name changes
 *
 * `HoneycombGrillage`'s link residual is `R = ΔW + (d/2)·unitY·(Φ_a + Φ_b)`, and `W` is the
 * deflection **normal to the face**. So a relative `W` displacement of two crossover-bonded
 * duplexes decomposes on their own line of centres `n̂ = (unitY, unitZ)`:
 *
 * - at an **in-plane** bond `unitZ = 0`, and the whole of it is a transverse **shear** of the
 *   connector — `CH-0242`'s and `C-0194` §4's mechanism, where `C-0205`'s ceiling is exact;
 * - at a bond running **through the thickness** `unitZ² = 0.75`, and three quarters of it is a
 *   change of the interhelical **separation**.
 *
 * `CH-0259` calls the second constant *axial*. In `HoneycombGrillage` `axial` already means
 * *along the duplex beam* — `axialPinBeam`, `axialEnergy`, `axialRelaxed`,
 * `HoneycombTetherElement.axialStiffness` — so here it is **radial**, after the central-force
 * decomposition `K = V″(r) n̂n̂ᵀ + (V′(r)/r)(I − n̂n̂ᵀ)` it comes from. They are one number.
 *
 * [resolvedLinkStiffness] is that decomposition projected onto the link's own gradient direction,
 * which is `z`. It is the same expression `HoneycombTetherElement.normalStiffness` already
 * carries for a chain — the source file has resolved a **tether**'s two mechanisms by direction
 * since `T-299` and has resolved a **bond**'s by nothing.
 *
 * ## What is on the radial axis
 *
 * Two things, in parallel, both resisting a change of separation about the built state:
 *
 * 1. the **connector's** own resistance to being stretched — bracketed by the corpus's own two
 *    candidates, [impliedPhosphodiesterStepStiffness] (`C-0194`'s implied step stiffness, from a
 *    tension the same claim derives) and the duplex stretch modulus over the span, which
 *    `RIGID_LINK_STIFFNESS`'s own KDoc already prices `1e4 pN/nm` against;
 * 2. the **pair's own** repulsion, `V″(d)` — and that one is **measured**. `C-0205` §1b carried
 *    the *transverse* half of the same tensor for its **sign** and quoted it per unit of force,
 *    i.e. never evaluated it; the radial half is [centralPairRadialStiffness], positive for a
 *    repulsive decaying law, in closed form from `MengMagnesium`'s osmotic-stress equation of
 *    state at a separation **above** that fit's own data floor.
 */

/**
 * A bond's normal-link stiffness in pN/nm, resolved by the bond's own line of centres —
 * `k_radial·unitZ² + k_transverse·unitY²`.
 *
 * `(unitY, unitZ)` is the unit line of centres `HoneycombGrillage` already builds. The expression
 * is exactly `HoneycombTetherElement.normalStiffness`'s, with the chain's tangent and secant
 * replaced by a crossover's radial and transverse constants.
 */
fun resolvedLinkStiffness(
    radialStiffness: Double,
    transverseStiffness: Double,
    unitY: Double,
    unitZ: Double
): Double {
    require(radialStiffness > 0.0 && radialStiffness.isFinite()) {
        "radialStiffness must be positive and finite, was: $radialStiffness"
    }
    require(transverseStiffness > 0.0 && transverseStiffness.isFinite()) {
        "transverseStiffness must be positive and finite, was: $transverseStiffness"
    }
    return radialStiffness * unitZ * unitZ + transverseStiffness * unitY * unitY
}

/**
 * The **radial** stiffness a central pair interaction contributes over a contact length, in
 * pN/nm — `−(d f_∥/d d)·L`, positive for a repulsive law decaying faster than `1/d`.
 *
 * For `f_∥(d) = Π_R e^(−d/λ) d/√3` the derivative is closed form,
 * `d f_∥/d d = (Π_R/√3) e^(−d/λ) (1 − d/λ)`, so the returned stiffness is
 * `(Π_R/√3) e^(−d/λ) (d/λ − 1) L` — **exactly zero** at `d = λ` and negative below it, where the
 * force is still rising with separation.
 *
 * @param separation the interaxial distance `d` in nm.
 * @param contactLength the length of interface one crossover owns, in nm.
 */
fun centralPairRadialStiffness(
    equationOfState: OsmoticStressEquationOfState,
    separation: Double,
    contactLength: Double
): Double {
    require(separation > 0.0) { "separation must be positive, was: $separation" }
    require(contactLength >= 0.0) { "contactLength must be non-negative, was: $contactLength" }
    val lambda = equationOfState.decayLength
    val amplitude = equationOfState.repulsionAmplitude / SQRT_THREE
    return amplitude * exp(-separation / lambda) * (separation / lambda - 1.0) * contactLength
}

/**
 * The **transverse** stiffness the same central pair interaction contributes, in pN/nm —
 * `−f_∥(d)·L/d`, negative wherever the pair repels.
 *
 * `C-0205` §1b introduced [centralPairForceTransverseStiffness] and quoted it *per unit of
 * repulsive force per unit length*, so its `−2.81545741` is `−L/d` and carries no force at all.
 * This evaluates it at the measured law.
 */
fun centralPairTransverseStiffness(
    equationOfState: OsmoticStressEquationOfState,
    separation: Double,
    contactLength: Double
): Double = centralPairForceTransverseStiffness(
    equationOfState.parallelPairForcePerLength(separation), separation, contactLength
)

/**
 * The pair's outward force per crossover in pN — `f_∥(d)·L`, the work conjugate of a change of
 * separation, against which `C-0194`'s implied inward bond tension `T = 2k_θ/r_P` is a
 * cross-check that nothing in this repository fitted to anything in common.
 */
fun centralPairForcePerCrossover(
    equationOfState: OsmoticStressEquationOfState,
    separation: Double,
    contactLength: Double
): Double {
    require(contactLength >= 0.0) { "contactLength must be non-negative, was: $contactLength" }
    return equationOfState.parallelPairForcePerLength(separation) * contactLength
}

/**
 * `C-0194`'s implied phosphodiester-step stiffness in pN/nm — the bond tension `T = 2k_θ/r_P`
 * over the amount `g` stands above the **measured** relaxed step.
 *
 * `C-0194` §4 derives it as a cross-check on the tension and does not use it as a stiffness;
 * `CH-0259` is what reads it on the radial axis, where it is the connector's own.
 */
fun impliedPhosphodiesterStepStiffness(
    hingeStiffness: Double,
    phosphateRadius: Double,
    interhelicalDistance: Double,
    relaxedStep: Double
): Double {
    val span = crossoverSpanFloor(interhelicalDistance, phosphateRadius)
    require(span > relaxedStep) {
        "the built span must exceed the relaxed step, was: $span against $relaxedStep"
    }
    return impliedCrossoverBondTension(hingeStiffness, phosphateRadius) / (span - relaxedStep)
}

/** A bracket on the **radial** link constant, in pN/nm, with the pair term named separately. */
class CrossoverRadialLinkBracket(
    /** The connector alone, at `C-0194`'s implied phosphodiester-step stiffness. */
    val connectorAtImpliedStep: Double,
    /** The connector alone, at the duplex stretch modulus over the span. */
    val connectorAtDuplexStretch: Double,
    /** The measured pair's own `V″(d)` over one crossover's interface — the only measured term. */
    val pairRadial: Double,
    /** The smallest radial constant the two mechanisms in parallel support. */
    val floor: Double,
    /** The largest. */
    val ceiling: Double
)

/**
 * The radial bracket of `T-310`'s deliverable 1 — two connector candidates in parallel with one
 * measured pair term, all closed form, no solver.
 *
 * The pair term is **added** rather than reported, because on the radial axis it is positive: a
 * repulsive pair resists being compressed. That is the opposite of `C-0205` §1b's transverse
 * term, which is the same tensor's other eigenvalue and is negative — one decomposition, two
 * signs, and which one applies is decided by the bond's own direction.
 */
fun crossoverRadialLinkBracket(
    hingeStiffness: Double,
    phosphateRadius: Double,
    interhelicalDistance: Double,
    relaxedStep: Double,
    stretchModulus: Double,
    equationOfState: OsmoticStressEquationOfState,
    contactLength: Double
): CrossoverRadialLinkBracket {
    val span = crossoverSpanFloor(interhelicalDistance, phosphateRadius)
    val step = impliedPhosphodiesterStepStiffness(
        hingeStiffness, phosphateRadius, interhelicalDistance, relaxedStep
    )
    val duplex = stretchModulus / span
    val pair = centralPairRadialStiffness(equationOfState, interhelicalDistance, contactLength)
    require(duplex > step) {
        "the connector candidates must be ordered, was: $step against $duplex"
    }
    return CrossoverRadialLinkBracket(
        connectorAtImpliedStep = step,
        connectorAtDuplexStretch = duplex,
        pairRadial = pair,
        floor = step + pair,
        ceiling = duplex + pair
    )
}

/**
 * `T-303`'s [honeycombTiedLatticeAtLinkStiffness] with the **radial** constant exposed too.
 *
 * At `radialLinkStiffness = null` this is that function bit for bit — asserted rather than
 * claimed — so every census `C-0205` took is reproducible through this entry point.
 */
fun honeycombTiedLatticeAtResolvedLink(
    block: HoneycombBlock,
    rowBasePairs: Int,
    enhancement: Double,
    tied: Boolean,
    transverseLinkStiffness: Double = HoneycombGrillage.RIGID_LINK_STIFFNESS,
    radialLinkStiffness: Double? = null,
    subdivisions: Int = 1,
    firstAxialSign: Int = 1
): HoneycombGrillage {
    require(transverseLinkStiffness > 0.0 && transverseLinkStiffness.isFinite()) {
        "transverseLinkStiffness must be positive and finite, was: $transverseLinkStiffness"
    }
    fun build(ties: List<HoneycombScaffoldTurnTie>) = HoneycombGrillage(
        block = block,
        rowBasePairs = rowBasePairs,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        hingeStiffnessEnhancement = enhancement,
        subdivisions = subdivisions,
        linkStiffness = transverseLinkStiffness,
        radialLinkStiffness = radialLinkStiffness,
        scaffoldTurnTies = ties
    )
    val bare = build(emptyList())
    return if (!tied) bare else build(
        honeycombScaffoldTurnTies(block, bare.nodesPerBeam, firstAxialSign, 0.0)
    )
}

private const val SQRT_THREE: Double = 1.7320508075688772
