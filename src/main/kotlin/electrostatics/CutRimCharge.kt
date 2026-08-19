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

package com.xemantic.nano.plentyofroom.electrostatics

import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min

/**
 * Task `P-14`, leaf `A7.4` — **the charge a DNA-origami sheet's rim presents**, and why it is
 * not a free parameter.
 *
 * ## The question `C-0022` left open
 *
 * `C-0022` models the tile as an impermeable obstacle carrying *smeared* surface charges on its
 * bottom face, its top face and its rim. The faces come from `C-0008`; the rim came from nothing,
 * and its declared falsifier 5 fired: `σ_rim = 0` against `σ_rim = σ_face` moves the fitted collar
 * depth from −0.2906 to −0.1575, a **1.845×** bracket, with *"the two readings both defensible"*.
 *
 * ## They are not both defensible, and the discriminant needs no solver
 *
 * The tile's charge is **volumetric**: `ρ = σ_face · 2/t`, because `σ_face = ρt/2` is not a
 * convention at all but Gauss's law on a slab — a uniformly charged slab of thickness `t` has
 * *exactly* the exterior field of two sheets of `ρt/2`. A smearing is therefore a **partition of
 * one conserved charge onto one boundary**, and the arithmetic is immediate: the §3 tile's rim
 * area is half its face area, so putting `σ_face` on the rim as well hands the model a tile
 * carrying **1.5×** the charge the tile has (1.25× in the 2-D cross-section `C-0022` solves).
 *
 * ## The family that does conserve is ONE PARAMETER
 *
 * Charge taken by the rim has to be charge the faces gave up, and — the faces being exact in the
 * interior — it has to come from the **collar**. Writing the face charge as a linear taper of
 * length `ℓ` inward from the rim,
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`σ_face(s) = (ρt/2)·min(1, s/ℓ)`, &nbsp;&nbsp; `σ_rim = ρℓ/2`,
 *
 * conserves identically at every `ℓ`, because the face deficit `ρtℓ/2` per unit edge is exactly
 * the rim gain `t · ρℓ/2`. `ℓ = 0` is `C-0022`'s headline. `ℓ = t` is the *density* of its
 * falsifier — reached only with a face taper ten nanometres deep, which the falsifier did not
 * apply, and that missing taper **is** the 25 % of charge it added.
 *
 * ## And the geometry picks a member
 *
 * The nearest-surface (medial-axis) partition — each boundary element takes the material closer
 * to it than to any other boundary — is `ℓ = t/2` and gives
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`σ_rim = ρt/4 = σ_face/2` **exactly**,
 *
 * for any rectangular slab with `t ≤ 2a`, independently of `ρ`, `t`, the buffer and the Manning
 * fraction. Its rim profile is *triangular* in height, `ρ·min(ζ, t−ζ)`, peaking at the full face
 * density at mid-height and vanishing at both corners; [medial] carries that, and [taperedFace]
 * at the same `ℓ` carries the flat-rim reading of the same mean. The pair is the convention
 * bracket, and it is 2× narrower than the one `C-0022` had to quote.
 */

/**
 * Returns the tile's Manning-renormalised **volumetric** charge density in `e/nm³` implied by the
 * face density [faceChargeDensity] (`e/nm²`, signed) the whole programme uses and the tile
 * thickness [thickness] nm.
 *
 * The inverse of `σ_face = ρt/2`, which is Gauss's law on a slab and not a convention. Recovering
 * `ρ` this way rather than from a fresh phosphate count guarantees that every smearing below is a
 * repartition of **`C-0008`'s own charge** and can never be read as a second opinion about it.
 *
 * @throws IllegalArgumentException if [thickness] is not positive.
 */
fun tileVolumetricChargeDensity(faceChargeDensity: Double, thickness: Double): Double {
    require(thickness > 0.0) { "thickness must be positive, was: $thickness" }
    return 2.0 * faceChargeDensity / thickness
}

/** The boundary charge a smearing assigns, as a ratio to the tile's own, on the two cuts. */
@Serializable
data class CutRimChargeLedger(
    /** The 2-D cross-section `C-0022` solves: two faces of width `2a`, two rims of height `t`. */
    val twoDimensional: Double,
    /** The whole square tile: two faces of area `(2a)²`, four rims of area `2a·t`. */
    val threeDimensional: Double
)

/**
 * Returns the [CutRimChargeLedger] of the **non-conserving** family `C-0022` swept: a face held
 * at [faceChargeDensity] everywhere and a rim held uniformly at [rimChargeDensity].
 *
 * 1.0 at an uncharged rim, 1.125 / 1.25 at the geometric density, **1.25 / 1.5** at the face
 * density. The 3-D excess is twice the 2-D one because a square tile has four rims per two faces
 * where a cross-section has two per two.
 */
fun uniformRimBoundaryChargeRatio(
    faceChargeDensity: Double,
    rimChargeDensity: Double,
    thickness: Double,
    halfWidth: Double
): CutRimChargeLedger {
    require(thickness > 0.0) { "thickness must be positive, was: $thickness" }
    require(halfWidth > 0.0) { "halfWidth must be positive, was: $halfWidth" }
    val edge = 2.0 * halfWidth
    val tileCharge = 2.0 * faceChargeDensity * edge
    return CutRimChargeLedger(
        twoDimensional = (2.0 * faceChargeDensity * edge + 2.0 * thickness * rimChargeDensity) /
                tileCharge,
        threeDimensional = (2.0 * faceChargeDensity * edge * edge +
                4.0 * edge * thickness * rimChargeDensity) / (tileCharge * edge)
    )
}

/** Which of the two conserving rim profiles a [CutRimSmearing] carries. */
enum class CutRimProfile {
    /** Flat over the rim's height — the reading that shares the medial one's mean. */
    UNIFORM,

    /** `ρ·min(ζ, t−ζ)`: the nearest-surface partition's own profile, triangular in height. */
    MEDIAL_TRIANGULAR
}

/**
 * A **charge-conserving** smearing of a slab's volumetric charge onto its boundary, parametrised
 * by the face taper length [taperLength] nm.
 *
 * Use [taperedFace] and [medial] rather than the constructor; the invariant every member holds is
 * [boundaryChargeRatioTwoDimensional] = [boundaryChargeRatioThreeDimensional] = 1.
 *
 * @param volumetricChargeDensity `ρ` in `e/nm³`, **signed** (negative for DNA).
 * @param thickness the slab thickness `t` in nm.
 * @param halfWidth `a` in nm — the centre-line-to-rim half-extent of `C-0022`'s domain.
 * @param taperLength `ℓ` in nm, the depth over which the face charge tapers to zero at the rim.
 */
@Serializable
data class CutRimSmearing(
    val volumetricChargeDensity: Double,
    val thickness: Double,
    val halfWidth: Double,
    val taperLength: Double,
    val profile: CutRimProfile
) {

    init {
        require(thickness > 0.0) { "thickness must be positive, was: $thickness" }
        require(halfWidth > 0.0) { "halfWidth must be positive, was: $halfWidth" }
        require(taperLength >= 0.0) { "taperLength must not be negative, was: $taperLength" }
        require(taperLength <= halfWidth) {
            "taperLength must not exceed halfWidth ($halfWidth), was: $taperLength"
        }
        require(profile == CutRimProfile.UNIFORM || taperLength == thickness / 2.0) {
            "the medial profile exists only at taperLength = thickness/2 " +
                    "(${thickness / 2.0}), was: $taperLength"
        }
    }

    /** `σ_face = ρt/2` in `e/nm²` — exact for a slab, and untouched outside the collar. */
    val interiorFaceChargeDensity: Double get() = volumetricChargeDensity * thickness / 2.0

    /** `σ_rim = ρℓ/2` in `e/nm²` — the **mean** over the rim's height, whatever the profile. */
    val rimChargeDensity: Double get() = volumetricChargeDensity * taperLength / 2.0

    /** The face charge in `e/nm²` at [distanceFromRim] nm inward from the rim. */
    fun faceChargeDensityAt(distanceFromRim: Double): Double =
        if (taperLength == 0.0) interiorFaceChargeDensity
        else interiorFaceChargeDensity * min(1.0, distanceFromRim / taperLength)

    /** The rim charge in `e/nm²` at [heightInTile] nm above the tile's bottom face. */
    fun rimChargeDensityAt(heightInTile: Double): Double = when (profile) {
        CutRimProfile.UNIFORM -> rimChargeDensity
        CutRimProfile.MEDIAL_TRIANGULAR ->
            volumetricChargeDensity * min(heightInTile, thickness - heightInTile)
    }

    /** The charge the two faces give up per unit length of edge, in `e/nm`. */
    val faceDeficitPerUnitEdge: Double
        get() = volumetricChargeDensity * thickness * taperLength / 2.0

    /** The charge one rim takes per unit length of edge, in `e/nm`. */
    val rimGainPerUnitEdge: Double get() = when (profile) {
        CutRimProfile.UNIFORM -> rimChargeDensity * thickness
        CutRimProfile.MEDIAL_TRIANGULAR ->
            volumetricChargeDensity * thickness * thickness / 4.0
    }

    /** The tile's own charge per unit length of edge, `ρ t 2a`, in `e/nm`. */
    val tileChargePerUnitEdge: Double
        get() = volumetricChargeDensity * thickness * 2.0 * halfWidth

    /** Assigned over own, on the 2-D cross-section — **1 at every member, by construction**. */
    val boundaryChargeRatioTwoDimensional: Double
        get() = (2.0 * interiorFaceChargeDensity * 2.0 * halfWidth - 2.0 * faceDeficitPerUnitEdge +
                2.0 * rimGainPerUnitEdge) / tileChargePerUnitEdge

    /** Assigned over own, on the whole square tile — 1 at every member. */
    val boundaryChargeRatioThreeDimensional: Double
        get() {
            val edge = 2.0 * halfWidth
            val faces = 2.0 * interiorFaceChargeDensity * edge * edge -
                    4.0 * edge * faceDeficitPerUnitEdge
            val rims = 4.0 * edge * rimGainPerUnitEdge
            return (faces + rims) / (volumetricChargeDensity * thickness * edge * edge)
        }

    companion object {

        /** The family member with a linear face taper of length [taperLength] and a flat rim. */
        fun taperedFace(
            volumetricChargeDensity: Double,
            thickness: Double,
            halfWidth: Double,
            taperLength: Double
        ): CutRimSmearing = CutRimSmearing(
            volumetricChargeDensity, thickness, halfWidth, taperLength, CutRimProfile.UNIFORM
        )

        /**
         * The **nearest-surface** member: `ℓ = t/2`, `σ_rim = σ_face/2`, rim triangular in height.
         *
         * The only member of the family in which every element of charge is assigned to the
         * boundary element it is closest to.
         */
        fun medial(
            volumetricChargeDensity: Double,
            thickness: Double,
            halfWidth: Double
        ): CutRimSmearing = CutRimSmearing(
            volumetricChargeDensity, thickness, halfWidth, thickness / 2.0,
            CutRimProfile.MEDIAL_TRIANGULAR
        )
    }
}

/**
 * What the two rims of a single-crystal helix lattice actually present — the geometric half of
 * `P-14`, and the test of whether one areal density can serve both.
 *
 * A sheet's rim **across** the helices is a lattice of duplex **end faces**; its rim **along**
 * them is the outermost duplexes' **sidewalls**. They are different objects, and the same
 * volumetric charge stands behind both — so what can distinguish them is not a density but a
 * **depth**: how far below the rim plane the first phosphate sits.
 */
@Serializable
data class CutRimCensus(
    /** Duplex end faces per nm² of the across-helix rim — one per lattice cross-section. */
    val duplexEndsPerRimArea: Double,
    /** The fraction of the across-helix rim plane covered by duplex end faces. */
    val endFaceCoverage: Double,
    /** Bare phosphate charge in `e/nm²` within one rise of the across-helix rim plane. */
    val endRimFirstLayerCharge: Double,
    /** Depth in nm of the nearest phosphate below the across-helix rim plane. */
    val endRimNearestChargeDepth: Double,
    /** Depth in nm of the nearest phosphate below the along-helix rim plane. */
    val sidewallRimNearestChargeDepth: Double,
    /** `|end − sidewall|` in nm — the whole geometric difference between the two rims. */
    val chargeDepthDifference: Double
)

/**
 * Returns the [CutRimCensus] of [tile], reading the backbone at [phosphateRadius] nm.
 *
 * The across-helix rim plane **is** the lattice cross-section, so it carries exactly one duplex
 * end per [DnaOrigamiTile.areaPerHelixCrossSection] and the terminal phosphate lies *in* the
 * plane — depth 0. The along-helix rim plane is tangent to the outermost duplexes, whose
 * phosphates sit at [phosphateRadius] against a steric radius of [DnaOrigamiTile.helixRadius], so
 * its nearest charge is `R − r_P` **inside** the plane: 0.0914 nm at `T-71`'s measured
 * 0.9086 nm, against 0.0 at the round 1.0 nm that would put the two rims on top of each other.
 *
 * @throws IllegalArgumentException if [phosphateRadius] is not inside the duplex.
 */
fun cutRimCensus(tile: DnaOrigamiTile, phosphateRadius: Double): CutRimCensus {
    require(phosphateRadius > 0.0 && phosphateRadius <= tile.helixRadius) {
        "phosphateRadius must lie inside the duplex (0, ${tile.helixRadius}], " +
                "was: $phosphateRadius"
    }
    val endDepth = 0.0
    val sidewallDepth = tile.helixRadius - phosphateRadius
    return CutRimCensus(
        duplexEndsPerRimArea = 1.0 / tile.areaPerHelixCrossSection,
        endFaceCoverage = PI * tile.helixRadius * tile.helixRadius /
                tile.areaPerHelixCrossSection,
        endRimFirstLayerCharge = 2.0 / tile.areaPerHelixCrossSection,
        endRimNearestChargeDepth = endDepth,
        sidewallRimNearestChargeDepth = sidewallDepth,
        chargeDepthDifference = abs(sidewallDepth - endDepth)
    )
}

/**
 * Solves `C-0022`'s 2-D edge problem under the charge-conserving smearing [smearing], at electrode
 * reduced potential [electrodeReducedPotential].
 *
 * The face is held at its interior density and shaped by `min(1, s/ℓ)`; the rim is held at its
 * mean and shaped by its own profile. At `ℓ = 0` both shapes are `null`, so the recommended
 * family's own `C-0022` endpoint is the **same call** that claim made, not a reproduction of it.
 */
fun solveSmearing(
    solver: PoissonBoltzmannEdge,
    electrodeReducedPotential: Double,
    smearing: CutRimSmearing
): EdgeSolution {
    val taper = smearing.taperLength
    val faceShape = if (taper == 0.0) null else EdgeChargeShape { s -> min(1.0, s / taper) }
    val mean = smearing.rimChargeDensity
    val rimShape = when {
        mean == 0.0 -> null
        smearing.profile == CutRimProfile.UNIFORM -> null
        else -> EdgeChargeShape { z -> smearing.rimChargeDensityAt(z) / mean }
    }
    return solver.solve(
        electrodeReducedPotential,
        smearing.interiorFaceChargeDensity,
        smearing.interiorFaceChargeDensity,
        mean,
        faceShape,
        rimShape
    )
}
