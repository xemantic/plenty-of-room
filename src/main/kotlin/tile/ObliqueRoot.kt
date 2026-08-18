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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * `T-206` — what an **oblique** attachment root costs against a perpendicular one.
 *
 * ## The question `C-0122` raised and could not answer
 *
 * A honeycomb helix has **three** crossover azimuths, 120° apart, and caDNAno alternates the helix
 * orientation between the lattice's two sublattices. So along a slab's **top face** the free
 * direction alternates: half the helices carry one azimuth pointing **straight out**, and half
 * carry **two oblique** ones. `C-0122` counted them — 8 / 7 on `15 × 4`, 5 / 5 on `10 × 6` — and
 * treated every station as equivalent, because the *count* is unaffected.
 *
 * ## The cheap bound, which is the whole method
 *
 * A root's stiffness is a symmetric tensor, diagonal in its own two axes: the **radial** one along
 * its own azimuth (the direction a crossover's covalent link acts in) and the **tangential** one
 * perpendicular to it in the cross-section (the direction the crossover's dihedral hinge rotates
 * the attached body in). Loaded along the slab normal, at azimuth `ψ`,
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`1/k_z(ψ) = cos²ψ/k_radial + sin²ψ/k_tangential`,
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;**`κ(ψ) ≡ k_z(0)/k_z(ψ) = cos²ψ + sin²ψ·A`, `A ≡ k_radial/k_tangential`.**
 *
 * Three things follow with no computation at all:
 *
 * 1. **The whole question is ONE anisotropy** — the member, its ground and the material enter only
 *    through `A`.
 * 2. **`κ ≥ 1` whenever `A ≥ 1`, with equality iff `A = 1`** — an oblique root can never be
 *    *stiffer*, and it is free exactly when the root is **isotropic**.
 * 3. At the honeycomb's own `ψ = 60°`, `κ = 0.25 + 0.75 A`: **three quarters of the load path is
 *    the tangential axis**, whatever the radial one is. That is why the oblique root's *absolute*
 *    stiffness is quotable where its *ratio* to the perpendicular one is not.
 */

/**
 * The angle in **degrees** between an oblique free azimuth of a honeycomb slab's top face and the
 * slab normal.
 *
 * **Derived, not asserted.** The honeycomb is two interpenetrating triangular sublattices whose
 * azimuth sets differ by half the separation, so where sublattice A carries `{0°, 120°, 240°}`
 * sublattice B carries `{60°, 180°, 300°}`. A top-face A helix therefore has its `0°` azimuth free
 * and a top-face B helix has `±60°` free — which is `HoneycombLattice.azimuthSeparationDegrees()/2`
 * and would be a different number on any other lattice.
 */
fun obliqueAzimuthDegrees(): Double = HoneycombLattice.azimuthSeparationDegrees() / 2.0

/**
 * How many of a **top-face** helix's three azimuths are free, as a function of its position along
 * the face.
 *
 * A helix whose azimuth set points one direction **straight out** spends the other two on the
 * neighbours below it, so it has **one** free azimuth. A helix from the other sublattice points one
 * azimuth **straight down** and the other two obliquely out of the top face, so it has **two**.
 *
 * `C-0122`'s census assigns **one** to every top-face helix — *"every top-face helix has exactly
 * one free direction either way"* — and that is true of the perpendicular sublattice only. The two
 * oblique ladders **interleave** along the helix (each azimuth's period is 21 bp and consecutive
 * positions over all azimuths are 7 bp apart), so they do not collide and both are usable.
 */
fun freeAzimuthsOnTopFace(column: Int): Int =
    if (HoneycombLattice.pointsDirectlyOut(row = 0, column = column)) 1 else 2

/**
 * The top face's station count with **both** free azimuths of an oblique helix counted —
 * `C-0122`'s census corrected.
 */
fun topFaceStationsCountingBothAzimuths(
    rasterRows: Int,
    rowBasePairs: Int,
    phaseBasePairs: Int = 0
): Int {
    require(rasterRows >= 1) { "rasterRows must be at least 1, was: $rasterRows" }
    val perLadder = honeycombStationsOnHelix(rowBasePairs, phaseBasePairs)
    return (0 until rasterRows).sumOf { freeAzimuthsOnTopFace(it) } * perLadder
}

/** [obliqueAzimuthDegrees] in radians. */
fun obliqueAzimuthRadians(): Double = obliqueAzimuthDegrees() * PI / 180.0

/**
 * A root's stiffness against a load along the slab normal, as a function of its azimuth.
 *
 * [radial] and [tangential] are in `pN/nm` and are the two eigenvalues of the root's own
 * translational stiffness tensor. Either may be `+∞` — a covalent tie is a **constraint**, and
 * `CLAUDE.md` records that asking how stiff it is is asking the wrong question — in which case the
 * perpendicular reading is infinite and only the oblique one is a number.
 */
class ObliqueRootModel(
    val name: String,
    val radial: Double,
    val tangential: Double,
    val provenance: String = ""
) {

    init {
        require(radial > 0.0) { "radial must be positive, was: $radial" }
        require(tangential > 0.0) { "tangential must be positive, was: $tangential" }
    }

    /** `A = k_radial/k_tangential` — the one number the whole question reduces to. */
    val anisotropy: Double get() = radial / tangential

    /**
     * `k_z(ψ)` in `pN/nm`: the stiffness this root presents to a load along the slab normal when
     * its radial axis lies at [azimuth] radians from that normal.
     */
    fun normalStiffness(azimuth: Double): Double {
        val c = cos(azimuth)
        val s = sin(azimuth)
        val compliance = (if (c == 0.0) 0.0 else c * c / radial) +
                (if (s == 0.0) 0.0 else s * s / tangential)
        return if (compliance == 0.0) Double.POSITIVE_INFINITY else 1.0 / compliance
    }

    /**
     * `κ(ψ) = k_z(0)/k_z(ψ) = cos²ψ + sin²ψ·A` — how much **softer** an oblique root is than a
     * perpendicular one on the same construction.
     *
     * Exactly `1.0` at `ψ = 0` for every root, including one whose radial axis is a constraint —
     * the `0 · ∞` there is a limit, not an indeterminacy, and it is taken rather than evaluated.
     */
    fun costFactor(azimuth: Double): Double {
        val c = cos(azimuth)
        val s = sin(azimuth)
        val s2 = s * s
        if (s2 == 0.0) return 1.0
        return c * c + s2 * anisotropy
    }

    /**
     * The same `k_z(ψ)`, assembled from the stiffness **tensor** `k_r n nᵀ + k_t t tᵀ` and
     * condensed onto the normal coordinate with the lateral one free — `det K / K_yy`.
     *
     * It exists to falsify the decomposition rather than to be used: if the root's two axes were
     * not the eigenvectors of its tensor, this and [normalStiffness] would disagree. Finite
     * stiffnesses only.
     */
    fun normalStiffnessFromTensor(azimuth: Double): Double {
        require(radial.isFinite() && tangential.isFinite()) {
            "the tensor cross-check is defined for finite stiffnesses only"
        }
        val c = cos(azimuth)
        val s = sin(azimuth)
        // n = (sin ψ, cos ψ) in (y, z); t = (cos ψ, −sin ψ).
        val kYY = radial * s * s + tangential * c * c
        val kZZ = radial * c * c + tangential * s * s
        val kYZ = radial * s * c - tangential * c * s
        return (kYY * kZZ - kYZ * kYZ) / kYY
    }

    /**
     * The normal stiffness of a **pair** of roots on the same helix's two oblique azimuths,
     * `+[azimuth]` and `−[azimuth]`, tied to one rigid head.
     *
     * The two tensors sum, and the mirror symmetry cancels the off-diagonal exactly, so
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;`k_z,pair = 2(cos²ψ·k_radial + sin²ψ·k_tangential)`
     *
     * — a sum of **stiffnesses**, which is strictly larger than the `2·k_z(ψ)` a reader gets by
     * adding the two roots' own normal stiffnesses. That naive sum lets each head move laterally
     * on its own; the shared rigid head forbids it, and the forbidden motion is exactly the
     * compliance the scalar reading was counting.
     */
    fun pairedNormalStiffness(azimuth: Double): Double {
        val c = cos(azimuth)
        val s = sin(azimuth)
        return 2.0 * (c * c * radial + s * s * tangential)
    }
}

// ---------------------------------------------------------------- the three root models

/**
 * **R1** — a **flexible tie** rooted at the station: a strand leaving the backbone and running to
 * the driven body.
 *
 * `FlexureEndJoint`'s own invariant — *`k_⊥/k_axial` is exactly 1 for any isotropic element, and
 * for any covalent tie on a softened bond* — makes `A = 1` **exactly**, so `κ = 1` at *every*
 * azimuth. That is a **symmetry**, not a small number: a flexible link has no direction of its own,
 * so it cannot know which azimuth it left the helix on.
 *
 * The magnitude is `C-0009`/`C-0020`'s two softened backbone bonds, `2αS/(100a)`; it cancels out of
 * the cost factor entirely and is carried only so the absolute stiffness is quotable.
 */
fun flexibleTieRoot(alpha: Double = 1.0): ObliqueRootModel {
    val bonds = Gen1Tile.crossoverInPlaneStiffness(alpha)
    return ObliqueRootModel(
        name = "flexible tie on two softened bonds",
        radial = bonds,
        tangential = bonds,
        provenance = "FlexureEndJoint's isotropy invariant; magnitude from Gen1Tile" +
                ".crossoverInPlaneStiffness, DERIVED from Chen et al. and NOT measured"
    )
}

/**
 * **R2** — a **crossover-hinged rigid body** rooted at the station, the motif `C-0055`/`C-0061`
 * price and the one an out-of-plane arm or a second layer uses.
 *
 * - **radial**: the crossover's own covalent link, `2αS/(100a)` —
 *   [Gen1Tile.crossoverInPlaneStiffness].
 * - **tangential**: `C-0009`'s dihedral spring `k_θ` on the frame-indifferent lever `d/2`,
 *   `k_θ/(d/2)²`. A tangential force at the attached body's axis has that lever about the
 *   interface line, and `CLAUDE.md` records that frame indifference fixes the arm at exactly
 *   `d/2` — it is not a fitted parameter.
 */
fun crossoverHingedRoot(
    alpha: Double = 1.0,
    interhelicalDistance: Double = Gen1Tile.INTERHELICAL_HONEYCOMB,
    inPlaneMultiplier: Double = 1.0
): ObliqueRootModel {
    require(interhelicalDistance > 0.0) {
        "interhelicalDistance must be positive, was: $interhelicalDistance"
    }
    require(inPlaneMultiplier > 0.0) {
        "inPlaneMultiplier must be positive, was: $inPlaneMultiplier"
    }
    val lever = interhelicalDistance / 2.0
    return ObliqueRootModel(
        name = "crossover-hinged body, d = ${interhelicalDistance} nm",
        radial = inPlaneMultiplier * Gen1Tile.crossoverInPlaneStiffness(alpha),
        tangential = Gen1Tile.crossoverHingeStiffness(alpha) / (lever * lever),
        provenance = "C-0009's k_theta on the frame-indifferent d/2 lever against C-0020's " +
                "in-plane link, the latter DERIVED and NOT measured"
    )
}

/**
 * **R3** — the same body, with the covalent link read as this corpus reads it everywhere else:
 * a **constraint**, not a spring.
 *
 * `CLAUDE.md`: *a crossover's vertical link is a constraint tying two duplex surfaces together and
 * carries no rigidity at all … a covalent tie is therefore a BINARY, and asking how stiff it is is
 * asking the wrong question.* Under that reading the perpendicular root's normal stiffness is not
 * a number and the **ratio** is `NOT REPRESENTABLE` — while the oblique root's absolute stiffness
 * is `k_tangential/sin²ψ`, finite, and within a few per cent of R2's.
 */
fun constrainedLinkRoot(
    alpha: Double = 1.0,
    interhelicalDistance: Double = Gen1Tile.INTERHELICAL_HONEYCOMB
): ObliqueRootModel {
    val hinged = crossoverHingedRoot(alpha, interhelicalDistance)
    return ObliqueRootModel(
        name = "crossover-hinged body, link as a CONSTRAINT",
        radial = Double.POSITIVE_INFINITY,
        tangential = hinged.tangential,
        provenance = "CLAUDE.md: a crossover's vertical link is a constraint, and a covalent tie " +
                "is a binary"
    )
}

// ---------------------------------------------------------------- the path consequence

/** Two springs in series in `pN/nm`, with `+∞` handled as the identity. */
private fun series(first: Double, second: Double): Double = when {
    first.isInfinite() -> second
    second.isInfinite() -> first
    else -> 1.0 / (1.0 / first + 1.0 / second)
}

/**
 * The fraction of its mandated share an **oblique** path delivers when the rest of its load path is
 * **unchanged** from the perpendicular design.
 *
 * A coupling path is the root in series with whatever supplies the compliance `C-0017`'s mandate
 * demands — the mandate is 33.33 pN/nm over the whole coupling, so a path is *soft* by design and
 * the compliance is deliberately introduced. This sizes that partner on the **perpendicular** root
 * so the path delivers exactly [demandPerPath], then re-reads the same partner on the oblique root.
 *
 * It is therefore the cost of **not** re-sizing. Re-sizing is available and is priced separately:
 * the partner has to be `1/fraction`-ish stiffer, which is a spacer length, not a lattice.
 */
fun obliquePathFraction(
    root: ObliqueRootModel,
    azimuth: Double,
    demandPerPath: Double
): Double {
    require(demandPerPath > 0.0) { "demandPerPath must be positive, was: $demandPerPath" }
    val perpendicular = root.normalStiffness(0.0)
    require(perpendicular > demandPerPath) {
        "the root is softer than the demand, so no series partner can reach it: " +
                "$perpendicular pN/nm against $demandPerPath pN/nm"
    }
    val partner = if (perpendicular.isInfinite()) demandPerPath
    else 1.0 / (1.0 / demandPerPath - 1.0 / perpendicular)
    return series(root.normalStiffness(azimuth), partner) / demandPerPath
}

/** The stiffness the series partner must have for a perpendicular path to meet [demandPerPath]. */
fun seriesPartnerForDemand(root: ObliqueRootModel, demandPerPath: Double): Double {
    require(demandPerPath > 0.0) { "demandPerPath must be positive, was: $demandPerPath" }
    val perpendicular = root.normalStiffness(0.0)
    require(perpendicular > demandPerPath) {
        "the root is softer than the demand: $perpendicular against $demandPerPath"
    }
    return if (perpendicular.isInfinite()) demandPerPath
    else 1.0 / (1.0 / demandPerPath - 1.0 / perpendicular)
}

// ---------------------------------------------------------------- the alternation

/**
 * `C-0017`'s mandated total, distributed over an `attachmentGrid(columns, rows, …)` in which the
 * top face's helices **alternate** between a perpendicular root and an oblique one, the oblique
 * ones carrying [obliqueFraction] of a perpendicular one's stiffness.
 *
 * The grid is row-major, and the row index **is** the top-face helix index, so which rows are
 * perpendicular is read off `HoneycombLattice.pointsDirectlyOut` rather than assumed — the same
 * parity `C-0122`'s census counts with.
 *
 * The result is **renormalised to the mandate**, which isolates the *shape* of the alternation
 * from its *total*: the shortfall a design would suffer by not re-sizing its spacers is a
 * specification statement and is reported separately, not folded into a flatness number.
 */
fun alternatingShareOfMandate(
    rows: Int,
    columns: Int,
    obliqueFraction: Double,
    total: Double = MANDATED_TOTAL_STIFFNESS
): List<Double> {
    require(rows > 0) { "rows must be positive, was: $rows" }
    require(columns > 0) { "columns must be positive, was: $columns" }
    require(obliqueFraction > 0.0) { "obliqueFraction must be positive, was: $obliqueFraction" }
    require(total > 0.0) { "total must be positive, was: $total" }
    val weights = (0 until rows * columns).map { index ->
        val row = index / columns
        if (HoneycombLattice.pointsDirectlyOut(row = 0, column = row)) 1.0 else obliqueFraction
    }
    val sum = weights.sum()
    return weights.map { it * total / sum }
}
