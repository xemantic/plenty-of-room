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

import kotlin.math.PI
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * `T-195` — the **unpaired scaffold remainder** as a body beside the actuated gap.
 *
 * NDI's answer to decision 5 (2026-08-18) is *"M13, circular ~7–8 K nucleotides"*, which makes a
 * scaffold longer than the tile needs the **default** rather than one of three options. What is
 * left over is a single-stranded coil, and `C-0022` solved the tile's edge load with nothing
 * there.
 *
 * This file carries only the closed forms the two **cheap bounds** need. Neither needs a field
 * solve, and either alone may settle the question:
 *
 * 1. **Saturation.** The tile's gap-facing wall sits at 85–98 % of the 2:1 saturated far-field
 *    amplitude, so its own charge ambiguity is already irrelevant (`C-0008`). Smear the **entire**
 *    remainder onto that wall — the closest plane any of it can reach — and read `σ_eff` again.
 * 2. **Confinement.** An ideal chain in a slit of width `d` pays `π² R_g²/d²`; a swollen one
 *    `(R_F/d)^{5/3}`. Where the coil is much larger than the gap it is expelled, and the number of
 *    nucleotides that can enter at a cost of order `k_BT` is a closed form.
 *
 * ## The Manning asymmetry, which runs against the answer
 *
 * The sheet is duplex DNA — one phosphate per **0.17 nm** of axis, so `q ξ_M = 8.4` and 11.90 %
 * survives. The remainder is **single-stranded** — one phosphate per **0.57–0.70 nm** of contour,
 * so `q ξ_M = 2.04–2.51` and **39.9–49.0 %** survives, i.e. 3.35–4.12× more per nucleotide.
 * `C-0086` compared the two bodies on **bare** charge; both readings are carried here and the
 * Manning one is the load-bearing one, because it is the one that makes the remainder look worse.
 */

/** M13mp18, circular — the wild-type scaffold, **CITED** via `C-0055`. */
const val M13MP18_NUCLEOTIDES: Int = 7249

/**
 * `p7560` — **CITED, READ DIRECTLY** from Douglas et al. (caDNAno, `PMC2731887`), which folds
 * every 60-helix block from it: *"20 nM scaffold (p7560 or p8064, derived from M13mp18)"*, and
 * *"The design: scaffold pairings are as follows: i: p8064, ii: p7560, iii: p8064, iv: p7560,
 * v: p8064, vi: p7560, vii: p7560."* Design **(ii) is the 10 × 6** cross-section `C-0120`
 * recommends.
 */
const val P7560_NUCLEOTIDES: Int = 7560

/**
 * `p8064` — **CITED, READ DIRECTLY** from the same passage; the scaffold of designs (i), (iii)
 * and (v), i.e. of the **15 × 4** cross-section `C-0119` reads as the programme's tile.
 */
const val P8064_NUCLEOTIDES: Int = 8064

/**
 * Returns the unpaired remainder in nucleotides: the scaffold less the nucleotides the tile's
 * duplexes consume.
 *
 * A raster puts exactly one scaffold nucleotide on each base pair of each duplex, so
 * [pairedNucleotides] is `duplexes × basePairsPerRow` — `anchoring.sheetScaffoldNucleotides`.
 *
 * @throws IllegalArgumentException if either count is below one or the tile does not fit.
 */
fun unpairedRemainder(scaffoldNucleotides: Int, pairedNucleotides: Int): Int {
    require(scaffoldNucleotides >= 1) {
        "scaffoldNucleotides must be at least one, was: $scaffoldNucleotides"
    }
    require(pairedNucleotides >= 1) {
        "pairedNucleotides must be at least one, was: $pairedNucleotides"
    }
    require(pairedNucleotides <= scaffoldNucleotides) {
        "the tile pairs $pairedNucleotides nt, more than the scaffold's $scaffoldNucleotides"
    }
    return scaffoldNucleotides - pairedNucleotides
}

/**
 * Returns the fraction of the bare phosphate charge that survives condensation of counterions of
 * valency [counterionValency] on a line charge of spacing [chargeSpacing] nm.
 *
 * The valency-free Manning parameter is `ξ_M = l_B/b`, condensation proceeds until the effective
 * parameter reaches `1/q`, and nothing condenses at all when `q ξ_M ≤ 1` — so the surviving
 * fraction is `min(1, 1/(q ξ_M))`. `DnaOrigamiTile.manningSurvivingFraction` is this expression
 * with the spacing pinned to `rise/2`; the remainder needs it at the ssDNA contour instead, which
 * is why it is written free here rather than duplicated inside a tile.
 *
 * @throws IllegalArgumentException on a non-positive spacing, valency or Bjerrum length.
 */
fun manningSurvivingFractionOfSpacing(
    chargeSpacing: Double,
    counterionValency: Int,
    bjerrumLength: Double
): Double {
    require(chargeSpacing > 0.0) { "chargeSpacing must be positive, was: $chargeSpacing" }
    require(counterionValency > 0) {
        "counterionValency must be positive, was: $counterionValency"
    }
    require(bjerrumLength > 0.0) { "bjerrumLength must be positive, was: $bjerrumLength" }
    val product = counterionValency * bjerrumLength / chargeSpacing
    return if (product <= 1.0) 1.0 else 1.0 / product
}

/**
 * Returns the remainder's charge as a multiple of the tile's own **bare** backbone charge —
 * `C-0086`'s reading, reproduced here so the Manning one can be measured against it.
 *
 * [tileBackboneNucleotides] is the tile's whole phosphate count, scaffold **and** staples, i.e.
 * twice its base-pair count.
 */
fun bareChargeRatio(remainderNucleotides: Int, tileBackboneNucleotides: Int): Double {
    require(remainderNucleotides >= 0) {
        "remainderNucleotides must not be negative, was: $remainderNucleotides"
    }
    require(tileBackboneNucleotides >= 1) {
        "tileBackboneNucleotides must be at least one, was: $tileBackboneNucleotides"
    }
    return remainderNucleotides.toDouble() / tileBackboneNucleotides.toDouble()
}

/**
 * Returns the remainder's charge as a multiple of the tile's own **Manning-renormalised**
 * backbone charge — the load-bearing reading, 3.35–4.12× the bare one.
 */
fun manningChargeRatio(
    remainderNucleotides: Int,
    tileBackboneNucleotides: Int,
    remainderSurvivingFraction: Double,
    tileSurvivingFraction: Double
): Double {
    require(remainderSurvivingFraction >= 0.0) {
        "remainderSurvivingFraction must not be negative, was: $remainderSurvivingFraction"
    }
    require(tileSurvivingFraction > 0.0) {
        "tileSurvivingFraction must be positive, was: $tileSurvivingFraction"
    }
    return bareChargeRatio(remainderNucleotides, tileBackboneNucleotides) *
            remainderSurvivingFraction / tileSurvivingFraction
}

/**
 * Returns the areal charge density in `e/nm²` the remainder would add to a plane of area
 * [footprintArea] nm² if **all** of it were smeared onto that plane.
 *
 * This is the worst case, and it is worst in every argument at once: the whole chain, at its
 * largest surviving charge fraction, on the closest plane it could occupy. It is not a
 * conformation — it is the bound a conformation cannot exceed.
 */
fun smearedRemainderChargeDensity(
    remainderNucleotides: Int,
    survivingFraction: Double,
    footprintArea: Double
): Double {
    require(remainderNucleotides >= 0) {
        "remainderNucleotides must not be negative, was: $remainderNucleotides"
    }
    require(survivingFraction >= 0.0) {
        "survivingFraction must not be negative, was: $survivingFraction"
    }
    require(footprintArea > 0.0) { "footprintArea must be positive, was: $footprintArea" }
    return remainderNucleotides * survivingFraction / footprintArea
}

/**
 * Returns the self-avoiding (Flory) **end-to-end** size in nm of a single strand of
 * [nucleotides], `R_F = b N_K^ν`.
 *
 * Carried beside the ideal `anchoring.singleStrandedRadiusOfGyration` because the two differ by
 * 3–4× at these chain lengths, and the confinement argument has to survive **both**. The default
 * exponent is the three-dimensional self-avoiding-walk value.
 */
fun singleStrandedFloryRadius(
    nucleotides: Int,
    kuhnLength: Double,
    contourPerNucleotide: Double,
    exponent: Double = SELF_AVOIDING_WALK_EXPONENT
): Double {
    require(nucleotides >= 1) { "nucleotides must be at least one, was: $nucleotides" }
    require(kuhnLength > 0.0) { "kuhnLength must be positive, was: $kuhnLength" }
    require(contourPerNucleotide > 0.0) {
        "contourPerNucleotide must be positive, was: $contourPerNucleotide"
    }
    require(exponent > 0.0) { "exponent must be positive, was: $exponent" }
    val kuhnSegments = nucleotides * contourPerNucleotide / kuhnLength
    return kuhnLength * kuhnSegments.pow(exponent)
}

/** The three-dimensional self-avoiding-walk exponent — **CITED**, 0.588. */
const val SELF_AVOIDING_WALK_EXPONENT: Double = 0.588

/**
 * Returns the confinement free energy in `k_BT` of an **ideal** chain of radius of gyration
 * [radiusOfGyration] nm squeezed into a slit of width [gapHeight] nm.
 *
 * The Edwards ground state in a slit with absorbing walls is `sin(πz/d)` with eigenvalue
 * `π² a²/(6 d²)` per segment, so `F/k_BT = π² R_ee²/(6 d²)` and `R_ee² = 6 R_g²` gives
 * `π² R_g²/d²`. Absorbing walls are the right boundary condition for a non-adsorbing chain
 * against a non-adsorbing wall, which is the same statement `C-0011`'s brush profile is solved
 * under.
 *
 * @throws IllegalArgumentException on a non-positive radius or gap.
 */
fun idealSlitConfinementFreeEnergy(radiusOfGyration: Double, gapHeight: Double): Double {
    require(radiusOfGyration > 0.0) {
        "radiusOfGyration must be positive, was: $radiusOfGyration"
    }
    require(gapHeight > 0.0) { "gapHeight must be positive, was: $gapHeight" }
    val ratio = radiusOfGyration / gapHeight
    return PI * PI * ratio * ratio
}

/**
 * Returns the confinement free energy in `k_BT` of a **swollen** chain of Flory radius
 * [floryRadius] nm in a slit of width [gapHeight] nm — de Gennes' blob result `(R/d)^{5/3}`.
 *
 * Weaker than the ideal law at a fixed size, which is why both are carried: the ideal chain is
 * the smaller coil **and** the steeper penalty, so the two errors do not run the same way and the
 * verdict has to survive their minimum.
 */
fun swollenSlitConfinementFreeEnergy(floryRadius: Double, gapHeight: Double): Double {
    require(floryRadius > 0.0) { "floryRadius must be positive, was: $floryRadius" }
    require(gapHeight > 0.0) { "gapHeight must be positive, was: $gapHeight" }
    return (floryRadius / gapHeight).pow(5.0 / 3.0)
}

/**
 * Returns how many nucleotides of a tethered strand can sit inside a slit of width [gapHeight] nm
 * at a confinement cost of **one** `k_BT` — capped at [remainderNucleotides], which is all there
 * is.
 *
 * Setting `π² R_g(n)²/d² = 1` on the ideal law gives `n = 6 d²/(π² b c)`, a closed form with no
 * solve in it. It is the quantity the smeared bound is a ceiling on: everything beyond it pays
 * more than thermal energy to be in the gap, so a chain tethered at the slit mouth threads this
 * many nucleotides in and keeps the rest outside.
 *
 * @throws IllegalArgumentException on a non-positive gap, Kuhn length or contour.
 */
fun slitPenetratingNucleotides(
    gapHeight: Double,
    kuhnLength: Double,
    contourPerNucleotide: Double,
    remainderNucleotides: Int
): Double {
    require(gapHeight > 0.0) { "gapHeight must be positive, was: $gapHeight" }
    require(kuhnLength > 0.0) { "kuhnLength must be positive, was: $kuhnLength" }
    require(contourPerNucleotide > 0.0) {
        "contourPerNucleotide must be positive, was: $contourPerNucleotide"
    }
    require(remainderNucleotides >= 0) {
        "remainderNucleotides must not be negative, was: $remainderNucleotides"
    }
    val unbounded = 6.0 * gapHeight * gapHeight / (PI * PI * kuhnLength * contourPerNucleotide)
    return min(unbounded, remainderNucleotides.toDouble())
}

/**
 * Returns the magnitude of the effective (far-field) charge density in `e/nm²` of a **negative**
 * 2:1 wall carrying bare charge [bareChargeDensity] `e/nm²` — a one-line convenience over
 * [asymmetricReducedSurfacePotential] and [asymmetricEffectiveChargeDensity], because the whole
 * saturation bound is that composition read twice.
 */
fun negativeWallEffectiveChargeDensity(
    bareChargeDensity: Double,
    inverseDebyeLength: Double,
    bjerrumLength: Double
): Double {
    require(bareChargeDensity > 0.0) {
        "bareChargeDensity must be positive (its magnitude), was: $bareChargeDensity"
    }
    val potential =
        asymmetricReducedSurfacePotential(-bareChargeDensity, inverseDebyeLength, bjerrumLength)
    return -asymmetricEffectiveChargeDensity(potential, inverseDebyeLength, bjerrumLength)
}

/**
 * Returns the ideal-chain radius of gyration in nm of a strand of [nucleotides] — the same
 * expression `anchoring.singleStrandedRadiusOfGyration` carries, restated here only so the
 * round-trip gate in [slitPenetratingNucleotides] does not depend on a package this file would
 * otherwise not import.
 */
fun singleStrandedGyrationRadius(
    nucleotides: Double,
    kuhnLength: Double,
    contourPerNucleotide: Double
): Double {
    require(nucleotides > 0.0) { "nucleotides must be positive, was: $nucleotides" }
    require(kuhnLength > 0.0) { "kuhnLength must be positive, was: $kuhnLength" }
    require(contourPerNucleotide > 0.0) {
        "contourPerNucleotide must be positive, was: $contourPerNucleotide"
    }
    return kuhnLength * sqrt(nucleotides * contourPerNucleotide / kuhnLength / 6.0)
}
