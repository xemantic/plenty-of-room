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

import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import kotlin.math.log10
import kotlin.math.pow

/**
 * `T-303` — how stiff a crossover's **normal link** can physically be, and where the coupled
 * recovery's threshold on it sits.
 *
 * ## What coordinate this is about
 *
 * `C-0194` settled that `HoneycombGrillage`'s vertical link **is** the crossover's common
 * azimuthal mode: its residual `R = ΔW + (d/2)·unitY·(Φ_a + Φ_b)` is a function of the **sum** of
 * the two rolls, and `d/2` is the only frame-indifferent arm. What that claim could not settle is
 * the link's **magnitude**. It derived one route — `CH-0242`'s premise carried one step, giving a
 * bond tension `T = 2 k_θ/r_P` and a link stiffness [spanDerivedLinkStiffness] `k_R = T/g` — and
 * recorded that the two coupled cells `C-0180` recovered are flat at `k_link ≥ 1000 pN/nm` and
 * **not** flat at `100` or at `k_R`.
 *
 * This file supplies the other routes, and every one of them is a multiplication.
 *
 * ## Route 2 — Chen et al.'s softened bond, read on the DISPLACEMENT axis
 *
 * [transverseSoftenedBondLinkStiffness] is `Gen1Tile.crossoverInPlaneStiffness` under a name that
 * says which coordinate it is being read on. `CLAUDE.md` already records the comparison and
 * nobody had made it: `RIGID_LINK_STIFFNESS`'s own KDoc prices `1e4 pN/nm` against the duplex
 * stretch modulus and never against `Gen1Tile.crossoverInPlaneStiffness = 64.7058824 pN/nm`,
 * *"the same two phosphate bonds on the orthogonal axis"*.
 *
 * The transfer is one sentence and it is this file's own: a relative **normal** displacement of
 * two crossover-bonded duplexes and a relative **axial slip** of the same two are the same kind of
 * coordinate — a displacement of the two anchor points transverse to the connector that joins
 * them — and `CLAUDE.md`'s standing entry *"an isotropic element cannot be stiff across and soft
 * along — `k_⊥/k_axial ≡ 1` for any flexible link, and for any covalent tie on a softened bond"*
 * makes them equal. The route is **independent of `k_θ`**: Chen et al.'s construction is
 * `2αX/(100a)` and this reading substitutes the stretch modulus `S` where the hinge substitutes
 * the bending rigidity `B`, so no part of route 1's attribution enters it.
 *
 * ## Route 3 — the connector's own bending, as a bracket that needs no fitted constant
 *
 * A connector of span `g` whose two ends are displaced transversely relative to each other, each
 * end held by a rotational spring `k_r` against its own duplex, is a beam in **double curvature**.
 * Slope-deflection with `θ_A = θ_B = θ` by symmetry and a chord rotation `ψ = δ/g` gives
 * `θ = 6ψ/(6+ρ)` with `ρ = k_r g/EI`, and the transverse force is
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`F = (12EI/g³)·(ρ/(6+ρ))·δ`,
 *
 * so [guidedEndConditionFactor] `c(ρ) = 12ρ/(6+ρ)` is **exactly 0** at a pinned end and **exactly
 * 12** at a clamped one. `CLAUDE.md` warns that a `c(ρ)` never transfers between boundary-value
 * problems, so this one is derived here rather than borrowed: `C-0025`'s `192(ρ+2)/(ρ+8)` is a
 * *midspan-loaded* beam and `C-0034`'s `12(1+ρ)/(4+ρ)` a *cantilever/guided* pair, and neither is
 * a relative end displacement. Because the two limits are exact, `c ∈ [0, 12]` is a bracket that
 * needs no `k_r` at all — which is the whole point, since nothing measures `k_r`.
 *
 * `EI = L_p k_BT` over the corpus's own ssDNA persistence bracket. That is a **model**: a
 * worm-like-chain persistence length is a thermal average over rotameric freedom, and a single
 * phosphodiester step is stiffer in bond bending and softer in torsion than a smooth rod. The
 * bracket absorbs the direction of both, because the torsional freedom acts as a hinge and
 * therefore lowers `c`.
 *
 * ## The fourth term, carried for its SIGN
 *
 * Two duplexes also interact directly, and that interaction is **central**. For a central
 * potential at separation `d`, a relative displacement `δ` perpendicular to the line of centres
 * gives `r = √(d² + δ²) ≈ d + δ²/(2d)`, so the transverse stiffness is `V′(d)/d` — **negative**
 * wherever the pair repels, which `CLAUDE.md` records this pair does at every separation on four
 * independent measured methods. [centralPairForceTransverseStiffness] returns it, and it
 * **lowers** the ceiling rather than raising it.
 */

/**
 * The end-condition factor of a connector whose two ends are displaced transversely relative to
 * each other, each held by a rotational spring — `12ρ/(6+ρ)`, dimensionless.
 *
 * Exactly `0` at `ρ = 0` (a pin at each end, which is a mechanism in this mode) and exactly `12`
 * in the limit `ρ → ∞` (a clamp at each end). `ρ = k_r g/EI`.
 */
fun guidedEndConditionFactor(reducedEndStiffness: Double): Double {
    require(reducedEndStiffness >= 0.0) {
        "reducedEndStiffness must be non-negative, was: $reducedEndStiffness"
    }
    return 12.0 * reducedEndStiffness / (6.0 + reducedEndStiffness)
}

/**
 * The transverse stiffness a connector's own **bending** supplies, in pN/nm.
 *
 * `c·EI/g³` with `EI = L_p k_BT` and `c` from [guidedEndConditionFactor], so passing
 * `endConditionFactor = 0.0` returns exactly zero and `12.0` returns the clamped ceiling.
 */
fun connectorBendingLinkStiffness(
    persistenceLength: Double,
    thermalEnergy: Double,
    span: Double,
    endConditionFactor: Double
): Double {
    require(persistenceLength > 0.0) {
        "persistenceLength must be positive, was: $persistenceLength"
    }
    require(thermalEnergy > 0.0) { "thermalEnergy must be positive, was: $thermalEnergy" }
    require(span > 0.0) { "span must be positive, was: $span" }
    require(endConditionFactor >= 0.0 && endConditionFactor <= 12.0) {
        "endConditionFactor must be in [0, 12], was: $endConditionFactor"
    }
    return endConditionFactor * persistenceLength * thermalEnergy / (span * span * span)
}

/**
 * Chen et al.'s softened-bond construction read on the **displacement** axis, in pN/nm —
 * `2αS/(100a)`, which is [Gen1Tile.crossoverInPlaneStiffness] under a name that says so.
 *
 * The lattice already uses this number for the crossover's resistance to **axial slip**; the
 * normal link is the same two phosphate bonds resisting a displacement in another direction, and
 * a softened covalent bond has no way to tell the two apart.
 */
fun transverseSoftenedBondLinkStiffness(alpha: Double = 1.0): Double =
    Gen1Tile.crossoverInPlaneStiffness(alpha)

/**
 * The transverse stiffness a **central** pair interaction contributes over a contact length, in
 * pN/nm — `−f·L/d`, negative for a repulsive `f`.
 *
 * @param repulsiveForcePerLength the pair force per unit length, positive when the two bodies
 *   push apart.
 * @param separation the interaxial distance `d` in nm.
 * @param contactLength the length of interface one crossover owns, in nm.
 */
fun centralPairForceTransverseStiffness(
    repulsiveForcePerLength: Double,
    separation: Double,
    contactLength: Double
): Double {
    require(separation > 0.0) { "separation must be positive, was: $separation" }
    require(contactLength >= 0.0) { "contactLength must be non-negative, was: $contactLength" }
    return -repulsiveForcePerLength * contactLength / separation
}

/** A bracket on the physically supportable link stiffness, in pN/nm. */
class CrossoverLinkStiffnessBracket(
    /** The pure-tension route, `C-0194`'s `k_R` — the smallest of the routes. */
    val floor: Double,
    /** Chen et al.'s softened bond read on the displacement axis. */
    val softenedBond: Double,
    /** The connector's bending at the softest persistence length and clamped ends. */
    val bendingSoft: Double,
    /** The connector's bending at the stiffest persistence length and clamped ends. */
    val bendingStiff: Double,
    /** The largest value any combination of the routes above supports. */
    val ceiling: Double
)

/**
 * The bracket of `T-303`'s deliverable 1 — three routes, all closed form, no solver.
 *
 * The **floor** is the tension route alone, which is `CH-0242`'s premise at full attribution.
 * The **ceiling** is the larger of the two `k_θ`-independent displacement routes plus the
 * connector's bending at the stiffest persistence length and **clamped** ends, i.e. every
 * mechanism at its most favourable simultaneously. The central pair term is negative and is
 * therefore not added.
 */
fun crossoverLinkStiffnessBracket(
    hingeStiffness: Double,
    phosphateRadius: Double,
    interhelicalDistance: Double,
    thermalEnergy: Double,
    softestPersistenceLength: Double,
    stiffestPersistenceLength: Double,
    alpha: Double = 1.0
): CrossoverLinkStiffnessBracket {
    require(softestPersistenceLength <= stiffestPersistenceLength) {
        "the persistence bracket must be ordered, was: $softestPersistenceLength .. " +
                stiffestPersistenceLength
    }
    val span = crossoverSpanFloor(interhelicalDistance, phosphateRadius)
    val tension = spanDerivedLinkStiffness(hingeStiffness, phosphateRadius, interhelicalDistance)
    val bond = transverseSoftenedBondLinkStiffness(alpha)
    val soft = connectorBendingLinkStiffness(softestPersistenceLength, thermalEnergy, span, 12.0)
    val stiff = connectorBendingLinkStiffness(stiffestPersistenceLength, thermalEnergy, span, 12.0)
    return CrossoverLinkStiffnessBracket(
        floor = tension,
        softenedBond = bond,
        bendingSoft = soft,
        bendingStiff = stiff,
        ceiling = maxOf(tension, bond) + stiff
    )
}

/**
 * `T-279`'s [honeycombTiedLattice] with the link penalty exposed.
 *
 * `linkStiffness` has been a `HoneycombGrillage` constructor argument all along, so this is the
 * one argument `T-303`'s sweep costs and no element, matrix or influence bank changes. At the
 * default it is asserted to be the object `C-0180` measured rather than claimed to be.
 */
fun honeycombTiedLatticeAtLinkStiffness(
    block: HoneycombBlock,
    rowBasePairs: Int,
    enhancement: Double,
    tied: Boolean,
    linkStiffness: Double = HoneycombGrillage.RIGID_LINK_STIFFNESS,
    prestrainRadians: Double = 0.0,
    subdivisions: Int = 1,
    firstAxialSign: Int = 1
): HoneycombGrillage {
    require(prestrainRadians.isFinite()) {
        "prestrainRadians must be finite, was: $prestrainRadians"
    }
    require(linkStiffness > 0.0 && linkStiffness.isFinite()) {
        "linkStiffness must be positive and finite, was: $linkStiffness"
    }
    fun build(ties: List<HoneycombScaffoldTurnTie>) = HoneycombGrillage(
        block = block,
        rowBasePairs = rowBasePairs,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        hingeStiffnessEnhancement = enhancement,
        subdivisions = subdivisions,
        linkStiffness = linkStiffness,
        scaffoldTurnTies = ties
    )
    val bare = build(emptyList())
    return if (!tied) bare else build(
        honeycombScaffoldTurnTies(block, bare.nodesPerBeam, firstAxialSign, prestrainRadians)
    )
}

/**
 * Bisect a monotone residual on `log₁₀` of the link stiffness.
 *
 * The axis is logarithmic because `C-0194` §6's readings fall as `p90_∞ + A/k` over three decades,
 * so a linear axis puts almost the whole bracket on one side of the crossing. The bracket is
 * asserted to **straddle** before anything is bisected — `CLAUDE.md`'s own *a bisection whose
 * bracket never straddled returns its own endpoint, dressed as an answer* — and the exit is a
 * fixed iteration count, so the returned bracket width is `(log high − log low)/2ⁿ` decades by
 * construction and is quotable without a convergence claim.
 */
inline fun bisectLogLinkStiffnessThreshold(
    low: Double,
    high: Double,
    iterations: Int,
    residual: (Double) -> Double
): Double {
    require(low > 0.0 && high > low && low.isFinite() && high.isFinite()) {
        "the bracket must be a positive ordered interval, was: [$low, $high]"
    }
    require(iterations >= 1) { "iterations must be at least one, was: $iterations" }
    var lo = log10(low)
    var hi = log10(high)
    val atLow = residual(low)
    val atHigh = residual(high)
    require(atLow > 0.0 != atHigh > 0.0) {
        "the residual must change sign over [$low, $high], was: $atLow .. $atHigh"
    }
    val lowIsPositive = atLow > 0.0
    repeat(iterations) {
        val mid = 0.5 * (lo + hi)
        val here = residual(10.0.pow(mid))
        if ((here > 0.0) == lowIsPositive) lo = mid else hi = mid
    }
    return 10.0.pow(0.5 * (lo + hi))
}
