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

package com.xemantic.nano.plentyofroom.poroelastic

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Returns the drag coefficient in `pN·s/nm` that squeezing water laterally out of a
 * layer of transmissivity [transmissivity] costs a tile of footprint [footprint],
 * at solvent viscosity [viscosity]:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`γ = η G A / T`
 *
 * from `∇²p = η v̇ / T` with `p = 0` at the tile edge, mean pore pressure `p̄ = η v̇ G / T`,
 * and load `F = p̄ A`.
 *
 * It is one expression for two channels that are usually treated separately: put a Darcy
 * transmissivity in and it is poroelastic drainage; put `h³/12` in and it is the Reynolds
 * lubrication squeeze film. They are not additive and must not be added — the Brinkman
 * transmissivity interpolates between them, and `DrainageResponseTest` checks the free-film
 * limit against the classical `0.42174 η L⁴/h³` for a square plate.
 */
fun squeezeDragCoefficient(
    footprint: RectangularFootprint,
    transmissivity: Double,
    viscosity: Double
): Double {
    require(transmissivity > 0.0) { "transmissivity must be positive, was: $transmissivity" }
    require(viscosity > 0.0) { "viscosity must be positive, was: $viscosity" }
    return viscosity * footprint.drainageFactor * footprint.area / transmissivity
}

/**
 * Returns the broadside Stokes drag in `pN·s/nm` of the tile itself moving through bulk
 * buffer, `γ = 16 η R` for a disc of the same area — the second dissipation channel.
 *
 * A disc rather than the plate's own shape because the broadside result for a circular
 * disc is exact and the shape correction for a square of equal area is a few per cent,
 * well inside the factor-of-forty spread in [LayerPermeability]. It is the *floor* on the
 * tile's drag: it does not vanish however permeable the layer becomes.
 */
fun tileStokesDrag(
    footprint: RectangularFootprint,
    viscosity: Double
): Double {
    require(viscosity > 0.0) { "viscosity must be positive, was: $viscosity" }
    return 16.0 * viscosity * sqrt(footprint.area / PI)
}

/**
 * Returns the poroelastic diffusivity `D_p = k M / η` in nm²/s, with [longitudinalModulus]
 * `M = φ dΠ/dφ` in `pN/nm²`.
 *
 * `M` is deliberately an argument rather than a computation: `C-0002` gives the measured
 * equation of state from which `M = m_eff Π` follows, `C-0001` gives a layer stiffness
 * that implies `M = k h / A`, and `T-1c` is in the middle of changing the second. `T-7`
 * reports against all of them rather than picking one.
 */
fun poroelasticDiffusivity(
    permeability: Double,
    longitudinalModulus: Double,
    viscosity: Double
): Double {
    require(permeability > 0.0) { "permeability must be positive, was: $permeability" }
    require(longitudinalModulus > 0.0) {
        "longitudinalModulus must be positive, was: $longitudinalModulus"
    }
    require(viscosity > 0.0) { "viscosity must be positive, was: $viscosity" }
    return permeability * longitudinalModulus / viscosity
}

/**
 * Returns the Zimm relaxation time `η R³ / k_BT` in s of a coil of Flory radius [radius] —
 * the third dissipation channel, the polymer's own conformational relaxation.
 *
 * It matters because poroelasticity assumes the network responds *elastically* while the
 * solvent moves. If the chains relaxed more slowly than the water drained, the layer would
 * be viscoelastic rather than poroelastic and none of the rest of `T-7` would apply.
 */
fun zimmRelaxationTime(
    radius: Double,
    viscosity: Double,
    temperature: Double = ROOM_TEMPERATURE
): Double {
    require(radius > 0.0) { "radius must be positive, was: $radius" }
    require(viscosity > 0.0) { "viscosity must be positive, was: $viscosity" }
    return viscosity * radius.pow(3.0) / thermalEnergy(temperature)
}

/** Returns `γ/k` in s — the first-order relaxation time of a damped, overdamped layer. */
fun relaxationTime(drag: Double, stiffness: Double): Double {
    require(drag > 0.0) { "drag must be positive, was: $drag" }
    require(stiffness > 0.0) { "stiffness must be positive, was: $stiffness" }
    return drag / stiffness
}

/** Returns `1/(2πτ)` in Hz — the −3 dB corner of a first-order response. */
fun cornerFrequency(relaxationTime: Double): Double {
    require(relaxationTime > 0.0) { "relaxationTime must be positive, was: $relaxationTime" }
    return 1.0 / (2.0 * PI * relaxationTime)
}

/**
 * Returns `√(m k)/γ`, the quality factor of the tile as a damped oscillator.
 *
 * `Q < ½` is overdamped. For the Gen-1 tile it is ~1e-3, which is what licenses the
 * first-order [relaxationTime] picture used everywhere else, and discharges the
 * "over- versus under-damped" limiting case §5 of the problem definition names.
 */
fun qualityFactor(mass: Double, stiffness: Double, drag: Double): Double {
    require(mass > 0.0) { "mass must be positive, was: $mass" }
    require(stiffness > 0.0) { "stiffness must be positive, was: $stiffness" }
    require(drag > 0.0) { "drag must be positive, was: $drag" }
    return sqrt(mass * stiffness) / drag
}

/** Everything `T-7` computes at one (footprint, thickness, volume fraction, stiffness). */
@Serializable
data class DrainageResponse(
    val permeabilityModel: String,
    val permeability: Double,
    val screeningLength: Double,
    val screeningLengthOverThickness: Double,
    val transmissivity: Double,
    val darcyTransmissivity: Double,
    val poiseuilleTransmissivity: Double,
    val squeezeDrag: Double,
    val stokesDrag: Double,
    val totalDrag: Double,
    val stokesDragFraction: Double,
    val layerStiffness: Double,
    val relaxationTime: Double,
    val cornerFrequency: Double,
    val marginAtOneKilohertz: Double,
    val lateralDrainageLength: Double,
    val verticalDrainageLength: Double,
    val lateralOverVerticalTimeRatio: Double
)

/**
 * Assembles the whole `T-7` answer at one design point.
 *
 * [layerStiffness] is an input, not a computation, because `CH-0001` has reclassified
 * `C-0001`'s stiffnesses as bounds and `T-1c` is re-deriving them. Every reported time
 * is exactly inversely proportional to it, so a factor of two there is a factor of two
 * here and nothing else moves.
 */
fun drainageResponse(
    footprint: RectangularFootprint,
    thickness: Double,
    volumeFraction: Double,
    permeabilityModel: LayerPermeability,
    layerStiffness: Double,
    viscosity: Double
): DrainageResponse {
    require(thickness > 0.0) { "thickness must be positive, was: $thickness" }
    val permeability = permeabilityModel.permeability(volumeFraction)
    val transmissivity = brinkmanTransmissivity(permeability, thickness)
    val squeeze = squeezeDragCoefficient(footprint, transmissivity, viscosity)
    val stokes = tileStokesDrag(footprint, viscosity)
    val total = squeeze + stokes
    val tau = relaxationTime(total, layerStiffness)
    val corner = cornerFrequency(tau)
    val lateral = footprint.effectiveDrainageLength
    val vertical = verticalDrainageLength(thickness)
    return DrainageResponse(
        permeabilityModel = permeabilityModel.name,
        permeability = permeability,
        screeningLength = sqrt(permeability),
        screeningLengthOverThickness = sqrt(permeability) / thickness,
        transmissivity = transmissivity,
        darcyTransmissivity = darcyTransmissivity(permeability, thickness),
        poiseuilleTransmissivity = poiseuilleTransmissivity(thickness),
        squeezeDrag = squeeze,
        stokesDrag = stokes,
        totalDrag = total,
        stokesDragFraction = stokes / total,
        layerStiffness = layerStiffness,
        relaxationTime = tau,
        cornerFrequency = corner,
        marginAtOneKilohertz = corner / BANDWIDTH_TARGET,
        lateralDrainageLength = lateral,
        verticalDrainageLength = vertical,
        lateralOverVerticalTimeRatio = (lateral * lateral) / (vertical * vertical)
    )
}

/**
 * Returns the edge in nm of the square tile at which the corner frequency falls to
 * [targetFrequency], everything else held at the reference design point.
 *
 * The reference stiffness is scaled with the footprint area, which is what a layer of
 * fixed modulus and thickness does, so the drag's `L⁴` and the stiffness's `L²` leave
 * `τ ∝ L²` — the reason the boundary is a *tile size* statement and not a thickness one.
 *
 * Solved by bisection on the corner frequency, which is strictly decreasing in the edge.
 */
fun bindingSquareTileEdge(
    targetFrequency: Double,
    thickness: Double,
    volumeFraction: Double,
    permeabilityModel: LayerPermeability,
    referenceFootprint: RectangularFootprint,
    referenceStiffness: Double,
    viscosity: Double
): Double {
    require(targetFrequency > 0.0) { "targetFrequency must be positive, was: $targetFrequency" }
    val areaStiffness = referenceStiffness / referenceFootprint.area
    fun corner(edge: Double): Double {
        val footprint = RectangularFootprint(edge, edge, referenceFootprint.harmonics)
        return drainageResponse(
            footprint = footprint,
            thickness = thickness,
            volumeFraction = volumeFraction,
            permeabilityModel = permeabilityModel,
            layerStiffness = areaStiffness * footprint.area,
            viscosity = viscosity
        ).cornerFrequency
    }
    var low = 1e-3
    var high = 1e9
    repeat(200) {
        val middle = 0.5 * (low + high)
        if (corner(middle) > targetFrequency) low = middle else high = middle
    }
    return 0.5 * (low + high)
}

/** §3 of the problem definition: the Gen-1 bandwidth requirement, in Hz. */
const val BANDWIDTH_TARGET: Double = 1000.0
