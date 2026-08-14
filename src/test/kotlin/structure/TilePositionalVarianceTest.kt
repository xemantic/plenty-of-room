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

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.equipartitionRms
import com.xemantic.nano.plentyofroom.equipartitionStiffness
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.brush.heightUnderLoad
import com.xemantic.nano.plentyofroom.brush.stiffness
import com.xemantic.nano.plentyofroom.material.PegWater
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-8` / leaf `A1.2` — the tile's positional variance at 300 K.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem
 * definition. Gate 3 is deliberately **not** a restatement of the equipartition
 * construction the budget is built from: it checks two *independent* things —
 * the fluctuation-dissipation route to the same variance through a static compliance
 * solve, and the Lorentzian sum rule that ties the bandwidth split to that variance.
 */

/** The §3 Gen-1 tile footprint. */
private const val TILE_EDGE = 40.0
private const val TILE_AREA = TILE_EDGE * TILE_EDGE

/** A round anisotropic tile, so every assertion below is a closed form and not a regression. */
private val anisotropicTile = OrthotropicPlate(
    lengthX = TILE_EDGE,
    lengthY = TILE_EDGE,
    rigidityX = 80.0,
    rigidityY = 50.0,
    twistingRigidity = 40.0
)

/** The same footprint made effectively rigid, for the closed-form rigid-body limits. */
private val rigidTile = anisotropicTile.copy(
    rigidityX = 1e12, rigidityY = 1e12, twistingRigidity = 1e12
)

class TilePositionalVarianceTest {

    // ---------------------------------------------------------------- gate 1

    @Test
    fun `gate 1 dimensional consistency - equipartition should round-trip a stiffness through an amplitude`() {
        listOf(0.46, 7.402, 414.0).forEach { stiffness ->
            assert(equipartitionStiffness(equipartitionRms(stiffness)).isCloseTo(stiffness, 1e-12))
        }
    }

    @Test
    fun `gate 1 dimensional consistency - the Einstein relation should return k_BT when multiplied back by the drag`() {
        val drag = 9.84e-6
        val diffusivity = einsteinDiffusivity(drag)
        // pN*nm / (pN*s/nm) = nm^2/s, and D * gamma must return the energy exactly
        assert((diffusivity * drag).isCloseTo(thermalEnergy(), 1e-12))
    }

    @Test
    fun `gate 1 dimensional consistency - the Brinkman shear drag should be a viscosity times an area over a length`() {
        val viscosity = 8.541e-10
        val drag = brinkmanShearDrag(
            viscosity = viscosity, area = TILE_AREA, screeningLength = 1.0, thickness = 1e6
        )
        // deep-layer limit: coth -> 1, so the drag is eta * A / sqrt(k) exactly
        assert(drag.isCloseTo(viscosity * TILE_AREA / 1.0, 1e-9))
        // halving the screening length doubles the drag, in that limit
        val halved = brinkmanShearDrag(viscosity, TILE_AREA, 0.5, 1e6)
        assert(halved.isCloseTo(2.0 * drag, 1e-9))
    }

    @Test
    fun `gate 1 dimensional consistency - a variance fraction should be dimensionless and bounded by unity`() {
        listOf(1.0, 1e3, 1e5, 1e9).forEach { frequency ->
            val fraction = varianceFractionBelow(frequency, cornerFrequency = 9.1e4)
            assert(fraction > 0.0 && fraction <= 1.0)
        }
    }

    // ---------------------------------------------------------------- gate 2

    /**
     * The closed-form limit the whole modal budget has to reproduce: a plate stiff enough
     * to be rigid keeps exactly three degrees of freedom, and their amplitudes are exact
     * multiples of the piston mode, whatever the rigidity actually is.
     *
     * - piston `√(k_BT/(k_f A))`;
     * - two tilts, each of stiffness `k_f A/3`, contributing `√2` pistons to the area RMS;
     * - hence an area RMS of exactly `√3` pistons, a centre point of exactly 1, and a
     *   **corner** point of exactly `√7` — the corner is where both tilts are at full lever.
     */
    @Test
    fun `gate 2 limiting cases - a rigid plate should give exactly one, root three and root seven pistons`() {
        val budget = PlateOnFoundation(rigidTile, 0.01).positionalVarianceBudget()
        val piston = sqrt(thermalEnergy() / (0.01 * TILE_AREA))
        assert(budget.pistonRms.isCloseTo(piston, 1e-12))
        assert(budget.tiltRms.isCloseTo(sqrt(2.0) * piston, 1e-12))
        assert(budget.dishingRms < 1e-4 * piston)
        assert(budget.areaRms.isCloseTo(sqrt(3.0) * piston, 1e-4))
        assert(budget.centreRms.isCloseTo(piston, 1e-5))
        assert(budget.cornerRms.isCloseTo(sqrt(7.0) * piston, 1e-4))
        assert(budget.edgeMidpointRms.isCloseTo(2.0 * piston, 1e-4))
    }

    @Test
    fun `gate 2 limiting cases - a stiffer foundation should shrink every mode as its inverse square root`() {
        val soft = PlateOnFoundation(rigidTile, 0.01).positionalVarianceBudget()
        val stiff = PlateOnFoundation(rigidTile, 0.04).positionalVarianceBudget()
        assert(stiff.pistonRms.isCloseTo(soft.pistonRms / 2.0, 1e-9))
        assert(stiff.cornerRms.isCloseTo(soft.cornerRms / 2.0, 1e-6))
    }

    /**
     * The finding this task inherits from `C-0006` and has to re-test at a different
     * stiffness bracket: the *shape* modes carry more thermal amplitude than the rigid
     * piston mode, and the ratio grows as the foundation stiffens, because the dishing
     * modes are stiffened by `D q⁴` and the piston mode is not stiffened at all.
     */
    @Test
    fun `gate 2 limiting cases - the dishing to piston ratio should grow with the foundation stiffness`() {
        val ratios = listOf(0.0025, 0.01, 0.04, 0.16).map {
            PlateOnFoundation(anisotropicTile, it).positionalVarianceBudget().dishingOverPiston
        }
        assert(ratios == ratios.sorted())
    }

    @Test
    fun `gate 2 limiting cases - the Brinkman shear drag should reduce to a free water film when the layer stops screening`() {
        val viscosity = 8.541e-10
        val thickness = 10.0
        val free = brinkmanShearDrag(viscosity, TILE_AREA, screeningLength = 1e7, thickness = thickness)
        assert(free.isCloseTo(viscosity * TILE_AREA / thickness, 1e-6))
    }

    @Test
    fun `gate 2 limiting cases - the variance fraction should run from zero to one through a half at the corner`() {
        val corner = 9.1e4
        assert(varianceFractionBelow(1e-9, corner) < 1e-12)
        assert(varianceFractionBelow(corner, corner).isCloseTo(0.5, 1e-12))
        assert(varianceFractionBelow(1e12, corner).isCloseTo(1.0, 1e-6))
        val fractions = listOf(1e2, 1e3, 1e4, 1e5, 1e6).map { varianceFractionBelow(it, corner) }
        assert(fractions == fractions.sorted())
    }

    /**
     * The reason the piston mode's corner frequency may be used for the whole budget:
     * the fraction of variance below a fixed frequency is monotone **decreasing** in the
     * corner frequency, so the slowest mode gives the largest low-frequency share.
     */
    @Test
    fun `gate 2 limiting cases - a higher corner frequency should leave less variance below a fixed band edge`() {
        val fractions = listOf(1e4, 3e4, 9.1e4, 6e5).map { varianceFractionBelow(1000.0, it) }
        assert(fractions == fractions.sortedDescending())
    }

    /**
     * The lateral mode has no restoring force from a laterally uniform layer, so it is
     * **not** a harmonic degree of freedom and equipartition does not apply to it at all:
     * its excursion grows without bound as `√t`, and doubling the observation time
     * multiplies it by exactly `√2` rather than leaving it alone.
     */
    @Test
    fun `gate 2 limiting cases - an unconfined lateral mode should grow as the square root of time without bound`() {
        val diffusivity = 3.0e6
        val short = freeDiffusionRms(diffusivity, 1e-3)
        val long = freeDiffusionRms(diffusivity, 2e-3)
        assert(long.isCloseTo(sqrt(2.0) * short, 1e-12))
        // a micrometre of wander in one second, on a 40 nm tile: 61 tile widths
        assert(freeDiffusionRms(diffusivity, 1.0) > 1e3)
    }

    // ---------------------------------------------------------------- gate 3

    /**
     * Gate 3, and deliberately not equipartition restated. The piston variance is
     * `k_BT (K⁻¹)₀₀`, and the budget obtains it from the Cholesky inverse diagonal.
     * The **same** number is `k_BT` times the static compliance of the plate under a
     * unit uniform load — a completely different code path, which assembles the load
     * vector by Gauss quadrature over a pressure field and then back-substitutes.
     * Fluctuation-dissipation says the two must agree; if the load assembly, the
     * quadrature or the factorisation were wrong they would not.
     */
    @Test
    fun `gate 3 fluctuation-dissipation - the static compliance route should reproduce the modal piston variance`() {
        listOf(0.0025, 0.01, 0.05).forEach { foundation ->
            val solver = PlateOnFoundation(anisotropicTile, foundation)
            assert(
                solver.pistonComplianceRms().isCloseTo(
                    solver.thermalFluctuation(ROOM_TEMPERATURE).pistonRms, 1e-9
                )
            )
        }
    }

    /**
     * The same argument at a *point*: the point variance is `k_BT b(x)ᵀK⁻¹b(x)`, which is
     * `k_BT` times the deflection at `x` under a **unit point load at `x`** — the
     * reciprocal-compliance form of fluctuation-dissipation. Checked against the solver's
     * own centre-point fluctuation, which reaches it through the modal covariance.
     */
    @Test
    fun `gate 3 fluctuation-dissipation - the point compliance should reproduce the modal point variance`() {
        val solver = PlateOnFoundation(anisotropicTile, 0.01)
        assert(
            solver.pointFluctuationRms(0.0, 0.0)
                .isCloseTo(solver.thermalFluctuation(ROOM_TEMPERATURE).centreRms, 1e-9)
        )
    }

    /**
     * The Lorentzian sum rule. The bandwidth split is only meaningful if the spectrum it
     * splits integrates back to the equipartition variance:
     * `∫₀^∞ 4k_BTγ/(k² + (2πfγ)²) df = k_BT/k`. Integrated numerically, so the closed-form
     * `arctan` split in [varianceFractionBelow] is checked against the spectrum rather
     * than assumed to describe it.
     */
    @Test
    fun `gate 3 fluctuation-dissipation - the Lorentzian spectrum should integrate to the equipartition variance`() {
        val drag = 9.84e-6
        val stiffness = 7.402
        val corner = lorentzianCornerFrequency(stiffness, drag)
        // substitute f = corner * tan(u), which turns the Lorentzian into a constant
        val panels = 20_000
        var total = 0.0
        for (i in 0 until panels) {
            val u = (PI / 2.0) * (i + 0.5) / panels
            val frequency = corner * kotlin.math.tan(u)
            val jacobian = corner * (1.0 + kotlin.math.tan(u) * kotlin.math.tan(u))
            total += lorentzianSpectralDensity(frequency, drag, stiffness) * jacobian *
                    (PI / 2.0) / panels
        }
        assert(total.isCloseTo(thermalEnergy() / stiffness, 1e-6))
    }

    /**
     * The bridge between the two descriptions of the lateral mode, and the reason the
     * free-diffusion excursion is not an alternative theory but the `k → 0` limit of the
     * same one. An Ornstein-Uhlenbeck coordinate must reduce to free diffusion for
     * `t ≪ γ/k` and to equipartition for `t ≫ γ/k`.
     */
    @Test
    fun `gate 3 fluctuation-dissipation - a confined lateral mode should interpolate free diffusion and equipartition`() {
        val diffusivity = 3.0e6
        val stiffness = 0.46
        val relaxation = thermalEnergy() / (diffusivity * stiffness)
        assert(
            ornsteinUhlenbeckRms(diffusivity, stiffness, 1e-6 * relaxation)
                .isCloseTo(freeDiffusionRms(diffusivity, 1e-6 * relaxation), 1e-5)
        )
        assert(
            ornsteinUhlenbeckRms(diffusivity, stiffness, 1e6 * relaxation)
                .isCloseTo(equipartitionRms(stiffness), 1e-9)
        )
    }

    /**
     * Conservation, in the form this task needs: the modal budget is a partition of the
     * total, so the area-mean-square must be the sum of the three parts exactly, and the
     * rigid-body part must be what a rigid-plate model is able to represent.
     */
    @Test
    fun `gate 3 conservation - the area mean square should partition exactly into piston, tilt and dishing`() {
        val budget = PlateOnFoundation(anisotropicTile, 0.0046263).positionalVarianceBudget()
        val partitioned = budget.pistonRms * budget.pistonRms +
                budget.tiltRms * budget.tiltRms + budget.dishingRms * budget.dishingRms
        assert((budget.areaRms * budget.areaRms).isCloseTo(partitioned, 1e-12))
        assert(
            (budget.rigidBodyRms * budget.rigidBodyRms).isCloseTo(
                budget.pistonRms * budget.pistonRms + budget.tiltRms * budget.tiltRms, 1e-12
            )
        )
    }

    // ---------------------------------------------------------------- gate 4

    @Test
    fun `gate 4 numerical convergence - the point fluctuation should converge upward in the basis degree`() {
        val amplitudes = listOf(8, 12, 16, 20).map {
            PlateOnFoundation(anisotropicTile, 0.0046263, basisDegree = it)
                .positionalVarianceBudget().cornerRms
        }
        // a Ritz restriction can only stiffen, so the compliance and hence the variance
        // can only grow with the basis: monotone non-decreasing is a certain property
        assert(amplitudes == amplitudes.sorted())
        val last = amplitudes.last()
        assert(abs(last - amplitudes[amplitudes.size - 2]) / last < 0.05)
    }

    @Test
    fun `gate 4 numerical convergence - the area RMS should converge in the basis degree`() {
        val amplitudes = listOf(8, 12, 16).map {
            PlateOnFoundation(anisotropicTile, 0.0046263, basisDegree = it)
                .positionalVarianceBudget().areaRms
        }
        assert(amplitudes == amplitudes.sorted())
        assert(abs(amplitudes[2] - amplitudes[1]) / amplitudes[2] < 0.02)
    }

    // ---------------------------------------------------------------- gate 5

    /**
     * Gate 5 against NDI's own leaf `A1.1`, whose acceptance string *is* a bound table:
     * "sigma=3 nm -> k>=~0.46 pN/nm; sigma=0.1 nm (prize) -> k>=~414 pN/nm;
     * sigma=0.03 nm -> k>=~4.6 N/m (kBT=4.14 pN.nm @300K)".
     * Reproduced from `k_BT = 4.142 pN·nm` and nothing else.
     */
    @Test
    fun `gate 5 literature cross-check - the leaf A1_1 bound table should be reproduced from k_BT alone`() {
        assert(abs(equipartitionStiffness(3.0) - 0.46) < 0.005)
        assert(abs(equipartitionStiffness(0.1) - 414.0) < 1.0)
        // 4.6 N/m is 4600 pN/nm in the locked units
        assert(abs(equipartitionStiffness(0.03) - 4600.0) < 30.0)
    }

    /**
     * Gate 5 against the standing claim this task is required to consume: `C-0006` reports
     * 0.748 nm piston, 1.272 nm dishing and 1.365 nm at the centre for the nominal sheet on
     * `C-0001`'s stiffness at first contact. Reproduced here through the new budget, which
     * is a different assembly of the same solver.
     */
    @Test
    fun `gate 5 literature cross-check - the C-0006 thermal amplitudes should be reproduced by the budget`() {
        val (_, sheet) = gen1SheetVariants().first()
        val plate = sheet.plate(Gen1Tile.EDGE_X, Gen1Tile.EDGE_Y)
        val budget = PlateOnFoundation(plate, Gen1Tile.FOUNDATION_AT_REST)
            .positionalVarianceBudget()
        assert(abs(budget.pistonRms - 0.748) < 0.005)
        assert(abs(budget.dishingRms - 1.272) < 0.010)
        assert(abs(budget.centreRms - 1.365) < 0.010)
        // and C-0006's headline ratio: the shape modes beat the piston mode by 1.70x
        assert(abs(budget.dishingOverPiston - 1.70) < 0.02)
    }

    /**
     * Gate 5 against `C-0003`, and the reason this task is entitled to call its stiffness
     * bracket **derived** rather than cited. `T-8` rebuilds all six of `C-0003`'s layer
     * readings from the measured PEG/water virials rather than copying its table, so the
     * tangent stiffnesses at the 100 pN working point must come back as `C-0003` published
     * them — 47.669 / 53.693 / 50.312 (box) and 60.335 / 64.138 / 59.300 (strong stretching).
     */
    @Test
    fun `gate 5 literature cross-check - the rebuilt layer models should reproduce the C-0003 stiffness bracket`() {
        val expected = mapOf(
            ("alexander-box" to "two-body") to 47.669,
            ("alexander-box" to "virial") to 53.693,
            ("alexander-box" to "des-Cloizeaux") to 50.312,
            ("strong-stretching" to "two-body") to 60.335,
            ("strong-stretching" to "virial") to 64.138,
            ("strong-stretching" to "des-Cloizeaux") to 59.300
        )
        layerReadings(PegWater()).forEach { reading ->
            val height = reading.model.heightUnderLoad(
                reading.chain, Gen1Tile.TARGET_FORCE, TILE_AREA
            )
            val tangent = reading.model.stiffness(reading.chain, height, TILE_AREA)
            val published = expected.getValue(reading.profile to reading.interaction)
            assert(abs(tangent - published) / published < 1e-3)
        }
    }

    /**
     * And the same for the resting-height end of the bracket, which is where `C-0003`'s
     * surprise lives: the box models open with finite stiffness (9.840 / 12.917 / 13.830)
     * and the strong-stretching models with **exactly none**, because their disjoining
     * pressure vanishes quadratically at `L₀`. That zero is what makes the *unbiased*
     * positional variance not merely large but undefined.
     */
    @Test
    fun `gate 5 literature cross-check - the strong-stretching models should have no stiffness at all at first contact`() {
        val readings = layerReadings(PegWater())
        val floor = thermalEnergy() / (LAYER_HEIGHT * LAYER_HEIGHT)
        readings.filter { it.profile == "alexander-box" }.forEach {
            val stiffness = it.model.stiffness(it.chain, it.equilibriumHeight, TILE_AREA)
            assert(stiffness > 9.0 && stiffness < 14.0)
        }
        readings.filter { it.profile == "strong-stretching" }.forEach {
            val stiffness = it.model.stiffness(it.chain, it.equilibriumHeight, TILE_AREA)
            // below k_BT/L0^2, i.e. a fluctuation larger than the layer it fluctuates in
            assert(stiffness < floor)
        }
    }

    // ---------------------------------------------------------------- the physics under test

    /**
     * The finding this test was written to falsify, and did not.
     *
     * The predicate is `σ_RMS ≤ 3.0 nm`, and it is met comfortably by every *area* measure
     * of the tile's position — 0.75 nm for the mean height, 1.82 nm for the RMS over
     * ensemble and footprint together. It is **not** met at the tile's corners, where a
     * point sees 3.40 nm at `C-0001`'s stiffness at first contact, because both rigid tilts
     * are at full lever there and the free-edge dishing modes peak there too.
     *
     * So which quantity the predicate is read against decides the verdict, and the two
     * readings differ by 1.9×. Recorded as a test rather than as prose because it is the
     * thing most likely to be silently re-derived the other way by a later task.
     */
    @Test
    fun `the worst point of the tile should exceed the predicate where the area average does not`() {
        val (_, sheet) = gen1SheetVariants().first()
        val plate = sheet.plate(Gen1Tile.EDGE_X, Gen1Tile.EDGE_Y)
        val budget = PlateOnFoundation(plate, Gen1Tile.FOUNDATION_AT_REST)
            .positionalVarianceBudget()
        assert(budget.centreRms < budget.edgeMidpointRms)
        assert(budget.edgeMidpointRms < budget.cornerRms)
        assert(budget.pistonRms < 3.0)
        assert(budget.areaRms < 3.0)
        assert(budget.cornerRms > 3.0)
    }

    @Test
    fun `an unconfined normal mode should be rejected rather than returned as an amplitude`() {
        assertFailsWith<IllegalArgumentException> { equipartitionRms(0.0) }
        assertFailsWith<IllegalArgumentException> { PlateOnFoundation(anisotropicTile, 0.0) }
        assertFailsWith<IllegalArgumentException> { einsteinDiffusivity(0.0) }
        assertFailsWith<IllegalArgumentException> { varianceFractionBelow(1000.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { freeDiffusionRms(1.0, -1.0) }
        assertFailsWith<IllegalArgumentException> {
            brinkmanShearDrag(1.0, 1.0, screeningLength = 0.0, thickness = 1.0)
        }
    }

    @Test
    fun `the cantilever transverse stiffness should fall as the cube of the strut length`() {
        val short = cantileverTransverseStiffness(bendingRigidity = 230.0, length = 10.0)
        val long = cantileverTransverseStiffness(bendingRigidity = 230.0, length = 20.0)
        assert(short.isCloseTo(3.0 * 230.0 / 1000.0, 1e-12))
        assert(long.isCloseTo(short / 8.0, 1e-12))
        // a 10 nm duplex strut clears the A1.1 lateral requirement; a 20 nm one does not
        assert(short > equipartitionStiffness(3.0))
        assert(long < equipartitionStiffness(3.0))
    }

    @Test
    fun `an Ornstein-Uhlenbeck excursion should never exceed its own equipartition ceiling`() {
        val diffusivity = 3.0e6
        val stiffness = 0.46
        val ceiling = equipartitionRms(stiffness)
        listOf(1e-9, 1e-6, 1e-3, 1.0).forEach { time ->
            val rms = ornsteinUhlenbeckRms(diffusivity, stiffness, time)
            assert(rms <= ceiling * (1.0 + 1e-12))
            assert(rms <= freeDiffusionRms(diffusivity, time) * (1.0 + 1e-12))
        }
    }

    /**
     * `P-15` / `C-0031` exposed this, and it is `CLAUDE.md`'s own rule bitten from a new side:
     * **a quantity that is meant to be zero must not be compared to zero.**
     *
     * The undefined-case record reports `unconstrainedPistonRms` as a diagnostic, guarded — in the
     * original — by `layerStiffness > 0.0`. But the case this record exists for is the
     * strong-stretching profile *at* `L₀`, where the disjoining pressure vanishes **quadratically**
     * and the stiffness is therefore **exactly zero** (`C-0003`); the block's own comment says so,
     * in the words "numerically a rounding-level positive, physically nothing".
     *
     * A sign test on a rounding-level zero is decided by the noise. Repairing `bracketedRoot` moved
     * the solved height by `~1e-6` — its declared `HEIGHT_TOLERANCE` — which flipped that zero from
     * a rounding-level negative to a rounding-level positive `2.2e-14 pN/nm`, and the emitted file
     * turned from `null` into a piston RMS of **13 637 236 nm**: 13.6 mm, against a 10 nm layer,
     * in a field whose own comment says *"null, not Infinity … writing one would be a number where
     * the honest answer is 'not well posed'"*.
     *
     * The guard is therefore written on the **physics** rather than on the sign: the amplitude is
     * reportable only while the linearised fluctuation stays inside the layer it is fluctuating
     * against. That is noise-immune, it is the criterion the surrounding block already applies to
     * decide the case is undefined at all, and it needs no tolerance.
     */
    @Test
    fun `an unconstrained piston amplitude should be null unless it fits inside the layer`() {
        // A stiffness at rounding level — the strong-stretching profile at L0, either sign.
        assert(unconstrainedPistonRms(2.2e-14, LAYER_HEIGHT) == null)
        assert(unconstrainedPistonRms(-2.2e-14, LAYER_HEIGHT) == null)
        assert(unconstrainedPistonRms(0.0, LAYER_HEIGHT) == null)
        // The sign of a rounding-level zero must not change the answer. This is the property the
        // original guard lacked, and it is what the P-15 repair falsified.
        listOf(1e-30, 1e-20, 1e-14, 1e-9).forEach { magnitude ->
            assert(unconstrainedPistonRms(magnitude, LAYER_HEIGHT) ==
                    unconstrainedPistonRms(-magnitude, LAYER_HEIGHT)) {
                "the reported amplitude depends on the SIGN of a zero, at $magnitude"
            }
        }
        // Softer than k_BT/L0² but still inside the layer: a real, reportable amplitude.
        val marginal = thermalEnergy() / (0.9 * LAYER_HEIGHT * 0.9 * LAYER_HEIGHT)
        val rms = unconstrainedPistonRms(marginal, LAYER_HEIGHT)
        assert(rms != null)
        assert(rms!!.isCloseTo(0.9 * LAYER_HEIGHT, 1e-12))
        // and it is exactly the equipartition amplitude wherever it is defined at all
        assert(rms.isCloseTo(equipartitionRms(marginal), 1e-12))
    }

}
