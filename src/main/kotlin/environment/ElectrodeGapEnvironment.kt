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

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.electrostatics.DEFAULT_GAP_MESH_NODES
import com.xemantic.nano.plentyofroom.electrostatics.GapMedium
import com.xemantic.nano.plentyofroom.electrostatics.GapMediumProfile
import com.xemantic.nano.plentyofroom.electrostatics.GapSolution
import com.xemantic.nano.plentyofroom.electrostatics.IonModel
import com.xemantic.nano.plentyofroom.electrostatics.MagnesiumChlorideBuffer
import com.xemantic.nano.plentyofroom.electrostatics.PoissonBoltzmannGap
import com.xemantic.nano.plentyofroom.electrostatics.bjerrumLength
import com.xemantic.nano.plentyofroom.electrostatics.diffusePotentialOfAppliedBias
import com.xemantic.nano.plentyofroom.electrostatics.sternChargeDensityPerVolt
import com.xemantic.nano.plentyofroom.electrostatics.thermalVoltage
import com.xemantic.nano.plentyofroom.electrostatics.uniformMedium
import com.xemantic.nano.plentyofroom.quantities.ScreeningLength

/**
 * A charged wall above a biased planar electrode, in 0.5–10 mM MgCl₂ — the 1-D nonlinear 2:1
 * Poisson-Boltzmann solve of `T-3a`, as an [Environment].
 *
 * This is one of the two things this repository has that nothing in the DNA-nanotech ecosystem
 * does. oxDNA2's salt-dependent electrostatics is *"restricted to salt concentrations of 0.1 M of
 * monovalent salt or greater"* and magnesium *"is not included in the oxDNA model"*; mrDNA applies
 * an external field and solves no electrode boundary at all. What is behind this class is a 2:1
 * asymmetric electrolyte, a nonlinear Poisson-Boltzmann boundary-value problem on a graded mesh,
 * and a **Stern series** — because a gap model's applied bias is not a diffuse-layer drop and the
 * compact layer takes 66 % of 0.1 V and 88 % of 2 V. Nothing here knows what a duplex is: the wall
 * is a signed surface charge density in `e/nm²`.
 *
 * ## The bias is a rational potential
 *
 * [diffusePotentialOfAppliedBias] solves `V = ψ_d + σ_e/C_S`, which vanishes exactly when the
 * electrode carries no free charge — so every bias here is the electrode's distance from **its own**
 * potential of zero charge, and not a potentiostat setting. `E_pzc(Au(111))` sits at 0.46–0.51 V vs
 * SHE, so a nominal zero is not a rational zero, and the sign of the residual field at "zero bias"
 * lives inside 11.2 mV of rational potential at a 10 nm gap.
 *
 * @param buffer the 2:1 reservoir.
 * @param wallChargeDensity the **signed** fixed charge on the wall facing the gap, in `e/nm²`.
 * @param referenceGapNm the separation [decayLength] is read at.
 * @param referenceBiasVolts the applied bias [decayLength] is read at. Not defaulted: a decay
 *          length here is a two-parameter reading, and the reading at zero bias is a different
 *          number that can have the other sign.
 * @param referenceArea the footprint [force] is quoted over; one square nanometre by default.
 * @param lowestGapNm, [highestGapNm] the separations the regime admits.
 * @param highestBiasVolts the largest applied bias the regime admits; §3 names 2 V.
 * @param sternCapacitance the compact-layer capacitance in `μF/cm²`; 20 is `T-3a`'s.
 * @param mediumProfile the medium filling the gap — free buffer by default, or a layer plus buffer.
 * @param maximumIonDensity the lattice-site density in `nm⁻³` for a Bikerman solve; infinite (the
 *          default) is point ions, i.e. classical Poisson-Boltzmann.
 * @param bjerrumLengthNm `l_B` at the reference permittivity.
 * @param biasSearchNodes the mesh the Stern-series bisection is run on.
 * @param decayDifferenceStepNm the central-difference step of `−F/(dF/dh)`.
 */
class ElectrodeGapEnvironment(
    override val buffer: MagnesiumChlorideBuffer,
    val wallChargeDensity: Double,
    val referenceGapNm: Double,
    val referenceBiasVolts: Double = 0.0,
    override val referenceArea: Double = 1.0,
    val lowestGapNm: Double = DEFAULT_LOWEST_GAP,
    val highestGapNm: Double = DEFAULT_HIGHEST_GAP,
    val lowestBiasVolts: Double = 0.0,
    val highestBiasVolts: Double = DEFAULT_HIGHEST_BIAS,
    val sternCapacitance: Double = DEFAULT_STERN_CAPACITANCE,
    val mediumProfile: GapMediumProfile = uniformMedium(GapMedium()),
    val maximumIonDensity: Double = Double.POSITIVE_INFINITY,
    val bjerrumLengthNm: Double = bjerrumLength(),
    val biasSearchNodes: Int = DEFAULT_BIAS_SEARCH_NODES,
    val decayDifferenceStepNm: Double = DEFAULT_DECAY_DIFFERENCE_STEP,
    val temperatureKelvin: Double = ROOM_TEMPERATURE,
    val bandwidthHz: Double? = null
) : ElectrolyteEnvironment {

    /** The gap [decayLength] is read at — an [Environment]'s separation coordinate. */
    override val referenceHeightNm: Double get() = referenceGapNm

    init {
        require(referenceArea > 0.0) { "referenceArea must be positive, was: $referenceArea" }
        require(decayDifferenceStepNm > 0.0) {
            "decayDifferenceStepNm must be positive, was: $decayDifferenceStepNm"
        }
        require(biasSearchNodes >= 8) {
            "biasSearchNodes must be at least 8, was: $biasSearchNodes"
        }
        require(decayDifferenceStepNm < 0.1 * (highestGapNm - lowestGapNm)) {
            "decayDifferenceStepNm must be small against the declared gap range, was: " +
                "$decayDifferenceStepNm against [$lowestGapNm, $highestGapNm] nm"
        }
    }

    override val name: String
        get() = "electrode gap: ${buffer.concentration} mM MgCl2, " +
            "wall $wallChargeDensity e/nm^2"

    override val respondsToBias: Boolean get() = true

    override val regime: Regime by lazy {
        Regime.magnesiumChloride(
            name = name,
            concentrationMillimolar = buffer.concentration,
            lowestHeightNm = lowestGapNm,
            highestHeightNm = highestGapNm,
            lowestBiasVolts = lowestBiasVolts,
            highestBiasVolts = highestBiasVolts,
            temperatureKelvin = temperatureKelvin,
            bandwidthHz = bandwidthHz
        )
    }

    private val ionModel = IonModel(buffer.magnesiumNumberDensity, maximumIonDensity)

    private val stern = sternChargeDensityPerVolt(sternCapacitance)

    private val solutions = LinkedHashMap<Pair<Double, Double>, GapSolution>(
        GAP_SOLUTION_CACHE_ENTRIES, 0.75f, true
    )

    /**
     * The number of mesh intervals used at a gap of [gapHeight] nm.
     *
     * `T-3a`'s own policy, `max(4000, 1200·h)`: the Gouy-Chapman length at a Manning-renormalised
     * wall is ~0.09 nm and at a driven electrode shorter still, so the node count has to grow with
     * the gap or the graded mesh stops resolving it. It is stated here as a **function** rather than
     * inherited, because it was private to the study; the guard against the two drifting apart is
     * that the environment reproduces the study's committed forces exactly (`T-265`, `P2`).
     */
    fun meshNodesAt(gapHeight: Double): Int =
        maxOf(DEFAULT_GAP_MESH_NODES, (gapHeight * GAP_MESH_NODES_PER_NANOMETRE).toInt())

    /**
     * The diffuse-layer drop in volt that an applied bias of [biasVolts] produces at [gapHeightNm].
     *
     * The Stern series, which is not optional: the diffuse and compact layers are in series and
     * `σ_e` grows exponentially in `ψ_d`, so 2 V of applied bias is only ≈ 0.24 V of diffuse drop.
     * `CH-0007` compared the two directly and made a 1.2× exceedance look like 10×.
     */
    fun diffuseLayerPotential(gapHeightNm: Double, biasVolts: Double): Double =
        diffusePotentialOfAppliedBias(
            gapHeightNm, biasVolts, wallChargeDensity, stern,
            ionModel, mediumProfile, bjerrumLengthNm, nodes = biasSearchNodes,
            temperature = temperatureKelvin
        )

    /** The converged 1-D profile at [gapHeightNm] and applied bias [biasVolts]. */
    fun solve(gapHeightNm: Double, biasVolts: Double): GapSolution {
        regime.requireAdmits(gapHeightNm, biasVolts)
        return solveUnchecked(gapHeightNm, biasVolts)
    }

    private fun solveUnchecked(gapHeightNm: Double, biasVolts: Double): GapSolution {
        val key = gapHeightNm to biasVolts
        solutions[key]?.let { return it }
        val diffuse = diffuseLayerPotential(gapHeightNm, biasVolts)
        val solution = PoissonBoltzmannGap(
            gapHeightNm, ionModel, mediumProfile, bjerrumLengthNm,
            nodes = meshNodesAt(gapHeightNm), temperature = temperatureKelvin
        ).solve(diffuse / thermalVoltage(temperatureKelvin), wallChargeDensity)
        if (solutions.size >= GAP_SOLUTION_CACHE_ENTRIES) solutions.remove(solutions.keys.first())
        solutions[key] = solution
        return solution
    }

    override fun pressure(heightNm: Double): Double =
        solve(heightNm, 0.0).disjoiningPressureInPiconewtonPerSquareNanometre

    override fun force(heightNm: Double, biasVolts: Double): Double =
        solve(heightNm, biasVolts).forceOnTile(referenceArea)

    /**
     * [force] with the **height** bound relaxed by one differencing step.
     *
     * A central difference at a state on the boundary of the declared range needs a sample just
     * outside it; refusing that would mean no decay length could be read at the range's own ends,
     * which is `T-3a`'s 3 nm gap and is exactly where the reading is interesting. The **reference
     * state itself** is checked against the regime before the difference is taken, and the two
     * samples may leave the range by at most [decayDifferenceStepNm] — asserted below, so the
     * relaxation cannot grow into a hole.
     */
    private fun forceStraddling(heightNm: Double, biasVolts: Double): Double {
        require(
            heightNm > lowestGapNm - decayDifferenceStepNm - STRADDLE_SLACK &&
                heightNm < highestGapNm + decayDifferenceStepNm + STRADDLE_SLACK
        ) {
            "$name: $heightNm nm is more than one differencing step outside " +
                "[$lowestGapNm, $highestGapNm] nm"
        }
        regime.requireAdmitsBias(biasVolts)
        return solveUnchecked(heightNm, biasVolts).forceOnTile(referenceArea)
    }

    override val bulkScreeningLength: ScreeningLength by lazy {
        ScreeningLength(
            nanometres = buffer.debyeLength(temperatureKelvin),
            where = ScreeningLength.BULK_RESERVOIR,
            readAt = linkedMapOf(
                "concentrationMillimolar" to buffer.concentration.toString(),
                "ionicStrengthMillimolar" to buffer.ionicStrength.toString()
            )
        )
    }

    /**
     * `−F/(dF/dh)` at the reference gap and bias — `T-3a`'s own `forceDecayLength`.
     *
     * It is **not** the bulk Debye length and it is not the counterion-dominated uniform-density
     * estimate either. `C-0110`: counterion dominance is a statement about ion **content** and
     * never about a decay length, and the standing shorthand that conflates them has been offered
     * twice as an answer to a question about reach.
     */
    override val decayLength: ScreeningLength by lazy {
        val step = decayDifferenceStepNm
        regime.requireAdmits(referenceGapNm, referenceBiasVolts)
        val here = forceStraddling(referenceGapNm, referenceBiasVolts)
        val slope = (
            forceStraddling(referenceGapNm + step, referenceBiasVolts) -
                forceStraddling(referenceGapNm - step, referenceBiasVolts)
            ) / (2.0 * step)
        ScreeningLength(
            nanometres = -here / slope,
            where = ScreeningLength.CONFINED_GAP,
            readAt = linkedMapOf(
                "concentrationMillimolar" to buffer.concentration.toString(),
                "gapNm" to referenceHeightNm.toString(),
                "biasVolts" to referenceBiasVolts.toString(),
                "differenceStepNm" to step.toString()
            )
        )
    }

}

/** The compact-layer capacitance in `μF/cm²` — `T-3a`'s, and a cited order of magnitude. */
const val DEFAULT_STERN_CAPACITANCE: Double = 20.0

/** Mesh intervals per nm of gap, above the floor — `T-3a`'s `max(4000, 1200 h)`. */
const val GAP_MESH_NODES_PER_NANOMETRE: Double = 1200.0

/** The mesh the Stern-series bisection runs on — `T-3a`'s `SEARCH_NODES`. */
const val DEFAULT_BIAS_SEARCH_NODES: Int = 800

/** The smallest separation `T-3a` solved, in nm. */
const val DEFAULT_LOWEST_GAP: Double = 3.0

/** The largest separation `T-3a` solved, in nm. */
const val DEFAULT_HIGHEST_GAP: Double = 30.0

/** §3's own bias ceiling, in volt. */
const val DEFAULT_HIGHEST_BIAS: Double = 2.0

private const val GAP_SOLUTION_CACHE_ENTRIES = 32

/** Floating-point slack on the one-differencing-step relaxation, in nm. */
private const val STRADDLE_SLACK = 1e-12
