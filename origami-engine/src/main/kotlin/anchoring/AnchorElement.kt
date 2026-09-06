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
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.PI
import kotlin.math.cosh
import kotlin.math.sinh
import kotlin.math.sqrt

/**
 * The mechanics of one anchor element, in the locked units: nm, pN, `pN·nm²` for `EI`.
 *
 * `T-12` needs three kinds of element and they behave completely differently:
 * a **rod** (a DNA duplex or a helix bundle), which is stiff along its axis and soft across it;
 * a **chain** (single-stranded DNA), which is a linear entropic spring at low extension and
 * strain-stiffens without bound as it approaches its contour length; and a **column**, which is
 * a rod that has been asked to carry compression and may buckle instead.
 */

// ---------------------------------------------------------------- end conditions

/**
 * The two end conditions a substrate-to-tile strut can plausibly have, and the two numbers
 * that follow from each.
 *
 * An origami-to-substrate attachment is **not obviously either**, which is why both are carried
 * through every result rather than one being chosen. They differ by exactly 4 in transverse
 * stiffness and by exactly 4 in buckling load, so a scheme that passes under one and fails under
 * the other has not been decided by this task.
 *
 * @property transverseFactor the `c` in `k_t = c EI/L³` for a transverse displacement of the
 *   moving (tile) end.
 * @property effectiveLengthFactor the `K` in the Euler load `P_c = π²EI/(K L)²`.
 */
enum class BeamEndCondition(
    val transverseFactor: Double,
    val effectiveLengthFactor: Double
) {

    /**
     * Built in at the substrate, free to **rotate** at the tile — a cantilever.
     * `k_t = 3EI/L³`, `K = 2`. This is the condition `C-0010`'s bracket used.
     */
    PINNED_HEAD(3.0, 2.0),

    /**
     * Built in at both ends, the tile end free to **translate** but not to rotate — the
     * clamped-guided (sway) column. `k_t = 12EI/L³`, `K = 1`. This is what a strut whose head
     * is hybridised flat into the tile's own plane actually has, as long as the tile stays flat.
     */
    GUIDED_HEAD(12.0, 1.0)
}

/**
 * The transverse stiffness in `pN/nm` at the moving end of a strut of bending rigidity
 * [bendingRigidity] in `pN·nm²`, length [length] nm and end condition [endCondition]:
 * `k_t = c EI/L³`.
 *
 * The `1/L³` is why `C-0010`'s 10 nm and 20 nm brackets differ by 8×, and it is the single
 * strongest lever in any strut-based scheme — stronger than the material, stronger than the
 * end condition, and it points the wrong way, because the length is set by the layer height.
 */
fun beamTransverseStiffness(
    bendingRigidity: Double,
    length: Double,
    endCondition: BeamEndCondition
): Double {
    require(bendingRigidity > 0.0) {
        "bendingRigidity must be positive, was: $bendingRigidity"
    }
    require(length > 0.0) { "length must be positive, was: $length" }
    return endCondition.transverseFactor * bendingRigidity / (length * length * length)
}

/**
 * The axial stiffness in `pN/nm` of a rod of stretch modulus [stretchModulus] in pN and
 * [length] nm: `k_a = S/L`.
 *
 * For a B-form duplex `S = 1100 pN` (`C-0006`, Wang et al. 1997), so a 10 nm duplex is
 * **110 pN/nm** along its axis — nearly twice the whole polymer layer's tangent stiffness under
 * the tile. That number is the reason a vertical strut cannot be part of a stroking actuator.
 */
fun rodAxialStiffness(stretchModulus: Double, length: Double): Double {
    require(stretchModulus > 0.0) { "stretchModulus must be positive, was: $stretchModulus" }
    require(length > 0.0) { "length must be positive, was: $length" }
    return stretchModulus / length
}

/**
 * The bending rigidity in `pN·nm²` of a bundle of parallel helices whose centres sit at
 * [offsets] nm from the bending neutral axis, by the parallel-axis theorem:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`EI = n EI₁ + S Σ yᵢ²`.
 *
 * The second term dominates completely: at the measured 2.69 nm interhelical distance one
 * helix contributes `S (d/2)² = 1989 pN·nm²` against its own `EI₁ = 230`, so a four-helix
 * bundle is ~39× a single duplex rather than 4×.
 */
fun bundleBendingRigidity(
    offsets: List<Double>,
    helixBendingRigidity: Double,
    stretchModulus: Double
): Double {
    require(offsets.isNotEmpty()) { "offsets must not be empty" }
    require(helixBendingRigidity > 0.0) {
        "helixBendingRigidity must be positive, was: $helixBendingRigidity"
    }
    require(stretchModulus > 0.0) { "stretchModulus must be positive, was: $stretchModulus" }
    return offsets.size * helixBendingRigidity +
            stretchModulus * offsets.sumOf { it * it }
}

/**
 * The Euler critical load in pN of a column of rigidity [bendingRigidity], [length] nm and
 * end condition [endCondition]: `P_c = π²EI/(K L)²`.
 *
 * A strut standing under the tile carries the actuation load in **compression**, so this is
 * not an academic limit: it is the load at which the element the scheme depends on stops
 * being a spring.
 */
fun eulerBucklingLoad(
    bendingRigidity: Double,
    length: Double,
    endCondition: BeamEndCondition
): Double {
    require(bendingRigidity > 0.0) {
        "bendingRigidity must be positive, was: $bendingRigidity"
    }
    require(length > 0.0) { "length must be positive, was: $length" }
    val effective = endCondition.effectiveLengthFactor * length
    return PI * PI * bendingRigidity / (effective * effective)
}

/**
 * The transverse stiffness of a strut of unloaded stiffness [unloadedStiffness] carrying an
 * axial **compression** [compression], against its critical load [criticalLoad]:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`k(P) = k₀ (1 − P/P_c)`.
 *
 * The standard first-order geometric-stiffness result, and it is the whole of the argument
 * against a load-bearing vertical strut: the same compression that the actuator applies
 * **softens the element laterally**, and at the Euler load the lateral stiffness is gone.
 *
 * Past the critical load nothing is returned: the column has buckled and a linearised
 * stiffness is no longer a description of it.
 */
fun compressedTransverseStiffness(
    unloadedStiffness: Double,
    compression: Double,
    criticalLoad: Double
): Double {
    require(unloadedStiffness > 0.0) {
        "unloadedStiffness must be positive, was: $unloadedStiffness"
    }
    require(criticalLoad > 0.0) { "criticalLoad must be positive, was: $criticalLoad" }
    require(compression >= 0.0) { "compression must not be negative, was: $compression" }
    require(compression <= criticalLoad) {
        "the column has buckled: compression $compression exceeds the critical load $criticalLoad"
    }
    return unloadedStiffness * (1.0 - compression / criticalLoad)
}

// ---------------------------------------------------------------- the entropic chain

/**
 * A freely jointed chain of contour length [contourLength] nm and Kuhn length [kuhnLength] nm
 * at [temperature] — the model of a single-stranded DNA tether.
 *
 * Three stiffnesses live on this object and they are **not** the same number, which is exactly
 * where `C-0010`'s bracket went wrong:
 *
 * - [gaussianStiffness] `= 3k_BT/(L_c b)` — the linear spring the chain *is* at low extension,
 *   isotropic in all three Cartesian directions because a Gaussian chain's components are
 *   independent;
 * - [transverseStiffness] `= f/x` — the restoring stiffness against motion **across** the
 *   tether, which is the one a laterally wandering tile feels;
 * - [tangentStiffness] `= df/dx` — the stiffness **along** the tether, which is what the
 *   actuator has to overcome.
 *
 * At vanishing force all three coincide, because there the chain is a linear spring. As the
 * chain is pulled out they separate without bound, and [secantToTangentRatio] says which way.
 */
class FreelyJointedChain(
    val contourLength: Double,
    val kuhnLength: Double,
    val temperature: Double = ROOM_TEMPERATURE
) {

    init {
        require(contourLength > 0.0) { "contourLength must be positive, was: $contourLength" }
        require(kuhnLength > 0.0) { "kuhnLength must be positive, was: $kuhnLength" }
    }

    private val energy: Double = thermalEnergy(temperature)

    /** The number of Kuhn segments, `L_c/b` — the chain's only dimensionless parameter. */
    val segments: Double get() = contourLength / kuhnLength

    /** `3k_BT/(L_c b)` in `pN/nm`: the linear spring constant of the unstretched chain. */
    val gaussianStiffness: Double get() = 3.0 * energy / (contourLength * kuhnLength)

    /**
     * The end-to-end extension in nm at tension [force] pN:
     * `x = L_c [coth(u) − 1/u]` with `u = f b/k_BT`.
     *
     * The Langevin bracket loses every significant digit to cancellation below `u ≈ 1e-2`
     * — the same failure mode `CLAUDE.md` records for `1 − tanh(x)/x` — so it is replaced
     * there by its series `u/3 − u³/45 + 2u⁵/945`.
     */
    fun extension(force: Double): Double {
        require(force >= 0.0) { "force must not be negative, was: $force" }
        return contourLength * langevin(force * kuhnLength / energy)
    }

    /**
     * The tension in pN at end-to-end extension [extension] nm, by bisection on the
     * monotone [extension] function.
     *
     * Exits on the **bracket width**, never on a residual: `CLAUDE.md` records an
     * unreachable residual tolerance silently running its full iteration cap.
     */
    fun tension(extension: Double): Double {
        require(extension >= 0.0) { "extension must not be negative, was: $extension" }
        require(extension < contourLength) {
            "extension $extension must be below the contour length $contourLength"
        }
        if (extension == 0.0) return 0.0
        var low = 0.0
        var high = 3.0 * energy * extension / (contourLength * kuhnLength) + energy / kuhnLength
        while (extension(high) < extension) high *= 2.0
        repeat(200) {
            val middle = 0.5 * (low + high)
            if (extension(middle) < extension) low = middle else high = middle
            if (high - low <= 1e-14 * high) return 0.5 * (low + high)
        }
        return 0.5 * (low + high)
    }

    /**
     * The **transverse** stiffness `f/x` in `pN/nm` at tension [force] — the secant of the
     * force-extension curve, and the stiffness that opposes lateral motion of a tile held at
     * the far end of the tether.
     *
     * This is the quantity `C-0010` reported as "essentially nothing at zero tension". It is
     * nothing only in the limit `L_c b ≫ x²`, where the chain is slack; at the extension the
     * §3 geometry actually imposes it equals the Gaussian spring constant, and that is a
     * number of order the requirement. See `CH-0013`.
     */
    fun transverseStiffness(force: Double): Double {
        require(force >= 0.0) { "force must not be negative, was: $force" }
        if (force == 0.0) return gaussianStiffness
        return force / extension(force)
    }

    /**
     * The **axial** tangent stiffness `df/dx` in `pN/nm` at tension [force], from the closed
     * form `dx/df = (L_c b/k_BT)[1/u² − csch²u]`, series-expanded at small `u` for the same
     * cancellation reason as [extension].
     */
    fun tangentStiffness(force: Double): Double {
        require(force >= 0.0) { "force must not be negative, was: $force" }
        val u = force * kuhnLength / energy
        val derivative = when {
            u < 1e-2 -> 1.0 / 3.0 - u * u / 15.0 + 2.0 * u * u * u * u / 189.0
            // above u ~ 20 sinh overflows to infinity and its reciprocal square underflows to
            // zero, which is the correct limit but returns NaN if evaluated as cosh/sinh
            u > 20.0 -> 1.0 / (u * u)
            else -> {
                val s = sinh(u)
                1.0 / (u * u) - 1.0 / (s * s)
            }
        }
        return energy / (contourLength * kuhnLength * derivative)
    }

    private fun langevin(u: Double): Double = when {
        u < 1e-2 -> u / 3.0 - u * u * u / 45.0 + 2.0 * u * u * u * u * u / 945.0
        // coth(u) is 1 to within 1e-17 above u = 20, and cosh/sinh both overflow there and
        // return NaN rather than the limit — the same trap `brinkmanShearDrag` documents
        u > 20.0 -> 1.0 - 1.0 / u
        else -> cosh(u) / sinh(u) - 1.0 / u
    }
}

// ---------------------------------------------------------------- the cable term

/**
 * The tension in pN induced in a **surface-parallel** tether of stretch modulus
 * [stretchModulus] pN and in-plane span [length] nm when the tile it holds moves normal to the
 * surface by [normalOffset] nm.
 *
 * Purely geometric — the chord between the two ends grows to `√(L² + δ²)` while the tether's
 * unstressed length does not — and it is the one cost an in-plane scheme cannot design away:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`T = S (√(L² + δ²) − L)/L ≈ S δ²/(2L²)`.
 *
 * It grows as the **square** of the stroke, so a scheme that is comfortable at §3's acceptable
 * 3 nm can be past the duplex allowables at §3's desired 10 nm.
 */
fun cableTension(stretchModulus: Double, length: Double, normalOffset: Double): Double {
    require(stretchModulus > 0.0) { "stretchModulus must be positive, was: $stretchModulus" }
    require(length > 0.0) { "length must be positive, was: $length" }
    require(normalOffset >= 0.0) { "normalOffset must not be negative, was: $normalOffset" }
    val chord = sqrt(length * length + normalOffset * normalOffset)
    return stretchModulus * (chord - length) / length
}

/**
 * The normal (out-of-plane) force in pN that the same tether exerts back on the tile:
 * `F_z = T sin α` with `sin α = δ/√(L² + δ²)`, i.e. `≈ S δ³/(2L³)`.
 *
 * Cubic in the stroke, so it is negligible at small displacement and the dominant anchor cost
 * at large one. This is the trade between lateral confinement and stroke, and it is the
 * quantity `T-2`'s window has to carry.
 */
fun cableNormalForce(stretchModulus: Double, length: Double, normalOffset: Double): Double {
    val chord = sqrt(length * length + normalOffset * normalOffset)
    return cableTension(stretchModulus, length, normalOffset) * normalOffset / chord
}

/**
 * The **secant** normal stiffness `F_z/δ` in `pN/nm` of the same tether at offset
 * [normalOffset] — the quantity that is subtracted from the actuator over a stroke of that
 * size. Zero at zero offset, which is why it does not appear in a linearised anchor budget
 * and has to be added by hand.
 */
fun cableNormalSecantStiffness(
    stretchModulus: Double,
    length: Double,
    normalOffset: Double
): Double {
    require(normalOffset > 0.0) { "normalOffset must be positive, was: $normalOffset" }
    return cableNormalForce(stretchModulus, length, normalOffset) / normalOffset
}

// ---------------------------------------------------------------- material constants

/**
 * The element material parameters `T-12` uses, each carrying its provenance.
 *
 * The duplex numbers are **cited** from `C-0006`'s own set rather than re-sourced, because
 * they are the same physical objects; they are reproduced here rather than imported so that
 * this package does not depend on a package two other agents are editing concurrently.
 * `C-0006`'s [structure][com.xemantic.nano.plentyofroom.structure.Gen1Tile] remains the
 * authority and the gate-5 tests assert against it.
 */
object AnchorMaterials {

    /** `EI` of a B-form duplex in `pN·nm²` — **CITED**, CanDo (Kim et al., *NAR* **40**:2862, 2012). */
    const val CANDO_BENDING_RIGIDITY: Double = 230.0

    /**
     * `EI` implied by the persistence length **measured with Mg²⁺**, `L_p = 40 nm`
     * (Wang et al., *Biophys. J.* **72**:1335, 1997), through `EI = L_p k_BT`.
     * 28 % below CanDo's model input, and it is the buffer §3 actually specifies.
     */
    val MAGNESIUM_BENDING_RIGIDITY: Double = 40.0 * thermalEnergy()

    /** Stretch modulus of a B-form duplex in pN — **CITED, MEASURED**, Wang et al. (1997). */
    const val DUPLEX_STRETCH_MODULUS: Double = 1100.0

    /** `GJ` of a B-form duplex in `pN·nm²` — **CITED**, CanDo (2012). */
    const val DUPLEX_TORSIONAL_RIGIDITY: Double = 460.0

    /** Interhelical distance of a single-layer sheet in nm — **CITED, MEASURED** (SAXS), Fischer et al. (2016). */
    const val INTERHELICAL_DISTANCE: Double = 2.69

    /** Rise per base pair in nm — **CITED**, Douglas et al. (2009). */
    const val RISE_PER_BASE_PAIR: Double = 0.34
}

/**
 * Single-stranded DNA as an entropic tether, in the buffer §3 actually specifies.
 *
 * Every number here was read from the primary source for this task, per `CLAUDE.md`'s research
 * practice, rather than recalled — and the recalled one would have been wrong for this regime.
 *
 * **The Kuhn length is a bracket and it is method-systematic, not noisy.**
 * Force spectroscopy in real MgCl₂ (Bosco, Camunas-Soler & Ritort, *Nucleic Acids Res.*
 * **42**:2064 (2014), Table 4, optical tweezers, **MEASURED**, fits over **10–40 pN**) gives
 * `L_K = 1.41 ± 0.03 nm` at 2 mM, `1.41 ± 0.05` at 4 mM and `1.34 ± 0.04` at 10 mM MgCl₂.
 * Zero-force scattering (Chen et al., *PNAS* **109**:799 (2012), SAXS + smFRET on dT₄₀,
 * **MEASURED**) gives `l_p = 1.05–1.42 nm` over the same ionic strengths, i.e. `b = 2l_p =
 * 2.1–2.84 nm` — twice the force-spectroscopy value.
 *
 * **The tethers in this task carry ~1 pN, an order of magnitude below the lowest force the
 * spectroscopy fits cover, so the zero-force end of the bracket is the applicable one** — and
 * it is also the soft end, i.e. the conservative one for a confinement requirement. Both ends
 * are carried through every result. Chen et al. further note that a *surface-tethered* chain
 * measures ~50 % stiffer in `l_p` than the same chain free in solution, so the upper bound is
 * a lower bound on what a real tether would show.
 */
object SsDnaTether {

    /** Kuhn length in nm at 10 mM MgCl₂, from 10–40 pN force spectroscopy — **CITED, MEASURED**, Bosco et al. (2014) Table 4. */
    const val KUHN_LENGTH_FORCE_SPECTROSCOPY: Double = 1.34

    /** Kuhn length in nm at 2 mM MgCl₂, same source and same convention. */
    const val KUHN_LENGTH_FORCE_SPECTROSCOPY_TWO_MILLIMOLAR: Double = 1.41

    /** Kuhn length in nm at ionic strength 30 mM (10 mM MgCl₂) from zero-force SAXS/smFRET — **CITED, MEASURED**, Chen et al. (2012). */
    const val KUHN_LENGTH_ZERO_FORCE: Double = 2.10

    /** Kuhn length in nm at ionic strength 6 mM (2 mM MgCl₂), same source. */
    const val KUHN_LENGTH_ZERO_FORCE_TWO_MILLIMOLAR: Double = 2.84

    /**
     * Contour length per nucleotide in nm, **inextensible** convention — **CITED, MEASURED**:
     * 0.65 ± 0.07 nm (Sim et al., *Phys. Rev. E* **86**:021901, 2012, SAXS) and 0.68–0.70 nm
     * (Bosco et al. 2014, inextensible WLC fits, NaCl and MgCl₂).
     *
     * **The convention travels with the number.** Bosco et al. state that an *extensible* fit
     * needs the shorter crystallographic 0.57 nm instead, "with respect to the experimentally
     * reported range (0.60–0.70 nm)" — mixing the two double-counts the extension. The chains
     * here are modelled inextensibly, which at ~1 pN against a 630–710 pN stretch modulus
     * costs 0.15 % of extension, so 0.65 nm/nt is the consistent choice.
     */
    const val CONTOUR_PER_NUCLEOTIDE: Double = 0.65

    /** The lower end of the contour-per-nucleotide bracket, the crystallographic/extensible convention. */
    const val CONTOUR_PER_NUCLEOTIDE_MIN: Double = 0.57

    /** The upper end, the inextensible-WLC fits of Bosco et al. (2014). */
    const val CONTOUR_PER_NUCLEOTIDE_MAX: Double = 0.70

    /**
     * Stretch modulus in pN — **CITED, MEASURED**, Bosco et al. (2014) Table 4: 630 ± 60 at
     * 2 mM, 670 ± 50 at 4 mM, 710 ± 60 at 10 mM MgCl₂. Smith, Cui & Bustamante,
     * *Science* **271**:795 (1996) report 800 pN at 150 mM Na⁺, which sits at the top of the
     * measured range. Not used in the inextensible model; recorded because its size is what
     * licenses leaving it out.
     */
    const val STRETCH_MODULUS: Double = 670.0
}
