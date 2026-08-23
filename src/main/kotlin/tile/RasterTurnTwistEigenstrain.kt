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

import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * `T-291` — the allowed departure is **common-mode**, and what replaces it is a per-beam **twist**.
 *
 * ## The coordinate, in two lines of the corpus's own algebra
 *
 * A honeycomb scaffold crossover joins two duplexes that are parallel, same-handed and at one
 * design twist, and caDNAno's *"points of closest proximity"* puts their backbones **antipodal**
 * at the staple level. Displacing the crossover by `k` base pairs therefore rotates **both**
 * backbones by `k · 240/7°` in the **same** sense — [ForcedCrossoverPrice]'s own header says so,
 * and `forcedCrossoverSpan`'s `(θ, 180° + θ)` is that construction. So
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`∂(ψ_P − ψ_Q)/∂ζ = 0`, identically,
 *
 * which is [honeycombBondRelativeAzimuthDegrees]. **A level displacement produces no relative
 * azimuth at all**, and the model's tie prestrain — an equal-and-opposite couple pair, the work
 * conjugate of `Φ_upper − Φ_lower` — has coefficient exactly zero on it (`CH-0240`).
 *
 * ## And the relative channel does not merely relieve it less well; it does not relieve it at all
 *
 * At a common-mode departure `δ`, a **pure relative** roll of amplitude `r` puts the two backbones
 * at `(δ + r, 180° + δ − r)`, and the span is then
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`span²(u) = (d − 2 r_P cos δ · u)² + 4 r_P² sin²δ · u²`, `u = cos r`,
 *
 * whose stationary point is `u* = d cos δ / (2 r_P)`. At the honeycomb's own `d = 2.536 nm` and
 * `T-71`'s measured `r_P` that is **1.38**, i.e. **outside** the reachable `[−1, 1]` — so the
 * minimum over the whole relative channel is at `u = 1`, which is `r = 0`, the built state itself.
 * [relativeRollSpanShortfall] is the amount it leaves on the table, and it is the whole departure.
 *
 * ## What replaces it
 *
 * The relief is a roll of **each** duplex about its own axis by `−δ`. `C-0187` derives that the
 * departures **alternate**, so an interior helix is asked for `−δ` at one end and `+δ` at the
 * other: a **twist** of `2δ = 17.1428571°` over its own row, uniform in sign over every interior
 * helix of the recommended raster. A helix with a demand at only **one** end carries no twist at
 * all, because a single-ended roll demand is a rigid roll of that duplex.
 *
 * Units: angles in **degrees** at this API and radians only where a lattice is loaded; `GJ` in
 * pN·nm², lengths nm, energies pN·nm.
 */

/**
 * The relative backbone azimuth in degrees of the two duplexes of a honeycomb scaffold crossover
 * displaced by [displacementBasePairs] from the staple position — **180° at every displacement**.
 *
 * It is `ForcedCrossoverPrice`'s own `(θ, 180° + θ)` pair read as a difference, which is the whole
 * of `CH-0240` §2: the derivative in the level is identically zero. The **algebra** is exact; its
 * floating-point evaluation is not, because `(180 + θ) − θ` loses the last unit in the last place
 * of `θ`, so its test is written at `1e−12` degrees rather than as an equality.
 */
fun honeycombBondRelativeAzimuthDegrees(displacementBasePairs: Int): Double {
    val exit = azimuthalDepartureDegrees(displacementBasePairs)
    val entry = 180.0 + exit
    return entry - exit
}

/**
 * How much phosphate span in nm the **best** pure relative roll still leaves above the minimum, at
 * a common-mode departure of [departureDegrees].
 *
 * `0` exactly at zero departure. Above it the stationary point `u* = d cos δ / (2 r_P)` is greater
 * than one for every geometry with `d cos δ > 2 r_P` — which the honeycomb is — so the minimum is
 * at `u = 1` and the shortfall is the **whole** span excess the departure carries: the relative
 * channel does not relieve a common-mode demand at all.
 */
fun relativeRollSpanShortfall(
    interhelicalDistance: Double,
    phosphateRadius: Double,
    departureDegrees: Double
): Double {
    require(interhelicalDistance > 0.0) {
        "interhelicalDistance must be positive, was: $interhelicalDistance"
    }
    require(phosphateRadius > 0.0) {
        "phosphateRadius must be positive, was: $phosphateRadius"
    }
    val delta = Math.toRadians(departureDegrees)
    val stationary = interhelicalDistance * cos(delta) / (2.0 * phosphateRadius)
    val u = max(-1.0, min(1.0, stationary))
    val x = interhelicalDistance - 2.0 * phosphateRadius * cos(delta) * u
    val y = 2.0 * phosphateRadius * sin(delta) * u
    val best = sqrt(x * x + y * y)
    return best - (interhelicalDistance - 2.0 * phosphateRadius)
}

/** The stationary point `u* = d cos δ / (2 r_P)` of the relative channel, in `cos r`. */
fun relativeRollStationaryCosine(
    interhelicalDistance: Double,
    phosphateRadius: Double,
    departureDegrees: Double
): Double = interhelicalDistance * cos(Math.toRadians(departureDegrees)) / (2.0 * phosphateRadius)

/** The roll a raster turn demands of **both** of its duplexes. */
data class HoneycombTurnRollDemand(

    /** The turn's position along the raster path. */
    val index: Int,

    /** The lower of the two beam indices the turn joins. */
    val lowerBeam: Int,

    /** The upper of the two. */
    val upperBeam: Int,

    /** Whether the turn sits at the block's high axial rim, `s = +L/2`. */
    val atHighEnd: Boolean,

    /** The derived azimuthal departure in degrees the crossover is built at. */
    val departureDegrees: Double,

    /**
     * The roll in degrees the demand asks of **each** duplex — the negation of the departure,
     * carried through the one global phase the lattice does not fix.
     */
    val rollDegrees: Double
)

/** One beam's twist demand, and the two roll demands it is the difference of. */
data class HoneycombBeamTwistDemand(

    /** The beam index, `rasterRow · helicesPerRow + column`. */
    val beam: Int,

    /** The roll in degrees demanded at `s = +L/2`, or `null` where no turn sits there. */
    val highEndRollDegrees: Double?,

    /** The roll in degrees demanded at `s = −L/2`, or `null` where no turn sits there. */
    val lowEndRollDegrees: Double?,

    /**
     * `φ_d(+L/2) − φ_d(−L/2)` in degrees, and **exactly zero** where either end is free: a
     * single-ended roll demand is a rigid roll of the duplex and the model has no spring for it.
     */
    val twistDegrees: Double
)

/**
 * The roll every raster turn of [signs] demands of both of its duplexes.
 *
 * @param phase the one binary neither the lattice nor this repository fixes — the map from the
 *   derived azimuthal sense onto the model's own roll sense `Φ`. `+1` and `−1` are graded.
 * @param departureDegrees the magnitude, `C-0152`'s allowed one by default.
 */
fun honeycombTurnRollDemands(
    signs: HoneycombRasterTurnSigns,
    phase: Int,
    departureDegrees: Double = allowedScaffoldCrossoverDepartureDegrees()
): List<HoneycombTurnRollDemand> {
    require(phase == 1 || phase == -1) { "phase must be +1 or -1, was: $phase" }
    require(departureDegrees.isFinite() && departureDegrees >= 0.0) {
        "departureDegrees must be a finite magnitude, was: $departureDegrees"
    }
    val turns = honeycombRasterTurnList(signs.block, signs.firstAxialSign)
    val assignment = signs.signs
    check(turns.size == assignment.size) {
        "the tie list carries ${turns.size} turns and the derived assignment ${assignment.size}"
    }
    val scale = departureDegrees / allowedScaffoldCrossoverDepartureDegrees()
    return turns.mapIndexed { k, turn ->
        val departure = scale * assignment[k].departureDegrees
        HoneycombTurnRollDemand(
            index = k,
            lowerBeam = turn.lowerBeam,
            upperBeam = turn.upperBeam,
            atHighEnd = turn.atHighEnd,
            departureDegrees = departure,
            rollDegrees = -phase * departure
        )
    }
}

/**
 * The per-beam twist demand of [signs] — one entry per beam of the block, in raster-independent
 * beam order.
 *
 * Every beam that carries a turn at **both** rims takes the difference of the two roll demands;
 * every beam that does not takes zero.
 */
fun honeycombBeamTwistDemands(
    signs: HoneycombRasterTurnSigns,
    phase: Int,
    departureDegrees: Double = allowedScaffoldCrossoverDepartureDegrees()
): List<HoneycombBeamTwistDemand> {
    val demands = honeycombTurnRollDemands(signs, phase, departureDegrees)
    val high = HashMap<Int, Double>()
    val low = HashMap<Int, Double>()
    demands.forEach { demand ->
        val target = if (demand.atHighEnd) high else low
        listOf(demand.lowerBeam, demand.upperBeam).forEach { beam ->
            val standing = target[beam]
            check(standing == null || standing == demand.rollDegrees) {
                "beam $beam carries two different roll demands at one rim: " +
                        "$standing and ${demand.rollDegrees}"
            }
            target[beam] = demand.rollDegrees
        }
    }
    return (0 until signs.block.helices).map { beam ->
        val top = high[beam]
        val bottom = low[beam]
        HoneycombBeamTwistDemand(
            beam = beam,
            highEndRollDegrees = top,
            lowEndRollDegrees = bottom,
            twistDegrees = if (top == null || bottom == null) 0.0 else top - bottom
        )
    }
}

/** The twist demand of [signs] as the radian map [HoneycombGrillage.beamTwistResponse] takes. */
fun honeycombBeamTwists(
    signs: HoneycombRasterTurnSigns,
    phase: Int,
    departureDegrees: Double = allowedScaffoldCrossoverDepartureDegrees()
): Map<Int, Double> = honeycombBeamTwistDemands(signs, phase, departureDegrees)
    .filter { it.twistDegrees != 0.0 }
    .associate { it.beam to Math.toRadians(it.twistDegrees) }

/**
 * The energy in pN·nm a twist of [twistRadians] costs a beam of length [beamLength] whose ends are
 * **rigidly held** — `½ (GJ/L) θ₀²`.
 *
 * It is a strict **upper** bound on what the eigenstrain actually stores, because the relaxed
 * state minimises over every admissible channel; it is the cheap bound `C-0152` §5 used on the
 * roll channel, read on the one the demand actually loads.
 */
fun beamTwistRestrainedEnergy(
    torsionalRigidity: Double,
    beamLength: Double,
    twistRadians: Double
): Double {
    require(torsionalRigidity > 0.0) {
        "torsionalRigidity must be positive, was: $torsionalRigidity"
    }
    require(beamLength > 0.0) { "beamLength must be positive, was: $beamLength" }
    require(twistRadians.isFinite()) { "twistRadians must be finite, was: $twistRadians" }
    return 0.5 * (torsionalRigidity / beamLength) * twistRadians * twistRadians
}
