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

package com.xemantic.nano.plentyofroom.structure

import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow

/**
 * `T-120` — is a sheet held together by **one crossover per interface** still a plate?
 *
 * ## What this file is for
 *
 * `C-0054` caps the hinge budget of a *connected* 15-duplex sheet at **42 of 56** by a
 * pigeonhole, and `C-0053` reaches the same body from the plan view and lands at **25**. Both
 * compute everything on the **lattice**, so nothing either reports is wrong — what neither asks
 * is whether the **continuum** statements the rest of the programme rests on (`C-0006`'s load
 * distribution, `C-0010`'s positional variance, `C-0047`'s flatness) still describe that body.
 *
 * `C-0009` already found the reduction *marginal by its own criterion on the intact sheet*, and
 * corrected the criterion `C-0006` had used: **a discreteness criterion must pair lengths in the
 * same direction.** `C-0006` compared the *across*-helix bending length with the *along*-helix
 * hinge spacing; matched, the two criteria are `ℓ_∥/p` and `ℓ_⊥/d`. `CLAUDE.md` records the
 * correction and adds the reading that needs no convention at all: **the number of crossovers
 * inside an anchor's influence patch.**
 *
 * ## The one parameter
 *
 * Everything here is a function of the **retained** crossover count `N_ret`, through two
 * depletion rules that are exact by construction and reduce to `C-0009`'s own constants at
 * `N_ret = N`:
 *
 * - the **depleted per-interface pitch** `p_eff = p · N/N_ret` — the areal-density reading of
 *   the along-helix crossover spacing;
 * - the **depleted across-helix rigidity** `D_⊥ = k_θ d/p_eff`, i.e. `C-0009`'s continuum
 *   `k_θ d/p` scaled by `N_ret/N`.
 *
 * `D_∥ = EI/d` is **untouched** by consumption: a crossover is not a load path along the
 * helices. So with `ℓ = (D/k_f)^(1/4)` all four criteria are exact powers of `N_ret`,
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`ℓ_⊥/d ∝ N_ret^(1/4)`, &nbsp; `ℓ_∥/p_eff ∝ N_ret`, &nbsp;
 * **`patch ∝ N_ret^(5/4)`**,
 *
 * and each **inverts in closed form** — which is the point. `CLAUDE.md`: *"invert a
 * length-dependent allowable, do not just evaluate it"*. The question a design has to answer is
 * not what the criterion reads at 14 retained; it is the count at which it crosses one.
 *
 * ## What this file deliberately does not decide
 *
 * `D_⊥` is the **smeared** (areal-density) rigidity here, which is what a continuum plate can
 * express. `C-0054` shows that the honest across-helix rigidity of a *depleted* lattice is a
 * **harmonic** mean over the interfaces and goes to exactly zero at the first empty interface,
 * where the smeared one is still finite. The two agree up to `(D/(D−1))²` **as long as no
 * interface is empty**, which is exactly the regime this file is written for: at and below the
 * connectivity ceiling every interface still holds at least one crossover. Above the ceiling the
 * smeared reading is not a conservative approximation — it is the wrong kind of object, and
 * [modelSelection] is what settles which model to use instead.
 */

// ---------------------------------------------------------------------------- the criteria

/**
 * `C-0009`'s discreteness criteria, evaluated on a lattice whose crossovers have been depleted
 * to [retainedCrossovers].
 *
 * The four ratios are, in the order `C-0009` reports them:
 *
 * | | | pairing |
 * |---|---|---|
 * | [acrossLengthOverPitch] | `ℓ_⊥/p_eff` | **mismatched** — `C-0006`'s, which `C-0009` corrected |
 * | [alongLengthOverPitch] | `ℓ_∥/p_eff` | matched, along the helices |
 * | [acrossLengthOverInterhelical] | `ℓ_⊥/d` | matched, across the helices |
 * | [crossoversInAnchorPatch] | `π ℓ_∥ ℓ_⊥/(d p_eff)` | **no convention at all** |
 *
 * @param retainedCrossovers the count the criteria are read at — a real number, because the
 *   inversions below return one and a design rounds it.
 */
@Serializable
data class LatticeDiscreteness(
    val retainedCrossovers: Double,
    val inventory: Int,
    val effectiveInterfacePitch: Double,
    val acrossHelixRigidity: Double,
    val bendingLengthAlongHelices: Double,
    val bendingLengthAcrossHelices: Double,
    val acrossLengthOverPitch: Double,
    val alongLengthOverPitch: Double,
    val acrossLengthOverInterhelical: Double,
    val crossoversInAnchorPatch: Double,
    val duplexesInAnchorPatch: Double,
    val continuumValidByMatchedCriteria: Boolean,
    val continuumValidByPatchCount: Boolean
)

/**
 * [LatticeDiscreteness] at a **real** retained count.
 *
 * The continuous form is the primitive because every inversion below returns a real number and
 * the round-trip *"evaluate the criterion at its own inverted count and get the target back"* is
 * a gate-3 check rather than a tautology.
 *
 * A count **above** the inventory is admitted here and is not an error: it is the hypothetical
 * denser lattice a criterion demands, and the along-helix one demands exactly that on the Gen-1
 * sheet (`C-0009`: *"`ℓ_∥/p` is the criterion that fails"*). The integer entry point
 * [latticeDiscreteness], which describes a lattice that exists, refuses it.
 *
 * @param retainedCrossovers must be positive — a sheet with no crossover at all has no
 *   across-helix rigidity, no bending length and therefore no criterion; that state is
 *   [modelSelection]'s `BEAM_ARRAY` and not a depleted plate.
 * @param nominalInterfacePitch `p`, the **per-interface** crossover pitch of the intact lattice
 *   (32 bp = 10.88 nm on a single-layer Rothemund sheet — `C-0015`, `C-0040`).
 * @param alongHelixRigidity `D_∥ = EI/d`, untouched by consumption.
 * @param hingeStiffness `k_θ`, the interhelical dihedral constant.
 * @param foundationStiffness `k_f` in `pN/nm³`.
 */
fun latticeDiscretenessAt(
    retainedCrossovers: Double,
    inventory: Int,
    nominalInterfacePitch: Double,
    interhelicalDistance: Double,
    alongHelixRigidity: Double,
    hingeStiffness: Double,
    foundationStiffness: Double
): LatticeDiscreteness {
    require(retainedCrossovers > 0.0) {
        "retainedCrossovers must be positive, was: $retainedCrossovers"
    }
    require(inventory > 0) { "inventory must be positive, was: $inventory" }
    require(nominalInterfacePitch > 0.0) {
        "nominalInterfacePitch must be positive, was: $nominalInterfacePitch"
    }
    require(interhelicalDistance > 0.0) {
        "interhelicalDistance must be positive, was: $interhelicalDistance"
    }
    require(alongHelixRigidity > 0.0) {
        "alongHelixRigidity must be positive, was: $alongHelixRigidity"
    }
    require(hingeStiffness > 0.0) { "hingeStiffness must be positive, was: $hingeStiffness" }
    require(foundationStiffness > 0.0) {
        "foundationStiffness must be positive, was: $foundationStiffness"
    }
    val pitch = nominalInterfacePitch * inventory / retainedCrossovers
    val acrossRigidity = hingeStiffness * interhelicalDistance / pitch
    val along = winklerLength(alongHelixRigidity, foundationStiffness)
    val across = winklerLength(acrossRigidity, foundationStiffness)
    val patch = PI * along * across / (interhelicalDistance * pitch)
    return LatticeDiscreteness(
        retainedCrossovers = retainedCrossovers,
        inventory = inventory,
        effectiveInterfacePitch = pitch,
        acrossHelixRigidity = acrossRigidity,
        bendingLengthAlongHelices = along,
        bendingLengthAcrossHelices = across,
        acrossLengthOverPitch = across / pitch,
        alongLengthOverPitch = along / pitch,
        acrossLengthOverInterhelical = across / interhelicalDistance,
        crossoversInAnchorPatch = patch,
        duplexesInAnchorPatch = 2.0 * across / interhelicalDistance,
        continuumValidByMatchedCriteria = along > pitch && across > interhelicalDistance,
        continuumValidByPatchCount = patch >= 1.0
    )
}

/** [latticeDiscretenessAt] at an integer retained count, which is what a lattice has. */
fun latticeDiscreteness(
    retainedCrossovers: Int,
    inventory: Int,
    nominalInterfacePitch: Double,
    interhelicalDistance: Double,
    alongHelixRigidity: Double,
    hingeStiffness: Double,
    foundationStiffness: Double
): LatticeDiscreteness {
    require(retainedCrossovers <= inventory) {
        "cannot retain $retainedCrossovers of an inventory of $inventory"
    }
    return latticeDiscretenessAt(
        retainedCrossovers.toDouble(), inventory, nominalInterfacePitch, interhelicalDistance,
        alongHelixRigidity, hingeStiffness, foundationStiffness
    )
}

// ---------------------------------------------------------------------------- the inversions

private fun invert(
    exponent: Double,
    target: Double,
    inventory: Int,
    valueAtInventory: Double
): Double {
    require(target > 0.0) { "target must be positive, was: $target" }
    return inventory * (target / valueAtInventory).pow(1.0 / exponent)
}

/**
 * The retained crossover count at which an anchor's influence patch holds exactly [target]
 * crossovers — `N (target/patch_N)^(4/5)`, because `patch ∝ N_ret^(5/4)`.
 *
 * **This is the criterion that decides the task**, because it needs no direction convention:
 * it counts elements inside the region an anchor's load actually reaches.
 */
fun retainedForPatchCount(
    target: Double,
    inventory: Int,
    nominalInterfacePitch: Double,
    interhelicalDistance: Double,
    alongHelixRigidity: Double,
    hingeStiffness: Double,
    foundationStiffness: Double
): Double = invert(
    1.25, target, inventory,
    latticeDiscreteness(
        inventory, inventory, nominalInterfacePitch, interhelicalDistance,
        alongHelixRigidity, hingeStiffness, foundationStiffness
    ).crossoversInAnchorPatch
)

/**
 * The retained crossover count at which `ℓ_⊥/d` reaches [target] — `N (target/ratio_N)^4`,
 * because `ℓ_⊥ ∝ D_⊥^(1/4) ∝ N_ret^(1/4)`.
 */
fun retainedForAcrossHelixCriterion(
    target: Double,
    inventory: Int,
    nominalInterfacePitch: Double,
    interhelicalDistance: Double,
    alongHelixRigidity: Double,
    hingeStiffness: Double,
    foundationStiffness: Double
): Double = invert(
    0.25, target, inventory,
    latticeDiscreteness(
        inventory, inventory, nominalInterfacePitch, interhelicalDistance,
        alongHelixRigidity, hingeStiffness, foundationStiffness
    ).acrossLengthOverInterhelical
)

/**
 * The retained crossover count at which `ℓ_∥/p_eff` reaches [target] — `N target/ratio_N`,
 * because `ℓ_∥` does not move at all and `p_eff ∝ 1/N_ret`.
 *
 * On the Gen-1 sheet at the nominal foundation this returns **more than the inventory**, which
 * is `C-0009`'s finding restated: `ℓ_∥/p` is the criterion that fails, and it fails on the
 * intact sheet.
 */
fun retainedForAlongHelixCriterion(
    target: Double,
    inventory: Int,
    nominalInterfacePitch: Double,
    interhelicalDistance: Double,
    alongHelixRigidity: Double,
    hingeStiffness: Double,
    foundationStiffness: Double
): Double = invert(
    1.0, target, inventory,
    latticeDiscreteness(
        inventory, inventory, nominalInterfacePitch, interhelicalDistance,
        alongHelixRigidity, hingeStiffness, foundationStiffness
    ).alongLengthOverPitch
)

// ---------------------------------------------------------------------------- the retention

/**
 * One retained crossover on **every** interface, chosen to spread the survivors as evenly as
 * the column lattice allows **along the helices** as well as across them.
 *
 * `C-0054`'s `SPREAD` pattern is round robin over the *interfaces* and is optimal for
 * connectivity — it is what makes the pigeonhole ceiling tight. It says nothing about which
 * **column** each survivor sits in, and its tie-break takes the lowest available one, so at the
 * ceiling all fourteen survivors land in the two lowest columns and the right two thirds of the
 * tile has no across-helix load path at all. That is a placement artefact of a tie-break, not a
 * property of the ceiling, and it is worth several tenths of a crossover in any census taken
 * over the footprint.
 *
 * This is the other extreme and the one a designer would build: interface `b` keeps the
 * crossover nearest to `(b + ½)/(D − 1)` of the tile's span. Both are swept, because
 * `C-0015`'s rule is to sweep **shapes**, and the two bracket what a real array would leave.
 *
 * @param inventory every site of the intact lattice.
 * @param duplexes the duplex count; the sheet has `duplexes − 1` interfaces.
 * @param columns the crossover column count, which fixes the span the survivors spread over.
 */
fun staggeredRetention(
    inventory: List<CrossoverSite>,
    duplexes: Int,
    columns: Int
): Set<CrossoverSite> {
    require(duplexes >= 2) { "duplexes must be at least two, was: $duplexes" }
    require(columns >= 1) { "columns must be at least one, was: $columns" }
    val interfaces = duplexes - 1
    return (0 until interfaces).mapNotNull { lower ->
        val available = inventory.filter { it.lowerBeam == lower }
        require(available.isNotEmpty()) {
            "interface $lower has no crossover at all, so no retention can connect the sheet"
        }
        val wanted = (lower + 0.5) * columns / interfaces - 0.5
        available.minByOrNull { abs(it.column - wanted) }
    }.toSet()
}

// ---------------------------------------------------------------------------- the census

/**
 * How many of [sites] lie inside the elliptical influence patch of semi-axes
 * [semiAxisAlong] (`ℓ_∥`, along `x`) and [semiAxisAcross] (`ℓ_⊥`, along `y`) centred on
 * ([centreX], [centreY]).
 *
 * The integer beside [LatticeDiscreteness.crossoversInAnchorPatch]'s continuum density. It is
 * worth carrying both: the density is a smooth function of the retained count and the census is
 * what an anchor at a *stated place* actually has to work with — and below one crossover per
 * patch the two stop being the same statement, because a count of 0.69 is 0 or 1 depending on
 * where the anchor lands. That gap is the discreteness.
 */
fun crossoversInEllipticalPatch(
    sites: List<Pair<Double, Double>>,
    centreX: Double,
    centreY: Double,
    semiAxisAlong: Double,
    semiAxisAcross: Double
): Int {
    require(semiAxisAlong > 0.0) { "semiAxisAlong must be positive, was: $semiAxisAlong" }
    require(semiAxisAcross > 0.0) { "semiAxisAcross must be positive, was: $semiAxisAcross" }
    return sites.count { (x, y) ->
        val dx = (x - centreX) / semiAxisAlong
        val dy = (y - centreY) / semiAxisAcross
        dx * dx + dy * dy <= 1.0
    }
}

// ------------------------------------------------------------------------- model selection

/**
 * Which of two reductions is the nearer model to the lattice on one quantity.
 *
 * A discreteness criterion says a continuum reduction has stopped being valid; it does not say
 * what to use instead. The two candidates here are
 *
 * - **`PLATE`** — `C-0006`'s orthotropic Kirchhoff plate with `D_⊥` smeared to `k_θ d/p_eff`;
 * - **`BEAM_ARRAY`** — the same duplexes on the same Winkler foundation with **no across-helix
 *   load path at all**, i.e. `C-0009`'s grillage with every crossover consumed.
 *
 * and the honest verdict is the consumption at which the second becomes the nearer one.
 *
 * @param lattice the reference value, which must not be zero — a departure relative to zero is
 *   not a departure, and where both models are meant to vanish the comparison must be absolute
 *   (`CLAUDE.md`).
 */
@Serializable
data class ModelSelection(
    val lattice: Double,
    val plate: Double,
    val beamArray: Double,
    val plateDeparture: Double,
    val beamArrayDeparture: Double,
    val nearerModel: String,
    val departureRatio: Double
)

/** Builds a [ModelSelection] from the three models' readings of one quantity. */
fun modelSelection(lattice: Double, plate: Double, beamArray: Double): ModelSelection {
    require(abs(lattice) > 0.0) {
        "the lattice reference must not be zero: a relative departure from zero is not a " +
                "departure, and such a comparison must be made absolutely"
    }
    val plateDeparture = abs(plate - lattice) / abs(lattice)
    val beamDeparture = abs(beamArray - lattice) / abs(lattice)
    return ModelSelection(
        lattice = lattice,
        plate = plate,
        beamArray = beamArray,
        plateDeparture = plateDeparture,
        beamArrayDeparture = beamDeparture,
        nearerModel = if (plateDeparture <= beamDeparture) "PLATE" else "BEAM_ARRAY",
        departureRatio = if (beamDeparture > 0.0) plateDeparture / beamDeparture
        else PLATE_EXACT_SENTINEL
    )
}

/**
 * Reported for [ModelSelection.departureRatio] where the beam array reproduces the lattice
 * exactly, so the ratio is unbounded.
 *
 * `CLAUDE.md`: `kotlinx.serialization` refuses `Infinity` as well as `NaN`, and an unbounded
 * ratio is the absence of a comparison rather than a large number.
 */
const val PLATE_EXACT_SENTINEL: Double = -1.0

/**
 * The spread of one response over a set of load *placements* — `max/min`.
 *
 * A continuum is **homogeneous**: it answers a point load the same way wherever the point is.
 * A lattice does not, and the size of the difference is a discreteness measure that needs no
 * length convention at all — `C-0015` measured it as *"where the anchor sits within the unit
 * cell is worth another 30 %"* on the intact sheet.
 */
@Serializable
data class RegistrationSpread(
    val minimum: Double,
    val maximum: Double,
    val ratio: Double,
    val samples: Int
)

/** Builds a [RegistrationSpread] over [values], which must be positive and non-empty. */
fun registrationSpread(values: List<Double>): RegistrationSpread {
    require(values.isNotEmpty()) { "values must not be empty" }
    require(values.all { it > 0.0 }) { "every value must be positive, were: $values" }
    val minimum = values.min()
    val maximum = values.max()
    return RegistrationSpread(minimum, maximum, maximum / minimum, values.size)
}
