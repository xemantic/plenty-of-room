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
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

/**
 * Task `T-72`, covering `T-66` — the **triangulated standoff**, priced as a *stability* remedy.
 *
 * ## Why the question changed shape
 *
 * `C-0028` raised `T-66` as a **rigidity** question: the literature's only rigid out-of-plane
 * mounting is triangulated — Pumm et al.'s inclined plates *"were held rigidly at this angle with
 * a **set** of double-helical spacers"* — so would a truss give the standoff a base couple a
 * single duplex cannot?
 *
 * `C-0029` changed it, by a **counting theorem**. A duplex END presents exactly two strand
 * termini, so a base joint has at most **two** covalent links on a chord of half-width
 * `≤ r_P = 1.0 nm`. Two links on a chord restrain **one** axis — up to
 * `2k_bond,θ + 2k_bond,s r_P²` = 78.24 pN·nm/rad about the chord's perpendicular bisector — and
 * leave the orthogonal one with `2k_bond,θ` = 13.53, which *is* `C-0028`'s `B1`. **A column
 * buckles about its softest axis**, so `P6` fails at every length and the branch closes at §3's
 * desired stroke.
 *
 * So a truss is no longer wanted for rigidity in the loaded plane. It is wanted to restore **the
 * axis the two-link base leaves free**.
 *
 * ## The one structural fact this file is built on
 *
 * `n` legs sharing a rigid head cap act **in parallel** in bending, and their **axial**
 * stiffnesses at their offsets add a *frame couple* to the head's rotation and to nothing else —
 * a rigid-body rotation `φ` about the centroid stretches leg `i` by `∓d_i φ` and translates none
 * of them. So, in either plane,
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;**`K_truss = n·K_leg + [[0, 0], [0, k_frame]]`**, &nbsp;
 * `k_frame = series(k_a Σd_i², k_tie)`, &nbsp; `k_a = series(S/ℓ, k_z,base)`.
 *
 * `Σd_i²` is a **rank-one tensor on the leg offsets**, so for a two-leg row of separation `w` at
 * azimuth `θ` to the flexure's axis
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`Σx_i² = (w²/2)cos²θ`, &nbsp; `Σy_i² = (w²/2)sin²θ`, &nbsp;
 * **`Σx_i² + Σy_i² = w²/2` identically.**
 *
 * **The truss has a fixed budget of frame couple and the azimuth spends it**, on the loaded plane
 * (where it destroys `C-0030`'s draw-in supply) or on the free plane (where it buys the stability
 * `C-0029` closed the branch for). That conservation identity is why a *partially* triangulated
 * head exists at all, and it is asserted as a gate-3 test.
 *
 * ## Geometry and sign conventions
 *
 * The sheet is the `x–y` plane and `z` its normal; every leg runs along `+z` with length `ℓ`.
 * **`x` is the flexure's own axis**, so the **loaded** plane is `x–z` — head coordinates
 * `(u_x, φ_y)`, which is exactly the pair `C-0030`'s [CoupledJointFlexure] consumes, and `u_x` is
 * `C-0028`'s sway, which **is** the flexure's draw-in. The **free** plane is `y–z`, coordinates
 * `(u_y, φ_x)`: nothing loads it, and it is the plane the column buckles in. Each leg's base
 * chord is laid along `x`, so the base's strong constant restrains `φ_y` and its weak one `φ_x`.
 */

// ---------------------------------------------------------------- the layout

/**
 * One leg's planar offset from the truss head's centroid, in nm.
 *
 * @property alongFlexure `x` — along the flexure's own axis, i.e. **in** the loaded plane, where a
 *   frame couple costs draw-in.
 * @property acrossFlexure `y` — across it, i.e. in the **free** plane, where a frame couple buys
 *   stability.
 */
data class LegOffset(val alongFlexure: Double, val acrossFlexure: Double) {

    /** The offset's distance from the centroid, in nm. */
    val radius: Double get() = hypot(alongFlexure, acrossFlexure)
}

/**
 * Where a truss's legs stand, as offsets from the head centroid.
 *
 * The centroid is required to be at the origin: a truss whose legs are not balanced about the
 * point the load enters at is a different (and eccentrically loaded) problem, and silently
 * re-centring one would hide that.
 */
data class TrussLayout(
    val name: String,
    val legs: List<LegOffset>,
    val provenance: String = ""
) {

    init {
        require(legs.isNotEmpty()) { "a truss must have at least one leg" }
        val along = legs.sumOf { it.alongFlexure }
        val across = legs.sumOf { it.acrossFlexure }
        val scale = legs.maxOf { it.radius }.coerceAtLeast(1.0)
        require(abs(along) <= 1.0e-12 * scale * legs.size) {
            "the layout's centroid must lie at the origin, along-axis residual: $along"
        }
        require(abs(across) <= 1.0e-12 * scale * legs.size) {
            "the layout's centroid must lie at the origin, across-axis residual: $across"
        }
    }

    val legCount: Int get() = legs.size

    /** `Σx_i²` in nm² — the **loaded** plane's second moment, which costs draw-in. */
    val alongSecondMoment: Double get() = legs.sumOf { it.alongFlexure * it.alongFlexure }

    /** `Σy_i²` in nm² — the **free** plane's second moment, which buys stability. */
    val acrossSecondMoment: Double get() = legs.sumOf { it.acrossFlexure * it.acrossFlexure }

    /** `Σd_i²` — the conserved budget, invariant under the row's azimuth. */
    val totalSecondMoment: Double get() = alongSecondMoment + acrossSecondMoment

    /** The largest `|x_i|`, which is the leg the head moment loads hardest. */
    val maximumAlongOffset: Double get() = legs.maxOf { abs(it.alongFlexure) }

    /** The closest approach of any two leg axes, in nm; `+∞` for a single leg. */
    val minimumLegSeparation: Double
        get() {
            if (legs.size < 2) return Double.POSITIVE_INFINITY
            var smallest = Double.MAX_VALUE
            for (i in legs.indices) for (j in i + 1 until legs.size) {
                val gap = hypot(
                    legs[i].alongFlexure - legs[j].alongFlexure,
                    legs[i].acrossFlexure - legs[j].acrossFlexure
                )
                if (gap < smallest) smallest = gap
            }
            return smallest
        }

    /**
     * Whether no two legs overlap — two B-form duplexes cannot stand closer than one duplex
     * diameter, which is [Gen1Tile.INTERHELICAL_SHEET]'s own lower relative, `2R = 2.0 nm`.
     */
    val stericallyRealisable: Boolean
        get() = minimumLegSeparation >= 2.0 * BForm.DUPLEX_RADIUS

    /** The plan-view area in nm² the leg bases occupy, as `n` duplex cross-sections. */
    val footprintArea: Double
        get() = legCount * 4.0 * BForm.DUPLEX_RADIUS * BForm.DUPLEX_RADIUS

    companion object {

        /** `C-0029`'s single standoff — the object every limiting case reduces to. */
        fun single(): TrussLayout = TrussLayout(
            name = "single standoff",
            legs = listOf(LegOffset(0.0, 0.0)),
            provenance = "C-0029's standoff, carried so the truss reduces to it exactly"
        )

        /**
         * [count] legs in a straight row of pitch [separation], laid at [azimuth] radians to the
         * flexure's own axis — `0` along it (the frame couple falls entirely in the **loaded**
         * plane) and `π/2` across it (entirely in the **free** plane).
         *
         * Components below a picometre are snapped to exactly zero. That is not a numerical
         * convenience but a statement about the lattice: a leg offset is a multiple of a base-pair
         * rise or of an interhelical distance, and `6e-17 nm` is not a placement.
         */
        fun row(
            count: Int,
            separation: Double,
            azimuth: Double,
            name: String = "$count-leg row"
        ): TrussLayout {
            require(count >= 1) { "count must be at least one, was: $count" }
            require(separation > 0.0) { "separation must be positive, was: $separation" }
            val c = snap(cos(azimuth))
            val s = snap(sin(azimuth))
            val legs = (0 until count).map {
                val t = (it - (count - 1) / 2.0) * separation
                LegOffset(snap(t * c), snap(t * s))
            }
            return TrussLayout(name, legs, "lattice placement, pitch $separation nm")
        }

        /**
         * A three-leg **triangle**: two legs across the flexure axis at `±[across]/2` and one
         * along it at [along] — the *fully* triangulated head, which restrains both axes and is
         * the one the draw-in has to be paid for on.
         */
        fun triangle(
            across: Double,
            along: Double,
            name: String = "triangle"
        ): TrussLayout {
            require(across > 0.0) { "across must be positive, was: $across" }
            require(along > 0.0) { "along must be positive, was: $along" }
            // centroid at the origin: the apex sits at +2h/3 and the base pair at -h/3
            return TrussLayout(
                name,
                listOf(
                    LegOffset(2.0 * along / 3.0, 0.0),
                    LegOffset(-along / 3.0, across / 2.0),
                    LegOffset(-along / 3.0, -across / 2.0)
                ),
                "lattice placement, base $across nm, height $along nm"
            )
        }

        /** A four-leg rectangle, [along] by [across] — the fully triangulated head at `n = 4`. */
        fun rectangle(
            along: Double,
            across: Double,
            name: String = "rectangle"
        ): TrussLayout {
            require(along > 0.0) { "along must be positive, was: $along" }
            require(across > 0.0) { "across must be positive, was: $across" }
            return TrussLayout(
                name,
                listOf(
                    LegOffset(along / 2.0, across / 2.0),
                    LegOffset(along / 2.0, -across / 2.0),
                    LegOffset(-along / 2.0, across / 2.0),
                    LegOffset(-along / 2.0, -across / 2.0)
                ),
                "lattice placement, $along x $across nm"
            )
        }

        private fun snap(value: Double): Double = if (abs(value) < 1.0e-12) 0.0 else value
    }
}

// ---------------------------------------------------------------- the base, on both its axes

/**
 * `C-0029`'s two-link base, carried on **both** its axes at once — which is the whole point: a
 * chord's couple is a rank-one tensor, so the strong and the free reading are not alternatives a
 * designer picks between but simultaneous facts about the same joint.
 *
 * @property restrainedAxis `k_θ` in pN·nm/rad about the chord's perpendicular bisector.
 * @property freeAxis `k_θ` about the chord itself — `2 k_bond,θ`, which reproduces `C-0028`'s `B1`.
 * @property axial `k_z` in pN/nm normal to the sheet.
 */
data class TwoLinkBase(
    val name: String,
    val restrainedAxis: Double,
    val freeAxis: Double,
    val axial: Double,
    val provenance: String = ""
) {

    init {
        require(restrainedAxis > 0.0) { "restrainedAxis must be positive, was: $restrainedAxis" }
        require(freeAxis > 0.0) { "freeAxis must be positive, was: $freeAxis" }
        require(axial > 0.0) { "axial must be positive, was: $axial" }
        require(restrainedAxis >= freeAxis) {
            "the restrained axis cannot be softer than the free one"
        }
    }

    companion object {

        /**
         * The base a **realisable** perpendicular junction supplies — `C-0029`'s two termini on
         * the terminal chord, read on both axes, from that claim's own
         * [realisablePerpendicularBase].
         *
         * The default is the **hard, convention-free** 180° reading, `2 r_P` of chord, which is
         * the bound no groove convention can move.
         */
        fun realisable(
            backbone: DuplexBackbone = DuplexBackbone(minorGrooveAngle = 180.0),
            alpha: Double = 1.0,
            inPlaneMultiplier: Double = 1.0,
            misalignment: Double = 0.0
        ): TwoLinkBase {
            val strong = realisablePerpendicularBase(
                backbone, true, alpha, inPlaneMultiplier, misalignment
            )
            val weak = realisablePerpendicularBase(
                backbone, false, alpha, inPlaneMultiplier, misalignment
            )
            return TwoLinkBase(
                name = "two-terminus junction, chord ${"%.3f".format(backbone.terminalChord)} nm",
                restrainedAxis = strong.rotationalStiffness,
                freeAxis = weak.rotationalStiffness,
                axial = strong.axialStiffness,
                provenance = strong.provenance
            )
        }

        /**
         * `C-0028`'s `B2` — two antiparallel crossovers to adjacent sheet duplexes, on both its
         * orientations. **`C-0029` shows it is not realisable** (it needs a 1.345 nm lever arm out
         * of a 1.0 nm backbone radius); it is carried here only so that `C-0030`'s filed design
         * can be reproduced as a gate-5 test.
         */
        fun c0028TwoCrossovers(alpha: Double = 1.0, inPlaneMultiplier: Double = 1.0): TwoLinkBase {
            val strong = StandoffBase.crossovers(2, true, alpha, inPlaneMultiplier)
            val weak = StandoffBase.crossovers(2, false, alpha, inPlaneMultiplier)
            return TwoLinkBase(
                name = "C-0028 B2 (NOT realisable, CH-0039)",
                restrainedAxis = strong.rotationalStiffness,
                freeAxis = weak.rotationalStiffness,
                axial = strong.axialStiffness,
                provenance = "C-0028's B2, retained only to reproduce C-0030's filed design"
            )
        }
    }
}

// ---------------------------------------------------------------- the frame

/**
 * One leg's **axial** stiffness in pN/nm — its own `S/ℓ` in series with its base's `k_z`.
 *
 * `C-0028`'s series discipline, and it matters here more than it did there: this is the constant
 * the whole frame couple is built from, and it is **softer than either member**.
 */
fun legAxialStiffness(
    length: Double,
    base: TwoLinkBase,
    stretchModulus: Double = Gen1Tile.DUPLEX_STRETCH_MODULUS
): Double {
    require(length > 0.0) { "length must be positive, was: $length" }
    require(stretchModulus > 0.0) { "stretchModulus must be positive, was: $stretchModulus" }
    return seriesStiffness(stretchModulus / length, base.axial)
}

/**
 * The **frame couple** in pN·nm/rad a truss presents at its head in one plane:
 * `series(k_a Σd_i², k_tie)`.
 *
 * The head tie is **not** assumed rigid. The cap is a real body — whatever ties the leg heads to
 * each other and to the flexure's end — and its own rotational stiffness is in series with the
 * legs' axial couple. `C-0029`'s open question about the guided arm, in a new place: an assumed
 * rigid connector is exactly the sort of thing that turned out to be the binding constraint.
 */
fun trussFrameCouple(
    secondMoment: Double,
    legAxialStiffness: Double,
    headTieStiffness: Double = Double.POSITIVE_INFINITY
): Double {
    require(secondMoment >= 0.0) { "secondMoment must not be negative, was: $secondMoment" }
    require(legAxialStiffness >= 0.0) {
        "legAxialStiffness must not be negative, was: $legAxialStiffness"
    }
    require(headTieStiffness >= 0.0) {
        "headTieStiffness must not be negative, was: $headTieStiffness"
    }
    return seriesStiffness(legAxialStiffness * secondMoment, headTieStiffness)
}

// ---------------------------------------------------------------- the assembled head

private fun invert(
    c11: Double, c12: Double, c21: Double, c22: Double
): DoubleArray {
    val determinant = c11 * c22 - c12 * c21
    require(determinant > 0.0) { "the 2 x 2 is not invertible, determinant: $determinant" }
    return doubleArrayOf(c22 / determinant, -c12 / determinant, -c21 / determinant, c11 / determinant)
}

private fun assemble(
    leg: StandoffTipFlexibility,
    legCount: Int,
    frameCouple: Double
): StandoffTipFlexibility {
    val k = invert(
        leg.translationUnderForce, leg.translationUnderMoment,
        leg.rotationUnderForce, leg.rotationUnderMoment
    )
    val n = legCount.toDouble()
    val assembled = invert(n * k[0], n * k[1], n * k[2], n * k[3] + frameCouple)
    return StandoffTipFlexibility(
        translationUnderForce = assembled[0],
        translationUnderMoment = assembled[1],
        rotationUnderForce = assembled[2],
        rotationUnderMoment = assembled[3]
    )
}

/**
 * The **assembled** 2 × 2 tip flexibility of a truss head: [legCount] legs of rigidity
 * [bendingRigidity] and [length] on bases of rotational stiffness [baseRotationalStiffness],
 * sharing a cap that supplies [frameCouple].
 *
 * Two limits make it a strict generalisation rather than a different model, and both are asserted:
 *
 * - `legCount = 1`, `frameCouple = 0` returns `C-0030`'s [standoffTipFlexibility] **entry by
 *   entry**;
 * - `frameCouple = 0` at any `n` returns that flexibility divided by `n` exactly — `n` springs in
 *   parallel, the correlation untouched.
 *
 * And as `frameCouple → ∞` the off-diagonal goes to **zero**: a fully triangulated head supplies
 * no draw-in at all, and its sway stiffness is the *rotation-fixed* reading, exactly 4× the
 * other-load-zero one at a clamped base.
 */
fun trussTipFlexibility(
    bendingRigidity: Double,
    length: Double,
    baseRotationalStiffness: Double,
    legCount: Int,
    frameCouple: Double
): StandoffTipFlexibility {
    require(legCount >= 1) { "legCount must be at least one, was: $legCount" }
    require(frameCouple >= 0.0) { "frameCouple must not be negative, was: $frameCouple" }
    return assemble(
        standoffTipFlexibility(bendingRigidity, length, baseRotationalStiffness),
        legCount, frameCouple
    )
}

/**
 * The same assembly, with each leg's flexibility obtained by **quadrature** rather than in closed
 * form — so that Maxwell-Betti can be asserted on the *assembled* object.
 *
 * `C12` reaches the assembly through a **double** cumulative-Simpson integration and `C21` through
 * a **single** one; the legs are then inverted, summed with the frame couple and inverted back.
 * Nothing in that route forces the assembled off-diagonals to be equal.
 */
fun trussTipFlexibilityByIntegration(
    bendingRigidity: Double,
    length: Double,
    baseRotationalStiffness: Double,
    legCount: Int,
    frameCouple: Double,
    steps: Int = 1024
): StandoffTipFlexibility {
    require(legCount >= 1) { "legCount must be at least one, was: $legCount" }
    require(frameCouple >= 0.0) { "frameCouple must not be negative, was: $frameCouple" }
    return assemble(
        standoffTipFlexibilityByIntegration(
            bendingRigidity, length, baseRotationalStiffness, steps
        ),
        legCount, frameCouple
    )
}

// ---------------------------------------------------------------- the truss's stability

/**
 * The truss's critical **total** axial load in pN in one plane, `n·u²EI/ℓ²`.
 *
 * The cap enforces one common head rotation, so the energy of the assembly is `n` times a single
 * column's plus `½ k_frame φ²` — i.e. **per column the external head spring is `k_frame/n`** — and
 * `u` is the first root of `C-0028`'s own sway determinant
 * `sin u(u² − ρ_bρ_h) − cos u(ρ_b + ρ_h)u` at `ρ_b = k_θb ℓ/EI` and `ρ_h = (k_frame/n)ℓ/EI`.
 *
 * The head is otherwise **free to translate**, which is not conservatism but a requirement: the
 * head's translation in the loaded plane **is** the flexure's draw-in, and `C-0028` establishes
 * that holding it is `C-0023`'s *ends held axially* reading that the whole of `T-30` was spent
 * escaping.
 */
fun trussBucklingLoad(
    bendingRigidity: Double,
    length: Double,
    baseRotationalStiffness: Double,
    legCount: Int,
    frameCouple: Double,
    scanSteps: Int = 512
): Double {
    require(bendingRigidity > 0.0) { "bendingRigidity must be positive, was: $bendingRigidity" }
    require(length > 0.0) { "length must be positive, was: $length" }
    require(legCount >= 1) { "legCount must be at least one, was: $legCount" }
    require(frameCouple >= 0.0) { "frameCouple must not be negative, was: $frameCouple" }
    val baseRestraint = baseRestraintParameter(baseRotationalStiffness, bendingRigidity, length)
    val headRestraint = frameCouple / legCount * length / bendingRigidity
    return legCount * standoffBucklingLoad(
        bendingRigidity, length, baseRestraint, headRestraint, scanSteps
    )
}

// ---------------------------------------------------------------- the whole head

/**
 * A truss head: [layout]'s legs, each of [length], each on a `C-0029`-legal two-link [base], tied
 * at the cap by [headTieStiffness].
 *
 * It exposes the two planes side by side, because that is the finding: the loaded plane is where
 * the draw-in is supplied and the free plane is where the column buckles, and the leg row's
 * **azimuth** decides which of them the truss's one budget of frame couple is spent on.
 */
class TriangulatedStandoff(
    val layout: TrussLayout,
    val length: Double,
    val base: TwoLinkBase,
    val headTieStiffness: Double = Double.POSITIVE_INFINITY,
    val bendingRigidity: Double = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
    val stretchModulus: Double = Gen1Tile.DUPLEX_STRETCH_MODULUS
) {

    init {
        require(length > 0.0) { "length must be positive, was: $length" }
        require(bendingRigidity > 0.0) { "bendingRigidity must be positive, was: $bendingRigidity" }
        require(stretchModulus > 0.0) { "stretchModulus must be positive, was: $stretchModulus" }
        require(headTieStiffness >= 0.0) {
            "headTieStiffness must not be negative, was: $headTieStiffness"
        }
    }

    val legCount: Int get() = layout.legCount

    /** One leg's `k_a` — `S/ℓ` in series with the base's own `k_z`. */
    val legAxial: Double = legAxialStiffness(length, base, stretchModulus)

    /** `series(k_a Σx_i², k_tie)` — the frame couple in the **loaded** plane, which costs draw-in. */
    val loadedFrameCouple: Double =
        trussFrameCouple(layout.alongSecondMoment, legAxial, headTieStiffness)

    /** `series(k_a Σy_i², k_tie)` — the frame couple in the **free** plane, which buys stability. */
    val freeFrameCouple: Double =
        trussFrameCouple(layout.acrossSecondMoment, legAxial, headTieStiffness)

    /** The head's assembled 2 × 2 in the loaded plane — what `C-0030`'s beam consumes. */
    val flexibility: StandoffTipFlexibility = trussTipFlexibility(
        bendingRigidity, length, base.restrainedAxis, legCount, loadedFrameCouple
    )

    /** The head's assembled 2 × 2 in the free plane, on the base's **weak** constant. */
    val freePlaneFlexibility: StandoffTipFlexibility = trussTipFlexibility(
        bendingRigidity, length, base.freeAxis, legCount, freeFrameCouple
    )

    /** The truss's total critical load in the loaded plane, in pN. */
    val loadedCriticalLoad: Double = trussBucklingLoad(
        bendingRigidity, length, base.restrainedAxis, legCount, loadedFrameCouple
    )

    /** The truss's total critical load in the free plane, in pN. */
    val freeCriticalLoad: Double = trussBucklingLoad(
        bendingRigidity, length, base.freeAxis, legCount, freeFrameCouple
    )

    /** **The adopted `P6` number — a column buckles about its softest axis.** */
    val criticalLoad: Double get() = min(loadedCriticalLoad, freeCriticalLoad)

    /** Which plane the truss buckles in: `"free"` or `"loaded"`. */
    val governingPlane: String
        get() = if (freeCriticalLoad <= loadedCriticalLoad) "free" else "loaded"

    /** The head's transverse (normal-to-sheet) support, `n k_a` — what `P1` is written on. */
    val transverseStiffness: Double get() = legCount * legAxial

    /**
     * The **peak** compression in one leg, in pN, under a total head shear [totalShear] and a head
     * rotation [headRotation].
     *
     * The mean is `P/n`; the head moment is shared between the legs' own bending and the frame,
     * and the frame's share `k_frame·φ` is reacted by **axial** forces `x_i/Σx_i²` of it. So a row
     * laid **across** the flexure axis loads no leg axially with the head moment at all, and a row
     * laid **along** it loads its outermost leg hardest — `P6` is judged on this number, not on
     * the mean, which is `P9`.
     */
    fun peakLegCompression(totalShear: Double, headRotation: Double): Double {
        val mean = abs(totalShear) / legCount
        if (layout.alongSecondMoment <= 0.0) return mean
        return mean + loadedFrameCouple * abs(headRotation) *
                layout.maximumAlongOffset / layout.alongSecondMoment
    }
}
