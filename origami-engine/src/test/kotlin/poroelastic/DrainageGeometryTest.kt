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
import com.xemantic.kotlin.test.have
import com.xemantic.kotlin.test.should
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * Where the water actually goes, and how hard the layer makes it to get there.
 *
 * `T-7` asks which drainage length dominates rather than assuming one, so both are
 * computed as *lengths* and compared: the lateral path out to the tile edge, and the
 * vertical path through the layer thickness. Both are defined so that the drainage
 * time is `ℓ²/D_p` with the same `D_p`, which is the only way the comparison means
 * anything.
 */
class DrainageGeometryTest {

    private val gen1Tile = RectangularFootprint(40.0, 40.0)
    private val testTile = RectangularFootprint(70.0, 100.0)

    @Test
    fun `should reduce the drainage factor to an area and the drainage length to a length`() {
        // gate 1, dimensional consistency: the drainage factor is the footprint average of
        // the solution of nabla^2 u = -1, so it carries nm^2, and its square root is the
        // length that enters tau = l^2/D_p
        assert(gen1Tile.area.isCloseTo(1600.0))
        assert(gen1Tile.effectiveDrainageLength.isCloseTo(sqrt(gen1Tile.drainageFactor)))
    }

    @Test
    fun `should give the unit square the known torsion-function mean`() {
        // gate 5, cross-check against a classical result rather than against ourselves:
        // the mean of the Saint-Venant torsion function of a unit square is 0.0351443.
        assert(RectangularFootprint(1.0, 1.0).drainageFactor.isCloseTo(0.03514425, 1e-6))
        assert(gen1Tile.drainageFactor.isCloseTo(56.2308, relativeTolerance = 1e-5))
        assert(testTile.drainageFactor.isCloseTo(232.175, relativeTolerance = 1e-5))
    }

    @Test
    fun `should approach the infinite-strip limit of one twelfth`() {
        // gate 2, limiting case: for a very long rectangle the flow becomes
        // one-dimensional and the mean of (W^2/4 - x^2)/2 over the width is W^2/12
        val strip = RectangularFootprint(1.0, 1000.0, harmonics = 1499)
        assert((strip.drainageFactor * 12.0).isCloseTo(1.0, relativeTolerance = 1e-3))
    }

    @Test
    fun `should scale the drainage factor as the square of the footprint`() {
        // gate 3, symmetry: the Poisson problem is homogeneous of degree two in length,
        // and the factor is symmetric under exchanging the two edges
        assert(
            RectangularFootprint(80.0, 80.0).drainageFactor
                .isCloseTo(4.0 * gen1Tile.drainageFactor, relativeTolerance = 1e-9)
        )
        assert(
            RectangularFootprint(100.0, 70.0).drainageFactor
                .isCloseTo(testTile.drainageFactor, relativeTolerance = 1e-12)
        )
    }

    @Test
    fun `should converge the drainage factor under harmonic refinement`() {
        // gate 4, numerical convergence: the double Fourier series is truncated, and the
        // truncation error has to be demonstrably below the precision we quote
        val coarse = RectangularFootprint(40.0, 40.0, harmonics = 25).drainageFactor
        val fine = RectangularFootprint(40.0, 40.0, harmonics = 201).drainageFactor
        val finest = RectangularFootprint(40.0, 40.0, harmonics = 801).drainageFactor
        assert(abs(fine - finest) < abs(coarse - finest) / 100.0)
        assert(abs(fine - finest) / finest < 1e-7)
    }

    @Test
    fun `should recover the Darcy transmissivity when the screening length is small`() {
        // gate 2, limiting case: when sqrt(k) << h the no-slip walls matter only in thin
        // boundary layers and the depth-integrated permeability is simply k*h
        val permeability = 1e-4
        val thickness = 10.0
        val brinkman = brinkmanTransmissivity(permeability, thickness)
        assert(
            brinkman.isCloseTo(
                darcyTransmissivity(permeability, thickness),
                relativeTolerance = 3e-3
            )
        )
    }

    @Test
    fun `should recover the Poiseuille transmissivity when the screening length is large`() {
        // gate 2, the opposite limit, and the reason the Brinkman form is used at all
        // rather than plain Darcy: as the polymer vanishes the layer becomes a free
        // liquid film and the transmissivity must go to h^3/12, not to infinity
        val thickness = 10.0
        val brinkman = brinkmanTransmissivity(permeability = 1e8, thickness = thickness)
        assert(brinkman.isCloseTo(poiseuilleTransmissivity(thickness), 1e-6))
        assert(poiseuilleTransmissivity(thickness).isCloseTo(1000.0 / 12.0))
    }

    @Test
    fun `should keep the transmissivity below both limiting forms`() {
        // gate 2, monotonicity between the limits: a partially screened channel drains
        // less well than a free film and less well than an unbounded Darcy slab
        listOf(0.01, 0.1, 1.0, 10.0, 100.0).forEach { permeability ->
            val thickness = 7.0
            val brinkman = brinkmanTransmissivity(permeability, thickness)
            assert(brinkman < darcyTransmissivity(permeability, thickness))
            assert(brinkman < poiseuilleTransmissivity(thickness))
            assert(brinkman > 0.0)
        }
    }

    @Test
    fun `should put the lateral and vertical drainage lengths within one factor of the Gen-1 tile`() {
        // The finding T-7 was told not to assume. For a 40 x 40 nm tile on a 10 nm layer
        // the lateral drainage length is 7.50 nm and the vertical one 2h/pi = 6.37 nm:
        // the paths differ by 18 % in length and 1.4x in time, so neither is negligible.
        assert(gen1Tile.effectiveDrainageLength.isCloseTo(7.4987, relativeTolerance = 1e-4))
        assert(verticalDrainageLength(10.0).isCloseTo(6.3662, relativeTolerance = 1e-4))
        val ratio = gen1Tile.effectiveDrainageLength / verticalDrainageLength(10.0)
        assert(ratio.isCloseTo(1.178, relativeTolerance = 1e-3))
        // and at the 70 x 100 nm test tile of §3 the lateral path clearly wins
        assert(testTile.effectiveDrainageLength / verticalDrainageLength(10.0) > 2.0)
    }

    @Test
    fun `should place the lateral-vertical crossover at three point four layer thicknesses`() {
        // gate 5 as a closed form: sqrt(0.0351443) L = 2h/pi gives L = 3.395 h for a
        // square tile, independent of every material parameter. The Gen-1 tile at
        // L/h = 4.0 sits only 18 % past that line, which is why it had to be computed.
        val crossover = squareLateralVerticalCrossover()
        assert(crossover.isCloseTo(3.3956, relativeTolerance = 1e-4))
        val atCrossover = RectangularFootprint(crossover * 10.0, crossover * 10.0)
        assert(atCrossover.effectiveDrainageLength.isCloseTo(verticalDrainageLength(10.0), 1e-9))
    }

    @Test
    fun `should reject a non-positive footprint or thickness`() {
        assertFailsWith<IllegalArgumentException> {
            RectangularFootprint(0.0, 40.0)
        } should {
            have(message == "length must be positive, was: 0.0")
        }
        assertFailsWith<IllegalArgumentException> { brinkmanTransmissivity(1.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { brinkmanTransmissivity(0.0, 1.0) }
        assertFailsWith<IllegalArgumentException> { verticalDrainageLength(-1.0) }
    }

    @Test
    fun `should reject a harmonic count that is not a positive odd integer`() {
        assertFailsWith<IllegalArgumentException> {
            RectangularFootprint(40.0, 40.0, harmonics = 40)
        } should {
            have(message == "harmonics must be odd, was: 40")
        }
    }

    private fun squareLateralVerticalCrossover(): Double =
        (2.0 / PI) / sqrt(RectangularFootprint(1.0, 1.0).drainageFactor)

}
