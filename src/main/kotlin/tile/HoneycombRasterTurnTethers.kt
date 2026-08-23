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
import com.xemantic.nano.plentyofroom.structure.inverseLangevin
import com.xemantic.nano.plentyofroom.structure.langevin
import kotlin.math.sinh

/**
 * `T-299` — the raster turn as the entropic **tether** the built precedent's allowance makes it.
 *
 * ## Which object this is
 *
 * `C-0175`'s [HoneycombScaffoldTurnTie] is **route A**: a raster turn carrying **zero** unpaired
 * nucleotides, which *is* a scaffold crossover — a covalent tie between two duplex ends at
 * `s = ±L/2`, carrying a dihedral spring, a normal link and an axial slip spring.
 *
 * **Route B** is the built precedent's own accounting, read off its own strand diagram by
 * `C-0193`: the scaffold occupies bases `14 → 140` of every helix and the staples `28 → 126`, so
 * the covalent phosphodiester link sits `14 bp = 4.76 nm` **outboard** of the duplex end on each
 * of the two helices a turn joins, and what stands between the two **rim nodes** is `28`
 * nucleotides of single-stranded scaffold.
 *
 * A freely-jointed chain transmits a **force** and no **moment**. So route B's turn carries
 * **no dihedral spring at all**, and that — not its softness — is the qualitative difference
 * between the two routes.
 *
 * ## The element, derived rather than chosen
 *
 * A chain held at end-to-end distance `x` on contour `L_c` and Kuhn length `b` carries
 * `f(x) = (k_BT/b)·L⁻¹(x/L_c)`, directed along its own line and always **pulling**. Linearised
 * about that taut state a central-force element is
 *
 * ```
 * K = (df/dx)·n̂n̂ᵀ + (f/x)·(I − n̂n̂ᵀ)
 * ```
 *
 * — the **tangent** along the chain and the **secant** transverse to it, which is the standard
 * geometric stiffness of a taut cable. `HoneycombGrillage` has coordinates for the relative
 * **normal** displacement `ζ` (its link residual, arm `d/2·unitY`) and the relative **axial**
 * displacement `s` (its slip residual, arm `d/2·unitZ`), and **no in-plane transverse
 * coordinate** — so `δ_y ≡ 0` by construction rather than by neglect. With
 * `n̂ = (unitY, unitZ, 0)` the decomposition collapses to two scalars,
 *
 * ```
 * E = ½[(df/dx)·unitZ² + (f/x)·unitY²]·δ_ζ² + ½(f/x)·δ_s²
 * ```
 *
 * which ride the grillage's two existing gradients unchanged.
 *
 * ## The preload is a LOAD
 *
 * `f > 0` at every `x > 0`, so the built state is **taut** and the chain applies a
 * self-equilibrated pull between its two rim nodes. `C-0104`'s rule applies verbatim: it changes
 * no entry of the stiffness matrix, the field is exactly linear in it, one solve fixes the whole
 * axis, and every influence function must be taken on `withoutPrestrain`. Its projection onto
 * the model's coordinates is `f·unitZ` times the link gradient, so the **in-plane** turns — whose
 * pull is entirely along `y` — contribute exactly **zero** preload, and the through-thickness
 * ones carry it.
 *
 * ## One-sidedness
 *
 * A tether pulls and does not push, so this is a **preloaded** element and not a spring. The
 * linearisation is taken at the undeflected block at a stated span, loop length and `(b, c)`
 * corner, where `x/L_c = 0.037–0.446` over the whole bracket; the compressive branch would need
 * the two rim nodes to close by the whole span, `2.5–4.4 nm`, against solved deflections four
 * orders smaller. `df/dx > 0` and `f/x > 0` everywhere, so the element is positive definite and
 * the solve stays linear.
 */

/** Above this argument the Langevin derivative is evaluated on `1/u² − 1/sinh²u`. */
private const val LANGEVIN_DERIVATIVE_SMALL: Double = 0.5

/** Above this argument `1/sinh²u` underflows to zero and the derivative is `1/u²`. */
private const val LANGEVIN_DERIVATIVE_LARGE: Double = 350.0

/**
 * `L′(u) = 1/u² − 1/sinh²(u)`, guarded at both ends.
 *
 * `sinh` overflows above `u ≈ 710` and its square above `u ≈ 355`, so the reciprocal is not
 * formed there at all — it is below `1e−300` and the derivative is `1/u²` to the last ulp. Below
 * `u ≈ 0.5` the difference of two large reciprocals loses every digit to cancellation (the trap
 * `CLAUDE.md` records for `1/tanh(u) − 1/u`, one derivative down), so the series is used instead.
 * `L′(0) = 1/3` exactly, which is the limiting case the tests take.
 */
fun langevinDerivative(u: Double): Double {
    require(u >= 0.0) { "the Langevin argument must not be negative, was: $u" }
    if (u < LANGEVIN_DERIVATIVE_SMALL) {
        val u2 = u * u
        return 1.0 / 3.0 - u2 / 15.0 + 2.0 * u2 * u2 / 189.0 - u2 * u2 * u2 / 675.0
    }
    if (u > LANGEVIN_DERIVATIVE_LARGE) return 1.0 / (u * u)
    val s = sinh(u)
    return 1.0 / (u * u) - 1.0 / (s * s)
}

/**
 * One raster turn's tether, as the linearised central-force element it is.
 *
 * @param span the distance in nm between the chain's two anchoring phosphates. It is an
 *   **azimuth bracket** — `d − 2r_P` aligned to `d + 2r_P` at the worst azimuth — and the worst
 *   azimuth is the **stiffest** end, therefore the adverse one for *"is the tether negligible"*.
 * @param tension `f(x)` in pN. Always positive: a chain at any `x > 0` pulls.
 * @param secantStiffness `f/x` in pN/nm, the **transverse** stiffness of a taut chain.
 * @param tangentStiffness `df/dx` in pN/nm, the stiffness along the chain's own line.
 */
data class HoneycombTetherState(
    val unpairedNucleotides: Int,
    val kuhnLength: Double,
    val contourPerNucleotide: Double,
    val span: Double,
    val contourLength: Double,
    val extensionRatio: Double,
    val tension: Double,
    val secantStiffness: Double,
    val tangentStiffness: Double
) {

    /**
     * The same state with **both** stiffnesses set to [value] — the limiting-case handle, and the
     * only way to reach the `0` and `∞` corners the gates need.
     *
     * It is deliberately not a physical state: [tension] is left where it is, so a caller taking
     * the vanishing corner must also drop the preload, and a caller taking the stiff corner is
     * asking what a **constraint** at this site would do.
     */
    fun withStiffness(value: Double): HoneycombTetherState {
        require(value >= 0.0) { "a tether stiffness must not be negative, was: $value" }
        return copy(secantStiffness = value, tangentStiffness = value)
    }

}

/**
 * The state of a freely-jointed chain of [unpairedNucleotides] nucleotides held at [span].
 *
 * `f = (k_BT/b)·L⁻¹(x/L_c)` and, with `u = L⁻¹(x/L_c)` so that `x = L_c·L(u)`,
 * `df/dx = (k_BT/b)/(L_c·L′(u))` — one bisection and two multiplications, no quadrature.
 *
 * @throws IllegalArgumentException if the chain cannot reach the span at all, which is
 *   `C-0147`'s reach bound restated where a polymer model would otherwise return infinity.
 */
fun freelyJointedTetherState(
    span: Double,
    unpairedNucleotides: Int,
    kuhnLength: Double,
    contourPerNucleotide: Double,
    thermalEnergy: Double
): HoneycombTetherState {
    require(span > 0.0) { "span must be positive, was: $span" }
    require(unpairedNucleotides > 0) {
        "unpairedNucleotides must be positive, was: $unpairedNucleotides"
    }
    require(kuhnLength > 0.0) { "kuhnLength must be positive, was: $kuhnLength" }
    require(contourPerNucleotide > 0.0) {
        "contourPerNucleotide must be positive, was: $contourPerNucleotide"
    }
    require(thermalEnergy > 0.0) { "thermalEnergy must be positive, was: $thermalEnergy" }
    val contour = unpairedNucleotides * contourPerNucleotide
    require(span < contour) {
        "a $unpairedNucleotides nt chain of contour $contour nm cannot reach $span nm"
    }
    val ratio = span / contour
    val u = inverseLangevin(ratio)
    val tension = thermalEnergy * u / kuhnLength
    return HoneycombTetherState(
        unpairedNucleotides = unpairedNucleotides,
        kuhnLength = kuhnLength,
        contourPerNucleotide = contourPerNucleotide,
        span = span,
        contourLength = contour,
        extensionRatio = ratio,
        tension = tension,
        secantStiffness = tension / span,
        tangentStiffness = thermalEnergy / kuhnLength / (contour * langevinDerivative(u))
    )
}

/** The Langevin function, re-exported so a caller of this file need not reach into `structure`. */
internal fun tetherLangevin(u: Double): Double = langevin(u)

/**
 * The turns of [block] as **tethers** on a lattice with [nodesPerBeam] nodes per beam.
 *
 * The site census is [honeycombScaffoldTurnTies]' own, unchanged, so a tethered lattice and a
 * tied one differ in **exactly** the element carried at 59 identical sites — which is what makes
 * the route A / route B comparison controlled rather than merely similar.
 *
 * @param withPreload whether the chain's own tension enters as a load. `false` is the state every
 *   influence function must be taken at, and it is the state the limiting-case gates use.
 */
fun honeycombScaffoldTurnTethers(
    block: HoneycombBlock,
    nodesPerBeam: Int,
    state: HoneycombTetherState,
    firstAxialSign: Int = 1,
    withPreload: Boolean = true
): List<HoneycombScaffoldTurnTether> =
    honeycombScaffoldTurnTethers(
        block, nodesPerBeam, state, state, firstAxialSign, withPreload
    )

/**
 * The turns of [block] as tethers with a **different chain at each rim**.
 *
 * `C-0200` reads the Nature paper's own staple order against the deposited design and finds the
 * `28` unpaired bases split **`12 / 16`** per helix rather than `14 / 14` — so the two **duplex**
 * ends a raster turn joins are `24` nucleotides apart at thirty turns and `32` at the other
 * thirty, whose **mean** is `C-0193`'s `28` exactly. Which rim takes which half is a free
 * convention of the reading and is swept by exchanging the two arguments.
 */
@Suppress("LongParameterList")
fun honeycombScaffoldTurnTethers(
    block: HoneycombBlock,
    nodesPerBeam: Int,
    lowRimState: HoneycombTetherState,
    highRimState: HoneycombTetherState,
    firstAxialSign: Int = 1,
    withPreload: Boolean = true
): List<HoneycombScaffoldTurnTether> {
    val turns = honeycombRasterTurnList(block, firstAxialSign)
    val ties = honeycombScaffoldTurnTies(block, nodesPerBeam, firstAxialSign)
    require(turns.size == ties.size) { "the turn census and the tie census must agree" }
    return ties.mapIndexed { index, tie ->
        val state = if (turns[index].atHighEnd) highRimState else lowRimState
        HoneycombScaffoldTurnTether(
            lowerBeam = tie.lowerBeam,
            upperBeam = tie.upperBeam,
            node = tie.node,
            secantStiffness = state.secantStiffness,
            tangentStiffness = state.tangentStiffness,
            tension = if (withPreload) state.tension else 0.0
        )
    }
}

/**
 * The recommended block's grillage with route B's 59 tethers in place of route A's 59 ties.
 *
 * Built the way [honeycombTiedLattice] builds its two states — one bare lattice to read
 * `nodesPerBeam` off, then one carrying the elements — so that nothing but the turn element can
 * differ between the three states of the comparison.
 */
@Suppress("LongParameterList")
fun honeycombTetheredLattice(
    block: HoneycombBlock,
    rowBasePairs: Int,
    enhancement: Double,
    state: HoneycombTetherState,
    withPreload: Boolean = true,
    subdivisions: Int = 1,
    firstAxialSign: Int = 1,
    linkStiffness: Double = HoneycombGrillage.RIGID_LINK_STIFFNESS
): HoneycombGrillage = honeycombTetheredLattice(
    block, rowBasePairs, enhancement, state, state, withPreload, subdivisions,
    firstAxialSign, linkStiffness
)

/** The same, with `C-0200`'s two rim chains — `24` nucleotides at one rim and `32` at the other. */
@Suppress("LongParameterList")
fun honeycombTetheredLattice(
    block: HoneycombBlock,
    rowBasePairs: Int,
    enhancement: Double,
    lowRimState: HoneycombTetherState,
    highRimState: HoneycombTetherState,
    withPreload: Boolean = true,
    subdivisions: Int = 1,
    firstAxialSign: Int = 1,
    linkStiffness: Double = HoneycombGrillage.RIGID_LINK_STIFFNESS
): HoneycombGrillage {
    fun build(tethers: List<HoneycombScaffoldTurnTether>) = HoneycombGrillage(
        block = block,
        rowBasePairs = rowBasePairs,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        hingeStiffnessEnhancement = enhancement,
        subdivisions = subdivisions,
        linkStiffness = linkStiffness,
        scaffoldTurnTethers = tethers
    )
    val bare = build(emptyList())
    return build(
        honeycombScaffoldTurnTethers(
            block, bare.nodesPerBeam, lowRimState, highRimState, firstAxialSign, withPreload
        )
    )
}
