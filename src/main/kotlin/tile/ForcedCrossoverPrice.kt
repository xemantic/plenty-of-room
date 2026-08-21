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

import com.xemantic.nano.plentyofroom.structure.turnPhosphateSpan
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * `T-246` — what a **forced** scaffold crossover costs, on the honeycomb residue lattice.
 *
 * `CH-0188` shows that `C-0140`'s recommended `112 / 108 bp` two-length raster does not close on
 * caDNAno's own `±5 bp` scaffold-crossover rule and would need **10 of 59** raster crossovers
 * *forced*. caDNAno permits forcing and warns only that *"departure from the default rules may
 * lead to folding failure if too much deviation from canonical DNA geometry is implied"* — and, in
 * the same paper's discussion, that *"additional software development will be required … for
 * caDNAno to predict the structural consequences of these changes"*. So the source that defines
 * the operation states, in 2009, that its consequences are **not predicted**.
 *
 * This file supplies what can be derived instead: the **geometric departure** a forced crossover
 * implies, whether it closes as a covalent bond at all, and a rigorous **ceiling** on its elastic
 * price.
 *
 * ## The departure is an azimuth, and 21 bp is TWO turns
 *
 * caDNAno's honeycomb fixes the twist at 10.5 bp per turn, so one base pair is `240/7 = 34.2857°`
 * of backbone azimuth and the 21 bp residue period is exactly **720°**. Displacing a crossover by
 * `k` base pairs rotates **both** backbones by `k · 240/7°` in the **same** sense — the two helices
 * are parallel and same-handed — so the departure folds to `(−180°, +180°]`.
 *
 * The consequence is counter-intuitive and it decides the answer: because one turn is **10.5** bp,
 * the smallest nonzero azimuthal departure the lattice offers is not one base pair (`34.2857°`) but
 * **ten or eleven** (`∓120/7 = ∓17.142857°`) — *half* a base-pair step. A forced crossover is
 * therefore cheapest when it is displaced **furthest** in base pairs, which no count of base pairs
 * can see.
 *
 * ## The span, and it is `C-0147`'s own geometry
 *
 * Two backbones rotated by `θ` from closest approach put their phosphates
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`span(θ) = √(d² − 4 d r_P cos θ + 4 r_P²)`
 *
 * apart, which is `turnPhosphateSpan(d, r_P, θ, 180° + θ)` — `C-0147`'s model consumed unmodified —
 * and whose two endpoints are `|d − 2r_P|` and `d + 2r_P`, the two rows of that claim's own table.
 * A scaffold crossover carries **zero** unpaired nucleotides, so its span must fall inside **one**
 * measured phosphodiester step (`T-71`) or it does not close as a bond at all.
 *
 * ## The price is an ELASTIC one, and the ceiling needs no solve
 *
 * Where it does not close, the departure must be absorbed by deformation. `C-0104`/`T-182`'s
 * [com.xemantic.nano.plentyofroom.structure.EdgeTwistRelief] already maps an azimuthal register
 * error at a crossover onto a **relative roll** penalised by the dihedral spring `k_θ` and relieved
 * by the duplex's own torsion over `λ = √(C p / k_θ)`. A localised defect `θ_f` on that field is two
 * springs in series, `½ · series(k_θ, 2C/λ) · θ_f²` — so the **rigid-duplex limit `½ k_θ θ_f²` is a
 * strict upper bound**, and because the structure minimises over channels, *any* admissible
 * channel's cost is a ceiling on the true price.
 *
 * Units: angles in **degrees** at the API and radians inside; `k_θ` **pN·nm/rad**, `C` **pN·nm²**,
 * lengths **nm**, energies **pN·nm**.
 */

/** Degrees of backbone azimuth per base pair on caDNAno's honeycomb, `360/10.5 = 240/7`. */
const val AZIMUTH_PER_BASE_PAIR: Double = 240.0 / 7.0

/**
 * The energy in `k_BT` that **one crossover column of the host sheet** demonstrably pays to hold
 * its own two duplexes at the SAXS 2.69 nm — `C-0079`'s calibration, and the sheet folds.
 *
 * Carried as a constant so the ceiling can be read against it in a unit test; the study asserts it
 * against `gpd/results/T-139-duplex-pair-separation.json`, which owns it.
 */
const val HOST_SHEET_COLUMN_ENERGY_KT: Double = 7.99969697

/**
 * The precision an azimuthal comparison is **decided** at, in degrees.
 *
 * Two residue departures can be exactly equal in azimuth and differ in the last ulp of the
 * arithmetic that produces them — `10` and `−11` base pairs are both `∓17.142857°` and came out
 * `3.4e−14` apart — so a bare `<` between them returns whichever the rounding favoured. Decided
 * coarser than the noise and finer than any physical distinction, with the smaller base-pair count
 * breaking the tie, which is `CLAUDE.md`'s own rule for a decision precision.
 */
private const val AZIMUTH_DECISION_DEGREES: Double = 1e-6

private fun decidedAzimuth(degrees: Double): Long =
    Math.round(abs(degrees) / AZIMUTH_DECISION_DEGREES)

/** Folds [degrees] into `(−180, +180]`. */
fun foldedDegrees(degrees: Double): Double {
    var a = degrees % 360.0
    if (a <= -180.0) a += 360.0
    if (a > 180.0) a -= 360.0
    return a
}

/** The azimuthal departure, folded, that a residue departure of [basePairs] implies. */
fun azimuthalDepartureDegrees(basePairs: Int): Double =
    foldedDegrees(basePairs * AZIMUTH_PER_BASE_PAIR)

/**
 * The phosphate span in nm of a crossover whose two backbones are rotated by [azimuthDegrees] from
 * closest approach — `C-0147`'s [turnPhosphateSpan] at `(θ, 180° + θ)`, consumed unmodified.
 */
fun forcedCrossoverSpan(
    interhelicalDistance: Double,
    phosphateRadius: Double,
    azimuthDegrees: Double
): Double = turnPhosphateSpan(
    interhelicalDistance, phosphateRadius, azimuthDegrees, 180.0 + azimuthDegrees
)

/**
 * The **smallest span reachable at any separation** by a crossover rotated by [azimuthDegrees]:
 * `2 r_P |sin θ|`, attained at `d = 2 r_P cos θ`.
 *
 * It is the exact minimum over the interhelical distance of [forcedCrossoverSpan], so *"can the
 * axes close this bond at all"* is a comparison and not a search — and above the azimuth at which
 * it crosses the measured phosphodiester step, **no approach whatever** closes the crossover.
 */
fun smallestReachableSpan(phosphateRadius: Double, azimuthDegrees: Double): Double =
    2.0 * phosphateRadius * abs(sin(Math.toRadians(azimuthDegrees)))

/**
 * [interhelicalDistanceClosingSpan] where the target is reachable, and `null` where it is not.
 *
 * The `null` is a **verdict** — *"no approach closes this crossover"* — and not a failure, which
 * is why the throwing form is kept beside it rather than replaced (`CLAUDE.md`).
 */
fun interhelicalDistanceClosingSpanOrNull(
    span: Double,
    phosphateRadius: Double,
    azimuthDegrees: Double
): Double? =
    if (span < smallestReachableSpan(phosphateRadius, azimuthDegrees)) null
    else interhelicalDistanceClosingSpan(span, phosphateRadius, azimuthDegrees)

/**
 * The interhelical distance at which a crossover rotated by [azimuthDegrees] would span exactly
 * [span] nm — the **larger** root of `d² − 4 d r_P cos θ + 4r_P² = span²`, i.e. the approach the
 * axes would have to make for the bond to close without any twist at all.
 *
 * @throws IllegalArgumentException if no real approach reaches that span, which happens when the
 *   rotation puts the two backbone loci further apart than [span] at **every** separation.
 */
fun interhelicalDistanceClosingSpan(
    span: Double,
    phosphateRadius: Double,
    azimuthDegrees: Double
): Double {
    require(span > 0.0) { "span must be positive, was: $span" }
    require(phosphateRadius > 0.0) {
        "phosphateRadius must be positive, was: $phosphateRadius"
    }
    val c = cos(Math.toRadians(azimuthDegrees))
    val half = 2.0 * phosphateRadius * c
    val discriminant = span * span - 4.0 * phosphateRadius * phosphateRadius * (1.0 - c * c)
    require(discriminant >= 0.0) {
        "a rotation of $azimuthDegrees deg never reaches a span of $span nm at any separation"
    }
    return half + sqrt(discriminant)
}

/**
 * The signed residue departure, folded to the range whose azimuth is smallest, from [residue] to
 * the nearest member of [allowed] — `0` when the residue is itself allowed.
 *
 * The minimum is taken on the **azimuth**, not on the base-pair count, because the two disagree:
 * see this file's header.
 */
fun minimumResidueDeparture(residue: Int, allowed: Set<Int>): Int {
    require(allowed.isNotEmpty()) { "the allowed set must not be empty" }
    val period = HoneycombCrossoverRule.SAME_PAIR_PERIOD_BP
    return allowed.flatMap { a ->
        val forward = Math.floorMod(a - residue, period)
        listOf(forward, forward - period)
    }.sortedWith(compareBy({ decidedAzimuth(azimuthalDepartureDegrees(it)) }, { abs(it) })).first()
}

/** The azimuthal departure in degrees a forced crossover at [residue] carries against [allowed]. */
fun minimumAzimuthalDeparture(residue: Int, allowed: Set<Int>): Double =
    azimuthalDepartureDegrees(minimumResidueDeparture(residue, allowed))

/** The forced-crossover census of one raster, at the class-zero residue that minimises it. */
data class ForcedCrossoverCensus(
    val rasterCrossovers: Int,
    val classZeroResidue: Int,
    val forcedCrossovers: Int,
    val residueDeparturesBasePairs: List<Int>,
    val azimuthalDeparturesDegrees: List<Double>
) {
    /** The largest azimuthal departure any forced crossover carries — `0` when none is forced. */
    val worstAzimuthalDepartureDegrees: Double =
        azimuthalDeparturesDegrees.maxOfOrNull { abs(it) } ?: 0.0
}

/**
 * The forced-crossover census of [residues], minimised first on the **count** — which is
 * `HoneycombRasterResidues.offRuleCrossovers`, reproduced here — and then on the summed azimuthal
 * departure, so that the reading is the cheapest a designer could choose.
 */
fun forcedCrossoverCensus(residues: HoneycombRasterResidues): ForcedCrossoverCensus {
    val period = HoneycombCrossoverRule.SAME_PAIR_PERIOD_BP
    val offset = HoneycombCrossoverRule.SCAFFOLD_OFFSET_BP
    val best = (0 until period).map { b0 ->
        val allowed = setOf(
            Math.floorMod(b0 + offset, period), Math.floorMod(b0 - offset, period)
        )
        val forced = residues.reducedResidues.filter { it !in allowed }
        val departures = forced.map { minimumResidueDeparture(it, allowed) }
        Triple(b0, departures, departures.sumOf { abs(azimuthalDepartureDegrees(it)) })
    }.sortedWith(compareBy({ it.second.size }, { it.third })).first()
    return ForcedCrossoverCensus(
        rasterCrossovers = residues.rasterCrossovers,
        classZeroResidue = best.first,
        forcedCrossovers = best.second.size,
        residueDeparturesBasePairs = best.second,
        azimuthalDeparturesDegrees = best.second.map { azimuthalDepartureDegrees(it) }
    )
}

/**
 * The stiffness in pN·nm/rad a localised azimuthal defect actually feels: the crossover's own
 * dihedral spring [hingeStiffness] in **series** with the duplex torsion the boundary layer offers,
 * `2C/λ` with `λ = √(C p / k_θ)` — so it is `series(k_θ, 2√(C k_θ / p))`.
 *
 * The two limits are the two things the model must have: it tends to `k_θ` as the duplex stiffens
 * (nothing relieves the crossover) and to zero as the duplex goes limp (the crossover is free).
 */
fun twistRelievedHingeStiffness(
    hingeStiffness: Double,
    torsionalRigidity: Double,
    crossoverSpacing: Double
): Double {
    require(hingeStiffness > 0.0) {
        "hingeStiffness must be positive, was: $hingeStiffness"
    }
    require(torsionalRigidity > 0.0) {
        "torsionalRigidity must be positive, was: $torsionalRigidity"
    }
    require(crossoverSpacing > 0.0) {
        "crossoverSpacing must be positive, was: $crossoverSpacing"
    }
    val duplex = 2.0 * sqrt(torsionalRigidity * hingeStiffness / crossoverSpacing)
    return hingeStiffness * duplex / (hingeStiffness + duplex)
}

/**
 * The elastic energy in pN·nm a departure of [azimuthDegrees] costs on a spring of
 * [stiffness] pN·nm/rad, **less** whatever [baselineDegrees] an allowed crossover already carries.
 *
 * The baseline is subtracted rather than added because it is a cost the design pays either way:
 * caDNAno's `±5 bp` is half a turn only to its own idealisation, the exact half turn being 5.25 bp,
 * so an *allowed* scaffold crossover already sits `0.25 bp = 8.571°` off the line of centres.
 */
fun forcedCrossoverEnergy(
    stiffness: Double,
    azimuthDegrees: Double,
    baselineDegrees: Double
): Double {
    require(stiffness > 0.0) { "stiffness must be positive, was: $stiffness" }
    require(abs(baselineDegrees) <= abs(azimuthDegrees)) {
        "the baseline $baselineDegrees cannot exceed the departure $azimuthDegrees"
    }
    val theta = Math.toRadians(azimuthDegrees)
    val base = Math.toRadians(baselineDegrees)
    return 0.5 * stiffness * (theta * theta - base * base)
}
