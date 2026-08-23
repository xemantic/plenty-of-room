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

/**
 * `T-297` — a crossover's **common** azimuthal mode, and the coordinate of the lattice it lives on.
 *
 * ## The span, expanded on the model's own kinematics
 *
 * `turnPhosphateSpan(d, r_P, θ_u, 180° + θ_l)` puts the two backbone phosphates
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`span² = (d − r_P(cos θ_u + cos θ_l))² + (ΔW + r_P(sin θ_u + sin θ_l))²`
 *
 * apart, where `θ_u` and `θ_l` are each duplex's own roll in the **same** rotational sense and
 * `ΔW` is the relative deflection of the two axes normal to the face. `CH-0242` expands it at
 * `ΔW = 0` and finds a quadratic form of rank two whose common mode costs
 * [commonModeSpanRatio] times the relative one.
 *
 * ## Which coordinate of the lattice that is
 *
 * `HoneycombGrillage`'s bond assembles its normal link with the gradient `(1, armY, −1, armY)`
 * over `(W_a, Φ_a, W_b, Φ_b)`, `armY = (d/2)·unitY`, so its residual is
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`R = ΔW + (d/2)·unitY·(Φ_a + Φ_b)`
 *
 * — **a function of the SUM of the two rolls**. The common mode is therefore not absent from the
 * element set at all: it is the link, carried at a **penalty** rather than at a stiffness.
 *
 * ## And `d/2` is a theorem
 *
 * The linearised rigid roll of the whole block is `Φ ≡ α`, `W = α y`, and the residual an arm `a`
 * leaves under it is `α·unitY·(2a − d)` ([rigidRollLinkResidual]) — zero for **every** bond
 * direction if and only if `a = d/2`. So no other arm may appear in a linear element, which is
 * `CLAUDE.md`'s own *"frame indifference fixes the crossover connector arm at exactly `d/2`"*
 * met on the honeycomb.
 *
 * The price of that theorem is one term: written on the two measures that annihilate the
 * linearised rigid modes — `Δφ = Φ_a − Φ_b` and `R` — the span excess is exactly
 * `(r_P/4)Δφ² + R²/(2g)`, `g = d − 2r_P`, and evaluated at fixed axes its common mode is
 * [linearisedCommonModeSpanRatio] rather than [commonModeSpanRatio]. The difference is exactly
 * [geometricCommonModeSpanRatio] — the prestress geometric stiffness of a taut connector under a
 * rotation the reduced kinematics cannot follow — and a linear analysis excludes it by
 * construction:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`d²/(2 g r_P) − d/(2 r_P) = d/g`, identically.
 *
 * Units: lengths nm, forces pN, rotational stiffness pN·nm/rad, translational stiffness pN/nm,
 * angles **radians** at this API — every function here is a coefficient rather than a geometry.
 */

/** The phosphate span in nm a crossover carries at zero departure, `g = d − 2 r_P`. */
fun crossoverSpanFloor(interhelicalDistance: Double, phosphateRadius: Double): Double {
    require(interhelicalDistance > 0.0) {
        "interhelicalDistance must be positive, was: $interhelicalDistance"
    }
    require(phosphateRadius > 0.0) { "phosphateRadius must be positive, was: $phosphateRadius" }
    require(interhelicalDistance > 2.0 * phosphateRadius) {
        "the two backbones must not overlap: $interhelicalDistance against $phosphateRadius"
    }
    return interhelicalDistance - 2.0 * phosphateRadius
}

/**
 * `CH-0242`'s ratio — the span excess a **common** roll costs over the excess an equal **relative**
 * roll costs, with the two duplex axes held at their nominal separation.
 *
 * `1 + 2 r_P/(d − 2 r_P)`, which is `d/(d − 2 r_P)` and carries neither the tension nor `k_θ`.
 */
fun commonModeSpanRatio(interhelicalDistance: Double, phosphateRadius: Double): Double =
    interhelicalDistance / crossoverSpanFloor(interhelicalDistance, phosphateRadius)

/**
 * The same ratio for the **frame-indifferent** element a linear lattice may carry,
 * `d²/(2 g r_P)`.
 *
 * It is [commonModeSpanRatio] plus [geometricCommonModeSpanRatio], exactly.
 */
fun linearisedCommonModeSpanRatio(interhelicalDistance: Double, phosphateRadius: Double): Double =
    interhelicalDistance * interhelicalDistance /
            (2.0 * crossoverSpanFloor(interhelicalDistance, phosphateRadius) * phosphateRadius)

/**
 * The prestress **geometric** term a linear analysis excludes, `d/(2 r_P)`.
 *
 * It is what a taut connector charges to a rigid roll of the pair once the axes are forbidden to
 * move with it, so it is an artefact of the reduced kinematics rather than a spring.
 */
fun geometricCommonModeSpanRatio(interhelicalDistance: Double, phosphateRadius: Double): Double {
    require(phosphateRadius > 0.0) { "phosphateRadius must be positive, was: $phosphateRadius" }
    require(interhelicalDistance > 0.0) {
        "interhelicalDistance must be positive, was: $interhelicalDistance"
    }
    return interhelicalDistance / (2.0 * phosphateRadius)
}

/**
 * The bond tension in pN that [hingeStiffness] implies, `T = 2 k_θ / r_P`.
 *
 * It is `CH-0242`'s own premise carried one step: if both eigenmodes of the span form are the
 * same mechanism, then the relative one's coefficient `T r_P/4` on `Δφ²` **is** `k_θ/2`, and the
 * tension follows. It is an attribution, not a measurement — `k_θ` is Chen et al.'s fitted
 * dihedral constant and nothing in it is resolved into stacking, backbone and junction geometry.
 */
fun impliedCrossoverBondTension(hingeStiffness: Double, phosphateRadius: Double): Double {
    require(hingeStiffness > 0.0) { "hingeStiffness must be positive, was: $hingeStiffness" }
    require(phosphateRadius > 0.0) { "phosphateRadius must be positive, was: $phosphateRadius" }
    return 2.0 * hingeStiffness / phosphateRadius
}

/**
 * The normal link stiffness in pN/nm the same span law implies, `k_R = T/g`.
 *
 * A connector nearly perpendicular to a relative transverse displacement resists it as a taut
 * string does, `tension over length`, because the displacement changes the span only at second
 * order. That is the number `HoneycombGrillage.RIGID_LINK_STIFFNESS` stands against.
 */
fun spanDerivedLinkStiffness(
    hingeStiffness: Double,
    phosphateRadius: Double,
    interhelicalDistance: Double
): Double = impliedCrossoverBondTension(hingeStiffness, phosphateRadius) /
        crossoverSpanFloor(interhelicalDistance, phosphateRadius)

/**
 * The common-mode azimuthal stiffness in pN·nm/rad one bond of the lattice carries, at fixed axes.
 *
 * A common roll `θ` of both duplexes leaves the residual `d·unitY·θ`, so the link stores
 * `½ k_link (d·unitY·θ)²`; read as `½ k (Φ_a + Φ_b)²` that is `k = k_link (d·unitY)²/4`.
 * It is **`unitY`-dependent**, so the model's in-plane bonds carry four times what its interlayer
 * ones do — an artefact of a kinematics with no in-plane transverse coordinate, since the span
 * form itself has no such anisotropy.
 */
fun latticeCommonModeAzimuthalStiffness(
    linkStiffness: Double,
    interhelicalDistance: Double,
    bondUnitY: Double
): Double {
    require(linkStiffness > 0.0) { "linkStiffness must be positive, was: $linkStiffness" }
    require(interhelicalDistance > 0.0) {
        "interhelicalDistance must be positive, was: $interhelicalDistance"
    }
    val arm = interhelicalDistance * bondUnitY
    return linkStiffness * arm * arm / 4.0
}

/** The connector arm frame indifference forces, `d/2`. */
fun frameIndifferentLinkArm(interhelicalDistance: Double): Double {
    require(interhelicalDistance > 0.0) {
        "interhelicalDistance must be positive, was: $interhelicalDistance"
    }
    return interhelicalDistance / 2.0
}

/**
 * The link residual in nm a linearised rigid roll of [rollRadians] leaves at a bond of direction
 * [bondUnitY] whose connector arm is [connectorArm] — `α·unitY·(2a − d)`.
 *
 * Zero at every bond direction if and only if `a = d/2`, which is what makes the arm a theorem.
 */
fun rigidRollLinkResidual(
    interhelicalDistance: Double,
    bondUnitY: Double,
    connectorArm: Double,
    rollRadians: Double
): Double {
    require(interhelicalDistance > 0.0) {
        "interhelicalDistance must be positive, was: $interhelicalDistance"
    }
    require(rollRadians.isFinite()) { "rollRadians must be finite, was: $rollRadians" }
    return rollRadians * bondUnitY * (2.0 * connectorArm - interhelicalDistance)
}

/**
 * The link residual in nm a crossover relaxed at a **common** roll of [rollRadians] sits at —
 * `d·unitY·roll`, the eigenstrain of the coordinate the departure actually loads.
 */
fun turnLinkOffset(
    interhelicalDistance: Double,
    bondUnitY: Double,
    rollRadians: Double
): Double {
    require(interhelicalDistance > 0.0) {
        "interhelicalDistance must be positive, was: $interhelicalDistance"
    }
    require(rollRadians.isFinite()) { "rollRadians must be finite, was: $rollRadians" }
    return interhelicalDistance * bondUnitY * rollRadians
}
