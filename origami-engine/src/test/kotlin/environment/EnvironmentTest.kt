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

package com.xemantic.nano.plentyofroom.environment

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.brush.AlexanderBoxLayer
import com.xemantic.nano.plentyofroom.brush.ScfDiscretisation
import com.xemantic.nano.plentyofroom.brush.SelfConsistentFieldLayer
import com.xemantic.nano.plentyofroom.brush.desCloizeauxInteraction
import com.xemantic.nano.plentyofroom.brush.graftedChain
import com.xemantic.nano.plentyofroom.electrostatics.MagnesiumChlorideBuffer
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.material.PegWater
import com.xemantic.nano.plentyofroom.quantities.ScreeningLength
import com.xemantic.nano.plentyofroom.quantities.ratioOf
import com.xemantic.nano.plentyofroom.quantities.statedRatio
import java.io.File
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-265`, step 4 of `ARCHITECTURE.md`: the two packages with no counterpart in the field, reachable
 * **without a tile**.
 *
 * The tooling survey's result is that the moat is the regime, not the ingredients. oxDNA2's
 * salt-dependent electrostatics is *"restricted to salt concentrations of 0.1 M of monovalent salt
 * or greater"* and magnesium *"is not included in the oxDNA model"*; mrDNA applies an external
 * field and solves no electrode boundary. This device's whole operating range is 0.5–10 mM MgCl₂.
 * So `brush/` and `electrostatics/` are the parts of this tree that nothing else has — and until
 * this interface existed they were reachable only *through* `Gen1Tile`.
 *
 * Every environment in this file is built from a **material**, a **charge density** and a
 * **length**. Not one line of this test constructs a tile, a lattice or a design, and
 * [the environment package imports neither structure nor tile] asserts that of the sources too.
 */
class EnvironmentTest {

    /**
     * A negative wall charge in `e/nm²` — `T-3a`'s nominal, quoted here as a **number**.
     *
     * That it happens to be a Manning-renormalised origami face is a fact about where the number
     * came from and not about what this layer needs: an environment is built from a material, a
     * charge density and a length.
     */
    private val wallCharge = -0.398665238

    private val peg = PegWater()

    private val boxModel =
        AlexanderBoxLayer(desCloizeauxInteraction(peg.crossoverIndex, peg.monomerVolume))

    private val boxChain = peg.graftedChain(250.0, 0.024)

    /** The box profile terminates, so its resting height is a root rather than a threshold. */
    private val boxRestingHeight = boxModel.equilibriumHeight(boxChain)

    private val boxLayer = GraftedLayerEnvironment(
        model = boxModel,
        chain = boxChain,
        referenceHeightNm = 0.7 * boxRestingHeight
    )

    private val scfLayer = GraftedLayerEnvironment(
        model = SelfConsistentFieldLayer(
            desCloizeauxInteraction(peg.crossoverIndex, peg.monomerVolume),
            ScfDiscretisation(nodeSpacing = 0.2, contourStepsPerMonomer = 2.0)
        ),
        chain = peg.graftedChain(250.0, 0.024),
        referenceHeightNm = 10.0
    )

    private val gap = ElectrodeGapEnvironment(
        buffer = MagnesiumChlorideBuffer(2.0),
        wallChargeDensity = wallCharge,
        referenceGapNm = 5.0,
        referenceBiasVolts = 0.5
    )

    private val edge = ElectrodeEdgeEnvironment(
        buffer = MagnesiumChlorideBuffer(2.0),
        faceChargeDensity = wallCharge,
        halfWidthNm = 20.0,
        thicknessNm = 10.0,
        referenceGapNm = 10.0,
        referenceBiasVolts = 0.192
    )

    private val environments: List<Environment> get() = listOf(boxLayer, scfLayer, gap, edge)

    // --- gate 1: P1, one interface over all four -----------------------------------------------

    @Test
    fun `every environment answers pressure, force and a decay length`() {
        environments.forEach { environment ->
            val height = environment.referenceHeightNm
            assert(environment.pressure(height).isFinite())
            assert(environment.force(height, 0.0).isFinite())
            assert(environment.decayLength.nanometres > 0.0)
            assert(environment.name.isNotBlank())
        }
    }

    @Test
    fun `the force over the reference area IS the pressure, on every environment`() {
        environments.forEach { environment ->
            val height = environment.referenceHeightNm
            val fromPressure = environment.pressure(height) * environment.referenceArea
            val fromForce = environment.force(height, 0.0)
            assert(abs(fromForce - fromPressure) < 1e-12 * (1.0 + abs(fromPressure))) {
                "${environment.name}: $fromForce against $fromPressure"
            }
        }
    }

    @Test
    fun `the reference area is one square nanometre unless a footprint is stated`() {
        assert(boxLayer.referenceArea == 1.0)
        assert(gap.referenceArea == 1.0)
        // the edge is a CROSS-SECTION, so its reference area is a strip of the half-width
        assert(edge.referenceArea.isCloseTo(20.0))
    }

    // --- gate 2: the sign convention, fixed before deriving --------------------------------------

    @Test
    fun `a compressed grafted layer pushes the two bodies apart`() {
        assert(boxLayer.pressure(0.5 * boxRestingHeight) > 0.0)
        assert(scfLayer.pressure(6.0) > 0.0)
    }

    @Test
    fun `a biased electrode pulls the charged wall toward it`() {
        // both walls are negative at zero rational bias, so the gap is weakly repulsive there and
        // strongly attractive once the electrode is driven positive
        assert(gap.force(5.0, 0.5) < 0.0)
    }

    // --- gate 3: a NEUTRAL layer is exactly bias-independent -------------------------------------

    @Test
    fun `an applied bias moves a neutral grafted layer by exactly zero`() {
        // CLAUDE.md: ideal mobile salt exerts EXACTLY no osmotic pressure on a grafted layer -
        // Pi = phi f' - f annihilates a term linear in phi. Not small: zero.
        val height = 0.5 * boxRestingHeight
        listOf(0.0, 0.1, 1.0, 2.0).forEach { bias ->
            assert(boxLayer.force(height, bias) == boxLayer.force(height, 0.0))
        }
        assert(!boxLayer.respondsToBias)
        assert(gap.respondsToBias)
        // the regime declares [0, 0] V because the MODEL contains no bias; that it is not a
        // refusal is exactly what respondsToBias = false means
        assert(boxLayer.regime.highestBiasVolts == 0.0)
    }

    // --- gate 4: P4, the regime is data and it refuses ------------------------------------------

    @Test
    fun `every environment carries a regime naming its buffer, valency, heights and band`() {
        environments.forEach { environment ->
            val regime = environment.regime
            assert(regime.name.isNotBlank())
            assert(regime.lowestHeightNm < regime.highestHeightNm)
            assert(regime.admitsHeight(environment.referenceHeightNm))
            assert(regime.temperatureKelvin.isCloseTo(300.0, 1e-3))
        }
        assert(gap.regime.bufferMillimolar == 2.0)
        assert(gap.regime.counterionValency == 2)
        assert(boxLayer.regime.bufferMillimolar == null)
    }

    @Test
    fun `an environment refuses a state outside its own regime`() {
        assertFailsWith<IllegalArgumentException> { gap.force(100.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { gap.force(5.0, 40.0) }
    }

    // --- gate 5: CH-0004 cannot be committed through this interface ------------------------------

    @Test
    fun `the gap's own decay length and the bulk Debye length are different quantities`() {
        assert(gap.bulkScreeningLength.where == ScreeningLength.BULK_RESERVOIR)
        assert(gap.decayLength.where == ScreeningLength.CONFINED_GAP)
        // C-0110: the measured decay in the gap is nowhere near the bulk lambda_D
        assert(gap.decayLength.nanometres < gap.bulkScreeningLength.nanometres)
        assertFailsWith<IllegalArgumentException> {
            ratioOf(gap.decayLength, gap.bulkScreeningLength)
        }
        val quoted = statedRatio(gap.decayLength, gap.bulkScreeningLength)
        assert(quoted.value > 0.0)
        assert(quoted.numeratorState.contains("gapNm"))
    }

    @Test
    fun `the edge's decay length is LATERAL and cannot be compared with a normal one`() {
        assert(edge.decayLength.axis == ScreeningLength.LATERAL)
        assert(gap.decayLength.axis == ScreeningLength.NORMAL)
        assertFailsWith<IllegalArgumentException> { ratioOf(edge.decayLength, gap.decayLength) }
    }

    @Test
    fun `a decay length carries the height and bias it was read at`() {
        assert(gap.decayLength.readAt["gapNm"] == "5.0")
        assert(gap.decayLength.readAt["biasVolts"] == "0.5")
        assert(scfLayer.decayLength.readAt["heightNm"] == "10.0")
    }

    // --- gate 6: P5, no tile, asserted of the SOURCES ---------------------------------------------

    @Test
    fun `the environment package imports neither structure nor tile`() {
        val sources = File("src/main/kotlin/environment").listFiles { file ->
            file.name.endsWith(".kt")
        }
        assert(sources != null && sources.isNotEmpty())
        sources!!.forEach { source ->
            val text = source.readText()
            listOf(
                "com.xemantic.nano.plentyofroom.structure",
                "com.xemantic.nano.plentyofroom.tile",
                "com.xemantic.nano.plentyofroom.coupling",
                "com.xemantic.nano.plentyofroom.crossover"
            ).forEach { forbidden ->
                assert(!text.contains("import $forbidden")) {
                    "${source.name} imports $forbidden — the whole point of layer 4 is that the " +
                        "layer, the electrolyte and the field are validatable without a tile"
                }
            }
        }
    }

    // --- gate 7: limiting cases ------------------------------------------------------------------

    @Test
    fun `the layer pressure falls monotonically as the wall is withdrawn`() {
        var previous = Double.MAX_VALUE
        listOf(0.4, 0.5, 0.6, 0.8, 1.0).forEach { fraction ->
            val pressure = boxLayer.pressure(fraction * boxRestingHeight)
            assert(pressure < previous)
            previous = pressure
        }
    }

    @Test
    fun `a wider gap carries less electrostatic force at one bias`() {
        assert(abs(gap.force(10.0, 0.5)) < abs(gap.force(5.0, 0.5)))
    }

}
