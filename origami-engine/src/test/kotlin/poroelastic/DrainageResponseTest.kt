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

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.material.PegWater
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * The three dissipation channels a displacing tile has to pay, and the bandwidth
 * that comes out of the slowest of them.
 *
 * §4(d) of the problem definition asks for the drainage time *bounded*, and for the
 * conditions that would make it bind at 1 kHz. Both are computed here from the drag
 * coefficient and a stiffness supplied from outside, so that `T-1c` moving the
 * stiffness moves this result by an amount that is visible rather than buried.
 */
class DrainageResponseTest {

    private val peg = PegWater()
    private val viscosity = waterViscosity()
    private val gen1Tile = RectangularFootprint(40.0, 40.0)
    private val testTile = RectangularFootprint(70.0, 100.0)

    /** `C-0001` at `L₀ = 10 nm`, `σ = 0.024 nm⁻²`: `k = 7.4 pN/nm` over a 1600 nm² tile. */
    private val designStiffness = 7.4
    private val designVolumeFraction = 0.0288872
    private val designThickness = 10.0

    private val segmentScale = FreeDrainingSegments(
        segmentLength = peg.kuhnLength,
        segmentDiameter = peg.kuhnSegmentDiameter,
        segmentVolume = peg.kuhnSegmentVolume
    )

    @Test
    fun `should reduce the squeeze drag to a force per velocity`() {
        // gate 1, dimensional consistency: eta [pN*s/nm^2] * G [nm^2] * A [nm^2] over
        // T [nm^3] is pN*s/nm, which times a velocity in nm/s is a force in pN
        val transmissivity = brinkmanTransmissivity(0.737, designThickness)
        val drag = squeezeDragCoefficient(gen1Tile, transmissivity, viscosity)
        assert(drag.isCloseTo(viscosity * 56.2308 * 1600.0 / transmissivity, 1e-6))
        // and a relaxation time is that drag over a stiffness in pN/nm, i.e. seconds
        assert(relaxationTime(drag, designStiffness).isCloseTo(drag / designStiffness))
        assert(cornerFrequency(1.0 / (2.0 * PI)).isCloseTo(1.0))
    }

    @Test
    fun `should recover the Reynolds squeeze-film coefficient for a free water film`() {
        // gate 2, limiting case, and the check that the porous formulation degrades
        // correctly: with no polymer the drag must be the classical lubrication result
        // for a square plate, gamma = 0.42174 eta L^4 / h^3.
        val thickness = 10.0
        val edge = 40.0
        val freeFilm = squeezeDragCoefficient(
            RectangularFootprint(edge, edge),
            brinkmanTransmissivity(permeability = 1e10, thickness = thickness),
            viscosity
        )
        val reynolds = 0.4217310 * viscosity * edge.pow(4.0) / thickness.pow(3.0)
        assert(freeFilm.isCloseTo(reynolds, relativeTolerance = 1e-6))
    }

    @Test
    fun `should agree between the drag route and the poroelastic diffusion route`() {
        // gate 3, an identity rather than a coincidence: with the stiffness the layer's
        // own longitudinal modulus implies, k_layer = M A / h, the two independent routes
        //     tau = gamma / k_layer          (drag over stiffness)
        //     tau = G / D_p, D_p = k M / eta (poroelastic diffusion over a length)
        // are the SAME number, exactly, in the Darcy limit. That is what licenses
        // quoting either one, and it is the hand-off between T-7 and T-1c.
        val permeability = 1e-3
        val modulus = 0.036
        val thickness = 10.0
        val stiffness = modulus * gen1Tile.area / thickness
        val drag = squeezeDragCoefficient(
            gen1Tile, darcyTransmissivity(permeability, thickness), viscosity
        )
        val diffusive = gen1Tile.drainageFactor /
                poroelasticDiffusivity(permeability, modulus, viscosity)
        assert(relaxationTime(drag, stiffness).isCloseTo(diffusive, relativeTolerance = 1e-12))
    }

    @Test
    fun `should leave the tile overdamped by six orders of magnitude`() {
        // gate 2, over- versus under-damped, which §5 of the problem definition names
        // explicitly: a 40 x 40 x 10 nm origami tile at 1.7 g/cm^3 has an inertial time
        // of ~2 ps against a drainage time of ~2 us, so no inertial ringing exists and
        // the first-order relaxation picture used throughout is the right one.
        val mass = 1.7 * 16000.0 * GRAM_PER_CUBIC_CENTIMETRE
        val drag = squeezeDragCoefficient(
            gen1Tile, brinkmanTransmissivity(0.737, designThickness), viscosity
        )
        assert(qualityFactor(mass, designStiffness, drag) < 1e-2)
        assert(mass / drag < 1e-11)
    }

    @Test
    fun `should keep the tile Stokes drag a small correction at the design point`() {
        // gate 2 and the second dissipation channel: broadside Stokes drag on a disc of
        // equal area, 16 eta R, is 2.4 % of the squeeze drag at the conservative
        // permeability — so drainage, not bulk viscous drag on the tile, sets the band.
        val stokes = tileStokesDrag(gen1Tile, viscosity)
        val squeeze = squeezeDragCoefficient(
            gen1Tile, brinkmanTransmissivity(0.737, designThickness), viscosity
        )
        assert(stokes.isCloseTo(16.0 * viscosity * sqrt(1600.0 / PI), 1e-9))
        assert(stokes / squeeze < 0.05)
    }

    @Test
    fun `should keep the polymer's own Zimm relaxation faster than the drainage`() {
        // the third channel: if the chains themselves relaxed more slowly than the water
        // drained, the layer would not behave poroelastically at all. R_F = 8.39 nm gives
        // tau_Zimm = 0.12 us against a drainage time of 1.7 us — a factor of 14.
        val floryRadius = peg.effectiveMonomerLength * 199.44.pow(0.6)
        val zimm = zimmRelaxationTime(floryRadius, viscosity)
        assert(zimm.isCloseTo(1.219e-7, relativeTolerance = 1e-2))
        val drag = squeezeDragCoefficient(
            gen1Tile, brinkmanTransmissivity(0.737, designThickness), viscosity
        ) + tileStokesDrag(gen1Tile, viscosity)
        assert(zimm < relaxationTime(drag, designStiffness) / 10.0)
    }

    @Test
    fun `should bound the Gen-1 drainage time in the microsecond decade`() {
        // The acceptance predicate's first half. At the C-0001 design point, with the
        // slowest of the three permeability models and the 40 x 40 nm tile:
        // tau = 1.4 us, corner frequency 116 kHz, 116x the 1 kHz requirement.
        val response = drainageResponse(
            footprint = gen1Tile,
            thickness = designThickness,
            volumeFraction = designVolumeFraction,
            permeabilityModel = segmentScale,
            layerStiffness = designStiffness,
            viscosity = viscosity
        )
        assert(response.relaxationTime.isCloseTo(1.372e-6, relativeTolerance = 1e-2))
        assert(response.cornerFrequency.isCloseTo(1.160e5, relativeTolerance = 1e-2))
        assert(response.cornerFrequency > 1000.0)
    }

    @Test
    fun `should stay above one kilohertz on the largest test tile of the parameter table`() {
        // §3 allows test tiles up to ~70 x 100 nm, which is the worst case in the whole
        // design space because tau scales as the square of the footprint. Even there the
        // corner frequency is 29 kHz, and it stays above 1 kHz for a stiffness four times
        // below C-0001's — which is the sensitivity CH-0001 and T-1c make necessary.
        val stiffness = designStiffness * testTile.area / gen1Tile.area
        val worst = drainageResponse(
            footprint = testTile,
            thickness = designThickness,
            volumeFraction = designVolumeFraction,
            permeabilityModel = segmentScale,
            layerStiffness = stiffness,
            viscosity = viscosity
        )
        assert(worst.cornerFrequency.isCloseTo(2.887e4, relativeTolerance = 1e-2))
        val quarterStiffness = drainageResponse(
            footprint = testTile,
            thickness = designThickness,
            volumeFraction = designVolumeFraction,
            permeabilityModel = segmentScale,
            layerStiffness = stiffness / 4.0,
            viscosity = viscosity
        )
        assert(quarterStiffness.cornerFrequency > 1000.0)
        assert(quarterStiffness.cornerFrequency.isCloseTo(worst.cornerFrequency / 4.0, 1e-9))
    }

    @Test
    fun `should make the drainage time nearly independent of layer thickness`() {
        // The scaling half of the acceptance predicate, and the surprise: at fixed volume
        // fraction the layer thickness cancels out of tau = eta G / (k M f) except through
        // the Brinkman wall correction f, which moves only 3 % across 5-10 nm.
        // Drainage is a *footprint* problem, not a *thickness* problem.
        val times = listOf(5.0, 7.0, 10.0).map { thickness ->
            val modulus = 0.036
            val stiffness = modulus * gen1Tile.area / thickness
            drainageResponse(
                footprint = gen1Tile,
                thickness = thickness,
                volumeFraction = designVolumeFraction,
                permeabilityModel = segmentScale,
                layerStiffness = stiffness,
                viscosity = viscosity
            ).relaxationTime
        }
        val spread = times.max() / times.min()
        assert(spread < 1.35)
        // and the sign is the opposite of a thickness-limited process: the THIN layer is
        // the slow one, because a thin channel screens the flow against its own walls.
        // A vertically drained layer would be four times faster at 5 nm than at 10 nm.
        assert(times.first() > times.last())
    }

    @Test
    fun `should scale the drainage time as the square of the tile edge`() {
        // gate 3, symmetry: the squeeze drag grows as L^4 but so does the stiffness of the
        // layer under it, so the relaxation time grows only as L^2 — which is what makes
        // the 1 kHz boundary a *tile size* statement.
        val edges = listOf(40.0, 80.0)
        val responses = edges.map { edge ->
            val footprint = RectangularFootprint(edge, edge)
            drainageResponse(
                footprint = footprint,
                thickness = designThickness,
                volumeFraction = designVolumeFraction,
                permeabilityModel = segmentScale,
                layerStiffness = designStiffness * footprint.area / gen1Tile.area,
                viscosity = viscosity
            )
        }
        // exactly L^2 in the drainage channel alone
        val squeezeOnly = responses.map { it.squeezeDrag / it.layerStiffness }
        assert((squeezeOnly[1] / squeezeOnly[0]).isCloseTo(4.0, relativeTolerance = 1e-9))
        // and slightly under it once the Stokes floor, which grows only as L, is added:
        // the small tile pays proportionally more of it, so the observed ratio is 3.89
        assert(
            (responses[1].relaxationTime / responses[0].relaxationTime)
                .isCloseTo(3.894, relativeTolerance = 1e-3)
        )
    }

    @Test
    fun `should locate the tile edge at which poroelasticity binds at one kilohertz`() {
        // The second half of the acceptance predicate, as a number rather than a caveat:
        // holding everything else at the design point, a square tile has to reach 437 nm
        // on a side before drainage costs the 1 kHz requirement.
        val edge = bindingSquareTileEdge(
            targetFrequency = 1000.0,
            thickness = designThickness,
            volumeFraction = designVolumeFraction,
            permeabilityModel = segmentScale,
            referenceFootprint = gen1Tile,
            referenceStiffness = designStiffness,
            viscosity = viscosity
        )
        assert(edge.isCloseTo(437.4, relativeTolerance = 1e-2))
        val atBoundary = RectangularFootprint(edge, edge)
        val response = drainageResponse(
            footprint = atBoundary,
            thickness = designThickness,
            volumeFraction = designVolumeFraction,
            permeabilityModel = segmentScale,
            layerStiffness = designStiffness * atBoundary.area / gen1Tile.area,
            viscosity = viscosity
        )
        assert(response.cornerFrequency.isCloseTo(1000.0, relativeTolerance = 1e-6))
    }

    @Test
    fun `should keep every corner of the Gen-1 design space above one kilohertz`() {
        // The acceptance predicate, taken over the whole product space rather than at one
        // point: 4 design points x 2 footprints x 2 segment-scale permeability models x a
        // stiffness four times below C-0001's. The minimum is 5.64 kHz — a 5.6x margin at
        // the single most pessimistic combination the parameter table permits.
        val fibre = FiberArrayPermeability(fiberRadius = peg.kuhnSegmentDiameter / 2.0)
        val corners = listOf(
            Triple(5.0, 0.0707800, 110.96),
            Triple(7.0, 0.0439185, 27.11),
            Triple(10.0, 0.0288872, 7.39),
            Triple(10.0, 0.0334770, 10.33)
        ).flatMap { (thickness, phi, stiffness) ->
            listOf(gen1Tile, testTile).flatMap { footprint ->
                listOf(segmentScale, fibre).map { model ->
                    drainageResponse(
                        footprint = footprint,
                        thickness = thickness,
                        volumeFraction = phi,
                        permeabilityModel = model,
                        layerStiffness = 0.25 * stiffness * footprint.area / gen1Tile.area,
                        viscosity = viscosity
                    ).cornerFrequency
                }
            }
        }
        assert(corners.min() > BANDWIDTH_TARGET)
        assert(corners.min().isCloseTo(5.643e3, relativeTolerance = 2e-3))
    }

    @Test
    fun `should put the one kilohertz contour a factor of thirteen below the design density`() {
        // The other half of the boundary: at fixed 10 nm thickness and the Gen-1 footprint,
        // drainage costs 1 kHz only once the layer is diluted to phi = 0.00216, which is
        // 7.5 % of the design volume fraction. The binding direction is DOWNWARD, because
        // dilution raises the permeability but lowers the modulus faster.
        val bindingPhi = 0.0021573192
        val modulus = 2.25 * peg.crossoverIndex * (thermalEnergy() / peg.monomerVolume) *
                bindingPhi.pow(2.25)
        val response = drainageResponse(
            footprint = gen1Tile,
            thickness = designThickness,
            volumeFraction = bindingPhi,
            permeabilityModel = segmentScale,
            layerStiffness = modulus * gen1Tile.area / designThickness,
            viscosity = viscosity
        )
        assert(response.cornerFrequency.isCloseTo(BANDWIDTH_TARGET, relativeTolerance = 1e-6))
        assert((bindingPhi / designVolumeFraction).isCloseTo(0.0747, relativeTolerance = 1e-3))
        // and there the model has left its own domain: sqrt(k) is 0.36 of the thickness,
        // so the layer the model calls drainage-limited is one it cannot describe
        assert(response.screeningLengthOverThickness > 0.2)
    }

    @Test
    fun `should agree with the measured hydrogel poroelastic diffusivity within an order`() {
        // gate 5, against measurement on a comparable material: Gao & Cho
        // (arXiv:2209.14382, Table 1) report 21 hydrogels with permeability 2.8-23.1 nm^2
        // and modulus 7.0-85.7 kPa, i.e. k*K = 68-266 nm^2*kPa, hence a poroelastic
        // diffusivity of 0.8-3.1 x 10^8 nm^2/s in water at 300 K. Our layer, evaluated
        // with the segment-scale permeability and its own des Cloizeaux modulus, gives
        // 2.5 x 10^7 nm^2/s — a factor of 3-12 SLOWER than anything measured, which is
        // the direction a bound must err in.
        val modulus = 2.25 * peg.crossoverIndex * (thermalEnergy() / peg.monomerVolume) *
                designVolumeFraction.pow(2.25)
        val ours = poroelasticDiffusivity(
            segmentScale.permeability(designVolumeFraction), modulus, viscosity
        )
        // slowest measured gel: "Pure 0.5 %", 9.7 nm^2 at 7.0 kPa = 7.0e-3 pN/nm^2
        val slowestMeasured = poroelasticDiffusivity(9.7, 7.0e-3, viscosity)
        // fastest measured gel: "Hydro 7 %", 3.1 nm^2 at 85.7 kPa
        val fastestMeasured = poroelasticDiffusivity(3.1, 85.7e-3, viscosity)
        assert(modulus.isCloseTo(0.026034, relativeTolerance = 1e-3))
        assert(ours.isCloseTo(2.962e7, relativeTolerance = 1e-2))
        assert(slowestMeasured.isCloseTo(7.951e7, relativeTolerance = 1e-3))
        assert(fastestMeasured.isCloseTo(3.111e8, relativeTolerance = 1e-3))
        assert(ours < slowestMeasured)
        assert(ours > slowestMeasured / 10.0)
    }

    @Test
    fun `should reject a non-positive stiffness or drag`() {
        assertFailsWith<IllegalArgumentException> { relaxationTime(1.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { cornerFrequency(0.0) }
        assertFailsWith<IllegalArgumentException> {
            poroelasticDiffusivity(1.0, 0.0, viscosity)
        }
        assertFailsWith<IllegalArgumentException> { zimmRelaxationTime(0.0, viscosity) }
    }

}
